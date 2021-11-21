/*******************************************************************************
 * Copyright 2021 Danny Kunz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.omnaest.utils.rest.client.internal;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.rest.client.RestClient;
import org.omnaest.utils.rest.client.RestHelper.RESTAccessExeption;
import org.omnaest.utils.rest.client.RestHelper.RESTConnectException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RetryingRestClient extends InternalRestClient
{
    private static final Logger LOG = LoggerFactory.getLogger(RetryingRestClient.class);
    private RestClient          restClient;
    private int                 times;
    private long                duration;
    private TimeUnit            timeUnit;

    public RetryingRestClient(RestClient restClient, int times, long duration, TimeUnit timeUnit)
    {
        super();
        this.restClient = restClient;
        this.times = times;
        this.duration = duration;
        this.timeUnit = timeUnit;
    }

    private static interface Operation<T>
    {
        public T execute();
    }

    private <T> T execute(Operation<T> operation)
    {
        for (int ii = 0; ii < this.times; ii++)
        {
            boolean isNotLastRetry = ii < this.times - 1;
            try
            {
                return operation.execute();
            }
            catch (RESTAccessExeption e)
            {
                int statusCode = e.getStatusCode();
                if ((statusCode < 200 || (statusCode > 299 && statusCode < 400) || statusCode > 404) && isNotLastRetry)
                {
                    try
                    {
                        LOG.info("Request failed -> retry operation in " + this.duration + " " + this.timeUnit);
                        Thread.sleep(this.timeUnit.toMillis(this.duration));
                    }
                    catch (InterruptedException e1)
                    {
                        //do nothing
                    }

                }
                else
                {
                    LOG.trace("Failed to execute request", e);
                    throw e;
                }
            }
            catch (RESTConnectException e)
            {
                if (isNotLastRetry)
                {
                    try
                    {
                        LOG.info("Request failed -> retry operation in " + this.duration + " " + this.timeUnit);
                        Thread.sleep(this.timeUnit.toMillis(this.duration));
                    }
                    catch (InterruptedException e1)
                    {
                        //do nothing
                    }
                }
                else
                {
                    LOG.trace("Failed to connect", e);
                    throw e;
                }
            }
        }
        throw new IllegalStateException();
    }

    @Override
    public <T> T requestGet(String url, Class<T> type)
    {
        return this.execute(() -> this.restClient.requestGet(url, type));
    }

    @Override
    public <T> T requestGet(String url, Class<T> type, Map<String, String> headers)
    {
        return this.execute(() -> this.restClient.requestGet(url, type, headers));
    }

    @Override
    public <T> ResponseHolder<T> requestGetAnd(String url, Class<T> type, Map<String, String> headers)
    {
        return this.execute(() -> this.restClient.requestGetAnd(url, type, headers));
    }

    @Override
    public <R, B> R requestPost(String url, B body, Class<R> resultType, Map<String, String> headers)
    {
        return this.execute(() -> this.restClient.requestPost(url, body, resultType, headers));
    }

    @Override
    public <R, B> R requestPost(String url, B body, Class<R> resultType)
    {
        return this.execute(() -> this.restClient.requestPost(url, body, resultType));
    }

    @Override
    public <R, B> R requestPatch(String url, B body, Class<R> resultType, Map<String, String> headers)
    {
        return this.execute(() -> this.restClient.requestPatch(url, body, resultType, headers));
    }

    @Override
    public RestClient withProxy(Proxy proxy)
    {
        return this.restClient.withProxy(proxy);
    }

    @Override
    public RestClient withCache(Cache cache)
    {
        return this.restClient.withCache(cache);
    }

    @Override
    public RestClient withRetry(int times, long duration, TimeUnit timeUnit)
    {
        return this.restClient.withRetry(times, duration, timeUnit);
    }

    @Override
    public RestClient withAcceptCharset(Charset charset)
    {
        return this.restClient.withAcceptCharset(charset);
    }

    @Override
    public RestClient withDefaultLocalhostProxy()
    {
        return this.restClient.withDefaultLocalhostProxy();
    }

    @Override
    public RestClient withoutSSLHostnameVerification()
    {
        return this.restClient.withoutSSLHostnameVerification();
    }

    @Override
    public RestClient withAcceptMediaType(String mediaType)
    {
        return this.restClient.withAcceptMediaType(mediaType);
    }

    @Override
    public RestClient withAcceptMediaType(MediaType mediaType)
    {
        return this.restClient.withAcceptMediaType(mediaType);
    }

    @Override
    public String toString()
    {
        return "RetryingRestClient [restClient=" + this.restClient + ", times=" + this.times + ", duration=" + this.duration + ", timeUnit=" + this.timeUnit
                + "]";
    }

    @Override
    public RestClient withContentMediaType(MediaType mediaType)
    {
        return this.restClient.withContentMediaType(mediaType);
    }

    @Override
    public RestClient withContentMediaType(String mediaType)
    {
        return this.restClient.withContentMediaType(mediaType);
    }

}

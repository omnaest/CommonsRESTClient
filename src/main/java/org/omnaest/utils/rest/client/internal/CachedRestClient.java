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
/*

	Copyright 2017 Danny Kunz

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.


*/
package org.omnaest.utils.rest.client.internal;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.omnaest.utils.CacheUtils;
import org.omnaest.utils.JSONHelper;
import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.rest.client.RestClient;
import org.omnaest.utils.rest.client.RestHelper.RESTAccessExeption;
import org.omnaest.utils.rest.client.internal.holder.ResponseHolderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RestClient} which uses a given {@link Cache} to cache <br>
 * <ul>
 * <li>{@link #requestGet(String, Class)} and</li>
 * <li>{@link #requestGet(String, Class, Map)}</li>
 * </ul>
 * calls
 *
 * @see #setCache(Cache)
 * @author Omnaest
 */
public class CachedRestClient extends IntrinsicRestClient
{
    private static final Logger LOG = LoggerFactory.getLogger(CachedRestClient.class);

    private RestClient restClient;
    private Cache      cache;

    public CachedRestClient(RestClient restClient, Cache cache)
    {
        super();
        this.restClient = restClient;
        this.cache = cache;
    }

    /**
     * Uses a {@link CacheUtils#newConcurrentInMemoryCache()} as default cache
     *
     * @param restClient
     */
    public CachedRestClient(RestClient restClient)
    {
        this(restClient, CacheUtils.newConcurrentInMemoryCache());
    }

    public CachedRestClient setCache(Cache cache)
    {
        if (cache != null)
        {
            Cache oldCache = this.cache;
            CacheUtils.populateCacheContentToNewCache(oldCache, cache);

            this.cache = cache;
        }
        return this;
    }

    @Override
    public <T> T requestGet(String url, Class<T> type, Map<String, String> headers)
    {
        LOG.trace("Request to url: " + url);

        AtomicBoolean cached = new AtomicBoolean(true);
        String key = this.generateCacheKey(url, headers);
        try
        {
            //
            boolean containsKey = this.cache.contains(key);
            T retval = this.cache.computeIfAbsent(key, () ->
            {
                if (containsKey)
                {
                    return null;
                }
                else
                {
                    cached.set(false);
                    return this.rawRequestGet(url, type, headers);
                }
            }, type);

            //
            if (cached.get())
            {
                LOG.trace("Cached");
            }

            //
            return retval;
        }
        catch (RESTAccessExeption e)
        {
            int statusCode = e.getStatusCode();
            if (statusCode == 400 || statusCode == 404)
            {
                this.cache.put(url, null);
                return null;
            }
            else
            {
                this.cache.remove(key);
                throw e;
            }
        }

        catch (Exception e)
        {
            this.cache.remove(key);
            throw e;
        }
    }

    @Override
    public <T> ResponseHolder<T> requestGetAnd(String url, Class<T> type, Map<String, String> headers)
    {
        LOG.trace("Request to url: " + url);

        AtomicBoolean cached = new AtomicBoolean(true);
        String key = this.generateCacheKey(url, headers);
        try
        {
            //
            AtomicReference<ResponseHolder<T>> responseHolderReference = new AtomicReference<>();
            boolean containsKey = this.cache.contains(key);
            T result = this.cache.computeIfAbsent(key, () ->
            {
                if (containsKey)
                {
                    return null;
                }
                else
                {
                    cached.set(false);
                    ResponseHolder<T> responseHolder = this.rawRequestGetAnd(url, type, headers);
                    responseHolderReference.set(responseHolder);
                    return responseHolder.get();
                }
            }, type);

            //
            if (cached.get())
            {
                LOG.trace("Cached");
            }

            return Optional.ofNullable(responseHolderReference.get())
                           .orElseGet(() -> new ResponseHolderImpl<>(result, 200));
        }
        catch (RESTAccessExeption e)
        {
            int statusCode = e.getStatusCode();
            if (statusCode == 400 || statusCode == 404)
            {
                this.cache.put(url, null);
                return new ResponseHolderImpl<>(null, statusCode);
            }
            else
            {
                this.cache.remove(key);
                throw e;
            }
        }
        catch (Exception e)
        {
            this.cache.remove(key);
            throw e;
        }
    }

    @Override
    public <R, B> R requestPost(String url, B body, Class<R> resultType, Map<String, String> headers)
    {
        return this.restClient.requestPost(url, body, resultType, headers);
    }

    @Override
    public <R, B> R requestPatch(String url, B body, Class<R> resultType, Map<String, String> headers)
    {
        return this.restClient.requestPatch(url, body, resultType, headers);
    }

    @Override
    public RestClient withProxy(Proxy proxy)
    {
        return this.restClient.withProxy(proxy);
    }

    private String generateCacheKey(String url, Map<String, String> headers)
    {
        return url + " " + this.encode(headers);
    }

    protected <T> T rawRequestGet(String url, Class<T> type, Map<String, String> headers)
    {
        LOG.trace("Executing raw request to " + url);
        return this.restClient.requestGet(url, type, headers);
    }

    protected <T> ResponseHolder<T> rawRequestGetAnd(String url, Class<T> type, Map<String, String> headers)
    {
        LOG.trace("Executing raw request to " + url);
        return this.restClient.requestGetAnd(url, type, headers);
    }

    protected String encode(Map<String, String> headers)
    {
        return JSONHelper.prettyPrint(headers);
    }

    @Override
    public RestClient withAcceptCharset(Charset charset)
    {
        return this.restClient.withAcceptCharset(charset);
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
    public RestClient withContentMediaType(String mediaType)
    {
        return this.restClient.withContentMediaType(mediaType);
    }

}

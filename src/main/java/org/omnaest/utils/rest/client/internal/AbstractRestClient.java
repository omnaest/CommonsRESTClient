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

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.rest.client.RestClient;
import org.omnaest.utils.rest.client.RestHelper;
import org.omnaest.utils.rest.client.RestHelper.RequestOptions;
import org.omnaest.utils.rest.client.URLBuilder;

/**
 * @see RestClient
 * @author Omnaest
 */
public abstract class AbstractRestClient extends InternalRestClient
{
    protected Proxy  proxy                         = null;
    private Charset  acceptCharset                 = StandardCharsets.UTF_8;
    private boolean  ignoreSSLHostnameVerification = false;
    protected String acceptMediaType               = null;
    protected String contentMediaType              = null;

    public AbstractRestClient()
    {
        super();
    }

    @Override
    public RestClient withAcceptCharset(Charset charset)
    {
        this.acceptCharset = charset;
        return this;
    }

    @Override
    public <T> T requestGet(String url, Class<T> type)
    {
        Map<String, String> headers = Collections.emptyMap();
        return this.requestGet(url, type, headers);
    }

    @Override
    public URLBuilder urlBuilder()
    {
        return new URLBuilder()
        {
            @Override
            public URLBuilderWithBaseUrl setBaseUrl(String scheme, String host, int port)
            {
                String userInfo = null;
                String path = null;
                String query = null;
                String fragment = null;
                try
                {
                    return this.setBaseUrl(new URI(scheme, userInfo, host, port, path, query, fragment).toString());
                }
                catch (URISyntaxException e)
                {
                    throw new IllegalArgumentException("Syntax error in url for " + scheme + " " + host + " " + port, e);
                }
            }

            @Override
            public URLBuilderWithBaseUrl setBaseUrl(String baseUrl)
            {
                return new URLBuilderWithBaseUrl()
                {
                    private StringBuilder       url        = new StringBuilder(baseUrl);
                    private Map<String, String> parameters = new LinkedHashMap<>();

                    @Override
                    public String build()
                    {
                        StringBuilder sb = new StringBuilder();
                        try
                        {
                            sb.append(this.url);

                            String parameters = Optional.ofNullable(this.parameters)
                                                        .orElseGet(() -> Collections.emptyMap())
                                                        .entrySet()
                                                        .stream()
                                                        .map(entry -> entry.getKey() + "=" + RestHelper.encodeUrlParameter(entry.getValue()))
                                                        .collect(Collectors.joining("&"));
                            sb.append((StringUtils.isNotBlank(parameters) ? "?" + parameters : ""));

                            return new URI(sb.toString()).toString();
                        }
                        catch (URISyntaxException e)
                        {
                            throw new IllegalArgumentException("Illegal syntax of url: " + sb.toString(), e);
                        }
                    }

                    @Override
                    public URLBuilderWithBaseUrl addQueryParameter(String key, String value)
                    {
                        this.parameters.put(key, value);
                        return this;
                    }

                    @Override
                    public URLBuilderWithBaseUrl addQueryParameter(String key, int value)
                    {
                        return this.addQueryParameter(key, "" + value);
                    }

                    @Override
                    public URLBuilderWithBaseUrl addPathToken(String pathToken)
                    {
                        this.url.append("/" + RestHelper.encodeUrlParameter(pathToken));
                        return this;
                    }

                };
            }
        };

    }

    @Override
    public RestClient withProxy(Proxy proxy)
    {
        this.proxy = proxy;
        return this;
    }

    @Override
    public RestClient withDefaultLocalhostProxy()
    {
        return this.withProxy(new FiddlerLocalhostProxy());
    }

    @Override
    public RestClient withoutSSLHostnameVerification()
    {
        this.ignoreSSLHostnameVerification = true;
        return this;
    }

    protected RequestOptions createRequestOptions()
    {
        return new RequestOptions().setAcceptCharset(this.acceptCharset)
                                   .setIgnoreSSLHostnameVerification(this.ignoreSSLHostnameVerification)
                                   .setProxy(this.proxy != null ? new RestHelper.Proxy(this.proxy.getHost(), this.proxy.getPort()) : null);
    }

    @Override
    public RestClient withCache(Cache cache)
    {
        if (cache != null)
        {
            return new CachedRestClient(this, cache);
        }
        else
        {
            return this;
        }
    }

    @Override
    public RestClient withRetry(int times, long duration, TimeUnit timeUnit)
    {
        return new RetryingRestClient(this, times, duration, timeUnit);
    }

    @Override
    public RestClient withAcceptMediaType(String mediaType)
    {
        this.acceptMediaType = mediaType;
        return this;
    }

    @Override
    public RestClient withAcceptMediaType(MediaType mediaType)
    {
        return this.withAcceptMediaType(mediaType.getHeaderValue());
    }

    @Override
    public RestClient withContentMediaType(String mediaType)
    {
        this.contentMediaType = mediaType;
        return this;
    }

    @Override
    public RestClient withContentMediaType(MediaType mediaType)
    {
        return this.withContentMediaType(mediaType.getHeaderValue());
    }

}
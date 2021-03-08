package org.omnaest.utils.rest.client.internal;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.rest.client.RestClient;

public abstract class IntrinsicRestClient extends InternalRestClient
{
    public IntrinsicRestClient()
    {
        super();
    }

    @Override
    public <T> T requestGet(String url, Class<T> type)
    {
        Map<String, String> headers = Collections.emptyMap();
        return this.requestGet(url, type, headers);
    }

    @Override
    public <R, B> R requestPost(String url, B body, Class<R> resultType)
    {
        Map<String, String> headers = Collections.emptyMap();
        return this.requestPost(url, body, resultType, headers);
    }

    @Override
    public RestClient withDefaultLocalhostProxy()
    {
        return this.withProxy(new FiddlerLocalhostProxy());
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
    public RestClient withAcceptMediaType(MediaType mediaType)
    {
        return this.withAcceptMediaType(mediaType.getHeaderValue());
    }

    @Override
    public RestClient withContentMediaType(MediaType mediaType)
    {
        return this.withContentMediaType(mediaType.getHeaderValue());
    }

}
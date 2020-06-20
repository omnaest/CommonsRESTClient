package org.omnaest.utils.rest.client.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.omnaest.utils.CacheUtils;
import org.omnaest.utils.rest.client.FormBuilder;
import org.omnaest.utils.rest.client.RestClient;
import org.omnaest.utils.rest.client.URLBuilder;
import org.omnaest.utils.rest.client.URLBuilder.URLBuilderWithBaseUrl;

public abstract class InternalRestClient implements RestClient
{
    @Override
    public RequestBuilder request()
    {
        return new RequestBuilder()
        {
            @Override
            public RequestBuilderWithUrl toUrl(String url)
            {
                return new RequestBuilderWithUrl()
                {
                    private Map<String, String> headers = new LinkedHashMap<>();

                    @Override
                    public RequestBuilderWithUrl withHeader(String key, String value)
                    {
                        if (key != null)
                        {
                            this.headers.put(key, value);
                        }
                        return this;
                    }

                    @Override
                    public RequestBuilderWithUrl withHeaders(Map<String, String> headers)
                    {
                        if (headers != null)
                        {
                            this.headers.putAll(headers);
                        }
                        return this;
                    }

                    @Override
                    public <T> T get(Class<T> type)
                    {
                        return InternalRestClient.this.requestGet(url, type, this.headers);
                    }

                    @Override
                    public <R, B> R post(B body, Class<R> resultType)
                    {
                        return InternalRestClient.this.requestPost(url, body, resultType, this.headers);
                    }

                    @Override
                    public <R, B> R postForm(Consumer<FormBuilder> formBuilderConsumer, Class<R> resultType)
                    {
                        FormBuilder formBuilder = RestClient.formBuilder();
                        formBuilderConsumer.accept(formBuilder);
                        return this.post(formBuilder, resultType);
                    }
                };
            }

            @Override
            public RequestBuilderWithUrl toUrl(Function<URLBuilder, URLBuilderWithBaseUrl> urlBuilderConsumer)
            {
                URLBuilder urlBuilder = InternalRestClient.this.urlBuilder();
                URLBuilderWithBaseUrl appliedUrlBuilder = urlBuilderConsumer.apply(urlBuilder);
                return this.toUrl(appliedUrlBuilder.build());
            }

        };
    }

    @Override
    public RestClient withLocalCache(String name)
    {
        return this.withCache(CacheUtils.newLocalJsonFolderCache(name));
    }

}

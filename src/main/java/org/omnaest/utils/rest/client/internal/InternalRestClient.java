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
                    public <T> ResponseHolder<T> getAnd(Class<T> type)
                    {
                        return InternalRestClient.this.requestGetAnd(url, type, this.headers);
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

                    @Override
                    public <R, B> R patch(B body, Class<R> resultType)
                    {
                        return InternalRestClient.this.requestPatch(url, body, resultType, this.headers);
                    }

                };
            }

            @Override
            public RequestBuilderWithUrl toUrl(Function<URLBuilder, URLBuilderWithBaseUrl> urlBuilderConsumer)
            {
                URLBuilder urlBuilder = RestClient.urlBuilder();
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

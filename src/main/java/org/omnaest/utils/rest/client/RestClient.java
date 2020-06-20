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
package org.omnaest.utils.rest.client;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import org.omnaest.utils.ReflectionUtils;
import org.omnaest.utils.cache.Cache;
import org.omnaest.utils.rest.client.URLBuilder.URLBuilderWithBaseUrl;
import org.omnaest.utils.rest.client.internal.JSONRestClient;
import org.omnaest.utils.rest.client.internal.StringRestClient;
import org.omnaest.utils.rest.client.internal.XMLRestClient;

/**
 * @see XMLRestClient
 * @see JSONRestClient
 * @author Omnaest
 */
public interface RestClient
{
    /**
     * Helper to create urls with encoded parameters
     *
     * @see URLBuilder
     * @return
     */
    public URLBuilder urlBuilder();

    /**
     * @see #urlBuilder()
     * @see #requestGet(String, Class, Map)
     * @param url
     * @param type
     * @return
     */
    public <T> T requestGet(String url, Class<T> type);

    /**
     * @see #urlBuilder()
     * @param url
     * @param type
     * @param headers
     * @return
     */
    public <T> T requestGet(String url, Class<T> type, Map<String, String> headers);

    /**
     * Sends a POST request
     * 
     * @param url
     * @param body
     * @param resultType
     * @param headers
     * @return
     */
    public <R, B> R requestPost(String url, B body, Class<R> resultType, Map<String, String> headers);

    /**
     * @see FiddlerLocalhostProxy
     * @author omnaest
     */
    public static class Proxy
    {
        private String host;
        private int    port;

        public Proxy(String host, int port)
        {
            super();
            this.host = host;
            this.port = port;
        }

        public String getHost()
        {
            return host;
        }

        public int getPort()
        {
            return port;
        }

    }

    /**
     * {@link Proxy} using localhost:8888
     * 
     * @author omnaest
     */
    public static class FiddlerLocalhostProxy extends Proxy
    {
        public FiddlerLocalhostProxy()
        {
            super("localhost", 8888);
        }
    }

    /**
     * Enabled an intermediate {@link Proxy} to be set for the requests
     * <br>
     * <br>
     * An example is the fiddler web debugger which uses port 8888
     * 
     * @param host
     * @param port
     * @return
     */
    public RestClient withProxy(Proxy proxy);

    /**
     * Returns a {@link RestClient} using a given {@link Cache}. If null is given, no cache is applied.
     * 
     * @return
     */
    public RestClient withCache(Cache cache);

    RestClient withLocalCache(String name);

    public RestClient withRetry(int times, long duration, TimeUnit timeUnit);

    public RestClient withAcceptCharset(Charset charset);

    /**
     * Uses the FiddlerLocalhostProxy
     * 
     * @return
     */
    public RestClient withDefaultLocalhostProxy();

    public RestClient withoutSSLHostnameVerification();

    public RestClient withAcceptMediaType(String mediaType);

    public RestClient withAcceptMediaType(MediaType mediaType);

    public RestClient withContentMediaType(MediaType mediaType);

    public RestClient withContentMediaType(String mediaType);

    public static enum MediaType
    {
        APPLICATION_JSON("application/json"),
        APPLICATION_JSON_UTF8("application/json;charset=utf-8"),
        APPLICATION_XML("application/xml;charset=utf-8"),
        APPLICATION_XML_UTF8("application/xml"),
        APPLICATION_FORM_URL_ENCODED("application/x-www-form-urlencoded"),
        TEXT_PLAIN("text/plain"),
        TEXT_HTML("text/html"),
        ALL("*/*");

        private String headerValue;

        private MediaType(String headerValue)
        {
            this.headerValue = headerValue;
        }

        public String getHeaderValue()
        {
            return headerValue;
        }

    }

    public static RestClient newJSONRestClient()
    {
        return new JSONRestClient();
    }

    public static RestClient newXMLRestClient()
    {
        return new XMLRestClient();
    }

    public static RestClient newStringRestClient()
    {
        return new StringRestClient();
    }

    public static RestClient newRestClient(Class<? extends RestClient> type)
    {
        return ReflectionUtils.newInstance(type);
    }

    public static interface RequestBuilder
    {
        public RequestBuilderWithUrl toUrl(Function<URLBuilder, URLBuilderWithBaseUrl> urlBuilderConsumer);

        public RequestBuilderWithUrl toUrl(String url);
    }

    public static interface RequestBuilderWithUrl
    {
        public RequestBuilderWithUrl withHeader(String key, String value);

        public <T> T get(Class<T> type);

        public RequestBuilderWithUrl withHeaders(Map<String, String> headers);

        public <R, B> R post(B body, Class<R> resultType);

        public <R, B> R postForm(Consumer<FormBuilder> formBuilderConsumer, Class<R> resultType);
    }

    public RequestBuilder request();

    /**
     * Returns a {@link FormBuilder} instance
     * 
     * @return
     */
    public static FormBuilder formBuilder()
    {
        return RestHelper.newFormBuilder();
    }
}

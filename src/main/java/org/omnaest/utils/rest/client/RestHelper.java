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
package org.omnaest.utils.rest.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;
import org.omnaest.utils.exception.RuntimeIOException;
import org.omnaest.utils.rest.client.RestClient.ResponseHolder;
import org.omnaest.utils.rest.client.internal.holder.ResponseHolderImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.net.UrlEscapers;

/**
 * @see RestClient
 * @author Omnaest
 */
public class RestHelper
{
    private static Logger LOG = LoggerFactory.getLogger(RestHelper.class);

    /**
     * @see #getStatusCode()
     * @author Omnaest
     */
    public static class RESTAccessExeption extends RuntimeException
    {
        private static final long serialVersionUID = 13836403364765387L;
        private int               statusCode;
        private String            content;

        public RESTAccessExeption(int statusCode, String content)
        {
            super("REST access failed with a non 2xx status code: " + statusCode + " " + StringUtils.defaultString(content));
            this.statusCode = statusCode;
            this.content = content;
        }

        public int getStatusCode()
        {
            return this.statusCode;
        }

        public String getContent()
        {
            return this.content;
        }

    }

    public static class RESTConnectException extends RuntimeException
    {
        /**
         * 
         */
        private static final long serialVersionUID = 3834045402416638779L;

        public RESTConnectException(Throwable cause)
        {
            super(cause);
        }
    }

    public static interface ResponseListener
    {
        public void observe(HttpResponse response);
    }

    public static class RequestOptions
    {
        private Proxy                  proxy                         = null;
        private Charset                acceptCharset                 = StandardCharsets.UTF_8;
        private String                 contentType                   = null;
        private boolean                ignoreSSLHostnameVerification = false;
        private List<ResponseListener> responseListeners             = new ArrayList<>();
        private boolean                disableRedirects              = false;

        private CookieStore cookieStore = new BasicCookieStore();

        public Proxy getProxy()
        {
            return this.proxy;
        }

        public RequestOptions setProxy(Proxy proxy)
        {
            this.proxy = proxy;
            return this;
        }

        public boolean hasProxy()
        {
            return this.proxy != null;
        }

        public Charset getAcceptCharset()
        {
            return this.acceptCharset;
        }

        public RequestOptions setAcceptCharset(Charset acceptCharset)
        {
            this.acceptCharset = acceptCharset;
            return this;
        }

        public RequestOptions setIgnoreSSLHostnameVerification(boolean ignoreSSLHostnameVerification)
        {
            this.ignoreSSLHostnameVerification = ignoreSSLHostnameVerification;
            return this;
        }

        public boolean isIgnoreSSLHostnameVerification()
        {
            return this.ignoreSSLHostnameVerification;
        }

        public boolean hasResponseListeners()
        {
            return !this.responseListeners.isEmpty();
        }

        public List<ResponseListener> getResponseListeners()
        {
            return this.responseListeners;
        }

        public RequestOptions addResponseListener(ResponseListener responseListener)
        {
            this.responseListeners.add(responseListener);
            return this;
        }

        public boolean isDisableRedirects()
        {
            return this.disableRedirects;
        }

        public RequestOptions disableRedirects()
        {
            this.disableRedirects = true;
            return this;
        }

        public String getContentType()
        {
            return this.contentType;
        }

        public RequestOptions setContentType(String contentType)
        {
            this.contentType = contentType;
            return this;
        }

        public RequestOptions addCookie(String name, String value, String domain, String path)
        {
            BasicClientCookie cookie = new BasicClientCookie(name, value);
            cookie.setDomain(domain);
            cookie.setPath(path);
            this.cookieStore.addCookie(cookie);
            return this;
        }

        public CookieStore getCookieStore()
        {
            return this.cookieStore;
        }

        public RequestOptions setCookieStore(CookieStore cookieStore)
        {
            this.cookieStore = cookieStore;
            return this;
        }

    }

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
            return this.host;
        }

        public int getPort()
        {
            return this.port;
        }

    }

    public static String encodeUrlParameter(String parameter)
    {
        if (parameter != null)
        {
            try
            {
                return URLEncoder.encode(parameter, "UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException(e);
            }
        }
        else
        {
            return null;
        }
    }

    public static String encodeUrlPathSegment(String pathSegment)
    {
        return UrlEscapers.urlFragmentEscaper()
                          .escape(pathSegment);
    }

    public static String requestGet(String url)
    {
        Map<String, String> headers = Collections.emptyMap();
        return requestGet(url, headers);
    }

    public static String requestGet(String url, RequestOptions options)
    {
        Map<String, String> headers = Collections.emptyMap();
        return requestGet(url, headers, options);
    }

    public static String requestGet(String url, Map<String, String> headers)
    {
        Map<String, String> queryParameters = null;
        RequestOptions options = null;
        return requestGet(url, queryParameters, headers, options);
    }

    public static String requestGet(String url, Map<String, String> headers, RequestOptions options)
    {
        Map<String, String> queryParameters = null;
        return requestGet(url, queryParameters, headers, options);
    }

    public static byte[] requestGetAsByteArray(String url, Map<String, String> headers, RequestOptions requestOptions)
    {
        Map<String, String> queryParameters = null;
        return requestGetAsByteArrayAnd(url, queryParameters, headers, requestOptions).get();
    }

    public static ResponseHolder<byte[]> requestGetAsByteArrayAnd(String url, Map<String, String> queryParameters, Map<String, String> headers,
                                                                  RequestOptions requestOptions)
    {
        return requestGetAnd(url, queryParameters, headers, requestOptions, entity ->
        {
            try
            {
                return EntityUtils.toByteArray(entity);
            }
            catch (IOException e)
            {
                throw new RuntimeIOException(e);
            }
            catch (Exception e)
            {
                throw new IllegalStateException(e);
            }
        });
    }

    public static String requestGet(String url, Map<String, String> queryParameters, Map<String, String> headers, RequestOptions requestOptions)
    {
        return requestGetAsStringAnd(url, queryParameters, headers, requestOptions).get();
    }

    public static ResponseHolder<String> requestGetAsStringAnd(String url, Map<String, String> queryParameters, Map<String, String> headers,
                                                               RequestOptions requestOptions)
    {
        return requestGetAnd(url, queryParameters, headers, requestOptions, entity ->
        {
            try
            {
                return EntityUtils.toString(entity, requestOptions != null && requestOptions.getAcceptCharset() != null ? requestOptions.getAcceptCharset()
                        : StandardCharsets.UTF_8);
            }
            catch (IOException e)
            {
                throw new RuntimeIOException(e);
            }
            catch (Exception e)
            {
                throw new IllegalStateException(e);
            }
        });
    }

    public static <T> ResponseHolder<T> requestGetAnd(String url, Map<String, String> queryParameters, Map<String, String> headers,
                                                      RequestOptions requestOptions, Function<HttpEntity, T> bodyExtractFunction)
    {
        return requestGet(url, queryParameters, headers, requestOptions, (response, options) ->
        {
            applyResponseListeners(options, response);

            T result = Optional.ofNullable(response.getEntity())
                               .map(bodyExtractFunction)
                               .orElse(null);

            int responseHttpStatusCode = response.getStatusLine()
                                                 .getStatusCode();

            return new ResponseHolderImpl<T>(result, responseHttpStatusCode);
        });
    }

    public static <T> T requestGet(String url, Map<String, String> queryParameters, Map<String, String> headers, RequestOptions options,
                                   BiFunction<CloseableHttpResponse, RequestOptions, T> responseHandler)
    {
        T retval = null;
        try (CloseableHttpClient httpclient = createHttpClient(options))
        {
            String parameters = Optional.ofNullable(queryParameters)
                                        .orElseGet(() -> Collections.emptyMap())
                                        .entrySet()
                                        .stream()
                                        .map(entry -> entry.getKey() + "=" + encodeUrlParameter(entry.getValue()))
                                        .collect(Collectors.joining("&"));

            url = url + (StringUtils.isNotBlank(parameters) ? "?" + parameters : "");

            HttpGet httpGet = new HttpGet(url);
            applyHeaders(headers, httpGet);
            applyOptions(options, httpGet);
            try (CloseableHttpResponse response = httpclient.execute(httpGet))
            {
                retval = responseHandler.apply(response, options);
            }
            catch (IOException | RuntimeIOException e)
            {
                LOG.debug("", e);
                throw new RESTConnectException(e);
            }
        }
        catch (IOException e)
        {
            LOG.debug("", e);
            throw new RESTConnectException(e);
        }
        return retval;
    }

    public static String requestPut(String url, String body)
    {
        RequestOptions options = new RequestOptions().setContentType("application/json");
        return requestPut(url, body, options);
    }

    public static String requestPut(String url, String body, RequestOptions options)
    {
        Map<String, String> queryParameters = Collections.emptyMap();
        Map<String, String> headers = Collections.emptyMap();
        return requestPut(url, queryParameters, headers, body, options);
    }

    public static String requestPut(String url, Map<String, String> queryParameters, Map<String, String> headers, String body, RequestOptions options)
    {
        String retval = null;
        try (CloseableHttpClient httpclient = createHttpClient(options))
        {
            String parameters = Optional.ofNullable(queryParameters)
                                        .orElseGet(() -> Collections.emptyMap())
                                        .entrySet()
                                        .stream()
                                        .map(entry -> entry.getKey() + "=" + encodeUrlParameter(entry.getValue()))
                                        .collect(Collectors.joining("&"));

            url = url + (StringUtils.isNotBlank(parameters) ? "?" + parameters : "");

            HttpPut httpPut = new HttpPut(url);
            httpPut.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
            applyHeaders(headers, httpPut);
            applyOptions(options, httpPut);
            try (CloseableHttpResponse response = httpclient.execute(httpPut))
            {
                HttpEntity entity = response.getEntity();

                retval = entity != null
                        ? EntityUtils.toString(entity,
                                               options != null && options.getAcceptCharset() != null ? options.getAcceptCharset() : StandardCharsets.UTF_8)
                        : "";

                applyResponseListeners(options, response);

                int statusCode = response.getStatusLine()
                                         .getStatusCode();
                if ((statusCode < 200 || statusCode > 299) && (statusCode != 302))
                {
                    throw new RESTAccessExeption(statusCode, retval);
                }

            }
            catch (IOException e)
            {
                LOG.debug("", e);
                throw new RESTConnectException(e);
            }
        }
        catch (IOException e)
        {
            LOG.debug("", e);
            throw new RESTConnectException(e);
        }
        return retval;
    }

    private static void applyResponseListeners(RequestOptions options, CloseableHttpResponse response)
    {
        if (options != null && options.hasResponseListeners())
        {
            List<ResponseListener> responseListeners = options.getResponseListeners();
            for (ResponseListener responseListener : responseListeners)
            {
                responseListener.observe(response);
            }
        }
    }

    private static CloseableHttpClient createHttpClient(RequestOptions options)
    {
        CloseableHttpClient retval;
        if (options != null && options.isIgnoreSSLHostnameVerification())
        {
            try
            {
                SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, (chain, authType) -> true)
                                                               .build();

                retval = HttpClients.custom()
                                    .setDefaultCookieStore(options.getCookieStore())
                                    .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                                    .setSSLContext(sslContext)
                                    .setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext,
                                                                                        new String[] { "SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.1", "TLSv1.2" },
                                                                                        null, NoopHostnameVerifier.INSTANCE))
                                    .build();
            }
            catch (Exception e)
            {
                throw new IllegalStateException(e);
            }
        }
        else
        {
            retval = HttpClients.createDefault();
        }
        return retval;
    }

    private static void applyOptions(RequestOptions options, HttpRequestBase httpRequest)
    {
        if (options != null)
        {
            //
            Builder builder = RequestConfig.custom();
            if (options.hasProxy())
            {
                builder.setProxy(new HttpHost(options.getProxy()
                                                     .getHost(),
                                              options.getProxy()
                                                     .getPort()));
            }

            if (options.isDisableRedirects())
            {
                builder.setRedirectsEnabled(false);
            }

            RequestConfig requestConfig = builder.build();
            httpRequest.setConfig(requestConfig);

            //
            Charset acceptCharset = options.getAcceptCharset();
            if (acceptCharset != null)
            {
                httpRequest.setHeader("Accept-Charset", acceptCharset.name());
            }

            //
            String contentType = options.getContentType();
            if (contentType != null)
            {
                httpRequest.setHeader("Content-Type", contentType);
            }
        }
    }

    private static void applyHeaders(Map<String, String> headers, HttpRequest httpRequest)
    {
        if (headers != null)
        {
            for (String name : headers.keySet())
            {
                httpRequest.addHeader(name, headers.get(name));
            }
        }
    }

    public static String requestPost(String url, String body, RequestOptions options)
    {
        Map<String, String> headers = Collections.emptyMap();
        return requestPost(url, body, headers, options);
    }

    public static String requestPost(String url, String body, Map<String, String> headers, RequestOptions options)
    {
        Map<String, String> queryParameters = Collections.emptyMap();
        return requestPost(url, queryParameters, body, headers, options);
    }

    public static String requestPost(String url, Map<String, String> queryParameters, String body, Map<String, String> headers, RequestOptions options)
    {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        return executeRequest(url, queryParameters, headers, options, httpPost);
    }

    public static String requestPatch(String url, String body, Map<String, String> headers, RequestOptions options)
    {
        Map<String, String> queryParameters = Collections.emptyMap();
        return requestPatch(url, queryParameters, body, headers, options);
    }

    public static String requestPatch(String url, Map<String, String> queryParameters, String body, Map<String, String> headers, RequestOptions options)
    {
        HttpEntityEnclosingRequestBase httpPatch = new HttpPatch(url);
        httpPatch.setEntity(new StringEntity(body, StandardCharsets.UTF_8));
        return executeRequest(url, queryParameters, headers, options, httpPatch);
    }

    private static String executeRequest(String url, Map<String, String> queryParameters, Map<String, String> headers, RequestOptions options,
                                         HttpEntityEnclosingRequestBase httpPatch)
    {
        String retval = null;
        try (CloseableHttpClient httpclient = createHttpClient(options))
        {
            String parameters = Optional.ofNullable(queryParameters)
                                        .orElseGet(() -> Collections.emptyMap())
                                        .entrySet()
                                        .stream()
                                        .map(entry -> entry.getKey() + "=" + encodeUrlParameter(entry.getValue()))
                                        .collect(Collectors.joining("&"));

            url = url + (StringUtils.isNotBlank(parameters) ? "?" + parameters : "");

            applyHeaders(headers, httpPatch);
            applyOptions(options, httpPatch);

            try (CloseableHttpResponse response = httpclient.execute(httpPatch))
            {
                HttpEntity entity = response.getEntity();

                retval = entity != null
                        ? EntityUtils.toString(entity,
                                               options != null && options.getAcceptCharset() != null ? options.getAcceptCharset() : StandardCharsets.UTF_8)
                        : "";

                applyResponseListeners(options, response);

                int statusCode = response.getStatusLine()
                                         .getStatusCode();
                if ((statusCode < 200 || statusCode > 299) && (statusCode != 302))
                {
                    throw new RESTAccessExeption(statusCode, retval);
                }

            }
            catch (IOException e)
            {
                LOG.debug("", e);
                throw new RESTConnectException(e);
            }
        }
        catch (IOException e)
        {
            LOG.debug("", e);
            throw new RESTConnectException(e);
        }
        return retval;
    }

    public static FormBuilder newFormBuilder()
    {
        return new FormBuilder();
    }
}

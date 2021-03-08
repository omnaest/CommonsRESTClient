package org.omnaest.utils.rest.client.internal;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.omnaest.utils.rest.client.RestHelper;
import org.omnaest.utils.rest.client.URLBuilder;
import org.omnaest.utils.rest.client.URLBuilder.URLBuilderWithBaseUrl;

public class URLBuilderImpl implements URLBuilder
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
                if (key != null)
                {
                    this.parameters.put(key, value);
                }
                return this;
            }

            @Override
            public URLBuilderWithBaseUrl addQueryParameterIfNotNull(String key, String value)
            {
                if (key != null && value != null)
                {
                    this.addQueryParameter(key, value);
                }
                return this;
            }

            @Override
            public URLBuilderWithBaseUrl addQueryParameterIfPresent(String key, Optional<String> value)
            {
                if (key != null && value != null && value.isPresent())
                {
                    this.addQueryParameter(key, value.get());
                }
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
}
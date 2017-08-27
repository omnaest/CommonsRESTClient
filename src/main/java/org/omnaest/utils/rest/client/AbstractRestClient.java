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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;

/**
 * @see RestClient
 * @author Omnaest
 */
public abstract class AbstractRestClient implements RestClient
{

	public AbstractRestClient()
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
					private StringBuilder		url			= new StringBuilder(baseUrl);
					private Map<String, String>	parameters	= new LinkedHashMap<>();

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
					public URLBuilderWithBaseUrl addPathToken(String pathToken)
					{
						this.url.append("/" + RestHelper.encodeUrlParameter(pathToken));
						return this;
					}

				};
			}
		};

	}

}
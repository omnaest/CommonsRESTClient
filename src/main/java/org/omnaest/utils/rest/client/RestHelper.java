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
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
		private static final long	serialVersionUID	= 13836403364765387L;
		private int					statusCode;

		public RESTAccessExeption(int statusCode)
		{
			super("REST access failed with a non 2xx status code: " + statusCode);
			this.statusCode = statusCode;
		}

		public int getStatusCode()
		{
			return this.statusCode;
		}

	}

	public static String encodeUrlParameter(String parameter)
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

	public static String requestGet(String url)
	{
		Map<String, String> headers = Collections.emptyMap();
		return requestGet(url, headers);
	}

	public static String requestGet(String url, Map<String, String> headers)
	{
		Map<String, String> queryParameters = null;
		return requestGet(url, queryParameters, headers);
	}

	public static String requestGet(String url, Map<String, String> queryParameters, Map<String, String> headers)
	{
		String retval = null;
		try (CloseableHttpClient httpclient = HttpClients.createDefault())
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
			try (CloseableHttpResponse response = httpclient.execute(httpGet))
			{
				HttpEntity entity = response.getEntity();

				int statusCode = response	.getStatusLine()
											.getStatusCode();
				if (statusCode < 200 || statusCode > 299)
				{
					throw new RESTAccessExeption(statusCode);
				}

				retval = EntityUtils.toString(entity, "utf-8");
			}
			catch (IOException e)
			{
				LOG.error("", e);
			}
		}
		catch (IOException e)
		{
			LOG.error("", e);
		}
		return retval;
	}

	private static void applyHeaders(Map<String, String> headers, HttpGet httpGet)
	{
		if (headers != null)
		{
			for (String name : headers.keySet())
			{
				httpGet.addHeader(name, headers.get(name));
			}
		}
	}
}

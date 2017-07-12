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

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RestHelper
{
	private static Logger LOG = LoggerFactory.getLogger(RestHelper.class);

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
		String retval = null;
		try (CloseableHttpClient httpclient = HttpClients.createDefault())
		{
			HttpGet httpGet = new HttpGet(url);
			try (CloseableHttpResponse response = httpclient.execute(httpGet))
			{
				HttpEntity entity = response.getEntity();

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
}

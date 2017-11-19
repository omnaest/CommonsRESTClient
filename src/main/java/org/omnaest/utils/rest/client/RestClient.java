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

import java.util.Map;

import org.omnaest.utils.cache.Cache;

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
	 * @see FiddlerLocalhostProxy
	 * @author omnaest
	 */
	public static class Proxy
	{
		private String	host;
		private int		port;

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

}

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
import java.util.concurrent.atomic.AtomicBoolean;

import org.omnaest.utils.CacheUtils;
import org.omnaest.utils.JSONHelper;
import org.omnaest.utils.cache.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link RestClient} which uses a given {@link Cache} to cache <br>
 * <ul>
 * <li>{@link #requestGet(String, Class)} and</li>
 * <li>{@link #requestGet(String, Class, Map)}</li>
 * </ul>
 * calls
 *
 * @see #setCache(Cache)
 * @author Omnaest
 */
public class CachedRestClient extends AbstractRestClient
{
	private static final Logger LOG = LoggerFactory.getLogger(CachedRestClient.class);

	private RestClient	restClient;
	private Cache		cache;

	public CachedRestClient(RestClient restClient, Cache cache)
	{
		super();
		this.restClient = restClient;
		this.cache = cache;
	}

	/**
	 * Uses a {@link CacheUtils#newConcurrentInMemoryCache()} as default cache
	 *
	 * @param restClient
	 */
	public CachedRestClient(RestClient restClient)
	{
		this(restClient, CacheUtils.newConcurrentInMemoryCache());
	}

	public CachedRestClient setCache(Cache cache)
	{
		if (cache != null)
		{
			Cache oldCache = this.cache;
			CacheUtils.populateCacheContentToNewCache(oldCache, cache);

			this.cache = cache;
		}
		return this;
	}

	@Override
	public <T> T requestGet(String url, Class<T> type, Map<String, String> headers)
	{
		LOG.info("Request to url: " + url);

		AtomicBoolean cached = new AtomicBoolean(true);
		String key = this.generateCacheKey(url, headers);
		try
		{
			//
			T retval = this.cache.computeIfAbsent(key, () ->
			{
				cached.set(false);
				return this.rawRequestGet(url, type);
			}, type);

			//
			if (cached.get())
			{
				LOG.info("Cached");
			}

			//
			return retval;
		} catch (Exception e)
		{
			this.cache.remove(key);
			throw e;
		}
	}

	@Override
	public RestClient withProxy(Proxy proxy)
	{
		return this.restClient.withProxy(proxy);
	}

	private String generateCacheKey(String url, Map<String, String> headers)
	{
		return url + " " + this.encode(headers);
	}

	protected <T> T rawRequestGet(String url, Class<T> type)
	{
		LOG.debug("Executing raw request to " + url);
		return this.restClient.requestGet(url, type);
	}

	protected String encode(Map<String, String> headers)
	{
		return JSONHelper.prettyPrint(headers);
	}

}

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

import java.util.Collections;
import java.util.Map;

import org.omnaest.utils.JSONHelper;

public class JSONRestClient implements RestClient
{
	@Override
	public <T> T requestGet(String url, Class<T> type)
	{
		Map<String, String> headers = Collections.emptyMap();
		return this.requestGet(url, type, headers);
	}

	@Override
	public <T> T requestGet(String url, Class<T> type, Map<String, String> headers)
	{
		return JSONHelper.readFromString(RestHelper.requestGet(url, headers), type);
	}
}

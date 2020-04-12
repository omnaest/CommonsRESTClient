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

/**
 * Helper to create urls with encoded parameters
 *
 * @author Omnaest
 */
public interface URLBuilder
{
    public interface URLBuilderWithBaseUrl
    {
        /**
         * Adds token of the path which gets encoded accordingly
         * 
         * @param pathToken
         * @return this
         */
        public URLBuilderWithBaseUrl addPathToken(String pathToken);

        /**
         * Adds a query parameter to the url which gets encoded encordingly
         * 
         * @param key
         * @param value
         * @return this
         */
        public URLBuilderWithBaseUrl addQueryParameter(String key, String value);

        /**
         * Similar to {@link #addQueryParameter(String, String)}
         * 
         * @param key
         * @param value
         * @return
         */
        public URLBuilderWithBaseUrl addQueryParameter(String key, int value);

        /**
         * Returns an url
         * 
         * @return
         */
        public String build();
    }

    public URLBuilderWithBaseUrl setBaseUrl(String baseUrl);

    public URLBuilderWithBaseUrl setBaseUrl(String scheme, String host, int port);
}

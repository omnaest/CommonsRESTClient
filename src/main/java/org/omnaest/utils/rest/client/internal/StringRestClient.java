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
package org.omnaest.utils.rest.client.internal;

import java.util.Collections;
import java.util.Map;

import org.omnaest.utils.JSONHelper;
import org.omnaest.utils.MapUtils;
import org.omnaest.utils.rest.client.RestClient;
import org.omnaest.utils.rest.client.RestHelper;

/**
 * @see RestClient
 * @see JSONHelper
 * @author Omnaest
 */
public class StringRestClient extends AbstractRestClient
{

    public StringRestClient()
    {
        super();
        this.acceptMediaType = MediaType.TEXT_PLAIN.getHeaderValue();
        this.contentMediaType = MediaType.TEXT_PLAIN.getHeaderValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T requestGet(String url, Class<T> type, Map<String, String> headers)
    {
        if (!String.class.isAssignableFrom(type))
        {
            throw new IllegalArgumentException("Only String type allowed for this implementation");
        }

        headers = MapUtils.builder()
                          .put("Accept", this.acceptMediaType)
                          .putAll(headers)
                          .build();
        return (T) RestHelper.requestGet(url, headers, this.createRequestOptions());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ResponseHolder<T> requestGetAnd(String url, Class<T> type, Map<String, String> headers)
    {
        if (!String.class.isAssignableFrom(type))
        {
            throw new IllegalArgumentException("Only String type allowed for this implementation");
        }

        headers = MapUtils.builder()
                          .put("Accept", this.acceptMediaType)
                          .putAll(headers)
                          .build();
        Map<String, String> queryParameters = Collections.emptyMap();
        return RestHelper.requestGetAsStringAnd(url, queryParameters, headers, this.createRequestOptions())
                         .map(string -> (T) string);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R, B> R requestPost(String url, B body, Class<R> resultType, Map<String, String> headers)
    {
        this.validateBodyAndResultType(body, resultType);
        return (R) RestHelper.requestPost(url, (String) body, this.enrichHeaders(headers), this.createRequestOptions());
    }

    private Map<String, String> enrichHeaders(Map<String, String> headers)
    {
        headers = MapUtils.builder()
                          .put("Accept", this.acceptMediaType)
                          .put("Content-Type", this.contentMediaType)
                          .putAll(headers)
                          .build();
        return headers;
    }

    private <B, R> void validateBodyAndResultType(B body, Class<R> resultType)
    {
        if (!String.class.isAssignableFrom(resultType) || (body != null && !String.class.isAssignableFrom(body.getClass())))
        {
            throw new IllegalArgumentException("Only String type allowed for this implementation");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R, B> R requestPatch(String url, B body, Class<R> resultType, Map<String, String> headers)
    {
        this.validateBodyAndResultType(body, resultType);
        return (R) RestHelper.requestPatch(url, (String) body, this.enrichHeaders(headers), this.createRequestOptions());
    }

}

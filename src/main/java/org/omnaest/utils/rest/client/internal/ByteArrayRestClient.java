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

import org.apache.commons.lang.ObjectUtils;
import org.omnaest.utils.JSONHelper;
import org.omnaest.utils.MapUtils;
import org.omnaest.utils.rest.client.RestClient;
import org.omnaest.utils.rest.client.RestHelper;

/**
 * @see RestClient
 * @see JSONHelper
 * @author Omnaest
 */
public class ByteArrayRestClient extends AbstractRestClient
{

    public ByteArrayRestClient()
    {
        super();
        this.acceptMediaType = MediaType.APPLICATION_OCTET_STREAM.getHeaderValue();
        this.contentMediaType = MediaType.APPLICATION_OCTET_STREAM.getHeaderValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T requestGet(String url, Class<T> type, Map<String, String> headers)
    {
        if (!byte[].class.isAssignableFrom(type))
        {
            throw new IllegalArgumentException("Only byte array type allowed for this implementation");
        }

        headers = MapUtils.builder()
                          .put("Accept", this.acceptMediaType)
                          .putAll(headers)
                          .build();
        return (T) RestHelper.requestGetAsByteArray(url, headers, this.createRequestOptions());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> ResponseHolder<T> requestGetAnd(String url, Class<T> type, Map<String, String> headers)
    {
        if (!byte[].class.isAssignableFrom(type))
        {
            throw new IllegalArgumentException("Only byte array type allowed for this implementation");
        }

        headers = MapUtils.builder()
                          .put("Accept", this.acceptMediaType)
                          .putAll(headers)
                          .build();
        Map<String, String> queryParameters = Collections.emptyMap();
        ResponseHolder<byte[]> responseHolder = RestHelper.requestGetAsByteArrayAnd(url, queryParameters, headers, this.createRequestOptions());
        return responseHolder.map(data -> (T) ObjectUtils.defaultIfNull(data, new byte[0]));
    }

    @Override
    public <R, B> R requestPost(String url, B body, Class<R> resultType, Map<String, String> headers)
    {
        throw new UnsupportedOperationException(this.getClass()
                                                    .getSimpleName()
                + " requestPost");
    }

    @Override
    public <R, B> R requestPatch(String url, B body, Class<R> resultType, Map<String, String> headers)
    {
        throw new UnsupportedOperationException(this.getClass()
                                                    .getSimpleName()
                + " requestPatch");
    }

}

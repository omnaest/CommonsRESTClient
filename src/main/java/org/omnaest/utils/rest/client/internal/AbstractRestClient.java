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

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.omnaest.utils.rest.client.RestClient;
import org.omnaest.utils.rest.client.RestHelper;
import org.omnaest.utils.rest.client.RestHelper.RequestOptions;

/**
 * @see RestClient
 * @author Omnaest
 */
public abstract class AbstractRestClient extends IntrinsicRestClient
{
    protected Proxy  proxy                         = null;
    private Charset  acceptCharset                 = StandardCharsets.UTF_8;
    private boolean  ignoreSSLHostnameVerification = false;
    protected String acceptMediaType               = null;
    protected String contentMediaType              = null;

    public AbstractRestClient()
    {
        super();
    }

    @Override
    public RestClient withAcceptCharset(Charset charset)
    {
        this.acceptCharset = charset;
        return this;
    }

    @Override
    public RestClient withProxy(Proxy proxy)
    {
        this.proxy = proxy;
        return this;
    }

    @Override
    public RestClient withoutSSLHostnameVerification()
    {
        this.ignoreSSLHostnameVerification = true;
        return this;
    }

    protected RequestOptions createRequestOptions()
    {
        return new RequestOptions().setAcceptCharset(this.acceptCharset)
                                   .setIgnoreSSLHostnameVerification(this.ignoreSSLHostnameVerification)
                                   .setProxy(this.proxy != null ? new RestHelper.Proxy(this.proxy.getHost(), this.proxy.getPort()) : null);
    }

    @Override
    public RestClient withAcceptMediaType(String mediaType)
    {
        this.acceptMediaType = mediaType;
        return this;
    }

    @Override
    public RestClient withContentMediaType(String mediaType)
    {
        this.contentMediaType = mediaType;
        return this;
    }

}

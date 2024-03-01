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
package org.omnaest.utils.rest.client.internal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.omnaest.utils.rest.client.RestClient;
import org.omnaest.utils.rest.client.RestHelper;

public class RetryingRestClientTest
{

    @Test
    public void testExecute() throws Exception
    {
        RestClient restClient = Mockito.mock(RestClient.class);
        int times = 5;
        long duration = 100;
        TimeUnit timeUnit = TimeUnit.MILLISECONDS;
        RetryingRestClient retryingRestClient = new RetryingRestClient(restClient, times, duration, timeUnit);

        Answer<String> answer = new Answer<String>()
        {
            private int counter = 0;

            @Override
            public String answer(InvocationOnMock invocation) throws Throwable
            {
                if (this.counter++ < 4)
                {
                    throw new RestHelper.RESTAccessExeption(429, null);
                }
                return "value";
            }
        };
        Mockito.when(restClient.requestGet(anyString(), any()))
               .thenAnswer(answer);

        retryingRestClient.requestGet("url", String.class);

        Mockito.verify(restClient, Mockito.times(5))
               .requestGet(anyString(), any());
    }

}

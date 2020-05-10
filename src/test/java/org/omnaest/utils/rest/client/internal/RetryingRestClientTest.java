package org.omnaest.utils.rest.client.internal;

import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.omnaest.utils.rest.client.RestClient;
import org.omnaest.utils.rest.client.RestHelper;
import org.omnaest.utils.rest.client.RestHelper.RESTAccessExeption;
import org.omnaest.utils.rest.client.internal.RetryingRestClient;

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
        Mockito.when(restClient.requestGet(Matchers.anyString(), Matchers.any()))
               .thenAnswer(answer);

        retryingRestClient.requestGet("url", String.class);

        Mockito.verify(restClient, Mockito.times(5))
               .requestGet(Matchers.anyString(), Matchers.any());
    }

}

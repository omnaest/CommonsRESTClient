package org.omnaest.utils.rest.client.internal;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.omnaest.utils.exception.RuntimeIOException;
import org.omnaest.utils.rest.client.RestClient.MultipartUploader;
import org.omnaest.utils.rest.client.RestClient.MultipartUploader.PreparedMultipartUpload.MultipartUploadResponse;

public class MultipartUploaderImpl implements MultipartUploader
{
    @Override
    public PreparedMultipartUpload toUrl(String url)
    {
        return new PreparedMultipartUpload()
        {
            private Map<String, String> headers = new HashMap<>();

            @Override
            public PreparedMultipartUpload upload(String name, String fileName, byte[] data, Consumer<MultipartUploadResponse> responseHandler)
            {
                try (CloseableHttpClient httpClient = HttpClients.createDefault())
                {
                    HttpPost post = new HttpPost(url);
                    this.headers.forEach(post::addHeader);
                    MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                    builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                    builder.addBinaryBody(name, data, ContentType.DEFAULT_BINARY, fileName);
                    HttpEntity entity = builder.build();
                    post.setEntity(entity);
                    HttpResponse response = httpClient.execute(post);

                    Optional.ofNullable(responseHandler)
                            .ifPresent(consumer -> consumer.accept(new MultipartUploadResponseImpl(response)));
                }
                catch (IOException e)
                {
                    throw new RuntimeIOException(e);
                }
                return this;
            }

            @Override
            public PreparedMultipartUpload addHeader(String name, String value)
            {
                if (name != null)
                {
                    this.headers.put(name, value);
                }
                return this;
            }
        };
    }

    public static class MultipartUploadResponseImpl implements MultipartUploadResponse
    {
        private final HttpResponse response;

        public MultipartUploadResponseImpl(HttpResponse response)
        {
            this.response = response;
        }

        @Override
        public String getBody()
        {
            try
            {
                return EntityUtils.toString(this.response.getEntity(), StandardCharsets.UTF_8);
            }
            catch (ParseException | IOException e)
            {
                return null;
            }
        }

        @Override
        public int getHttpStatusCode()
        {
            return this.response.getStatusLine()
                                .getStatusCode();
        }

        @Override
        public MultipartUploadResponse assertHttpStatusCode(int statusCode)
        {
            if (this.getHttpStatusCode() != statusCode)
            {
                throw new IllegalStateException("Invalid status code " + this.getHttpStatusCode() + " expected " + statusCode + " " + this.getBody());
            }
            return this;
        }
    }
}
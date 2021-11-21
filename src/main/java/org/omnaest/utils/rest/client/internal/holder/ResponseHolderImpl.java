package org.omnaest.utils.rest.client.internal.holder;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.omnaest.utils.element.cached.CachedElement;
import org.omnaest.utils.rest.client.RestClient.HttpStatusCodeHandler;
import org.omnaest.utils.rest.client.RestClient.ResponseHolder;
import org.omnaest.utils.rest.client.RestHelper.RESTAccessExeption;

public class ResponseHolderImpl<T> implements ResponseHolder<T>
{
    private final int   responseHttpStatusCode;
    private Supplier<T> resultSupplier;

    public ResponseHolderImpl(T result, int responseHttpStatusCode)
    {
        this.responseHttpStatusCode = responseHttpStatusCode;
        this.resultSupplier = () ->
        {
            this.validateResponseStatusCode(result);
            return result;
        };
    }

    private void validateResponseStatusCode(T result)
    {
        if ((this.responseHttpStatusCode < 200 || this.responseHttpStatusCode > 299) && (this.responseHttpStatusCode != 302))
        {
            throw new RESTAccessExeption(this.responseHttpStatusCode, Optional.ofNullable(result)
                                                                              .map(String::valueOf)
                                                                              .orElse(""));
        }
    }

    @Override
    public T get()
    {
        return this.resultSupplier.get();
    }

    @Override
    public ResponseHolder<T> handleStatusCode(int httpStatusCode, HttpStatusCodeHandler<T> statusCodeHandler)
    {
        if (this.responseHttpStatusCode == httpStatusCode)
        {
            T result = statusCodeHandler.apply(this);
            this.resultSupplier = () -> result;
        }
        return this;
    }

    @Override
    public <R> ResponseHolder<R> map(Function<T, R> mapper)
    {
        return this.mapResponse(responseHolder -> mapper.apply(responseHolder.get()));
    }

    @Override
    public <R> ResponseHolder<R> mapResponse(Function<ResponseHolder<T>, R> mapper)
    {
        ResponseHolder<T> responseHolder = this;
        return new ResponseHolder<R>()
        {
            private Supplier<R> resultSupplier = CachedElement.of(() -> mapper.apply(responseHolder));

            @Override
            public R get()
            {
                return this.resultSupplier.get();
            }

            @Override
            public ResponseHolder<R> handleStatusCode(int httpStatusCode, HttpStatusCodeHandler<R> statusCodeHandler)
            {
                responseHolder.handleStatusCode(httpStatusCode, rawResponse ->
                {
                    R result = statusCodeHandler.apply(this);
                    this.resultSupplier = () -> result;
                    return null;
                });
                return this;
            }

            @Override
            public <R1> ResponseHolder<R1> map(Function<R, R1> mapper2)
            {
                return responseHolder.mapResponse(mapper.andThen(mapper2));
            }

            @Override
            public Optional<R> asOptional()
            {
                return Optional.ofNullable(this.get());
            }

            @Override
            public <R1> ResponseHolder<R1> mapResponse(Function<ResponseHolder<R>, R1> mapper2)
            {
                return responseHolder.mapResponse(mapper)
                                     .mapResponse(mapper2);
            }
        };
    }

    @Override
    public Optional<T> asOptional()
    {
        return Optional.ofNullable(this.get());
    }
}
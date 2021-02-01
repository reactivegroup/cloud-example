package com.reactive.aws.ses;

import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

/**
 * The {@code Reactor} and {@code Future} adaptor.
 */
public abstract class ReactorFutureAdaptor {

    public static Executor ASYNC_EXECUTOR;

    static {
        ASYNC_EXECUTOR = Runnable::run;
    }

    /**
     * Wrap mono.
     *
     * @param <T>            the type parameter
     * @param futureSupplier the future supplier
     * @return the mono
     */
    public static <T> Mono<T> wrapCF(Supplier<CompletableFuture<T>> futureSupplier) {
        return ReactorFutureAdaptor.makeCompletableFutureToMono(futureSupplier.get());
    }

    /**
     * Make {@code CompletableFuture} to {@code Mono}.
     *
     * @param <T>               the type parameter
     * @param completableFuture the {@code CompletableFuture}
     * @return the {@code Mono}
     */
    public static <T> Mono<T> makeCompletableFutureToMono(CompletableFuture<T> completableFuture) {
        return Mono.fromFuture(completableFuture);
    }
}

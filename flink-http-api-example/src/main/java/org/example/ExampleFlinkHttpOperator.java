package org.example;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public class ExampleFlinkHttpOperator extends RichAsyncFunction<String, String> {
    // https://github.com/caarlos0-graveyard/flink-async-http-example/blob/main/src/main/java/dev/caarlos0/StreamingJob.java

    private static final Logger logger = LoggerFactory.getLogger(ExampleFlinkHttpOperator.class);
    private transient CloseableHttpAsyncClient client;

    @Override
    public void open(Configuration parameters) {
        client =
                HttpAsyncClients.custom()
                        .setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
                        .setConnectionReuseStrategy(DefaultConnectionReuseStrategy.INSTANCE)
                        .setDefaultRequestConfig(
                                RequestConfig.custom()
                                        .setConnectTimeout(60 * 1000)
                                        .setConnectionRequestTimeout(60 * 1000)
                                        .setSocketTimeout(60 * 1000)
                                        .build())
                        .build();
        client.start();
    }

    @Override
    public void asyncInvoke(String s, ResultFuture<String> resultFuture) {
        final HttpUriRequest request =
                RequestBuilder.get("https://pokeapi.co/api/v2/pokemon/ditto")
                        .addHeader(ACCEPT, APPLICATION_JSON.getMimeType())
                        .build();

        final Future<HttpResponse> result = client.execute(request, null);

        CompletableFuture.supplyAsync(
                () -> {
                    try {
                        final HttpResponse response = result.get();
                        logger.info("request completed: {}.", s);
                        return String.valueOf(response.getStatusLine().getStatusCode());
                    } catch (ExecutionException | InterruptedException e) {
                        logger.error("failed: {}.", s, e);
                        return "NOPE";
                    }
                })
                .whenCompleteAsync(
                        (status, ex) -> {
                            if (ex == null) {
                                resultFuture.complete(Collections.singleton(status));
                                logger.info("future completed: {} / {}.", s, status);
                            } else {
                                resultFuture.completeExceptionally(ex);
                                logger.error("future completed: {}.", s, ex);
                            }
                        });
    }


    @Override
    public void close() throws Exception {
        if (client != null && client.isRunning()) {
            client.close();
        }
    }
}

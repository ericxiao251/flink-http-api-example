package org.example;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.node.ObjectNode;
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

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

public class PokemonHttpOperator extends RichAsyncFunction<String, String> {
    // https://github.com/caarlos0-graveyard/flink-async-http-example/blob/main/src/main/java/dev/caarlos0/StreamingJob.java

    private static final Logger logger = LoggerFactory.getLogger(PokemonHttpOperator.class);

    private transient CloseableHttpAsyncClient client;
//    Approach 2.1: Automatically parse POJO.
//    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void open(Configuration parameters) {
        client = HttpAsyncClients.custom().build();
        client.start();
    }

    @Override
    public void asyncInvoke(String s, ResultFuture<String> resultFuture) {
        final HttpUriRequest request =
                RequestBuilder.get(String.format("https://pokeapi.co/api/v2/%s", s))
                        .addHeader(ACCEPT, APPLICATION_JSON.getMimeType())
//                        .addHeader("Authorization", "Bearer <token>") // Example Bearer HTTP authentication.
                        .build();

        final Future<HttpResponse> result = client.execute(request, null);

        CompletableFuture.supplyAsync(
                () -> {
                    try {
                        HttpResponse response = result.get();
//                        Approach 1: Return status code.
//                        return String.valueOf(response.getStatusLine().getStatusCode());

//                        Approach 2: Return a POJO.
//                        Approach 2.1: Automatically parse POJO.
//                        Pokemon pokemon = mapper.readValue(response.getEntity().getContent(), Pokemon.class);
//                        Approach 2.2: Parse only needed fields POJO.
                        ObjectNode object = new ObjectMapper().readValue(response.getEntity().getContent(), ObjectNode.class);
                        Pokemon pokemon = new Pokemon(
                            object.get("name").asText(),
                            object.get("order").asInt(),
                            object.get("weight").asInt()
                        );

                        logger.info("request completed: {}.", s);
                        return pokemon.name;
                    } catch (ExecutionException | InterruptedException | IOException e) {
                        logger.error("failed: {}.", s, e);
                        return "Bad";
                        // TODO: Add some proper logging to DataDog, Prometheus, etc.
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

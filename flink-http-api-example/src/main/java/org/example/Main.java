package org.example;


import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<String> inputStream = env.fromElements(
                "pokemon/5"
                ,"pokemon/6"
                ,"pokemon/7"
                ,"pokemon/8"
                ,"pokemon/9"
        );

        DataStream<String> outputStream = AsyncDataStream.unorderedWait(
                inputStream,
                new PokemonHttpOperator(),
                2,
                TimeUnit.MINUTES,
                1000
        );

        // TODO: Flink retries.

        outputStream.print();

        env.execute();
    }
}
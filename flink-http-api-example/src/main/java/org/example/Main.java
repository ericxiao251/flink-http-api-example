package org.example;


import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;


import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<String> inputStream = env.fromElements(
                "5"
                ,"6"
                ,"7"
                ,"8"
                ,"9"
        );
        inputStream.print();

        // TODO: Flink retries.
        DataStream<String> outputStream = AsyncDataStream.unorderedWait(
                inputStream,
                new PokemonHttpOperator(),
                2,
                TimeUnit.MINUTES,
                1000
        );
        outputStream.print();

        env.execute();
    }
}
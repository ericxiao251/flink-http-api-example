package org.example;


import org.apache.flink.streaming.api.datastream.AsyncDataStream;
import org.apache.flink.streaming.api.datastream.DataStream;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;


import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<String> inputStream = env.addSource(new IncrementingSourceFunction());
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

    // SourceFunction to generate incrementing values
    public static class IncrementingSourceFunction implements SourceFunction<String> {
        private volatile boolean isRunning = true;
        private Integer count = 0;

        @Override
        public void run(SourceContext<String> ctx) throws Exception {
            while (isRunning) {
                synchronized (ctx.getCheckpointLock()) {
                    ctx.collect(count.toString());
                    count++;
                }
                Thread.sleep(1000); // Increment every second
            }
        }

        @Override
        public void cancel() {
            isRunning = false;
        }
    }
}
package org.example;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.types.Row;

public class Main {
    public static void main(String[] args) throws Exception {
        // https://nightlies.apache.org/flink/flink-docs-release-1.13/docs/dev/table/data_stream_api/
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<Row> leftStream = env.fromElements(
                Row.of(1, "a"),
                Row.of(2, "b"),
                Row.of(3, "c"));
        leftStream.print();

        DataStream<Row> rightStream = env.fromElements(
                Row.of(1, "a"),
                Row.of(2, "b"),
                Row.of(2, "c")
        );
        rightStream.print();

        env.execute();
    }
}
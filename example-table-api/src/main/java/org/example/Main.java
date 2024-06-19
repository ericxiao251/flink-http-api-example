package org.example;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.types.Row;

// imports for Table API with bridging to Java DataStream API
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;

public class Main {
    public static void main(String[] args) throws Exception {
        // https://nightlies.apache.org/flink/flink-docs-release-1.13/docs/dev/table/data_stream_api/
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        DataStream<Row> leftStream = env.fromElements(
                Row.of(1, "a"),
                Row.of(2, "b"),
                Row.of(3, "c"));
        Table leftTable = tableEnv.fromDataStream(leftStream).as("id", "name");
        tableEnv.createTemporaryView("leftTable", leftTable);

        DataStream<Row> rightStream = env.fromElements(
                Row.of(1, "1"),
                Row.of(2, "1"),
                Row.of(2, "2")
        );
        Table rightTable = tableEnv.fromDataStream(rightStream).as("id", "version");
        tableEnv.createTemporaryView("rightTable", rightTable);

        Table resultTable = tableEnv.sqlQuery(
                "SELECT l.*, r.version FROM leftTable AS l INNER JOIN rightTable AS r ON l.id = r.id");
        DataStream<Row> resultStream = tableEnv.toChangelogStream(resultTable);
        resultStream.print();

        env.execute();
    }
}
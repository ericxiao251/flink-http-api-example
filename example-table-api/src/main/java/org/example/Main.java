package org.example;

import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.types.Row;

// imports for Table API with bridging to Java DataStream API
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.apache.flink.types.RowKind;

public class Main {
    public static void main(String[] args) throws Exception {
        // https://nightlies.apache.org/flink/flink-docs-release-1.13/docs/dev/table/data_stream_api/
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        DataStream<Row> leftStream = env.fromElements(
                Row.ofKind(RowKind.INSERT, 1, "a"),
                Row.ofKind(RowKind.INSERT, 2, "b"),
                Row.ofKind(RowKind.INSERT, 3, "c"),
                Row.ofKind(RowKind.DELETE, 1, "a"),
                Row.ofKind(RowKind.UPDATE_BEFORE, 3, "c"),
                Row.ofKind(RowKind.UPDATE_AFTER, 3, "cc")
        );
        Table leftTable = tableEnv.fromChangelogStream(leftStream).as("id", "name");
        tableEnv.createTemporaryView("leftTable", leftTable);

        DataStream<Row> rightStream = env.fromElements(
                Row.ofKind(RowKind.INSERT, 1, "1"),
                Row.ofKind(RowKind.INSERT, 2, "2"),
                Row.ofKind(RowKind.INSERT, 3, "3"),
                Row.ofKind(RowKind.UPDATE_BEFORE, 2, "2"),
                Row.ofKind(RowKind.UPDATE_AFTER, 2, "22"),
                Row.ofKind(RowKind.DELETE, 2, "22")
        );
        Table rightTable = tableEnv.fromChangelogStream(rightStream).as("id", "version");
        tableEnv.createTemporaryView("rightTable", rightTable);

        Table resultTable = tableEnv.sqlQuery(
                "SELECT " +
                        "l.*, " +
                        "r.version " +
                "FROM leftTable AS l " +
                "INNER JOIN rightTable AS r" +
                " ON l.id = r.id"
        );
        DataStream<Row> resultStream = tableEnv.toChangelogStream(resultTable);
        resultStream.print();

        env.execute();
    }
}
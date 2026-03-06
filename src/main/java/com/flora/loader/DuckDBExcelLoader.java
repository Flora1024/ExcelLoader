package com.flora.loader;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class DuckDBExcelLoader {

    private final Connection connection;
    private final String tableName;

    public DuckDBExcelLoader(Connection connection, String tableName) {
        this.connection = connection;
        this.tableName = tableName;
    }

    public String load(String filePath) {
        List<String> headers = new ArrayList<>();

        EasyExcel.read(filePath, new AnalysisEventListener<Map<Integer, String>>() {
            private boolean tableCreated = false;
            private PreparedStatement pstmt = null;
            private int batchSize = 0;

            @Override
            public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
                // 1. 提取表头并清理非法字符
                headers.addAll(headMap.values().stream()
                        .map(h -> h.replaceAll("[^a-zA-Z0-9_\\u4e00-\\u9fa5]", "_"))
                        .collect(Collectors.toList()));

                // 2. 动态创建 DuckDB 表
                try {
                    String columns = headers.stream().map(h -> "\"" + h + "\" VARCHAR").collect(Collectors.joining(", "));
                    String createSql = String.format("CREATE TABLE %s (%s)", tableName, columns);
                    connection.createStatement().execute("DROP TABLE IF EXISTS " + tableName);
                    connection.createStatement().execute(createSql);

                    String placeholders = headers.stream().map(h -> "?").collect(Collectors.joining(", "));
                    pstmt = connection.prepareStatement(String.format("INSERT INTO %s VALUES (%s)", tableName, placeholders));
                } catch (SQLException e) {
                    throw new RuntimeException("创建表失败", e);
                }
            }

            @Override
            public void invoke(Map<Integer, String> data, AnalysisContext context) {
                // 3. 批量插入数据
                try {
                    for (int i = 0; i < headers.size(); i++) {
                        pstmt.setString(i + 1, data.get(i));
                    }
                    pstmt.addBatch();
                    batchSize++;

                    if (batchSize >= 1000) { // 每1000行提交一次
                        pstmt.executeBatch();
                        batchSize = 0;
                    }
                } catch (SQLException e) {
                    throw new RuntimeException("数据插入失败", e);
                }
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext context) {
                try {
                    if (batchSize > 0) pstmt.executeBatch();
                    if (pstmt != null) pstmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }).sheet().doRead();

        // 4. 生成给 Spring AI 的 Schema 描述
        return String.format("本地数据库已就绪。表名: '%s', 包含列: [%s]。",
                tableName, String.join(", ", headers));
    }
}
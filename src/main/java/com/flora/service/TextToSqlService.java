package com.flora.service;

import com.flora.loader.DuckDBExcelLoader;

import java.sql.*;

public class TextToSqlService {

    public static void main(String[] args) {
        String filePath = "D:\\Dev\\personal-projects\\Xlsx2Markdown\\src\\main\\java\\com\\flora\\data\\data.xlsx";
        String sessionId = "1";
        try {
            // 显式加载 DuckDB 驱动
            Class.forName("org.duckdb.DuckDBDriver");
            String prompt = processExcelAndGetPrompt(filePath, sessionId);
            System.out.println(prompt);
        } catch (SQLException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String processExcelAndGetPrompt(String filePath, String sessionId) throws SQLException {
        // 使用内存模式，连接在 Session 结束后释放
        Connection conn = DriverManager.getConnection("jdbc:duckdb:");
        String tableName = "orders_" + sessionId;

        DuckDBExcelLoader loader = new DuckDBExcelLoader(conn, tableName);
        String schemaSummary = loader.load(filePath);

        // 验证数据是否真的加载成功
        verifyData(conn, tableName);

        // 构造最终喂给 Spring AI 的提示词
        return "你是一个专业的 SQL 数据分析专家。\n" +
                schemaSummary + "\n" +
                "请注意：\n" +
                "1. 请根据用户的问题生成符合 DuckDB 语法的 SQL 语句。\n" +
                "2. 始终使用双引号引用列名，例如 SELECT \"订单金额\" FROM " + tableName + "。\n" +
                "3. 只输出 SQL 语句，不要有任何多余的解释。";
    }

    /**
     * 验证数据库中的数据
     */
    private static void verifyData(Connection conn, String tableName) throws SQLException {
        System.out.println("\n========== 数据验证开始 ==========");

        // 1. 查询总条数
        String countSql = "SELECT COUNT(*) FROM " + tableName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {
            if (rs.next()) {
                System.out.println("表 " + tableName + " 中的数据条数: " + rs.getInt(1));
            }
        }

        // 2. 查询表结构
        System.out.println("\n表结构:");
        String schemaSql = "DESCRIBE " + tableName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(schemaSql)) {
            while (rs.next()) {
                System.out.println("  - " + rs.getString("column_name") + " : " + rs.getString("column_type"));
            }
        }

        // 3. 查询前3条数据样本
        System.out.println("\n前3条数据样本:");
        String sampleSql = "SELECT * FROM " + tableName + " LIMIT 3";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sampleSql)) {
            ResultSetMetaData meta = rs.getMetaData();
            int columnCount = meta.getColumnCount();

            // 打印表头
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(meta.getColumnName(i) + "\t");
            }
            System.out.println();

            // 打印数据
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + "\t");
                }
                System.out.println();
            }
        }

        System.out.println("========== 数据验证结束 ==========\n");
    }

}
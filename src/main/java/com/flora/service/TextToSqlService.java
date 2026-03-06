package com.flora.service;

import com.flora.loader.DuckDBExcelLoader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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

        // 构造最终喂给 Spring AI 的提示词
        return "你是一个专业的 SQL 数据分析专家。\n" +
                schemaSummary + "\n" +
                "请注意：\n" +
                "1. 请根据用户的问题生成符合 DuckDB 语法的 SQL 语句。\n" +
                "2. 始终使用双引号引用列名，例如 SELECT \"订单金额\" FROM " + tableName + "。\n" +
                "3. 只输出 SQL 语句，不要有任何多余的解释。";
    }

}
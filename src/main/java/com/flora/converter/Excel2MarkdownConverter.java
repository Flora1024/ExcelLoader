package com.flora.converter;

import com.flora.model.ExcelRowData;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Excel2MarkdownConverter {

    public static void convert2Markdown(List<ExcelRowData> rows, String outputFileName) {
        Path outputFilePath = Paths.get(outputFileName);
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(outputFilePath.toFile()));
            for (int i = 0; i < rows.size(); i++) {
                writer.println("## 第 " + (i + 1) + " 行数据");
                writer.println();

                for (Map.Entry<String, Object> entry : rows.get(i).getData().entrySet()) {
                    String field = entry.getKey();
                    Object value = Optional.of(entry.getValue()).orElse("空");
                    writer.println("- " + field + "为" + value);
                }
                writer.println();
            }
            System.out.println("文件已生成 -> " + outputFilePath.toAbsolutePath());
        } catch (IOException e) {
            System.out.println("文件生成失败 -> " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}

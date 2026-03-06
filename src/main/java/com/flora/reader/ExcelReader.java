package com.flora.reader;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.read.listener.ReadListener;
import com.flora.converter.Excel2MarkdownConverter;
import com.flora.model.ExcelRowData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ExcelReader {

    public static void readAndConvert(String filePath, String outputFileName) {
        List<ExcelRowData> rowDataList = new ArrayList<>();
        Map<Integer, String> headMap = new ConcurrentHashMap<>();
        final boolean[] isHead = {true};

        EasyExcel.read(filePath, new ReadListener<Map<Integer, String>>() {
            @Override
            public void invoke(Map<Integer, String> data, AnalysisContext analysisContext) {
                if (isHead[0]) {
                    for (Map.Entry<Integer, String> entry : data.entrySet()) {
                        headMap.put(entry.getKey(), entry.getValue());
                    }
                    isHead[0] = false;
                    return;
                }
                ExcelRowData rowData = new ExcelRowData();
                for (Map.Entry<Integer, String> entry : data.entrySet()) {
                    String fieldName = headMap.getOrDefault(entry.getKey(), "列" + entry.getKey());
                    rowData.addField(fieldName, entry.getValue());
                }
                rowDataList.add(rowData);
            }

            @Override
            public void doAfterAllAnalysed(AnalysisContext analysisContext) {
                System.out.println("读取完成 -> "+ rowDataList.size() + "行数据");
            }

            @Override
            public void onException(Exception exception, AnalysisContext context) throws Exception {
                System.out.println("读取异常");
                throw exception;
            }
        }).sheet().headRowNumber(0).doRead();

        Excel2MarkdownConverter.convert2Markdown(rowDataList, outputFileName);
    }
}

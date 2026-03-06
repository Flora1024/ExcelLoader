package com.flora.model;

import com.alibaba.excel.util.StringUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExcelRowData {
    Map<String, Object> data = new ConcurrentHashMap<>();

    public void addField (String fieldName, Object value) {
        if (StringUtils.isNotBlank(fieldName)) {
            data.put(fieldName, value == null ? "空" : value);
        }
    }
}

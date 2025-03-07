package com.alibaba.fastjson.basicType;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class LongTest {
    @Test
    public void test_array() throws Exception {
        long[] values = new long[] {Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE};
        String text = JSON.toJSONString(values);
        long[] values_2 = JSON.parseObject(text, long[].class);
        Assertions.assertEquals(values_2.length, values.length);
        for (int i = 0; i < values.length; ++i) {
            Assertions.assertEquals(values[i], values_2[i]);
        }
    }

    @Test
    public void test_map() throws Exception {
        long[] values = new long[] {Long.MIN_VALUE, -1, 0, 1, Long.MAX_VALUE};
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < values.length; ++i) {
            map.put(Long.toString(i), values[i]);
        }

        String text = JSON.toJSONString(map);
        JSONObject obj = JSON.parseObject(text);
        for (int i = 0; i < values.length; ++i) {
            Assertions.assertEquals(values[i], ((Number) obj.get(Long.toString(i))).longValue());
        }
    }
}

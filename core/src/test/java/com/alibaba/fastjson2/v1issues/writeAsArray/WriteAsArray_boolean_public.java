package com.alibaba.fastjson2.v1issues.writeAsArray;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class WriteAsArray_boolean_public {
    @Test
    public void test_0 () throws Exception {
        VO vo = new VO();
        vo.setId(true);
        vo.setName("wenshao");

        String text = JSON.toJSONString(vo, JSONWriter.Feature.BeanToArray);
        assertEquals("[true,\"wenshao\"]", text);
    }

    public static class VO {
        private boolean id;
        private String name;

        public boolean isId() {
            return id;
        }

        public void setId(boolean id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

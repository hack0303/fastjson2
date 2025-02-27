package com.alibaba.fastjson;

import junit.framework.TestCase;
import org.junit.Assert;

import java.math.BigInteger;

public class JSONObjectTest_getBigInteger extends TestCase {

    public void test_get_float() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("value", 123.45F);
        Assert.assertTrue(123.45F == ((Float) obj.get("value")).floatValue());
        Assert.assertEquals(new BigInteger("123"), obj.getBigInteger("value"));
    }

    public void test_get_double() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("value", 123.45D);
        Assert.assertTrue(123.45D == ((Double) obj.get("value")).doubleValue());
        Assert.assertEquals(new BigInteger("123"), obj.getBigInteger("value"));
    }

    public void test_get_empty() throws Exception {
        JSONObject obj = new JSONObject();
        obj.put("value", "");
        Assert.assertEquals("", obj.get("value"));
        Assert.assertNull(obj.getBigInteger("value"));
    }
}

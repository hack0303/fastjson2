package com.alibaba.fastjson.issue_1300;

import com.alibaba.fastjson.JSON;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by kimmking on 03/08/2017.
 */
public class Issue1369 {
    @Test
    public void test_for_issue() throws Exception {
        Foo foo = new Foo();
        foo.a = 1;
        foo.b = "b";
        foo.bars = new Bar();
        foo.bars.c = 3;
        String json = JSON.toJSONString(foo);
        System.out.println(json);
        Assertions.assertTrue(json.indexOf("\\")<0);
    }

    public static class Foo {
        public int a;
        public String b;
        public Bar bars;
    }
    public static class Bar {
        public int c;
    }
}

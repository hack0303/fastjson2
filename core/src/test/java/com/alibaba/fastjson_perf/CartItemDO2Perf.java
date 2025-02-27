package com.alibaba.fastjson_perf;

import com.alibaba.fastjson2.JSONB;
import com.alibaba.fastjson2.JSONWriter;
import com.alibaba.fastjson2_vo.CartItemDO2;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3)
@Measurement(iterations = 1000, time = 5, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class CartItemDO2Perf {
    static List<CartItemDO2> list;
    private static List<CartItemDO2> newCartsItem() {
        if (list != null) {
            return list;
        }

        list = new ArrayList();
        for (long i = 90000000000l; i < 90000000000l + 1000; i++) {
            CartItemDO2 cartItemDO2 = new CartItemDO2();
            cartItemDO2.setUserId(i);
            cartItemDO2.setAttributes(new HashMap());
            cartItemDO2.setCartId(i * 100);
            cartItemDO2.setCityCode(i * 12);
            cartItemDO2.setItemId(i * 3);
            cartItemDO2.setMainType(11);
            cartItemDO2.setQuantity(900);
            cartItemDO2.setSkuId(i * 5);
            cartItemDO2.setSubType(i * 6);
            cartItemDO2.setTpId(i * 7);
            cartItemDO2.setTrackId(String.valueOf(i * 8));
            list.add(cartItemDO2);
        }
        return list;
    }

    @Benchmark
    public void testCartItem() throws Exception {
        byte[] bytes3 = JSONB.toBytes(
                newCartsItem()
                , JSONB.symbolTable("myId")
                , JSONWriter.Feature.BeanToArray
        );
    }

}

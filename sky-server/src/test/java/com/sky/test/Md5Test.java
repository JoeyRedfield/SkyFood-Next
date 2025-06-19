package com.sky.test;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.DigestUtils;

//@SpringBootTest
public class Md5Test {

    @Test
    void test123456(){
        System.out.println(DigestUtils.md5DigestAsHex("123456".getBytes()));
    }
}

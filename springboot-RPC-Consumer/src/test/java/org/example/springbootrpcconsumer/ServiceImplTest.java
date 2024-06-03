package org.example.springbootrpcconsumer;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 *
 */
@SpringBootTest
public class ServiceImplTest {

    @Autowired
    private ServiceImpl service;


    @Test
    public void test1(){
        service.test();
    }
}

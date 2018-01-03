package com.x.jdbc.spring;

import dao.DaoInterface;
import com.x.jdbc.model.TTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * @author Liukx
 * @create 2018-01-03 13:31
 * @email liukx@elab-plus.com
 **/
@RunWith(SpringJUnit4ClassRunner.class)  //使用junit4进行测试
@ContextConfiguration
        ({"classpath:applicationContext-datasource.xml"})
public class JSpringTest {

    @Autowired
    private DaoInterface daoInterface;

    @Test
    public void testInterface() {
        TTest test = new TTest();
        test.setStatus("1");
        List<TTest> tTests = daoInterface.selectByExample(test);
        System.out.println("===============================");
    }

    @Test
    public void testInterface2() {
        TTest test = new TTest();
        test.setStatus("1");
        List<TTest> tTests = daoInterface.selectByName("1", "1");
        System.out.println("===============================");
    }


}

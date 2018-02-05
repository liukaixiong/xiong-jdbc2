package com.x.jdbc.spring;

import dao.DaoInterface;
import model.TTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
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


    // 增加
    @Test
    public void insert() throws Exception {
        TTest test = new TTest();
        test.setStatus("1");
        test.setUsername("lkx");
        test.setCreated(new Date());
        test.setLove_name("love you");
        test.setTest_id("1123");
//        test.setId(5);
        int insert = daoInterface.insert(test);
        System.out.println(insert);
    }

    @Test
    public void update() throws Exception {
        TTest test = new TTest();
        test.setName("===1===");
        test.setStatus("1");
        test.setUsername("lkx");
        test.setCreated(new Date());
        test.setLove_name("love you");
        test.setTest_id("1123");
//        test.setId(75352);
        int insert = daoInterface.updateById(test);
    }

    @Test
    public void delete() throws Exception {
        TTest test = new TTest();
        test.setStatus("1");
        test.setUsername("lkx");
        test.setCreated(new Date());
        test.setLove_name("love you");
        test.setTest_id("1123");
        test.setId(5);
        int insert = daoInterface.delete(test);
    }

    @Test
    public void testParentInterface2() throws Exception {
        TTest test = new TTest();
//        test.setStatus("1");
//        test.setUsername("lkx");
//        test.setCreated(new Date());
//        test.setLove_name("love you");
//        test.setTest_id("1123");
        test.setId(5);
//        daoInterface.insert(test);
//        daoInterface.update(test);
//        daoInterface.delete(test);
//        List<TTest> tTests = daoInterface.selectByList(test);
//        TTest tTest = daoInterface.selectByObject(test);
//        TTest tTest1 = daoInterface.selectById("10");
//        TTest tTest = daoInterface.selectByObject(test);
//        tTest.setGirlName("我要开始测试啦...");
//        TTest test1 = tTest.getTest();
//        List<TTest> testList = tTest.getTestList();
        System.out.println("==================");
//        List list = new ArrayList();
//        list.add("1");
//        TTestExample example = new TTestExample();
//        example.createCriteria().andStatusEqualTo("1").andStatusGreaterThan("0").andStatusIn(list);
//        example.createCriteria().andIdBetween(75340,75399);
//        example.createCriteria().andNameLike("t%");
//        example.createCriteria().andIdIsNull();
//        example.createCriteria().andIdGreaterThanOrEqualTo(75340);
//        String s = example.toString();
//        List<TTest> tTests = daoInterface.find(example);
//        System.out.println("===============================" + tTests.size());
    }


}

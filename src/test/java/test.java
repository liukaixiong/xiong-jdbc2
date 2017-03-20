import JDBCTemplate.JDBCTemplateUtils;
import model.TTest;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Liukx
 * @create 2017-03-15 14:07
 * @email liukx@elab-plus.com
 **/
public class test {
    private static JDBCTemplateUtils jdbcTemplate = new JDBCTemplateUtils();

    public static void insert() {
        System.out.println("★★★★★★★★★★★★★★★★★★★★★insert★★★★★★★★★★★★★★★★★★★★★★★★");
        TTest t = new TTest();
        t.setUsername("添加数据");
        t.setLove_name("mmm");
        t.setName("你知道?");
        t.setSex("男");
        t.setCreated(new Date());
        t.setTest_id("rrrrrrr");
        t.setTime(new Date());
        Long start = System.currentTimeMillis();
        int i = jdbcTemplate.executeInsert("test.insert", t);
        Long end = System.currentTimeMillis();
        System.out.println(" 查询耗时 : " + (end - start) + " 数据大小:" + i);
    }

    public static void update() {
        System.out.println("★★★★★★★★★★★★★★★★★★★★★update★★★★★★★★★★★★★★★★★★★★★★★★");
        TTest t = new TTest();
        t.setUsername("添加数据");
        t.setLove_name("mmm");
        t.setName("你知道?");
        t.setSex("男");
        t.setCreated(new Date());
        t.setTest_id("rrrrrrr");
        t.setTime(new Date());
        t.setId(2);
        Long start = System.currentTimeMillis();
        int i = jdbcTemplate.executeUpdate("test.updateByPrimaryKey", t);
        Long end = System.currentTimeMillis();
        System.out.println(" 查询耗时 : " + (end - start) + " 数据大小:" + i);
    }

    public static void findMap() {
        System.out.println("★★★★★★★★★★★★★★★★★★★★★findMap★★★★★★★★★★★★★★★★★★★★★★★★");
        TTest t = new TTest();
        t.setId(2);
        Long start = System.currentTimeMillis();
        Map<String, Object> map = jdbcTemplate.executeQueryMap("test.selectByPrimaryKey", t);
        Long end = System.currentTimeMillis();
        System.out.println(" 查询耗时 : " + (end - start) + " 数据大小:");
    }

    public static void findObject() {
        System.out.println("★★★★★★★★★★★★★★★★★★★★★findObject★★★★★★★★★★★★★★★★★★★★★★★★");
        Map map = new LinkedHashMap();
        map.put("id", 2);
        map.put("age", 18);
        map.put("ggg", "sssssss");
        Long start = System.currentTimeMillis();
        TTest test = jdbcTemplate.executeQueryObject("test.selectByPrimaryKey", map, TTest.class);
        Long end = System.currentTimeMillis();
        System.out.println(" 查询耗时 : " + (end - start) + " 数据大小: ");
    }

    public static void findListMap() {
        System.out.println("★★★★★★★★★★★★★★★★★★★★★findListMap★★★★★★★★★★★★★★★★★★★★★★★★");
        TTest t = new TTest();
        t.setStatus("1");
        Long start = System.currentTimeMillis();
        List<Map<String, Object>> list = jdbcTemplate.excuteQueryforListMap("test.selectByExample", t);
        Long end = System.currentTimeMillis();
        System.out.println(" 查询耗时 : " + (end - start) + " 数据大小:" + list.size());
    }

    public static void findListObject() {
        System.out.println("★★★★★★★★★★★★★★★★★★★★★findListObject★★★★★★★★★★★★★★★★★★★★★★★★");
        TTest t = new TTest();
        t.setStatus("1");
        Long start = System.currentTimeMillis();
        List<TTest> tTests = jdbcTemplate.executeQueryList("test.selectByExample", t, TTest.class);
        Long end = System.currentTimeMillis();
        System.out.println(" 查询耗时 : " + (end - start) + " 数据大小:" + tTests.size());
    }

    public static void findBigList() {
        System.out.println("★★★★★★★★★★★★★★★★★★★★★findBigList★★★★★★★★★★★★★★★★★★★★★★★★");
        TTest t = new TTest();
        t.setStatus("1");
        Long start = System.currentTimeMillis();
        List<TTest> tTests = jdbcTemplate.executeQueryBigDataList("test.selectByExample", t, TTest.class);
        Long end = System.currentTimeMillis();
        System.out.println(" 查询耗时 : " + (end - start) + " 数据大小:" + tTests.size());
    }

    public static void main(String[] args) {
//        findBigList();
//        for (int i = 0; i < 10; i++) {
//            System.out.println(" 执行第 [" + (i + 1) + "]");
//            findBigList();
//            findListObject();
//            findListMap();
//        }
//        findMap();
        findObject();
//        insert();
//        update();
    }
}

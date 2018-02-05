import com.x.jdbc.template.JdbcTemplateSupport;
import model.TTest;
import model.TTest1;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import java.util.*;

/**
 * @author Liukx
 * @create 2017-03-15 14:07
 * @email liukx@elab-plus.com
 **/
public class test {
    private static JdbcTemplateSupport jdbcTemplate = new JdbcTemplateSupport();

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

    public static void update() throws Exception {
        System.out.println("★★★★★★★★★★★★★★★★★★★★★update★★★★★★★★★★★★★★★★★★★★★★★★");
        TTest t = new TTest();
        // t.setUsername("添加数据");
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

    public static void findRowSet() {
        System.out.println("★★★★★★★★★★★★★★★★★★★★★findRowSet★★★★★★★★★★★★★★★★★★★★★★★★");
        String sql = " select\n" +
                "      id\n" +
                "      ,username,name,sex,status,created,time,test_id,love_name\n" +
                "      from t_test\n" +
                "      where\n" +
                "      id <= ?";
        Long start = System.currentTimeMillis();
        SqlRowSet sqlRowSet = jdbcTemplate.queryForRowSet(sql, "2");
        Long end = System.currentTimeMillis();
        System.out.println(" 查询耗时 : " + (end - start) + " 数据大小:" + sqlRowSet.getString(1));
    }


    public static void findMap() {
        System.out.println("★★★★★★★★★★★★★★★★★★★★★findMap★★★★★★★★★★★★★★★★★★★★★★★★");
        Map map = new LinkedHashMap();
        map.put("id", 2);
        map.put("age", 18);
        map.put("ggg", "sssssss");
        Long start = System.currentTimeMillis();
        Map<String, Object> maps = jdbcTemplate.executeQueryMap("test.selectByPrimaryKey", map);
        Long end = System.currentTimeMillis();
        System.out.println(" 查询耗时 : " + (end - start) + " 数据大小:" + maps);
    }

    public static void findSqlOjbect() {
        System.out.println("★★★★★★★★★★★★★★★★★★★★★findSqlOjbect★★★★★★★★★★★★★★★★★★★★★★★★");
        Object[] ids = new Object[3];
        ids[0] = "2";
        ids[1] = "3307424";
        ids[2] = "3307426";

        Map params = new HashMap();
//        params.put("username", "添加%");
        params.put("table", "t_test");
        params.put("id", ids);
        //params.put("minId", "3307423");
        //params.put("maxId", "3307426");
        Long start = System.currentTimeMillis();
        List<TTest> tTests = jdbcTemplate.executeQueryList("test.operationSql", params, TTest.class);
        Long end = System.currentTimeMillis();
        System.out.println(" 查询耗时 : " + (end - start) + " 数据大小:" + tTests.size());
    }

    public static void findObject() {
        System.out.println("★★★★★★★★★★★★★★★★★★★★★findObject★★★★★★★★★★★★★★★★★★★★★★★★");
        Map map = new LinkedHashMap();
        map.put("id", 1);
        map.put("age", 18);
        map.put("ggg", "sssssss");
        Long start = System.currentTimeMillis();
        TTest test = jdbcTemplate.executeQueryObject("test.selectByPrimaryKey", map, TTest.class);
        Long end = System.currentTimeMillis();
        System.out.println(" 查询耗时 : " + (end - start) + " 数据大小: " + test.toString());
    }

    public static void findListMap() {
        System.out.println("★★★★★★★★★★★★★★★★★★★★★findListMap★★★★★★★★★★★★★★★★★★★★★★★★");
        TTest t = new TTest();
        t.setStatus("1");
        Long start = System.currentTimeMillis();
        List<Map<String, Object>> list = jdbcTemplate.executeQueryforListMap("test.selectByExample", t);
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
        t.setId(2900000);
//        t.setStatus("10");
        Long start = System.currentTimeMillis();
        List<TTest> tTests = jdbcTemplate.executeQueryBigDataList("test.selectByExample", t, TTest.class);
        Long end = System.currentTimeMillis();
        System.out.println(" 查询耗时 : " + (end - start) + " 数据大小:" + tTests.size());
    }

    public static void findAnnotationBigList() {
        System.out.println("★★★★★★★★★★★★★★★★★★★★★findBigList★★★★★★★★★★★★★★★★★★★★★★★★");
        TTest t = new TTest();
        t.setId(2);
        t.setStatus("10");
        Long start = System.currentTimeMillis();
        List<TTest1> tTests = jdbcTemplate.executeQueryBigDataList("test.selectByExample", t, TTest1.class);
        Long end = System.currentTimeMillis();
        System.out.println(" 查询耗时 : " + (end - start) + " 数据大小:" + tTests.size());
    }

    public static void p(String s) {
        s = "22222";
    }


    public static void main(String[] args) {
//        findSqlOjbect();
//        update();
//        findMap();
        findObject();
//        findRowSet();
//        findBigList();
//        for (int i = 0; i < 10; i++) {
//            System.out.println(" 执行第 [" + (i + 1) + "]");
        //TODO 测试注解 赋值的时候做测试,将Test.class的接口类去掉 做测试.!
//        findAnnotationBigList();
//        findBigList();
//            findListObject();
//            findListMap();
//        }

//        insert();
//        update();
    }


}

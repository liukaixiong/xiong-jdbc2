package xn;

import JDBCTemplate.JDBCTemplateUtils;
import ObjectDuibi.JDBCTemplate2;
import com.alibaba.druid.pool.DruidDataSource;
import model.TTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Liukx
 * @create 2017-03-16 14:29
 * @email liukx@elab-plus.com
 **/
public class JDBCXN {
    JDBCTemplateUtils jdbcTemplateUtils = new JDBCTemplateUtils();
    static JDBCTemplate2 jdbcTemplate = null;

    static {
        jdbcTemplate = new JDBCTemplate2();
        jdbcTemplate.setFetchSize(Integer.MIN_VALUE);
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        druidDataSource.setUrl("jdbc:mysql://localhost/xiong?characterEncoding=UTF-8");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("1234");
        jdbcTemplate.setDataSource(druidDataSource);
    }

    public JdbcTemplate getJdbcTemplate() {
        JdbcTemplate jdbcTemplate = new JDBCTemplateUtils();
        return jdbcTemplate;
    }

    private void excuteInsert() {
        Object[] obj = new Object[6];
        for (int i = 0; i < obj.length; i++) {
            obj[i] = "测试JDBCTEMPLATE-" + i;
        }
        String sql = "insert into t_test (username, name, sex, status,   test_id, love_name)\n" +
                "    values (?, ?, ?, ?, ?, ?)";
        getJdbcTemplate().update(sql, obj);
    }

    private void excuteBatchInsert(int count) {
        Object[] obj = new Object[6];
        List<Object[]> list = new ArrayList<Object[]>();
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < obj.length; j++) {
                obj[j] = "测试JDBCTEMPLATE-" + j;
            }
            list.add(obj);
        }

        String sql = "insert into t_test (username, name, sex, status,   test_id, love_name)\n" +
                "    values (?, ?, ?, ?, ?, ?)";
        Long start = System.currentTimeMillis();
        getJdbcTemplate().batchUpdate(sql, list);
        Long end = System.currentTimeMillis();
        Long time = end - start;
        System.out.println(" --------- 耗时 : " + time);
    }

    private void testBatchInsert() {
        for (int i = 0; i < 10; i++) {
            System.out.println("测试 第 [ " + i + " ] 次 ..");
            excuteBatchInsert(100000);
        }
    }

    public void testInsert(int type, int count) {
        // 测试普通的jdbc性能
        java.util.Random r = new java.util.Random();
        Long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            excuteInsert();
        }
        Long end = System.currentTimeMillis();
        Long time = end - start;
        System.out.println(" 耗时  - " + time);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void insert() {
//        testInsert(0);
        testInsert(0, 1);
//        testInsert(1, 1);
        System.out.println("初始化完毕.....");
        for (int i = 0; i < 5; i++) {
            System.out.println("测试 [" + i + "] 次");
            testInsert(0, 100000);
        }

        System.out.println("执行完毕...");
    }

    ///////////////////////////////////////////////query//////////////////////////////////////

    public void queryForMap() {
        String sql = " select\n" +
                "      id\n" +
                "      ,username,name,sex,status,created,time,test_id,love_name\n" +
                "      from t_test\n" +
                "      where\n" +
                "      id = ?";
        Object[] objects = new Object[1];
        objects[0] = "2";
        Long start = System.currentTimeMillis();
        Map<String, Object> map = null;
        try {
            map = getJdbcTemplate().queryForMap(sql, objects);
        } catch (EmptyResultDataAccessException e) {

        }
        Long end = System.currentTimeMillis();
        Long time = end - start;
        System.out.println(" 查询结果 : " + map.toString());
        System.out.println(" Map 查询耗时 : " + time);
    }

    public void queryForListMap() {
        String sql = " select\n" +
                "      id\n" +
                "      ,username,name,sex,status,created,time,test_id,love_name\n" +
                "      from t_test\n" +
                "      where\n" +
                "      status = ?";
        Object[] objects = new Object[1];
        objects[0] = "1";
        Long start = System.currentTimeMillis();
        List<Map<String, Object>> maps = getJdbcTemplate().queryForList(sql, objects);
        Long end = System.currentTimeMillis();
        Long time = end - start;
        System.out.println(" 查询数据量: " + maps.size());
        System.out.println(" 耗时 : " + time);
    }

    public void queryForListObject() {
        String sql = " select\n" +
                "      id\n" +
                "      ,username,name,sex,status,created,time,test_id,love_name\n" +
                "      from t_test\n" +
                "      where\n" +
                "      status = ?";
        Object[] objects = new Object[1];
        objects[0] = "1";
        Long start = System.currentTimeMillis();
        BeanPropertyRowMapper<TTest> tBeanPropertyRowMapper = new BeanPropertyRowMapper<TTest>(TTest.class);
        List<TTest> query1 = getJdbcTemplate().query(sql, objects, tBeanPropertyRowMapper);
        Long end = System.currentTimeMillis();
        Long time = end - start;
        System.out.println(" 查询数据量: " + query1.size());
        System.out.println(" 耗时 : " + time);
    }


    public void fansheduibi() {
        String sql = " select\n" +
                "      id\n" +
                "      ,username,name,sex,status,created,time,test_id,love_name\n" +
                "      from t_test\n" +
                "      where\n" +
                "      id <= ?";
        Object[] p = new Object[1];
        p[0] = "1000001";
//        BeanPropertyRowMapper<TTest> rowMapper = new BeanPropertyRowMapper<TTest>(TTest.class);
//        List<TTest> query = getJdbcTemplate().query(sql, p, rowMapper);
//
//
//        Long start1 = System.currentTimeMillis();
//        List<TTest> query2 = getJdbcTemplate().query(sql, p, rowMapper);
//        Long end1 = System.currentTimeMillis();
//        Long time1 = end1 - start1;
//        System.out.println(" JDBCTemplate执行时间 - " + time1 + " 获取的结果集 : " + query2.size());


//        Long start = System.currentTimeMillis();
//        List<TTest> query1 = jdbcTemplate.queryForList2(sql, p, TTest.class);
//        Long end = System.currentTimeMillis();
//        Long time = end - start;
//        System.out.println(" 自定义的执行时间2 - " + time + " size : " + query1.size());
//        System.out.println("============================================================== start");
//        List<TTest> query9999 = jdbcTemplate.queryForList3(sql, p, TTest.class);
//        List<TTest> query8888 = jdbcTemplate.queryForList4(sql, p, TTest.class);
//        System.out.println("============================================================== end ");
//        System.out.println("============================================================== 初始化完成 ");
//        for (int i = 0; i < 3; i++) {
//            System.out.println(" ------------------------执行次数 : " + i);
//            Long start4 = System.currentTimeMillis();
//            List<TTest> query4 = jdbcTemplate.queryForList4(sql, p, TTest.class);
//            Long end4 = System.currentTimeMillis();
//            Long time4 = end4 - start4;
//            System.out.println(" 自定义的执行时间4444444 - " + time4 + " size : " + query4.size());
//        }
//
//
//
//        for (int i = 0; i < 3; i++) {
//            System.out.println(" --------------------JDBCTemplate开始------------- ");
//            Long start3 = System.currentTimeMillis();
//            List<TTest> query3 = jdbcTemplate.queryForList3(sql, p, TTest.class);
//            Long end3 = System.currentTimeMillis();
//            Long time3 = end3 - start3;
//            System.out.println(" 自定义的执行时间3 - " + time3 + " size : " + query3.size());
//            System.out.println(" --------------------JDBCTemplate结束------------- ");
//        }

//        for (int i = 0; i < 3; i++) {
//            System.out.println(" --------------------手动set开始------------- ");
//            Long start3 = System.currentTimeMillis();
//            List<TTest> query3 = jdbcTemplate.queryForList5(sql, p, TTest.class);
//            Long end3 = System.currentTimeMillis();
//            Long time3 = end3 - start3;
//            System.out.println(" 自定义的执行时间5 - " + time3 + " size : " + query3.size());
//            System.out.println(" --------------------手动set结束------------- ");
//        }

//        for (int i = 0; i < 3; i++) {
//            System.out.println(" --------------------手动set映射开始------------- ");
//            Long start3 = System.currentTimeMillis();
//            List<TTest> query3 = jdbcTemplate.queryForList6(sql, p, TTest.class);
//            Long end3 = System.currentTimeMillis();
//            Long time3 = end3 - start3;
//            System.out.println(" 自定义的执行时间666 - " + time3 + " size : " + query3.size());
//            System.out.println(" --------------------手动set映射结束------------- ");
//        }

        for (int i = 0; i < 3; i++) {
            System.out.println(" --------------------手动set以及混合jdbcTemplate映射开始------------- ");
            Long start3 = System.currentTimeMillis();
            List<TTest> query3 = jdbcTemplate.queryForList7(sql, p, TTest.class);
            Long end3 = System.currentTimeMillis();
            Long time3 = end3 - start3;
            System.out.println(" 自定义的执行时间777 - " + time3 + " size : " + query3.size()+" fetch : "+jdbcTemplate.getFetchSize());
            System.out.println(" --------------------手动set以及混合jdbcTemplate映射开始------------- ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        JDBCXN xn = new JDBCXN();
//        xn.testBatchInsert();
//        xn.queryForMap();
        System.out.println("初始化完毕...");
//        for (int i = 0; i < 10; i++) {
//            System.out.println(" 测试第 ["+i+"] 次..");
//            xn.queryForListMap();
//        }
//        for (int i = 0; i < 11; i++) {
//            System.out.println(" 测试 第 [" + i + "]次");
//            xn.queryForListObject();
//        }

        // 反射对比
        xn.fansheduibi();


    }
}

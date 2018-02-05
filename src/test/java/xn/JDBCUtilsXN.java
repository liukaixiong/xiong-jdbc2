package xn;

import com.x.jdbc.template.JdbcTemplateSupport;
import model.TTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * JDBCTemplateUtils性能测试
 *
 * @author Liukx
 * @create 2017-03-16 15:01
 * @email liukx@elab-plus.com
 **/
public class JDBCUtilsXN {
    JdbcTemplateSupport jdbcTemplateUtils = new JdbcTemplateSupport();

    public JdbcTemplate getJdbcTemplate() {
        JdbcTemplate jdbcTemplate = new JdbcTemplateSupport();
        return jdbcTemplate;
    }

    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = jdbcTemplateUtils.getNamedParameterJdbcTemplate();
        return namedParameterJdbcTemplate;
    }

    /////////////////////////////////////////////////insert start ///////////////////////////////////////////
    public void testInsert(int count) {
        // 测试普通的jdbc性能
        java.util.Random r = new java.util.Random();
        Long start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            TTest test = new TTest();
            test.setLove_name("测试");
            test.setName("test");
            test.setSex("男");
            test.setStatus("1");
            test.setUsername("测试用户名");
//            test.setTime(new Date());
//            test.setCreated(new Date());
            test.setTest_id(r.nextInt() + "");
            excuteInsert("test.insert", test);
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

    private void excuteInsert(String s, Object o) {
        int i = jdbcTemplateUtils.executeInsert(s, o);
//        System.out.println("插入結果："+i);
    }


    private void insert() {
//        testInsert(0);
        testInsert(1);
        System.out.println("初始化完毕.....");
        for (int i = 0; i < 5; i++) {
            testInsert(100000);
        }
        System.out.println(" 执行完毕..");
//        testInsert(100000);
    }
    //////////////////////////////insert end //////////////////////////////////////////////////////

    public void queryForListMap(int count) {
        for (int i = 0; i < count; i++) {
            System.out.println("开始测试  目标执行 第[" + i + "]次");
            TTest test = new TTest();
            test.setStatus("1");
            Long start = System.currentTimeMillis();
            List<Map<String, Object>> maps = jdbcTemplateUtils.executeQueryforListMap("test.selectByExample", test);
            Long end = System.currentTimeMillis();
            Long time = end - start;
            System.out.println(" 查询结果大小 : " + maps.size() + "\t  一共耗时: " + time);
//            System.out.println(maps);
        }
    }

    public void queryForListObject(int count) {
        for (int i = 0; i < count; i++) {
            System.out.println("开始测试  目标执行 第[" + i + "]次");
            TTest test = new TTest();
            test.setStatus("1");
            Long start = System.currentTimeMillis();
            List<TTest> tTests = jdbcTemplateUtils.executeQueryList("test.selectByExample", test, TTest.class);
            Long end = System.currentTimeMillis();
            Long time = end - start;
            System.out.println(" 查询结果大小 : " + tTests.size() + "\t  一共耗时: " + time);
//            System.out.println(maps);
        }
    }


    public static void main(String[] args) {
        JDBCUtilsXN xn = new JDBCUtilsXN();
//        xn.insert();
//        xn.queryForListMap(11);
        xn.queryForListObject(1);
    }
}

import com.x.jdbc.template.JdbcTemplateSupport;
import model.TTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 性能测试
 *
 * @author Liukx
 * @create 2017-03-15 23:07
 * @email liukx@elab-plus.com
 **/
public class xingnengTest {
    JdbcTemplateSupport jdbcTemplateUtils = new JdbcTemplateSupport();
    JdbcTemplate jdbcTemplate = new JdbcTemplateSupport();
    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        NamedParameterJdbcTemplate namedParameterJdbcTemplate = jdbcTemplateUtils.getNamedParameterJdbcTemplate();
        return namedParameterJdbcTemplate;
    }


    public void testInsert(int type, int count) {
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
            test.setTime(new Date());
            test.setCreated(new Date());
            test.setTest_id(r.nextInt()+"");
            excuteInsert("test.insert", test, type);
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

    private void excuteInsert(String s, Object o, int type) {
        if (type == 0) {
            Object[] obj = new Object[6];
            for (int i = 0; i < obj.length; i++) {
                obj[i] = i;
            }
            String sql = "insert into t_test (username, name, sex, status,   test_id, love_name)\n" +
                    "    values (?, ?, ?, ?, ?, ?)";
            getJdbcTemplate().update(sql, obj);
        } else {
            jdbcTemplateUtils.executeInsert(s, o);
        }
    }

    private void insert() {
//        testInsert(0);
//        testInsert(0, 1);
//        testInsert(1, 1);
        System.out.println("初始化完毕.....");
        testInsert(1, 1000);
//        testInsert(1, 100000);
    }


    public void selectType(int type, Object o) {
        Long start = System.currentTimeMillis();
        if (type == 0) {
//            String sql = SqlKit.sql("test.selectByExample");
//            Map map = new HashMap();
//            map.put("status", "1");
            String sql = " select\n" +
                    "      id\n" +
                    "      ,username,name,sex,status,created,time,test_id,love_name\n" +
                    "      from t_test\n" +
                    "      where status = ?";
            Object [] obj = new Object[1];
            obj[0] = "1";
//            List list =  jdbcTemplateUtils.queryForList(sql, obj,TTest.class);
            List<Map<String, Object>> maps = getJdbcTemplate().queryForList(sql, obj);
            System.out.println("=========>" + maps.size());
        } else {
            TTest test = new TTest();
            test.setStatus("1");
            List<TTest> tTests = jdbcTemplateUtils.executeQueryList("test.selectByExample", test, TTest.class);
            System.out.println("----------->" + tTests.size());
        }
        Long end = System.currentTimeMillis();
        Long time = end - start;
        System.out.println(" 耗时: " + time);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void select() {
        TTest t = new TTest();
        t.setStatus("1");
        selectType(0, t);
        selectType(1, t);
        System.out.println("============================初始化完毕============================");
        for(int i=0;i<10;i++) {
            System.out.println(" ======================================> 第"+i+"次测试。。");
            selectType(0, t);
            selectType(1, t);
        }
    }


    public static void main(String[] args) {
        xingnengTest t = new xingnengTest();
        t.insert();
//        t.select();
    }
}

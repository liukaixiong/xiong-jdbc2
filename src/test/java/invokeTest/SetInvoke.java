package invokeTest;

import com.x.jdbc.model.TTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Liukx
 * @create 2017-03-19 11:54
 * @email liukx@elab-plus.com
 **/
public class SetInvoke {
    public static void main(String[] args) {
        List<TTest> list = new ArrayList<TTest>();
        Long start = System.currentTimeMillis();
        for (int i = 0; i < 1200000; i++) {
            TTest t = new TTest();
            t.setId(1);
            t.setCreated(new Date());
            t.setLove_name("aaaaaa");
            t.setSex("1");
            t.setTime(new Date());
            t.setStatus("111");
            t.setTest_id("11111111");
            list.add(t);
        }
        Long end = System.currentTimeMillis();
        System.out.println("-->"+(end-start) +"\t"+list.size());


    }
}

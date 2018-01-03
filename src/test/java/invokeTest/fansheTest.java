package invokeTest;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.x.jdbc.model.TTest;

import java.lang.reflect.Method;

/**
 * @author Liukx
 * @create 2017-03-19 10:02
 * @email liukx@elab-plus.com
 **/
public class fansheTest {

    public static void testInvoke() throws Exception {
        long now;
        long sum = 0;
        TTest t = new TTest();

        now = System.currentTimeMillis();
///////////////////////////////getset反射////////////////////////////////////////
        for (int i = 0; i < 500000; ++i) {
            t.setId(i);
            sum += t.getId();
        }

        System.out.println("get-set耗时" + (System.currentTimeMillis() - now) + "ms秒，和是" + sum);

        sum = 0;
        now = System.currentTimeMillis();
///////////////////////////////标准反射////////////////////////////////////////
        for (int i = 0; i < 500000; ++i) {
            Class<?> c = Class.forName("model.TTest");
            Class<?>[] argsType = new Class[1];
            argsType[0] = Integer.class;
            Method m = c.getMethod("setId", argsType);
            m.invoke(t, i);
            sum += t.getId();
        }
        System.out.println("标准反射耗时" + (System.currentTimeMillis() - now) + "ms，和是" + sum);
///////////////////////////////缓存反射////////////////////////////////////////
        sum = 0;

        Class<?> c = Class.forName("model.TTest");
        Class<?>[] argsType = new Class[1];
        argsType[0] = Integer.class;
        Method m = c.getMethod("setId", argsType);

        now = System.currentTimeMillis();

        for (int i = 0; i < 500000; ++i) {
            m.invoke(t, i);
            sum += t.getId();
        }
        System.out.println("缓存反射耗时" + (System.currentTimeMillis() - now) + "ms，和是" + sum);
///////////////////////////////reflectasm反射////////////////////////////////////////
        sum = 0;
        MethodAccess ma = MethodAccess.get(TTest.class);
        int index = ma.getIndex("setId");
        now = System.currentTimeMillis();

        for (int i = 0; i < 50000000; ++i) {
            ma.invoke(t, index, i);
            sum += t.getId();
        }
        System.out.println("reflectasm反射耗时" + (System.currentTimeMillis() - now) + "ms，和是" + sum);
    }

    public static void main(String[] args) throws Exception {
        testInvoke();
    }
}

package ObjectDuibi;

import com.esotericsoftware.reflectasm.MethodAccess;
import com.x.jdbc.model.TTest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.jdbc.core.RowMapper;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Liukx
 * @create 2017-03-17 12:26
 * @email liukx@elab-plus.com
 **/
public class RowMapper2<T> implements RowMapper<T> {
    private Class<T> mappedClass;
    private static MethodAccess ma = MethodAccess.get(TTest.class);

    public RowMapper2(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
    }

    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        T t = null;
        try {
//            System.out.println("初始化成功~");
            T mappedObject = BeanUtils.instantiate(this.mappedClass);
            BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(mappedObject);
            BeanInfo beanInfo = Introspector.getBeanInfo(mappedClass, Object.class);
            PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();

            for (PropertyDescriptor pd : pds) {

                String name = pd.getName();
                // 假设已经缓存好了位置
                 int addNameIndex = ma.getIndex("set" + firstLetterToUpper(name));
//                Method method = pd.getWriteMethod();
                try {
                    Object object = rs.getObject(name);
                    ma.invoke(t, addNameIndex,object);
                } catch (Exception e) {
                    e.printStackTrace();
//                    System.out.println(" name - " + name + "\t value = " + rs.getObject(name));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return t;
    }

    public static String firstLetterToUpper(String val) {
        char[] array = val.toCharArray();
        array[0] -= 32;
        return String.valueOf(array);
    }



}

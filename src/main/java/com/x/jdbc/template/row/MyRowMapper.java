package com.x.jdbc.template.row;

import com.x.jdbc.anno.Column;
import com.x.jdbc.mapping.ColumnMapping;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.*;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * JDBCTemplate原始反射加上手动set方法混合使用方式
 *
 * @author Liukx
 * @create 2017-03-17 17:23
 * @email liukx@elab-plus.com
 **/
public class MyRowMapper<T> implements RowMapper<T> {

    /**
     * Logger available to subclasses
     */
    protected final Log logger = LogFactory.getLog(getClass());

    /**
     * The class we are mapping to
     */
    private Class<T> mappedClass;

    /**
     * Map of the fields we provide mapping for
     */
    private Map<String, PropertyDescriptor> mappedFields;

    /**
     * Set of bean properties we provide mapping for
     */
    private Set<String> mappedProperties;


    /**
     * 构建一个新的列映射
     *
     * @param mappedClass the class that each row should be mapped to
     */
    public MyRowMapper(Class<T> mappedClass) {
        initialize(mappedClass);
    }

    /**
     * Set the class that each row should be mapped to.
     */
    public void setMappedClass(Class<T> mappedClass) {
        if (this.mappedClass == null) {
            initialize(mappedClass);
        } else {
            if (!this.mappedClass.equals(mappedClass)) {
                throw new InvalidDataAccessApiUsageException("The mapped class can not be reassigned to map to " +
                        mappedClass + " since it is already providing mapping for " + this.mappedClass);
            }
        }
    }

    /**
     * 初始化该类的类信息
     *
     * @param mappedClass the mapped class.
     */
    protected void initialize(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
        this.mappedFields = new HashMap<String, PropertyDescriptor>();
        this.mappedProperties = new HashSet<String>();
        PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(mappedClass);
        for (PropertyDescriptor pd : pds) {
            if (pd.getWriteMethod() != null) {
                String name = pd.getName().toLowerCase();
                try {
                    Field declaredField = mappedClass.getDeclaredField(pd.getName());
                    // 判断该注解是否存在
                    if (declaredField.getAnnotation(Column.class) != null) {
                        name = declaredField.getAnnotation(Column.class).name();
                        // 将该注解绑定的name存放到map中
                        this.mappedFields.put(name, pd);
                    } else {
                        //将字段中的名称绑定到map中
                        this.mappedFields.put(name, pd);
                    }
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }

                // 将驼峰的命名也备份到里面,切记,优先级是 Column -> 驼峰 -> 数据库字段名称
                String underscoredName = underscoreName(pd.getName());
                if (!pd.getName().toLowerCase().equals(underscoredName)) {
                    this.mappedFields.put(underscoredName, pd);
                }
                this.mappedProperties.add(name);
            }
        }
//        System.out.println("复制完毕..." + this.mappedFields.toString());
    }

    /**
     * 驼峰命名查询
     * <p>
     * 如果数据库查询到的是 user_name字段, 则对应到的Model中的是userName
     * </p>
     *
     * @param name
     * @return
     */
    private String underscoreName(String name) {
        if (!StringUtils.hasLength(name)) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append(name.substring(0, 1).toLowerCase());
        for (int i = 1; i < name.length(); i++) {
            String s = name.substring(i, i + 1);
            String slc = s.toLowerCase();
            if (!s.equals(slc)) {
                result.append("_").append(slc);
            } else {
                result.append(s);
            }
        }
        return result.toString();
    }

    /**
     * Get the class that we are mapping to.
     */
    public final Class<T> getMappedClass() {
        return this.mappedClass;
    }

    /**
     * Extract the values for all columns in the current row.
     * <p>Utilizes public setters and result set metadata.
     *
     * @see ResultSetMetaData
     */
    @Override
    public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
        Assert.state(this.mappedClass != null, "Mapped class was not specified");
        T mappedObject = BeanUtils.instantiate(this.mappedClass);
        // 这里将实体类和接口进行映射,如果实体类中有实现ColumnMapping这个接口,可以手动赋值
        if (mappedObject instanceof ColumnMapping) {
            ((ColumnMapping) mappedObject).mappingColumn(rs);
            return mappedObject;
        }

        BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(mappedObject);
        initBeanWrapper(bw);

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        for (int index = 1; index <= columnCount; index++) {
            String column = JdbcUtils.lookupColumnName(rsmd, index);
            PropertyDescriptor pd = this.mappedFields.get(column.replaceAll(" ", "").toLowerCase());
            if (pd != null) {
                try {
                    Object value = getColumnValue(rs, index, pd);
                    try {
                        bw.setPropertyValue(pd.getName(), value);
                    } catch (TypeMismatchException e) {

                    }
                } catch (NotWritablePropertyException ex) {

                }
            }
        }

        return mappedObject;
    }

    /**
     * Initialize the given BeanWrapper to be used for row mapping.
     * To be called for each row.
     * <p>The default implementation is empty. Can be overridden in subclasses.
     *
     * @param bw the BeanWrapper to initialize
     */
    protected void initBeanWrapper(BeanWrapper bw) {
//        this.mappedClass = mappedClass;
//        this.mappedFields = new HashMap<String, PropertyDescriptor>();
//        this.mappedProperties = new HashSet<String>();
//        PropertyDescriptor[] pds = BeanUtils.getPropertyDescriptors(mappedClass);
//        for (PropertyDescriptor pd : pds) {
//            if (pd.getWriteMethod() != null) {
//                this.mappedFields.put(pd.getName().toLowerCase(), pd);
//                String underscoredName = underscoreName(pd.getName());
//                if (!pd.getName().toLowerCase().equals(underscoredName)) {
//                    this.mappedFields.put(underscoredName, pd);
//                }
//                this.mappedProperties.add(pd.getName());
//            }
//        }
    }

    /**
     * Retrieve a JDBC object value for the specified column.
     * <p>The default implementation calls
     * {@link JdbcUtils#getResultSetValue(ResultSet, int, Class)}.
     * Subclasses may override this to check specific value types upfront,
     * or to post-process values return from {@code getResultSetValue}.
     *
     * @param rs    is the ResultSet holding the data
     * @param index is the column index
     * @param pd    the bean property that each result object is expected to match
     *              (or {@code null} if none specified)
     * @return the Object value
     * @throws SQLException in case of extraction failure
     * @see JdbcUtils#getResultSetValue(ResultSet, int, Class)
     */
    protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) throws SQLException {
        return JdbcUtils.getResultSetValue(rs, index, pd.getPropertyType());
    }


    /**
     * Static factory method to create a new BeanPropertyRowMapper
     * (with the mapped class specified only once).
     *
     * @param mappedClass the class that each row should be mapped to
     */
    public static <T> BeanPropertyRowMapper<T> newInstance(Class<T> mappedClass) {
        BeanPropertyRowMapper<T> newInstance = new BeanPropertyRowMapper<T>();
        newInstance.setMappedClass(mappedClass);
        return newInstance;
    }

}

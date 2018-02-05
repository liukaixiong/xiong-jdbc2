package com.x.jdbc.template.base;

import com.x.jdbc.template.common.utils.StringUtils;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.jdbc.core.RowMapper;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * 实体对象的反射操作
 *
 * @param <T>
 * @author Tom
 */
public class EntityOperation<T> {
    private Logger log = Logger.getLogger(EntityOperation.class);
    public Class<T> entityClass = null; // 泛型实体Class对象
    public final Map<String, PropertyMapping> mappings;
    public final RowMapper<T> rowMapper;

    public final String tableName;
    public String allColumn = "*";
    public Field pkField;
    public final Map<String, String> allProperty;

    public EntityOperation(Class<T> clazz, String pk) {
//        if (!clazz.isAnnotationPresent(Entity.class)) {
//            throw new Exception("在" + clazz.getName() + "中没有找到Entity注解，不能做ORM映射");
//        }
        this.entityClass = clazz;
        Table table = entityClass.getAnnotation(Table.class);
        if (table != null) {
            this.tableName = table.name();
        } else {
            this.tableName = entityClass.getSimpleName();
        }
        Map<String, Method> getters = JdbcClassUtils.findPublicGetters(entityClass);
        Map<String, Method> setters = JdbcClassUtils.findPublicSetters(entityClass);
        Field[] fields = JdbcClassUtils.findFields(entityClass);
        fillPkFieldAndAllColumn(pk, fields);
        this.mappings = getPropertyMappings(getters, setters, fields);
        this.allColumn = this.mappings.keySet().toString().replace("[", "").replace("]", "").replaceAll(" ", "");
        this.rowMapper = createRowMapper();
        this.allProperty = getPropertyMappingMap(getters, setters, fields);
    }

    Map<String, PropertyMapping> getPropertyMappings(Map<String, Method> getters, Map<String, Method> setters, Field[] fields) {
        Map<String, PropertyMapping> mappings = new HashMap<String, PropertyMapping>();
        String name;
        for (Field field : fields) {
            if (field.isAnnotationPresent(Transient.class) || field.isAnnotationPresent(Ignore.class)) {
                continue;
            }
            name = field.getName();
            if (name.startsWith("is")) {
                name = name.substring(2);
            }
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
            Method setter = setters.get(name);
            Method getter = getters.get(name);
            if (setter == null || getter == null) {
                continue;
            }
            Column column = field.getAnnotation(Column.class);
            if (column == null) {
                mappings.put(field.getName(), new PropertyMapping(getter, setter, field));
            } else {
                mappings.put(column.name(), new PropertyMapping(getter, setter, field));
            }
        }
        return mappings;
    }

    public Map<String, String> getPropertyMappingMap(Map<String, Method> getters, Map<String, Method> setters, Field[] fields) {
        Map<String, String> mappings = new HashMap<String, String>();
        String name;
        for (Field field : fields) {
            // 忽略掉字段属性中带有Transient、Ignore的值
            if (field.isAnnotationPresent(Transient.class) || field.isAnnotationPresent(Ignore.class)) {
                continue;
            }
            name = field.getName();
            if (name.startsWith("is")) {
                name = name.substring(2);
            }
            name = Character.toLowerCase(name.charAt(0)) + name.substring(1);
            Method setter = setters.get(name);
            Method getter = getters.get(name);
            if (setter == null || getter == null) {
                continue;
            }

            // 查找到对应的@Column注解,如果有则按照注解中的值进行存储,没有则按照属性名进行存储
            Column column = field.getAnnotation(Column.class);
            if (column == null) {
                mappings.put(field.getName(), field.getName());
            } else {
                mappings.put(column.name(), field.getName());
            }
        }
        return mappings;
    }

    RowMapper<T> createRowMapper() {
        return new RowMapper<T>() {
            public T mapRow(ResultSet rs, int rowNum) throws SQLException {
                try {
//                    T t = entityClass.newInstance();
                    T t = BeanUtils.instantiate(entityClass);
                    // 使用BeanWrapper包装能够将对象的值进行适配 , 进行对象的隐士转换
                    BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(t);
                    ResultSetMetaData meta = rs.getMetaData();
                    int columns = meta.getColumnCount();
                    String columnName;
                    for (int i = 1; i <= columns; i++) {
                        Object value = rs.getObject(i);
                        columnName = meta.getColumnName(i);
                        String fieldName = mappings.get(columnName).fieldName;
                        bw.setPropertyValue(fieldName, value);
                    }
                    return t;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    protected void fillBeanFieldValue(T t, String columnName, Object value) {
        if (value != null) {
            PropertyMapping pm = mappings.get(columnName);
            if (pm != null) {
                try {
                    pm.set(t, value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 查找主键
    private void fillPkFieldAndAllColumn(String pk, Field[] fields) {
        //设定主键
        try {
            if (!StringUtils.isEmpty(pk)) {
                pkField = entityClass.getDeclaredField(pk);
                pkField.setAccessible(true);
            }
        } catch (Exception e) {
            log.debug("没找到主键列,主键列名必须与属性名相同");
        }
        for (int i = 0; i < fields.length; i++) {
            Field f = fields[i];
            if (StringUtils.isEmpty(pk)) {
                Id id = f.getAnnotation(Id.class);
                if (id != null) {
                    pkField = f;
                    break;
                }
            }
        }
    }

    public T parse(ResultSet rs) {
        T t = null;
        if (null == rs) {
            return null;
        }
        Object value = null;
        try {
            t = (T) entityClass.newInstance();
            for (String columnName : mappings.keySet()) {
                try {
                    value = rs.getObject(columnName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                fillBeanFieldValue(t, columnName, value);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return t;
    }

    public Map<String, Object> parse(T t) {
        Map<String, Object> _map = new HashMap<String, Object>();
        try {

            for (String columnName : mappings.keySet()) {
                Object value = mappings.get(columnName).getter.invoke(t);
                if (value == null)
                    continue;
                _map.put(columnName, value);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return _map;
    }

    public void println(T t) {
        try {
            for (String columnName : mappings.keySet()) {
                Object value = mappings.get(columnName).getter.invoke(t);
                if (value == null)
                    continue;
                System.out.println(columnName + " = " + value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class PropertyMapping {

    final boolean insertable;
    final boolean updatable;
    final String columnName;
    final boolean id;
    final Method getter;
    final Method setter;
    final Class enumClass;
    final String fieldName;

    public PropertyMapping(Method getter, Method setter, Field field) {
        this.getter = getter;
        this.setter = setter;
        this.enumClass = getter.getReturnType().isEnum() ? getter.getReturnType() : null;
        Column column = field.getAnnotation(Column.class);
        this.insertable = column == null || column.insertable();
        this.updatable = column == null || column.updatable();
        this.columnName = column == null ? JdbcClassUtils.getGetterName(getter) : ("".equals(column.name()) ? JdbcClassUtils.getGetterName(getter) : column.name());
        this.id = field.isAnnotationPresent(Id.class);
        this.fieldName = field.getName();
    }

    @SuppressWarnings("unchecked")
    Object get(Object target) throws Exception {
        Object r = getter.invoke(target);
        return enumClass == null ? r : Enum.valueOf(enumClass, (String) r);
    }

    @SuppressWarnings("unchecked")
    void set(Object target, Object value) throws Exception {
        if (enumClass != null && value != null) {
            value = Enum.valueOf(enumClass, (String) value);
        }
        //BeanUtils.setProperty(target, fieldName, value);
        try {
            if (value != null) {
                setter.invoke(target, setter.getParameterTypes()[0].cast(value));
            }
        } catch (Exception e) {
            e.printStackTrace();
            /**
             * 出错原因如果是boolean字段 mysql字段类型 设置tinyint(1)
             */
            System.err.println(fieldName + "--" + value);
        }

    }
}


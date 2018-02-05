package com.x.jdbc.template.binding;

import com.x.jdbc.sql.ConfigurableFactory;
import com.x.jdbc.template.IJdbcTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * dao类接口的注册
 * <p>
 * 这里会将dao类的接口统一进行注册到Map中,并且会对应一个处理工厂
 * </p>
 *
 * @author Liukx
 * @create 2018-01-03 14:40
 * @email liukx@elab-plus.com
 **/
public class DaoRegister<T> {

    // 承装动态代理的容器
    private final Map<Class<?>, DaoProxyFactory> knownMappers = new HashMap();

    public <T> T getDao(Class<T> type, IJdbcTemplate jdbcTemplate, ConfigurableFactory configurableFactory) {
        DaoProxyFactory daoProxyFactory = this.knownMappers.get(type);
        if (daoProxyFactory == null) {
            throw new BindingException("Type " + type + " is not known to the MapperRegistry.");
        } else {
            try {
                return (T) daoProxyFactory.newInstance(jdbcTemplate,configurableFactory);
            } catch (Exception var5) {
                throw new BindingException("Error getting mapper instance. Cause: " + var5, var5);
            }
        }
    }

    public <T> boolean hasMapper(Class<T> type) {
        return this.knownMappers.containsKey(type);
    }

    /**
     * 注册一个类型到map中,并对应一个对象
     *
     * @param type
     * @param <T>
     */
    public <T> void addMapper(Class<T> type) {
        if (type.isInterface()) {
            if (this.hasMapper(type)) {
                throw new BindingException("Type " + type + " is already known to the MapperRegistry.");
            }
            boolean loadCompleted = false;
            try {
                this.knownMappers.put(type, new DaoProxyFactory(type));
                loadCompleted = true;
            } finally {
                if (!loadCompleted) {
                    this.knownMappers.remove(type);
                }
            }
        }

    }
}

package com.x.jdbc.template.binding;

import com.x.jdbc.sql.ConfigurableFactory;
import com.x.jdbc.template.IJdbcTemplate;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 具体代理dao类的工厂
 * <p>
 * 持有整个dao集合对象,每当方法调用会从工厂里面找指定的代理方法
 * </p>
 *
 * @author Liukx
 * @create 2018-01-03 11:30
 * @email liukx@elab-plus.com
 **/
public class DaoProxyFactory<T> {

    private final Class<T> mapperInterface;
    private Map<Method, DaoProxy> methodCache = new ConcurrentHashMap();

    public DaoProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public Class<T> getMapperInterface() {
        return this.mapperInterface;
    }

    public Map<Method, DaoProxy> getMethodCache() {
        return this.methodCache;
    }

    protected T newInstance(DaoProxy<T> mapperProxy) {
        return (T) Proxy.newProxyInstance(this.mapperInterface.getClassLoader(), new Class[]{this.mapperInterface}, mapperProxy);
    }

    public T newInstance(IJdbcTemplate jdbcTemplate, ConfigurableFactory configurableFactory) {
        DaoProxy<T> mapperProxy = new DaoProxy(jdbcTemplate, this.mapperInterface, this.methodCache,configurableFactory);
        return this.newInstance(mapperProxy);
    }
}

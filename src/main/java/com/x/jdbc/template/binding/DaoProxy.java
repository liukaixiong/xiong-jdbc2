package com.x.jdbc.template.binding;

import com.x.jdbc.sql.ConfigurableFactory;
import com.x.jdbc.template.IJdbcTemplate;
import com.x.jdbc.template.common.utils.ExceptionUtil;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * 具体的jdk代理
 * <p>
 * JDK动态代理的具体实现类,这里会将具体的代理过程交给DaoMethod处理,该类只对做一次缓存
 * </p>
 *
 * @author Liukx
 * @create 2018-01-03 11:31
 * @email liukx@elab-plus.com
 **/
public class DaoProxy<T> implements InvocationHandler, Serializable {
    private static final long serialVersionUID = -6424540398559729838L;
    private final IJdbcTemplate jdbcTemplate;
    private final Class<T> mapperInterface;
    private final Map<Method, DaoMethod> methodCache;
    private ConfigurableFactory configurableFactory;

    public DaoProxy(IJdbcTemplate jdbcTemplate, Class<T> mapperInterface, Map<Method, DaoMethod> methodCache, ConfigurableFactory configurableFactory) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapperInterface = mapperInterface;
        this.methodCache = methodCache;
        this.configurableFactory = configurableFactory;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            try {
                return method.invoke(this, args);
            } catch (Throwable var5) {
                throw ExceptionUtil.unwrapThrowable(var5);
            }
        } else {
            DaoMethod mapperMethod = this.cachedMapperMethod(method);
            return mapperMethod.execute(args);
        }
    }

    /**
     * 缓存DaoMethod对象
     *
     * @param method
     * @return
     */
    private DaoMethod cachedMapperMethod(Method method) {
        DaoMethod daoMethod = this.methodCache.get(method);
        if (daoMethod == null) {
            daoMethod = new DaoMethod(this.mapperInterface, method, jdbcTemplate, configurableFactory);
            this.methodCache.put(method, daoMethod);
        }
        return daoMethod;
    }
}

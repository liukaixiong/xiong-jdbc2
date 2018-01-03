package com.x.jdbc.template.binding;

import com.x.jdbc.template.IJDBCTemplate;
import org.apache.ibatis.reflection.ExceptionUtil;

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
    private final IJDBCTemplate jdbcTemplate;
    private final Class<T> mapperInterface;
    private final Map<Method, DaoMethod> methodCache;

    public DaoProxy(IJDBCTemplate jdbcTemplate, Class<T> mapperInterface, Map<Method, DaoMethod> methodCache) {
        this.jdbcTemplate = jdbcTemplate;
        this.mapperInterface = mapperInterface;
        this.methodCache = methodCache;
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
            return mapperMethod.execute(this.jdbcTemplate, args);
        }
    }

    private DaoMethod cachedMapperMethod(Method method) {
        DaoMethod daoMethod = (DaoMethod) this.methodCache.get(method);
        if (daoMethod == null) {
            daoMethod = new DaoMethod(this.mapperInterface, method, jdbcTemplate);
            this.methodCache.put(method, daoMethod);
        }
        return daoMethod;
    }
}

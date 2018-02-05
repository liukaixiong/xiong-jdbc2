package com.x.jdbc.template.facotry;

import com.x.jdbc.model.JoinPropertiesModel;
import com.x.jdbc.template.IJdbcTemplate;
import com.x.jdbc.template.common.utils.BeanUtils;
import com.x.jdbc.template.common.utils.ObjectUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.collections.map.LinkedMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cglib.proxy.Callback;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import javax.persistence.JoinColumn;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 属性代理工厂
 *
 * @author Liukx
 * @create 2018-02-02 19:59
 * @email liukx@elab-plus.com
 **/
public class PropertiesProxyFactory {
    protected final static Log log = LogFactory.getLog(PropertiesProxyFactory.class);

    /**
     * 创建一个动态代理
     *
     * @param type                被代理的类信息
     * @param callback            回调方法
     * @param constructorArgTypes 构造参数类型
     * @param constructorArgs     构造参数值
     * @return
     */
    private static Object crateProxy(Class<?> type, Callback callback, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
        Enhancer enhancer = new Enhancer();
        enhancer.setCallback(callback);
        enhancer.setSuperclass(type);

        try {
            type.getDeclaredMethod("writeReplace", new Class[0]);
            log.debug("writeReplace method was found on bean " + type + ", make sure it returns this");
        } catch (NoSuchMethodException var8) {
//            enhancer.setInterfaces(new Class[]{WriteReplaceInterface.class});
        } catch (SecurityException var9) {
            ;
        }

        Object enhanced = null;
        if (constructorArgTypes.isEmpty()) {
            enhanced = enhancer.create();
        } else {
            Class[] typesArray = constructorArgTypes.toArray(new Class[constructorArgTypes.size()]);
            Object[] valuesArray = constructorArgs.toArray(new Object[constructorArgs.size()]);
            enhanced = enhancer.create(typesArray, valuesArray);
        }
        return enhanced;
    }

    /**
     * 创建一个懒加载代理工厂的入口
     *
     * @param jdbcTemplate  SQL执行器
     * @param mappingObject 被代理的类对象
     * @param list          需要触发执行的方法信息
     * @return
     */
    public Object createProxy(IJdbcTemplate jdbcTemplate, Object mappingObject, List<JoinPropertiesModel> list) {
        return PropertiesProxyFactory.EnhancedResultObjectProxyImpl.createProxy(jdbcTemplate, mappingObject, list, new ArrayList<Class<?>>(), new ArrayList<Object>());
    }

    private static class EnhancedResultObjectProxyImpl implements MethodInterceptor {
        /**
         * SQL执行器
         */
        private IJdbcTemplate jdbcTemplate;
        /**
         * 被代理的类
         */
        private Class<?> type;
        /**
         * 此次被代理的类的相关触发列表,最终会被构建成一个invokeJoinMap
         */
        private List<JoinPropertiesModel> joinPropertiesList;
        /**
         * 动态代理中的构造参数类型方法,目前没有实现
         */
        private List<Class<?>> constructorArgTypes;
        /**
         * 动态代理中的构造参数对应的值,目前没有实现
         */
        private List<Object> constructorArgs;
        /**
         * 触发懒加载的方法信息
         */
        private Map<Method, JoinPropertiesModel> invokeJoinMap;
        /**
         * 懒加载方法,把已经加载过的方法缓存进来,确保下一次不会执行
         */
        private Set<String> lazyLoadTriggerMethods;

        public EnhancedResultObjectProxyImpl(IJdbcTemplate jdbcTemplate, Class<?> supperGenricType, List<JoinPropertiesModel> joinPropertiesList) {
            this.jdbcTemplate = jdbcTemplate;
            this.type = supperGenricType;
            this.joinPropertiesList = joinPropertiesList;
            this.invokeJoinMap = new HashedMap();
            this.lazyLoadTriggerMethods = new HashSet<String>();
            if (this.joinPropertiesList.size() > 0) {
                for (int i = 0; i < this.joinPropertiesList.size(); i++) {
                    JoinPropertiesModel joinPropertiesModel = joinPropertiesList.get(i);
                    Method readMethod = joinPropertiesModel.getPd().getReadMethod();
                    this.invokeJoinMap.put(readMethod, joinPropertiesModel);
                }
            }
        }

        /**
         * 创建一个Cglib的动态代理
         *
         * @param jdbcTemplate        SQL执行器
         * @param mappingObject       代理的类信息
         * @param list                触发懒加载的信息
         * @param constructorArgTypes 构造参数类型
         * @param constructorArgs     构造参数值
         * @return
         */
        public static Object createProxy(IJdbcTemplate jdbcTemplate, Object mappingObject, List<JoinPropertiesModel> list, List<Class<?>> constructorArgTypes, List<Object> constructorArgs) {
            Class<?> type = mappingObject.getClass();
            PropertiesProxyFactory.EnhancedResultObjectProxyImpl callback = new PropertiesProxyFactory.EnhancedResultObjectProxyImpl(jdbcTemplate, type, list);
            Object enhanced = PropertiesProxyFactory.crateProxy(type, callback, constructorArgTypes, constructorArgs);
            BeanUtils.copyBeanProperties(type, mappingObject, enhanced);
            return enhanced;
        }

        /**
         * 执行Cglib代理切面
         * 1. 判断是否为触发的属性
         * 2. 如果是该属性,则构建一个相关的执行SQL流程
         * 3. 如果触发的属性的关联字段为null的话,则不会触发sql执行
         *
         * @param o
         * @param method
         * @param objects
         * @param methodProxy
         * @return
         * @throws Throwable
         */
        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {

            JoinPropertiesModel joinPropertiesModel = invokeJoinMap.get(method);
            if (joinPropertiesModel != null && !this.lazyLoadTriggerMethods.contains(method.getName())) {
                PropertyDescriptor pd = joinPropertiesModel.getPd();
                String sql = joinPropertiesModel.getSql();
                JoinColumn[] joinColumns = joinPropertiesModel.getJoinTable().joinColumns();
                Map<String, Object> map = new LinkedMap();
                // 参数验证
                boolean paramsCheck = true;
                for (int i = 0; i < joinColumns.length; i++) {
                    JoinColumn joinColumn = joinColumns[i];
                    String columnName = joinColumn.referencedColumnName();
                    String name = joinColumn.name();
                    Object value = BeanUtils.invokegetGetterMethod(o, name);
                    if (ObjectUtils.isNull(value)) {
                        log.debug(" Lazy loading cannot be performed and the associated condition is empty. [" + name + " = null ]");
                        paramsCheck = false;
                        break;
                    }
                    map.put(columnName, value);
                }
                Object object = null;
                if (paramsCheck) {
                    if (pd.getPropertyType() == List.class) {
                        Type type = pd.getWriteMethod().getGenericParameterTypes()[0];
                        Class<?> listType = BeanUtils.getListType(type);
                        object = this.jdbcTemplate.executeQueryList(sql, map, listType);
                    } else {
                        object = this.jdbcTemplate.executeQueryObject(sql, map, joinPropertiesModel.getPd().getPropertyType());
                    }
                    BeanUtils.setProperty(o, pd.getName(), object);
                    this.lazyLoadTriggerMethods.add(method.getName());
                }
            }

            return methodProxy.invokeSuper(o, objects);
        }

    }
}

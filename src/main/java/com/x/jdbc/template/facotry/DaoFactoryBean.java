package com.x.jdbc.template.facotry;

import com.x.jdbc.sql.ConfigurableFactory;
import com.x.jdbc.template.IJdbcTemplate;
import com.x.jdbc.template.binding.DaoRegister;
import org.springframework.beans.factory.FactoryBean;

/**
 * dao层代理工厂
 * <p>
 * 所有通过当前工厂产出的bean的一系列过程都会经过该类去处理
 * </p>
 *
 * @author Liukx
 * @create 2018-01-03 11:13
 * @email liukx@elab-plus.com
 **/
public class DaoFactoryBean<T> implements FactoryBean<T> {

    //具体执行者
    private IJdbcTemplate jdbcTemplate;

    //检查父类
    private Class<?> mapperInterface;

    // dao注册工厂
    private DaoRegister daoRegister;

    private ConfigurableFactory configurableFactory;

    public ConfigurableFactory getConfigurableFactory() {
        return configurableFactory;
    }

    public void setConfigurableFactory(ConfigurableFactory configurableFactory) {
        this.configurableFactory = configurableFactory;
    }

    public Class<?> getMapperInterface() {
        return mapperInterface;
    }

    public DaoRegister getDaoRegister() {
        return daoRegister;
    }

    public void setDaoRegister(DaoRegister daoRegister) {
        this.daoRegister = daoRegister;
    }

    public void setMapperInterface(Class<?> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    public IJdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(IJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * beanDefinition中的beanClass只要是这个工厂类的,就会从这里开始调用
     *
     * @return
     * @throws Exception
     */
    @Override
    public T getObject() throws Exception {
        return (T) daoRegister.getDao(mapperInterface, jdbcTemplate,configurableFactory);
    }

    /**
     * 获取工厂中的对象的类型
     *
     * @return
     */
    @Override
    public Class<?> getObjectType() {
        return this.mapperInterface;
    }

    /**
     * 从这个工厂中获取的对象是否是单例的
     *
     * @return
     */
    @Override
    public boolean isSingleton() {
        return true;
    }
}

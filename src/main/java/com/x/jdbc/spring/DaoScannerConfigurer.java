package com.x.jdbc.spring;

import com.x.jdbc.sql.ConfigurableFactory;
import com.x.jdbc.template.IJdbcTemplate;
import com.x.jdbc.template.JdbcTemplateSupport;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import static org.springframework.util.Assert.notNull;

/**
 * dao层扫描包
 * 1. 接口扫描
 * 2. 转化成BeanDefinition对象
 * 3. 设置代理对象
 *
 * @author Liukx
 * @create 2018-01-03 9:54
 * @email liukx@elab-plus.com
 **/
public class DaoScannerConfigurer implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware {
    //具体执行者
    private IJdbcTemplate jdbcTemplate;
    // Spring上下文
    private ApplicationContext applicationContext;
    // 要扫描的包
    private String basePackage;
    //检查父类
    private Class<?> markerInterface;

    private ConfigurableFactory configurableFactory;

    public ConfigurableFactory getConfigurableFactory() {
        return configurableFactory;
    }

    public void setConfigurableFactory(ConfigurableFactory configurableFactory) {
        this.configurableFactory = configurableFactory;
    }

    public Class<?> getMarkerInterface() {
        return markerInterface;
    }

    public void setMarkerInterface(Class<?> markerInterface) {
        this.markerInterface = markerInterface;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public IJdbcTemplate getJdbcTemplate() {
        if (this.jdbcTemplate == null) {
            this.jdbcTemplate = new JdbcTemplateSupport();
        }
        return jdbcTemplate;
    }

    public void setJdbcTemplate(IJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        ClassPathDaoScanner scanner = new ClassPathDaoScanner(registry);
        scanner.setJdbcTemplate(getJdbcTemplate());
        scanner.setMarkerInterface(this.markerInterface);
        scanner.setResourceLoader(this.applicationContext);
        scanner.setConfigurableFactory(this.configurableFactory);
        scanner.registerFilters();
        //注册对应的daoInterface
        scanner.scan(StringUtils.tokenizeToStringArray(this.basePackage, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS));
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        notNull(this.basePackage, "Property 'basePackage' is required");
    }
}

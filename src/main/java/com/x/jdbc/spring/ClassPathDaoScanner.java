package com.x.jdbc.spring;

import com.x.jdbc.template.IJDBCTemplate;
import com.x.jdbc.template.facotry.DaoFactoryBean;
import com.x.jdbc.template.binding.DaoRegister;
import org.apache.ibatis.io.Resources;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.env.Environment;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

/**
 * 具体扫描包下的类的共同属性类构建
 *
 * @author Liukx
 * @create 2018-01-03 10:21
 * @email liukx@elab-plus.com
 **/
public class ClassPathDaoScanner extends ClassPathBeanDefinitionScanner {

    //具体执行者
    private IJDBCTemplate jdbcTemplate;

    // 具体的接口
    private Class<?> markerInterface;

    private DaoRegister daoRegister;


    public DaoRegister getDaoRegister() {
        if (this.daoRegister == null) {
            this.daoRegister = new DaoRegister();
        }
        return daoRegister;
    }

    public void setDaoRegister(DaoRegister daoRegister) {
        this.daoRegister = daoRegister;
    }

    public ClassPathDaoScanner(BeanDefinitionRegistry registry) {
        super(registry);
    }

    public ClassPathDaoScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
        super(registry, useDefaultFilters);
    }

    public ClassPathDaoScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters, Environment environment) {
        super(registry, useDefaultFilters, environment);
    }

    public IJDBCTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    public void setJdbcTemplate(IJDBCTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Class<?> getMarkerInterface() {
        return markerInterface;
    }

    public void setMarkerInterface(Class<?> markerInterface) {
        this.markerInterface = markerInterface;
    }

    /**
     * 扫描指定路径的文件
     * <p>
     * 将扫描到的类解析成对应的BeanDefinition对象,并注册到ioc容器中
     * </p>
     *
     * @param basePackages 扫描地址
     * @return
     */
    @Override
    public Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
        if (beanDefinitions.isEmpty()) {
            logger.warn("No MyBatis mapper was found in '" + Arrays.toString(basePackages) + "' package. Please check your configuration.");
        } else {
            for (BeanDefinitionHolder holder : beanDefinitions) {
                GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();

                if (logger.isDebugEnabled()) {
                    logger.debug("Creating MapperFactoryBean with name '" + holder.getBeanName()
                            + "' and '" + definition.getBeanClassName() + "' mapperInterface");
                }
                Class<?> interfaceClass = null;
                // 拿到当前BeanDefinition对象的接口
                try {
                    interfaceClass = Resources.classForName(definition.getBeanClassName());
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                // 注册到一个容器中
                getDaoRegister().addMapper(interfaceClass);


                // 重新构建BeanDefinition对象的属性
                //mapperInterface 是接口类,实际上引用的是工厂类生产出来的bean
                definition.getPropertyValues().add("mapperInterface", definition.getBeanClassName());
                definition.setBeanClass(DaoFactoryBean.class);
                boolean explicitFactoryUsed = false;
                definition.getPropertyValues().add("jdbcTemplate", jdbcTemplate);
                definition.getPropertyValues().add("daoRegister", getDaoRegister());

                explicitFactoryUsed = true;
                if (!explicitFactoryUsed) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Enabling autowire by type for MapperFactoryBean with name '" + holder.getBeanName() + "'.");
                    }
                    // 表示需要根据类型获取
                    definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
                }
            }
        }

        return beanDefinitions;
    }


    /**
     * Configures parent scanner to search for the right interfaces. It can search
     * for all interfaces or just for those that extends a markerInterface or/and
     * those annotated with the annotationClass
     */
    public void registerFilters() {
        boolean acceptAllInterfaces = true;

        // if specified, use the given annotation and / or marker interface
//        if (this.annotationClass != null) {
//            addIncludeFilter(new AnnotationTypeFilter(this.annotationClass));
//            acceptAllInterfaces = false;
//        }

        // override AssignableTypeFilter to ignore matches on the actual marker interface
        if (this.markerInterface != null) {
            addIncludeFilter(new AssignableTypeFilter(this.markerInterface) {
                @Override
                protected boolean matchClassName(String className) {
                    return false;
                }
            });
            acceptAllInterfaces = false;
        }

        if (acceptAllInterfaces) {
            // default include filter that accepts all classes
            // 默认接受所有的注册类,只要是该类扫描的统统注册
            addIncludeFilter(new TypeFilter() {
                @Override
                public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                    return true;
                }
            });
        }

        // exclude package-info.java
        addExcludeFilter(new TypeFilter() {
            @Override
            public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
                String className = metadataReader.getClassMetadata().getClassName();
                return className.endsWith("package-info");
            }
        });
    }

    /**
     * 这里决定是否能够被注册到ioc的容器中
     *
     * @param beanDefinition
     * @return
     */
    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        return (beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent());
    }
}

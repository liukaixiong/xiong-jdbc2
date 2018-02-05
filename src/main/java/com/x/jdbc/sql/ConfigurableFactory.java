package com.x.jdbc.sql;

import com.x.jdbc.sql.config.SqlConfigurableFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * 配置文件工厂
 *
 * @author Liukx
 * @create 2018-01-11 14:24
 * @email liukx@elab-plus.com
 **/
public class ConfigurableFactory implements InitializingBean {

    // sql配置文件对象
    private SqlConfigurableFactory sqlConfigurableFactory;
    // sql文件存放地址
    private String sqlConfigurableLocations;

    public ConfigurableFactory() {

    }

    public SqlConfigurableFactory getSqlConfigurableFactory() {
        return sqlConfigurableFactory;
    }

    public void setSqlConfigurableFactory(SqlConfigurableFactory sqlConfigurableFactory) {
        this.sqlConfigurableFactory = sqlConfigurableFactory;
    }

    public String getSqlConfigurableLocations() {
        return sqlConfigurableLocations;
    }

    public void setSqlConfigurableLocations(String sqlConfigurableLocations) {
        this.sqlConfigurableLocations = sqlConfigurableLocations;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        buildSqlConfigurableFactory();
    }

    /**
     * 构建sql配置工厂
     */
    public void buildSqlConfigurableFactory() {
        this.sqlConfigurableFactory = new SqlConfigurableFactory(this.sqlConfigurableLocations);
        sqlConfigurableFactory.init();
    }
}

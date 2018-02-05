package com.x.jdbc.model;

/**
 * JDBCTemplate传递实体类
 *
 * @author Liukx
 * @create 2017-03-14 11:24
 * @email liukx@elab-plus.com
 **/
public class JdbcParamsModel {
    // 查询语句
    private String sql;
    // 参数
    private Object[] objects;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Object[] getObjects() {
        return objects;
    }

    public void setObjects(Object[] objects) {
        this.objects = objects;
    }
}

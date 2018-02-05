package com.x.jdbc.model;

import javax.persistence.JoinTable;
import java.beans.PropertyDescriptor;

/**
 * 关联实体类封装
 *
 * @author Liukx
 * @create 2018-02-05 11:51
 * @email liukx@elab-plus.com
 **/
public class JoinPropertiesModel {

    private JoinTable joinTable;
    private String sql;
    private PropertyDescriptor pd;


    public JoinPropertiesModel(PropertyDescriptor pd, JoinTable joinTable, String sql) {
        this.pd = pd;
        this.joinTable = joinTable;
        this.sql = sql;
    }

    public PropertyDescriptor getPd() {
        return pd;
    }

    public void setPd(PropertyDescriptor pd) {
        this.pd = pd;
    }

    public JoinTable getJoinTable() {
        return joinTable;
    }

    public void setJoinTable(JoinTable joinTable) {
        this.joinTable = joinTable;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }
}

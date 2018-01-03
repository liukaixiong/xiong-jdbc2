package com.x.jdbc.model;

import java.util.Set;

/**
 * 数据库对应字段实体
 *
 * @author Liukx
 * @create 2017-03-23 14:50
 * @email liukx@elab-plus.com
 **/
public class ColumnModel {
    //包含列名
    private Set<String> containColumnList;
    //排除列名
    private Set<String> excludeColumnList;

    public Set<String> getContainColumnList() {
        return containColumnList;
    }

    public void setContainColumnList(Set<String> containColumnList) {
        this.containColumnList = containColumnList;
    }

    public Set<String> getExcludeColumnList() {
        return excludeColumnList;
    }

    public void setExcludeColumnList(Set<String> excludeColumnList) {
        this.excludeColumnList = excludeColumnList;
    }
}

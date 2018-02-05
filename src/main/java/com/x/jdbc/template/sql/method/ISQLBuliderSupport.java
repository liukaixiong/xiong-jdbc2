package com.x.jdbc.template.sql.method;

import com.x.jdbc.template.base.EntityOperation;

import java.lang.reflect.Method;

/**
 * sql生成构建类
 *
 * @author Liukx
 * @create 2018-01-04 18:58
 * @email liukx@elab-plus.com
 **/
public interface ISQLBuliderSupport {

    /**
     * 创建sql执行的方法
     *
     * @param entityOperation
     * @param method
     * @return
     */
    String doCreateSQL(EntityOperation entityOperation, Method method);

//    String doCreateQueryRuleSQL(EntityOperation entityOperation, QueryRule arg);
}

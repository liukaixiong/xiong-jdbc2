package com.x.jdbc.template.sql.method;/**
 * Created by liukx on 2018/2/5.
 */

import com.x.jdbc.model.JdbcParamsModel;
import com.x.jdbc.sql.config.SqlCommandType;

/**
 * 检查SQL执行是否规范
 *
 * @author Liukx
 * @create 2018-02-05 17:34
 * @email liukx@elab-plus.com
 **/
public interface CheckSqlProcess {

    /**
     * 检查和规范SQL语句
     *
     *
     * @param update
     * @param model
     * @param o
     * @throws Exception
     */
    void checkProcess(SqlCommandType update, JdbcParamsModel model, Object o) throws Exception;

}

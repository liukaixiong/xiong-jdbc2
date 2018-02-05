package com.x.jdbc.template.sql.method;

import com.x.jdbc.model.JdbcParamsModel;
import com.x.jdbc.sql.config.SqlCommandType;
import org.apache.log4j.Logger;

/**
 * 默认的SQL检验执行
 *
 * @author Liukx
 * @create 2018-02-05 17:37
 * @email liukx@elab-plus.com
 **/
public class DefaultCheckSQLProcess implements CheckSqlProcess {

    private Logger log = Logger.getLogger(DefaultSQLBuilderSupport.class);

    @Override
    public void checkProcess(SqlCommandType sqlCommandType, JdbcParamsModel model, Object o) throws Exception {
        // 不检查添加语句
        if (sqlCommandType == SqlCommandType.INSERT) {
            return;
        }

        String sql = model.getSql();
        if (sql.indexOf("where") < 0) {
            throw new Exception(" 每个SQL执行,where条件后面必须有一个参数以上 ");
        }

    }
}

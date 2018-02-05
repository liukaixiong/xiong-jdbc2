package com.x.jdbc.template.sql.method;

import com.x.jdbc.template.base.EntityOperation;
import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 默认的sql实现
 *
 * @author Liukx
 * @create 2018-01-04 19:05
 * @email liukx@elab-plus.com
 **/
public class DefaultSQLBuilderSupport implements ISQLBuliderSupport {

    private Logger log = Logger.getLogger(DefaultSQLBuilderSupport.class);

    public String insertMethodName = "insert";
    public String updateMethodName = "updateById";
    public String deleteMethodName = "delete";
    public String selectByObjectMethodName = "selectByObject";
    public String selectByListMethodName = "selectByList";
    public String findMethodName = "find";

    /**
     * 创建sql语句流程
     *
     * @param entityOperation
     * @param method
     * @return
     */
    @Override
    public String doCreateSQL(EntityOperation entityOperation, Method method) {
        String name = method.getName();
        String sql = "";
        if (insertMethodName.equals(name)) {
            sql = insertSql(entityOperation);
        } else if (updateMethodName.equals(name)) {
            sql = updateSql(entityOperation);
        } else if (deleteMethodName.equals(name)) {
            sql = deleteSql(entityOperation);
        } else if (selectByObjectMethodName.equals(name) || selectByListMethodName.equals(name)) {
            sql = selectSql(entityOperation);
        } else if (findMethodName.equals(name)) {
//            sql = findSql(entityOperation,method);
        }
        log.debug(" 默认创建的SQL : " + sql);
        return sql;
    }

    /**
     * 创建删除语句流程
     *
     * @param entityOperation
     * @return
     */
    private String deleteSql(EntityOperation entityOperation) {
//        String allColumn = entityOperation.allColumn;
        String tableName = entityOperation.tableName;
        String id = entityOperation.pkField.getName();
        Map<String, String> allProperty = entityOperation.allProperty;
        allProperty.remove(id);
        StringBuffer sb = new StringBuffer();
        sb.append("delete from " + tableName + " where ");
        sb.append(" " + id + " =:" + id + " and ");
        mappingToString(allProperty, sb, "and", " =:");
        return sb.toString();
    }

    /**
     * 修改sql查询
     *
     * @param entityOperation
     * @return
     */
    private String updateSql(EntityOperation entityOperation) {
//        String allColumn = entityOperation.allColumn;
        String tableName = entityOperation.tableName;
        String id = entityOperation.pkField.getName();
        Map<String, String> allProperty = entityOperation.allProperty;
        StringBuffer sb = new StringBuffer();
        sb.append("update " + tableName + " set ");
        allProperty.remove(id);
        mappingToString(allProperty, sb, ",", " =:");
        sb.append(" where " + id + " =:" + id);
        return sb.toString();
    }

    /**
     * 字段名映射
     *
     * @param allProperty      所有属性
     * @param sb               sql语句
     * @param whereSeparator   条件分割符号
     * @param mappingSeparator 属性分隔符
     */
    private void mappingToString(Map<String, String> allProperty, StringBuffer sb, String whereSeparator, String mappingSeparator) {
        // 遍历map集合
        boolean isNotFirst = false;
        // 不同的操作
        for (Map.Entry<String, String> entry : allProperty.entrySet()) {
            String key = entry.getKey();
            if (isNotFirst) {
                sb.append(" " + whereSeparator + " ");
            }
            sb.append(key + " " + mappingSeparator + key);
            isNotFirst = true;
        }
    }

    /**
     * 添加sql语句特殊化处理
     *
     * @param allProperty
     * @param sb
     * @param whereSeparator
     * @param mappingSeparator
     */
    private void mappingToInsertString(Map<String, String> allProperty, StringBuffer sb, String whereSeparator, String mappingSeparator) {
        // 遍历map集合
        boolean isNotFirst = false;
        // 不同的操作
        for (Map.Entry<String, String> entry : allProperty.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (isNotFirst) {
                sb.append(" " + whereSeparator + " ");
            }
            sb.append(" " + mappingSeparator + value);
            isNotFirst = true;
        }
    }

    /**
     * 添加sql生成
     *
     * @param entityOperation
     * @return
     */
    public String insertSql(EntityOperation entityOperation) {
        String allColumn = entityOperation.allColumn;
        String tableName = entityOperation.tableName;
        Map<String, String> allProperty = entityOperation.allProperty;
        StringBuffer sb = new StringBuffer();
        sb.append(" insert into " + tableName + " (" + allColumn + ") values (");
        mappingToInsertString(allProperty, sb, ",", ":");
        sb.append(" )");
        return sb.toString();
    }

    /**
     * 查询sql生成
     *
     * @param entityOperation
     * @return
     */
    public String selectSql(EntityOperation entityOperation) {
        String allColumn = entityOperation.allColumn;
        String tableName = entityOperation.tableName;
        Map<String, String> allProperty = entityOperation.allProperty;
        String id = entityOperation.pkField.getName();
        allProperty.remove(id);
        StringBuffer sb = new StringBuffer();
        // 这里默认会将主键放在第一位
        sb.append(" select " + allColumn + " from " + tableName + " where " + id + " =:" + id + " and ");
        mappingToString(allProperty, sb, " and ", "=:");
        return sb.toString();
    }

}

package com.x.jdbc.template;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;

import java.util.List;
import java.util.Map;

/**
 * 对JDBC的一些拓展操作
 *
 * @author Liukx
 * @create 2018-01-03 10:02
 * @email liukx@elab-plus.com
 **/
public interface IJdbcTemplate extends JdbcOperations {

    /**
     * 执行添加操作
     *
     * @param sqlid 配置文件编号
     * @param o     传递对象
     * @return
     */
    public int executeInsert(String sqlid, Object o);

    /**
     * 执行修改操作
     *
     * @param sqlid
     * @param o
     * @return
     */
    public int executeUpdate(String sqlid, Object o) throws Exception;

    /**
     * 执行查询返回Map对象
     *
     * @param sqlid
     * @param o
     * @return
     */
    public Map<String, Object> executeQueryMap(String sqlid, Object o);

    /**
     * 执行查询返回对应的实体对象
     *
     * @param sqlid       配置文件中的sql编号
     * @param o           属性
     * @param elementType 返回结果类型
     * @param <T>
     * @return
     */
    public <T> T executeQueryObject(String sqlid, Object o, Class<T> elementType);

    /**
     * 执行代理类
     *
     * @param sql
     * @param o
     * @param <T>
     * @return
     */
    public <T> T executeQueryObject(String sql, Object o, RowMapper<T> rm);

    /**
     * 执行查询大数据量语句,并返回对应的集合
     *
     * @param sqlid       配置文件中的sql编号
     * @param o           参数
     * @param elementType 返回结果类型
     * @param <T>
     * @return
     */
    public <T> List<T> executeQueryBigDataList(String sqlid, Object o, Class<T> elementType);

    /**
     * 执行返回结果类型为List<Map>类型的操作
     *
     * @param sqlid 配置文件中的sql编号
     * @param o     参数
     * @return
     */
    public List<Map<String, Object>> executeQueryforListMap(String sqlid, Object o);

    /**
     * 执行查询返回集合的操作
     *
     * @param sqlid       配置文件中的sql编号
     * @param o           参数
     * @param elementType 返回结果类型
     * @param <T>
     * @return
     */
    public <T> List<T> executeQueryList(String sqlid, Object o, Class<T> elementType);

    /**
     * 执行返回大数据量的集合操作
     *
     * @param sql
     * @param args
     * @param elementType
     * @param <T>
     * @return
     * @throws DataAccessException
     */
    public <T> List<T> queryBigDataForList(String sql, Object[] args, Class<T> elementType) throws DataAccessException;

}

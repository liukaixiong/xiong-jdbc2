package JDBCTemplate;

import JDBCTemplate.params.NamedParameterUtils2;
import JDBCTemplate.params.ParsedSql2;
import JDBCTemplate.row.MyRowMapper;
import com.alibaba.druid.pool.DruidDataSource;
import model.JDBCParamsModel;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.Assert;
import sql.config.SqlKit;

import java.sql.*;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * jdbc管理类
 *
 * @author Liukx
 * @create 2017-03-13 19:53
 * @email liukx@elab-plus.com
 **/
public class JDBCTemplateUtils extends JdbcTemplate {

    {
        init();
    }

    void init() {
        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        druidDataSource.setUrl("jdbc:mysql://localhost/xiong?characterEncoding=UTF-8");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("1234");
        setDataSource(druidDataSource);
    }

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        if (this.namedParameterJdbcTemplate == null) {
            this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(this);
        }

        return this.namedParameterJdbcTemplate;
    }

    private volatile int cacheLimit = DEFAULT_CACHE_LIMIT;

    public void setCacheLimit(int cacheLimit) {
        this.cacheLimit = cacheLimit;
    }

    public int getCacheLimit() {
        return this.cacheLimit;
    }

    public static final int DEFAULT_CACHE_LIMIT = 256;

    @SuppressWarnings("serial")
    private final Map<String, ParsedSql2> parsedSqlCache =
            new LinkedHashMap<String, ParsedSql2>(DEFAULT_CACHE_LIMIT, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, ParsedSql2> eldest) {
                    return size() > getCacheLimit();
                }
            };

    /**
     * 将解析后的sql对象存放到缓存当中
     *
     * @param sql
     * @return
     */
    protected ParsedSql2 getParsedSql2(String sql) {
        if (getCacheLimit() <= 0) {
            return NamedParameterUtils2.parseSqlStatement(sql);
        }
        synchronized (this.parsedSqlCache) {
            ParsedSql2 parsedSql = this.parsedSqlCache.get(sql);
            if (parsedSql == null) {
                parsedSql = NamedParameterUtils2.parseSqlStatement(sql);
                this.parsedSqlCache.put(sql, parsedSql);
            }
            return parsedSql;
        }
    }

    /**
     * 查询列表集合
     *
     * @param sql         sql语句
     * @param obj         参数对象
     * @param elementType
     * @param <T>
     * @return
     */
    public <T> List<T> queryForList(String sql, Object[] obj, Class<T> elementType) {
        List<T> ts = this.query(sql, obj, new BeanPropertyRowMapper<T>(elementType));
        return ts;
    }

    /**
     * 查询大数据量的情况下,返回对象集合列表
     *
     * @param sql         sql语句
     * @param args        参数  [Map or Model]
     * @param elementType 返回出参结果类型
     * @param <T>
     * @return
     * @throws DataAccessException
     */
    public <T> List<T> queryBigDataForList(String sql, Object[] args, Class<T> elementType) throws DataAccessException {
        return query(sql, args, new MyRowMapper<T>(elementType));
    }

    private java.sql.Timestamp getCurrentTimeStamp() {
        java.util.Date today = new java.util.Date();
        return new java.sql.Timestamp(today.getTime());
    }

    /**
     * 查询列表对象
     *
     * @param sqlid       sql编号
     * @param o           参数对象
     * @param elementType 返回结果对象
     * @param <T>
     * @return
     */
    public <T> List<T> executeQueryList(String sqlid, Object o, Class<T> elementType) {
        JDBCParamsModel jdbcParamsModel = commonParseSql(sqlid, o);
        List<T> ts = queryForList(jdbcParamsModel.getSql(), jdbcParamsModel.getObjects(), elementType);
        return ts;
    }

    /**
     * 查询一个Map对象的集合
     *
     * @param sqlid
     * @param o
     * @return
     */
    public List<Map<String, Object>> excuteQueryforListMap(String sqlid, Object o) {
        JDBCParamsModel jdbcParamsModel = commonParseSql(sqlid, o);
        List<Map<String, Object>> maps = queryForList(jdbcParamsModel.getSql(), jdbcParamsModel.getObjects());
        return maps;
    }

    /**
     * 查询大量数据返回的情况下
     *
     * @param sqlid
     * @param o
     * @param elementType
     * @param <T>
     * @return
     */
    public <T> List<T> executeQueryBigDataList(String sqlid, Object o, Class<T> elementType) {
        JDBCParamsModel jdbcParamsModel = commonParseSql(sqlid, o);
        List<T> ts = queryBigDataForList(jdbcParamsModel.getSql(), jdbcParamsModel.getObjects(), elementType);
        return ts;
    }

    public <T> T executeQueryObject(String sqlid, Object o, Class<T> elementType) {
        JDBCParamsModel jdbcParamsModel = commonParseSql(sqlid, o);
        RowMapper<T> rm = BeanPropertyRowMapper.newInstance(elementType);
        T ts = queryForObject(jdbcParamsModel.getSql(), jdbcParamsModel.getObjects(), rm);
        return ts;
    }

    public Map<String, Object> executeQueryMap(String sqlid, Object o) {
        JDBCParamsModel jdbcParamsModel = commonParseSql(sqlid, o);
        Map<String, Object> map = queryForMap(jdbcParamsModel.getSql(), jdbcParamsModel.getObjects());
        return map;
    }

    public int executeUpdate(String sqlid, Object o) {
        JDBCParamsModel jdbcParamsModel = commonParseSql(sqlid, o);
        int update = update(jdbcParamsModel.getSql(), jdbcParamsModel.getObjects());
        return update;
    }

    public int executeInsert(String sqlid, Object o) {
        SqlParameterSource sqlParameterSource = getSqlParameterSource(o);
        String sql = SqlKit.sql(sqlid);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        int update = getNamedParameterJdbcTemplate().update(sql, sqlParameterSource,keyHolder);
        int i = keyHolder.getKey().intValue();
        return i;
    }

    /**
     * 参数转换 , 参数类型分别为 Map,Model
     *
     * @param params
     * @return
     */
    private SqlParameterSource getSqlParameterSource(Object params) {
        SqlParameterSource sqlParameterSource = null;
        if (params instanceof Map) {
            sqlParameterSource = new MapSqlParameterSource((Map) params);
        } else {
            sqlParameterSource = new BeanPropertySqlParameterSource(params);
        }
        return sqlParameterSource;
    }

    /**
     * 通用sql处理方式
     *
     * @param sqlid
     * @param params
     * @return
     */
    private JDBCParamsModel commonParseSql(String sqlid, Object params) {
        String sql = SqlKit.sql(sqlid);
        SqlParameterSource sqlParameterSource = getSqlParameterSource(params);
        ParsedSql2 parsedSql = getParsedSql2(sql);
        // 获取有效的参数
        Object[] data = NamedParameterUtils2.buildValueArray(parsedSql, sqlParameterSource, null);
        String s = NamedParameterUtils2.substituteNamedParameters(parsedSql, sqlParameterSource);
        String s1 = NamedParameterUtils2.validCheckSql(sql, s, parsedSql, sqlParameterSource);
        logger.debug(" 转换后的sql - " + s1);
        logger.debug(" 对应参数    -  " + Arrays.toString(data));
        JDBCParamsModel model = new JDBCParamsModel();
        model.setSql(s1);
        model.setObjects(data);
        return model;
    }


    public Long insertAndGetKey(final String sql, final Object[] object) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        update(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                for (int i = 0; i < object.length; i++) {
                    int index = i + 1;
                    ps.setObject(index, object[i]);
                }
                return ps;
            }
        }, keyHolder);
        Long generatedId = keyHolder.getKey().longValue();
        return generatedId;
    }

    public <T> T query(PreparedStatementCreator psc, final PreparedStatementSetter pss, final ResultSetExtractor<T> rse)
            throws DataAccessException {
        Assert.notNull(rse, "ResultSetExtractor must not be null");
        logger.debug("Executing prepared SQL query");
        return execute(psc, new PreparedStatementCallback<T>() {
            public T doInPreparedStatement(PreparedStatement ps) throws SQLException {
                ResultSet rs = null;
                try {
                    if (pss != null) {
                        pss.setValues(ps);
                    }
                    ps.setFetchSize(Integer.MIN_VALUE);
                    Long s = System.currentTimeMillis();
                    rs = ps.executeQuery();
                    logger.debug(" [[[[[[[[[[[[ 查询耗时 : " + (System.currentTimeMillis() - s) + " ]]]]]]]]");
                    ResultSet rsToUse = rs;
                    Long start = System.currentTimeMillis();
                    T t = rse.extractData(rsToUse);
                    Long end = System.currentTimeMillis();
                    Long time = end - start;
                    logger.debug(" [[[[[[[[[[[[  反射耗时: " + time + " ]]]]]]]]]]]]] ");
                    return t;
                } finally {
                    JdbcUtils.closeResultSet(rs);
                    if (pss instanceof ParameterDisposer) {
                        ((ParameterDisposer) pss).cleanupParameters();
                    }
                }
            }
        });
    }

    public static void main(String[] args) throws Exception {
//        JDBCTemplateUtils jdbcTemplate = new JDBCTemplateUtils();
//        // 1. todo 查询列表
//        TTest test = new TTest();
//        test.setLove_name("嘿嘿");
//        test.setUsername("xiong");
//        test.setCreated(new Date());
//        test.setTime(new Date());
////        jdbcTemplate.executeQueryUpdate("test.updateByPrimaryKey",test);
//        int i = jdbcTemplate.executeInsert("test.insert", test);
//        System.out.println(" ----> " + i);
//        System.out.println("---添加结果 : " + i);
//        TTest tTests = jdbcTemplate.executeQueryObject("test.selectByExample", map, TTest.class);
//        System.out.println(" --" + JSON.toJSONString(tTests));
//        List<TTest> tTests = executeQueryList("test.selectByExample", map, TTest.class);
//        System.out.println(tTests.size());
//        String sql = SqlKit.sql("TDd.selectByPrimaryKey");
//        System.out.println("---->查询出来的sql - "+sql);
//        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
//        map.put("id", "2");
//        map.put("aaa", "bbb");
//        JDBCParamsModel jdbcTemplateModel = SqlOperator.queryReplace2("TDd.selectByPrimaryKey", map);
//        System.out.println("查询的sql -> " + jdbcTemplateModel.getSql());
//        System.out.println(" 参数 - >" + JSON.toJSONString(jdbcTemplateModel.getObjects()));
//        List<TDd> tDdDTOs = JDBCTemplateUtils.queryForList(jdbcTemplateModel.getSql(), jdbcTemplateModel.getObjects(), TDd.class);
//        System.out.println(" 查询结果 -> " + JSON.toJSONString(tDdDTOs));
        // todo 查询对象
//        String sql = "select \n" +
//                "    id,houseid,tel,name,head_portrait,score,title,radar_img,introduce_path,introduce_img,evaluate,life_motto,status,is_job,creator,created,updator,updated\n" +
//                "     from t_dd\n" +
//                "     where id =?";
//        Object[] objects = new Object[1];
//        objects[0] = "1";
//        Map<String, Object> map = jdbcTemplate.queryForMap(sql, objects);
//        System.out.println("map -> " + JSON.toJSONString(map));
//        RowMapper<TDd> rm = BeanPropertyRowMapper.newInstance(TDd.class);
//        TDd tDdDTO = jdbcTemplate.queryForObject(sql, objects, rm);
//        System.out.println("obj -> " + JSON.toJSONString(tDdDTO));
        // todo 添加对象
//        TTest t = new TTest();
//        t.setName("某某某");
//        t.setUsername("123");
//        t.setSex("男");
//        t.setCreated(new Date());
//        LinkedHashMap<String, Object> param = new LinkedHashMap<String, Object>();
//        param.put("time", new Date());
//        param.put("name", "某某某");
//        param.put("username", "liukx");
//        param.put("sex", "男");
//        param.put("created", new Date());
//        param.put("aaa", "男");
//        param.put("bbb", "男");
//        JDBCParamsModel jdbcTemplateModel = SqlOperator.executeInsert2("test.insert", param);
//        Long aLong = insertAndGetKey(jdbcTemplateModel.getSql(), jdbcTemplateModel.getObjects());
        // todo 修改对象
//        String [] ids = new String[2];
//        ids[0]="1";
//        ids[1]="2";
//        List<Map> list = new ArrayList<Map>();
//        for (int i = 0; i < 10; i++) {
//            LinkedHashMap<String, Object> param = new LinkedHashMap<String, Object>();
//            param.put("time", "");
//            param.put("name", "insert");
////            param.put("id", ids);
//            param.put("username", "liukx222");
//            param.put("sex", "2");
//            param.put("created", null);
//            param.put("aaa", "男1");
//            param.put("status2", "1");
//            list.add(param);
//        }
//        Object[] params = NamedParameterUtils2.buildValueArray(parsedSql, mapSqlParameterSource, null);
//
//        String s = NamedParameterUtils2.substituteNamedParameters(parsedSql, mapSqlParameterSource);
//        String s1 = NamedParameterUtils2.validCheckSql(sql, s, parsedSql, mapSqlParameterSource);

//        JDBCParamsModel jdbcTemplateModel = SqlOperator.executeUpdate3("test.selectByExample", param);
//        List<TTest> tTests = queryForList(jdbcTemplateModel.getSql(), jdbcTemplateModel.getObjects(), TTest.class);
//        System.out.println(tTests.size());
//        int update = jdbcTemplate.update(jdbcTemplateModel.getSql(), jdbcTemplateModel.getObjects());
//        insertAndGetKey(jdbcTemplateModel.getSql(),jdbcTemplateModel.getObjects());
        // String e = SqlOperator.executeUpdate2("test.updateByPrimaryKey", param);
        //selectByExample
//        String sql = SqlKit.sql("test.selectByExample");
//        ParsedSql2 parsedSql = NamedParameterUtils2.parseSqlStatement(sql);
//        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource(param);
//        // 获取有效的参数
//        Object[] params = NamedParameterUtils2.buildValueArray(parsedSql, mapSqlParameterSource, null);
//
//        String s = NamedParameterUtils2.substituteNamedParameters(parsedSql, mapSqlParameterSource);
//        String s1 = NamedParameterUtils2.validCheckSql(sql, s, parsedSql, mapSqlParameterSource);
//
//        System.out.println("原始sql : " + parsedSql);
//        System.out.println(" 改变后的sql : " + s1);
//        System.out.println(" 对应参数    : " + Arrays.toString(params));
//        List<TTest> tTests = queryForList(s, params, TTest.class);
//        System.out.println("JSON");
        // int update = jdbcTemplate.update(s, params);
        //  System.out.println("--->update - " + update);

    }
}
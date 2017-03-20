package sql;


import JDBCTemplate.params.NamedParameterUtils2;
import JDBCTemplate.params.ParsedSql2;
import model.JDBCParamsModel;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import sql.config.SqlKit;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author liuhx on 2016/12/14 10:39
 * @version V1.0
 * @email liuhx@elab-plus.com
 */
public class SqlOperator {


    private static Map<String, SqlQuery> updateSqlUpdate = new HashMap<String, SqlQuery>();

    public static String processNull(String sqlString, LinkedHashMap<String, Object> params) {
        String sql = sqlString;
        int num = 0;
        long startTime = new Date().getTime();
        for (Map.Entry entry : params.entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey().toString();
            if (value == null || value.equals("%null%")) {
                if (key.matches("[0-9]{1}[a-z]+")) {
                    sql = sql.replaceFirst("(\\w*\\.?" + key.substring(1) + "\\b\\s*\\S+\\s*:" + key + "\\s*\\)?)(\\s*and\\s*|\\s*or\\b\\s*|\\s*)", "  ");
                } else {
                    sql = sql.replaceFirst("(\\w*\\.?" + key + "\\b\\s*\\S+\\s*:" + key + "\\s*\\)?)(\\s*and\\s*|\\s*or\\b\\s*|\\s*)", "  ");
                    String strKey = key.substring(0, key.length() - 1);
                    sql = sql.replaceFirst("(\\w*\\.?" + strKey + "\\b\\s*\\S+\\s*:" + key + "\\s*\\)?)(\\s*and\\s*|\\s*or\\b\\s*|\\s*)", "  ");
                    sql = sql.replaceFirst("(\\w*\\.?" + strKey + "\\b\\s*\\S+\\s*:" + key + "\\s*\\)?)(\\s*and\\s*|\\s*or\\b\\s*|\\s*)", "  ");
                    sql = sql.replaceFirst("(\\w*\\.?" + key + "\\b\\s*\\S+\\s*\\(:" + key + "\\)\\s*\\)?)(\\s*and\\s*|\\s*or\\b\\s*|\\s*)", "  ");
                }
                sql = sql.replaceAll("(and\\s*order\\s*by\\s*|or\\s*order\\s*by\\s*)", " order by ");
                sql = sql.replaceAll("(and\\s*group\\s*by\\s*|or\\s*group\\s*by\\s*)", " group by ");
                sql = sql.replaceAll("(and\\s*$|or\\s*$)", "  ");
                num++;
            }

            if (num == params.size()) {
                sql = sql.replaceFirst(" where ", " ");
            }

        }
        sql = sql.replaceAll("\\s(and|or)\\s$", " ");
        sql = sql.replaceAll("(and|or)\\s*where", " where ");
        long endTime = new Date().getTime();
        return sql;
    }


    private static String processNullForUpdate(String sqlString, LinkedHashMap<String, Object> params) {
        String sql = sqlString;
        String startSQL = sql.substring(0, sql.indexOf("set")).concat("set ");
        String setSQL = sql.substring(sql.lastIndexOf("set") + 3, sql.indexOf("where"));
        String whereSQL = sql.substring(sql.lastIndexOf("where") + 5, sql.length());
        System.out.println("原始update-SQL:" + sql);
        for (Map.Entry entry : params.entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey().toString();

            if (value == null) {
                setSQL = setSQL.replaceAll("(\\s*\\w*\\.?" + key + "\\s*\\S+\\s*:" + key + "\\b\\s*\\)?)(\\s*and\\s*|\\s*or\\s*|\\s*)",
                        " " + key + "=null ");
                setSQL = setSQL.replaceAll("(and\\s*$|or\\s*$)", "  ");
                whereSQL = whereSQL.replaceAll("(,?\\s*\\w*\\.?" + key + "\\s*\\S+\\s*:" + key + "\\b\\s*\\)?)(\\s*and\\s*|\\s*or\\s*|\\s*)", "  ");
                whereSQL = whereSQL.replaceAll("(and\\s*$|or\\s*$)", "  ");
            } else if (value != null && value.getClass().isArray()) {
                Object[] arr = (Object[]) value;
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i] == null) {
                        setSQL = setSQL.replaceAll("(,?\\s*\\w*\\.?" + key + "\\s*\\S+\\s*:" + key + "\\b\\s*\\)?)(\\s*and\\s*|\\s*or\\s*|\\s*)",
                                " " + key + "=null, ");
                        whereSQL = whereSQL.replaceAll("(,?\\s*\\w*\\.?" + key + "\\s*\\S+\\s*:" + key + "\\b\\s*\\)?)(\\s*and\\s*|\\s*or\\s*|\\s*)", "  ");
                        whereSQL = whereSQL.replaceAll("(and\\s*$|or\\s*$)", "  ");
                    }
                }
            }
        }
        String resultSQL = startSQL.concat(setSQL).concat(" where ").concat(whereSQL);
        resultSQL = resultSQL.replaceAll("(and\\s*$|or\\s*$)", " ");
        resultSQL = resultSQL.replaceAll(",\\s*where\\s*", " where ");
        System.out.println("过滤update-SQL：" + resultSQL);
        return resultSQL;

    }

    /**
     * 将参数中为null的语句中进行替换
     *
     * @param sqlString
     * @param params
     * @return
     */
    public static String processNullForUpdate2(String sqlString, LinkedHashMap<String, Object> params) {
        String sql = sqlString;

        String startSQL = sql.substring(0, sql.indexOf("set")).concat("set ");
        String setSQL = sql.substring(sql.lastIndexOf("set") + 3, sql.indexOf("where"));
        String replaceSetSql = replaceUpdateSqlColumnNull(setSQL, params);
        String whereSQL = sql.substring(sql.lastIndexOf("where") + 5, sql.length());
        String replaceWhereSql = replaceUpdateSqlWhereNull(whereSQL, params);

        String resultSQL = startSQL.concat(replaceSetSql).concat(" where ").concat(replaceWhereSql);
        resultSQL = resultSQL.replaceAll("(and\\s*$|\\s or\\s*$)", " ");
        resultSQL = resultSQL.replaceAll(",\\s*where\\s*", " where ");
        return resultSQL;
    }

    private static String replaceUpdateSqlColumnNull(String sql, LinkedHashMap<String, Object> params) {
        List<String> paramsColumn = findParamsColumn(sql, ":", ",");
        for (int index = 0; index < paramsColumn.size(); index++) {
            String key = paramsColumn.get(index);
            Object value = params.get(key);
            if (value == null) {
                sql = sql.replaceAll("(\\s*\\w*\\.?" + key + "\\s*\\S+\\s*:" + key + "\\b\\s*\\)?,|\\s*\\w*\\.?" + key + "\\s*\\S+\\s*:" + key + "\\b\\s*\\)?)",
                        "  ");
                sql = sql.replaceAll("(and\\s*$|\\sor\\s*$)", "  ");
            } else if (value != null && value.getClass().isArray()) {
                Object[] arr = (Object[]) value;
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i] == null) {
                        sql = sql.replaceAll("(,?\\s*\\w*\\.?" + key + "\\s*\\S+\\s*:" + key + "\\b\\s*\\)?)(\\s*and\\s*|\\s*or\\s*|\\s*)",
                                " " + key + "=null, ");
                    }
                }
            }
        }
        return sql;
    }

    private static String replaceUpdateSqlWhereNull(String sql, LinkedHashMap<String, Object> params) {
        List<String> paramsColumn = findParamsColumn(sql, ":", ",");
        for (int index = 0; index < paramsColumn.size(); index++) {
            String key = paramsColumn.get(index);
            Object value = params.get(key);
            if (value == null) {
                sql = sql.replaceAll("(\\s*and\\s*|\\s*or\\s*|\\s*)(,?\\s*\\w*\\.?" + key + "\\s*\\S+\\s*:" + key + "\\b\\s*\\)?|,?\\s*\\w*\\.?" + key + "\\s*\\S+\\s*:" + key + "\\d\\b\\s*\\)?)", "  ");
                sql = sql.replaceAll("(and\\s*$|\\sor\\s*$)", "  ");
            } else if (value != null && value.getClass().isArray()) {
                Object[] arr = (Object[]) value;
                for (int i = 0; i < arr.length; i++) {
                    if (arr[i] == null) {
                        sql = sql.replaceAll("(,?\\s*\\w*\\.?" + key + "\\s*\\S+\\s*:" + key + "\\b\\s*\\)?)(\\s*and\\s*|\\s*or\\s*|\\s*)", "  ");
                        sql = sql.replaceAll("(and\\s*$|or\\s*$)", "  ");
                    }
                }
            }
        }
        return sql;
    }

    /**
     * 找出指定范围内的所有数值
     *
     * @param sql     文本值
     * @param startNo 开始符号
     * @param endNo   结束符号
     * @return
     */
    public static List<String> findParamsColumn(String sql, String startNo, String endNo) {
        List<String> ls = new ArrayList<String>();
        Pattern pattern = Pattern.compile("(?<=\\" + startNo + ")(.+?)(?=\\" + endNo + "|\\n|\\s|\\b)");
        Matcher matcher = pattern.matcher(sql);
        while (matcher.find()) {
            ls.add(matcher.group());
            System.out.println("--->" + matcher.group());
        }
        return ls;
    }


    private static String processNullForDelete(String sqlString, LinkedHashMap<String, Object> params) {
        // delete 不支持参数替换，传N个参数就代表SQL文件中肯定会有N个需要替换
        return sqlString;

    }

    private static String processNullForInsert(String sqlString, LinkedHashMap<String, Object> params) {


        String sql = sqlString;

        String columnString = sql.substring(sql.indexOf("(") + 1, sql.indexOf(")"));
        String[] sqlColumns = columnString.split(",");

        for (String column : sqlColumns) {
            if (!params.containsKey(column.trim())) {
//                sql = sql.replaceFirst(":" + column.trim() + "\\b", " null ");
                sql = sql.replaceFirst(":" + column.trim() + "\\b", " null ");
            }
        }


        return sql;
    }

    public static String parseSQLForIn2(String sql, LinkedHashMap<String, Object> params) {
        List<String> paramsColumn = findParamsColumn(sql, ":", ",");
        for (int index = 0; index < paramsColumn.size(); index++) {
            String key = paramsColumn.get(index);
            Object value = params.get(key);

            if (value == null) continue;
            if (value instanceof List) {

                StringBuilder stringBuilder = new StringBuilder();

                List invalues = (List) value;
                for (int i = 0; i < invalues.size(); i++) {
                    stringBuilder.append("?");
                    stringBuilder.append(",");
                }

                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                sql = sql.replaceAll("\\s*:\\s*" + key.trim() + "\\b", stringBuilder.toString());
            } else if (value.getClass().isArray()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("?");
                Object[] arr = (Object[]) value;

                for (int i = 0; i < arr.length; i++) {
                    if (arr[i] == null) {
                        continue;
                    } else {
                        sql = sql.replaceAll("\\s*:\\s*" + key.trim() + i + "\\b", stringBuilder.toString());
                    }
                }
            } else {
                StringBuilder stringBuilder = new StringBuilder();

                stringBuilder.append("?");

                sql = sql.replaceAll("\\s*:\\s*" + key.trim() + "\\b", stringBuilder.toString());
            }

        }

        return sql;
    }

    private static String parseSQLForIn(String sql, LinkedHashMap<String, Object> params) {


        StringBuilder sqlBuffer = new StringBuilder();
        for (String key : params.keySet()) {

            Object value = params.get(key);


            if (value == null) continue;
            if (value instanceof List) {

                StringBuilder stringBuilder = new StringBuilder();

                List invalues = (List) value;
                for (int i = 0; i < invalues.size(); i++) {
                    stringBuilder.append("?");
                    stringBuilder.append(",");
                }

                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                sql = sql.replaceAll("\\s*:\\s*" + key.trim() + "\\b", stringBuilder.toString());
            } else if (value.getClass().isArray()) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("?");
                Object[] arr = (Object[]) value;

                for (int i = 0; i < arr.length; i++) {
                    if (arr[i] == null) {
                        continue;
                    } else {
                        sql = sql.replaceAll("\\s*:\\s*" + key.trim() + i + "\\b", stringBuilder.toString());
                    }
                }
            } else {
                StringBuilder stringBuilder = new StringBuilder();

                stringBuilder.append("?");

                sql = sql.replaceAll("\\s*:\\s*" + key.trim() + "\\b", stringBuilder.toString());
            }

        }

        return sql;
    }

    public static String queryReplace(String sqlid, LinkedHashMap<String, Object> params) throws Exception {
        String sqlFromCache = SqlKit.sql(sqlid);
        System.out.println("原始query-SQL:" + sqlFromCache);
        String sql = processNull(sqlFromCache, params);
        String parseSQL = parseSQLForIn(sql, params);
        System.out.println("过滤query-SQL:" + parseSQL);
        return parseSQL;
    }

    public static JDBCParamsModel queryReplace2(String sqlid, LinkedHashMap<String, Object> params) throws Exception {
        String sqlFromCache = SqlKit.sql(sqlid);
        System.out.println("原始query-SQL:" + sqlFromCache);
        String sql = processNull(sqlFromCache, params);
        String parseSQL = parseSQLForIn(sql, params);
        System.out.println("过滤query-SQL:" + parseSQL);
        List<String> paramsColumn = findParamsColumn(sql, ":", " ");
        Object[] objects = new Object[paramsColumn.size()];
        for (int i = 0; i < paramsColumn.size(); i++) {
            String s = paramsColumn.get(i);
            Object o = params.get(s);
            objects[i] = o.toString();
        }
        JDBCParamsModel model = new JDBCParamsModel();
        model.setSql(parseSQL);
        model.setObjects(objects);
        return model;
    }


    public static JDBCParamsModel executeInsert2(String sqlid, LinkedHashMap<String, Object> params) throws Exception {
        JDBCParamsModel jdbcParamsModel = new JDBCParamsModel();
        String sqlFromCache = SqlKit.sql(sqlid);
        String e = processNullForInsert(sqlFromCache, params);
        List<String> paramsColumn = findParamsColumn(e, ":", ",");
        Object[] sqlByParams = getSqlByParams(paramsColumn, params);
        String parseSQL = parseSQLForIn(e, params);
        parseSQL = parseSQL.replaceAll(":[a-z|A-Z|_|0-9]+", " null ");
        System.out.println("insert=" + parseSQL);
        jdbcParamsModel.setSql(parseSQL);
        jdbcParamsModel.setObjects(sqlByParams);
        return jdbcParamsModel;
    }

    private static Object[] getSqlByParams(List<String> list, LinkedHashMap linkedHashMap) {
        Object[] objArray = new Object[list.size()];
        for (int i = 0; i < list.size(); i++) {
            String s = list.get(i);
            Object o = linkedHashMap.get(s);
            objArray[i] = o;
        }
        return objArray;
    }

    public static String executeInsert(String sqlid, LinkedHashMap<String, Object> params) throws Exception {
        String sqlFromCache = SqlKit.sql(sqlid);
        String e = processNullForInsert(sqlFromCache, params);
        String parseSQL = parseSQLForIn(e, params);
        parseSQL = parseSQL.replaceAll(":[a-z|A-Z|_|0-9]+", " null ");
        System.out.println("insert=" + parseSQL);
        return parseSQL;
    }

    public static String executeUpdate(String sqlid, LinkedHashMap<String, Object> params) throws Exception {
        String sqlFromCache = SqlKit.sql(sqlid);
        String e = processNullForUpdate(sqlFromCache, params);
        String parseSQL = parseSQLForIn(e, params);
        return parseSQL;
    }

    public static String executeUpdate2(String sqlid, LinkedHashMap<String, Object> params) throws Exception {
        String sqlFromCache = SqlKit.sql(sqlid);
        System.out.println("原始sql - " + sqlFromCache);
        String e = processNullForUpdate2(sqlFromCache, params);
        System.out.println("清空null值后的sql----------------->" + e);
        String parseSQL = parseSQLForIn2(e, params);
        System.out.println("转化之后的sql----------------->" + parseSQL);
        return parseSQL;
    }

    public static JDBCParamsModel executeUpdate3(String sqlid, LinkedHashMap<String, Object> params) throws Exception {
        String sqlFromCache = SqlKit.sql(sqlid);
        ParsedSql2 parsedSql = NamedParameterUtils2.parseSqlStatement(sqlFromCache);
        MapSqlParameterSource mapSqlParameterSource = new MapSqlParameterSource(params);
        // 获取有效的参数
        Object[] data = NamedParameterUtils2.buildValueArray(parsedSql, mapSqlParameterSource, null);
        String s = NamedParameterUtils2.substituteNamedParameters(parsedSql, mapSqlParameterSource);
        String s1 = NamedParameterUtils2.validCheckSql(sqlFromCache, s, parsedSql, mapSqlParameterSource);
        System.out.println(" 转换后的sql - " + s1);
        System.out.println(" 对应参数    -  " + Arrays.toString(data));
        JDBCParamsModel model = new JDBCParamsModel();
        model.setSql(s1);
        model.setObjects(data);
        return model;
    }


    public static String executeDelete(String sqlid, LinkedHashMap<String, Object> params) throws Exception {
        String sqlFromCache = SqlKit.sql(sqlid);
        String e = processNullForDelete(sqlFromCache, params);
        String parseSQL = parseSQLForIn(e, params);
        System.out.println("delete=" + parseSQL);
        return parseSQL;
    }


    public static String findListForPaging(String sqlid, LinkedHashMap<String, Object> params) throws Exception {
        String sqlFromCache = SqlKit.sql(sqlid);
        System.out.println("原始pageQuery-SQL:" + sqlFromCache);
        String sql = processNull(sqlFromCache, params);
        String parseSQL = parseSQLForIn(sql, params);
//        parseSQL.concat(" limit " + (page.getCount() - 1) * page.getPageSize()).concat(", ").concat(page.getPageSize()+"");
        System.out.println("过滤pageQuery-SQL:" + parseSQL);
        return parseSQL;
    }

    public static void insert() {
        LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
        //houseid,:younger,:children,:older,:rooms,:area,:plan_code,:remark,
        params.put("houseid1", null);
        params.put("younger", "younger");
        params.put("children", null);
        params.put("older", null);
        params.put("rooms", null);
        params.put("area", null);
        params.put("plan_code", null);
        params.put("remark", null);
        try {
            String insert = SqlOperator.executeInsert("hello.insert", params);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void delete() {
        LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("id", null);
        params.put("status", "2");
        params.put("name", null);
        String delete = null;
        try {
            delete = SqlOperator.executeDelete("hello.delete", params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(delete);
    }


    public static void update() {
        LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("status", null);
        params.put("name", 123);
        params.put("id", "2");
        params.put("plan_code", null);
        params.put("customer_id", null);
        String update = null;
        try {
            update = SqlOperator.executeUpdate("hello.update", params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("替换后：" + update);
    }

    public static String query(String sqlname, LinkedHashMap<String, Object> params) {
        String querySql = null;
        try {
            querySql = SqlOperator.queryReplace(sqlname, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return querySql;
    }


    public static void main(String[] args) {
//        insert();
//        delete();
//        update();
        /*LinkedHashMap<String, Object> params1 = new LinkedHashMap<String, Object>();
        params1.put("name", null);
        params1.put("buildingnum", null);
        params1.put("frame_code", null);
        params1.put("template_code", null);
        params1.put("layoutplancode", null);
        params1.put("status", null);
        params1.put("isupdate", null);
        String strQuery = query("hello.template",params1);
        System.out.println(strQuery);*/
        /*String sql = "update table set a=null,b=34,c=333 where d=9999";
        String startSQL = sql.substring(0, sql.indexOf("set")).concat("set ");
        System.out.println(startSQL);
        String setSQL = sql.substring(sql.lastIndexOf("set")+3, sql.indexOf("where"));
        String whereSQL = sql.substring(sql.lastIndexOf("where")+5, sql.length());
        System.out.println(setSQL);
        System.out.println(whereSQL);*/
       /* String str = "select s.id subsubjectid,s.poiid,p.title,p.remark,s.plancode,c.plan_name,c.layoutplan_id,l.are,u.buildingnum," +
                "        l.sleeveare,l.poolare,l.multifunctional,l.attr1,l.attr2,c.attr3,l.house_unitid,l.turned,l.bedroom," +
                "        concat(convert(number_convert(l.bedroom) using utf8mb4),'室',convert(number_convert(l.sittingroom) using utf8mb4),'厅'," +
                "        convert(number_convert(l.bathroom) using utf8mb4),'卫') as layout_alias" +
                "        from t_subject_poi s" +
                "        left join t_poi p on p.id = s.poiid" +
                "        left join c_plancode c on c.plan_code = s.plancode" +
                "        left join c_layout_plan l on l.id = c.layoutplan_id" +
                "        left join house_unit u on l.house_unitid=u.id" +
                "        where s.status = 1 and l.status = 1 and c.status = 1 and u.status = 1" +
                "        and s.subsubjectid in(select id subsubjectid from t_child_subject where subjectid = :subjectid)" +
                "        and s.plancode is not null and s.plancode != '' and s.plancode like :plancode order by s.sortno desc";
//        str = str.replaceFirst("(\\w*\\.?subjectid\\b\\s*\\S+\\s*:subjectid\\s*\\)?)(\\s*and\\s*|\\s*or\\b\\s*|\\s*)", "  ");

        str = str.replaceFirst("(\\w*\\.?subjectid\\b\\s*\\S+\\s*:subjectid\\s*)(\\s*and\\s*|\\s*or\\b\\s*|\\s*)", " 1=1 ");

        str = str.replaceFirst("in\\s\\(\\W.*\\)", "  ");
        System.out.println(str);*/


        String update = "update t_poi set title = :title, ftitle = :ftitle, tag = :tag, remark = :remark, backpicurl = :backpicurl, icon = :icon, h5url = :h5url,\n" +
                "        shortname = :shortname, type = :type, scenepicurl = :scenepicurl, status = :status, updated = sysdate(), updator = :updator where id = :poiid or id2 = :id2 and creator = :creator";
        LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
        params.put("title", null);
        params.put("ftitle", null);
        params.put("tag", "");
        params.put("remark", "");
        params.put("backpicurl", "");
        params.put("icon", "");
        params.put("h5url", "");
        params.put("shortname", "");
        params.put("type", "");
        params.put("scenepicurl", "");
        params.put("status", "");
        params.put("updator", "");
        params.put("poiid", "");
        params.put("id2", null);
        params.put("creator", "111");
        params.put("aaa", "bbbb");

        String e = processNullForUpdate2(update, params);
//        System.out.println("update:    " + e + "\n\n\n\n");
        String parseSQL = parseSQLForIn2(e, params);
        System.out.println(" 最终JDBC执行的sql : \n" + parseSQL);
//        String sql = "    houseid = :houseid,type = :type,title = :title,remark = :remark,status = :status,fixed_imageurl = :fixed_imageurl,video_url = :video_url,sort = :sort,fixed_starttime = :fixed_starttime,fixed_endtime = :fixed_endtime,fixed_video_link = :fixed_video_link,able = :able,created = :created,creator = :creator,updated = :updated,updator = :updator\n";
        findParamsColumn(update, ":", ",");
//        String sql = "select * from tal where a=sdf and parent_pageid=:345parent_pageid345 and a_123b=:a_123b123543 and click_sortno=:click_sortno";
//        String tempSql = sql.replaceAll(":[a-z|A-Z|_|0-9]+", " null ");
//        System.out.println("tempSql>"+tempSql);
//        System.out.println(sql);
    }
}

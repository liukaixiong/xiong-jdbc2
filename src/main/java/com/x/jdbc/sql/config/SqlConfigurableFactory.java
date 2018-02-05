package com.x.jdbc.sql.config;

import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author liuhx on 2016/12/14 09:33
 * @version V1.0
 * @email liuhx@elab-plus.com
 */
public class SqlConfigurableFactory {

    private Logger log = Logger.getLogger(SqlConfigurableFactory.class);

    private Map<String, SqlItem> sqlMap = new ConcurrentHashMap<String, SqlItem>();
    private String sqlConfigLocations;

    public SqlConfigurableFactory(String sqlConfigurableLocations) {
        this.sqlConfigLocations = sqlConfigurableLocations;
    }

    /**
     * 获取sql的值
     *
     * @param sqlId sql的编号
     * @return
     */
    public String getSql(String sqlId) {
        if (sqlMap == null) {
            throw new NullPointerException("SqlInXmlPlugin not start");
        } else {
            return sqlMap.get(sqlId).value;
        }
    }

    /**
     * 判断sqlid是否存在
     *
     * @param sqlId
     * @return
     */
    public boolean existSqlId(String sqlId) {
        if (sqlMap == null) {
            throw new NullPointerException("SqlInXmlPlugin not start");
        } else {
            return sqlMap.get(sqlId) != null;
        }
    }

    /**
     * 获取这个sql的类型
     *
     * @param groupNameAndsqlId sql的编号
     * @return
     */
    public SqlCommandType getSqlType(String groupNameAndsqlId) {
        if (sqlMap == null) {
            throw new NullPointerException("SqlInXmlPlugin not start");
        } else {
            SqlItem sqlItem = sqlMap.get(groupNameAndsqlId);
            if (sqlItem == null) {
                return null;
            }
            return sqlItem.sqlCommandType;
        }
    }

    public void clearSqlMap() {
        sqlMap.clear();
    }

    /**
     * 获取列表下面的文件
     *
     * @param list 文件集合
     * @param file 文件夹路径
     */
    public void getListFile(List<File> list, File file) {
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file1 = files[i];
            if (file1.isDirectory()) {
                getListFile(list, file1);
            } else {
                list.add(file1);
            }
        }
    }

    /**
     * 初始化加载sql.xml后缀的配置文件
     */
    public void init() {
        sqlMap = new HashMap();
        File file = new File(SqlConfigurableFactory.class.getClassLoader().getResource("").getPath() + sqlConfigLocations + "/");
        List<File> list = new ArrayList<File>();
        getListFile(list, file);
        File[] files = list.toArray(new File[list.size()]);
        File[] var2 = files;
        int var3 = files.length;

        for (int var4 = 0; var4 < var3; ++var4) {
            File xmlfile = var2[var4];
            SqlGroup group = JaxbKit.unmarshal(xmlfile, SqlGroup.class);
            String name = group.name;
            if (name == null || name.trim().equals("")) {
                name = xmlfile.getName();
            }

            Iterator var8 = group.sqlItems.iterator();

            while (var8.hasNext()) {
                SqlItem sqlItem = (SqlItem) var8.next();
                String value = sqlItem.value;
                //判断该sql的类型
                SqlCommandType sqlCommandType = getSqlCommandType(value);
                sqlItem.sqlCommandType = sqlCommandType;
                sqlMap.put(name + "." + sqlItem.id, sqlItem);
            }
            log.debug("SQL 配置文件 [" + xmlfile.getName() + "] 加载完毕");
        }
    }

    public void registerSqlMap(String sqlId, String sql) {
        if (existSqlId(sqlId)) {
            System.out.println("===========sqlId存在重复创建 id : " + sqlId + " sql内容:" + sql);
            return;
        }
        SqlItem sqlItem = new SqlItem();
        sqlItem.id = sqlId;
        sqlItem.value = sql;
        sqlItem.sqlCommandType = getSqlCommandType(sql);
        sqlMap.put(sqlId, sqlItem);
    }

    private static SqlCommandType getSqlCommandType(String sql) {
        String s = sql.replaceAll("\\n+|\\s+", "");
        String sqlSource = s.toUpperCase();
        SqlCommandType sqlCommandType = SqlCommandType.UNKNOWN;
        if (sqlSource.startsWith(SqlCommandType.SELECT.name())) {
            sqlCommandType = SqlCommandType.SELECT;
        } else if (sqlSource.startsWith(SqlCommandType.INSERT.name())) {
            sqlCommandType = SqlCommandType.INSERT;
        } else if (sqlSource.startsWith(SqlCommandType.UPDATE.name())) {
            sqlCommandType = SqlCommandType.UPDATE;
        } else if (sqlSource.startsWith(SqlCommandType.DELETE.name())) {
            sqlCommandType = SqlCommandType.DELETE;
        }
        return sqlCommandType;
    }

    public static void main(String[] args) {
//        String sql = sql("hello.list");
//        System.out.println(sql);
    }
}

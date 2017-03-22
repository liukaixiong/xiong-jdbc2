package sql.config;

import java.io.File;
import java.util.*;

/**
 * @author liuhx on 2016/12/14 09:33
 * @version V1.0
 * @email liuhx@elab-plus.com
 */
public class SqlKit {
    public static Map<String, String> sqlMap;

    public SqlKit() {
    }

    /**
     * 获取
     *
     * @param groupNameAndsqlId
     * @return
     */
    public static String sql(String groupNameAndsqlId) {
        if (sqlMap == null) {
            throw new NullPointerException("SqlInXmlPlugin not start");
        } else {
            return sqlMap.get(groupNameAndsqlId);
        }
    }

    static void clearSqlMap() {
        sqlMap.clear();
    }

    public static void getListFile(List<File> list, File file) {
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
    static void init() {
        sqlMap = new HashMap();
        File file = new File(SqlKit.class.getClassLoader().getResource("").getPath() + "sql/");
        List<File> list = new ArrayList<File>();
        getListFile(list, file);
//        File[] files = file.listFiles(new FileFilter() {
//            public boolean accept(File pathname) {
//                return pathname.getName().endsWith(".xml");
//            }
//        });
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
                sqlMap.put(name + "." + sqlItem.id, sqlItem.value);
            }
        }

    }

    public static void main(String[] args) {
        String sql = sql("hello.list");
        System.out.println();
    }

    static {
        init();
    }
}

package common;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Liukx
 * @create 2017-03-15 23:08
 * @email liukx@elab-plus.com
 **/
public class JDBCClearMain {

    public static void mysqlTest(String table, String type, int typeSize, String updateColumn) throws SQLException {
        Connection conn = null;
        // MySQL的JDBC URL编写方式：jdbc:mysql://主机名称：连接端口/数据库的名称?参数=值
        // 避免中文乱码要指定useUnicode和characterEncoding
        // 执行数据库操作之前要在数据库管理系统上创建一个数据库，名字自己定，
        // 下面语句之前就要先创建javademo数据库
        String url = "jdbc:mysql://127.0.0.1:3306/test?"
                + "user=root&password=1234&useUnicode=true&characterEncoding=UTF8";

        try {
            // 之所以要使用下面这条语句，是因为要使用MySQL的驱动，所以我们要把它驱动起来，
            // 可以通过Class.forName把它加载进去，也可以通过初始化来驱动起来，下面三种形式都可以
            Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动
            // or:
            // com.mysql.jdbc.Driver driver = new com.mysql.jdbc.Driver();
            // or：
            // new com.mysql.jdbc.Driver();

            System.out.println("成功加载MySQL驱动程序");
            // 一个Connection代表一个数据库连接
            conn = DriverManager.getConnection(url);
            // Statement里面带有很多方法，比如executeUpdate可以实现插入，更新和删除等
            Statement stmt = conn.createStatement();
            String sql = "select a.id,a.uuid from " + table + " a where a.type = '" + type + "' order by a.uuid,id asc";
            System.out.println("查询SQL : " + sql);
            Long start = System.currentTimeMillis();
            ResultSet rs = stmt.executeQuery(sql);
            Long end = System.currentTimeMillis();
            Long time = end - start;
            System.out.println("查询耗时:" + time);

            List<String> list = new ArrayList<String>();
            Long s = System.currentTimeMillis();
            int index = 1;
            int maxIndex = typeSize;
            String tempUuid = "";
            while (rs.next()) {
                String id = rs.getString(1);
                String uuid = rs.getString(2);
                if (index == 1) {
                    tempUuid = uuid;
                } else if (!tempUuid.equals(uuid) || index > maxIndex) {
                    index = 1;
                    tempUuid = uuid;
                }
                System.out.println(id + "--------" + uuid + "-------" + index);
                String updateSql = "update " + table + " set " + updateColumn + " = '" + index + "' where id = '" + id + "' and uuid = '" + uuid + "'";
                System.out.println(updateSql);
                stmt.addBatch(updateSql);
                index++;
            }
            stmt.executeBatch();
            Long e = System.currentTimeMillis();
            System.out.println(" 手动转对象耗时 : " + (e - s) + "\t 结果集大小:" + list.size());

        } catch (SQLException e) {
            System.out.println("MySQL操作错误");
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            conn.close();
        }
    }

    public static void main(String[] args) throws SQLException {
        String tableName = "t_daren_score";
        String type = "6";
        int typeSize = 6;
        String updateColumn = "updator";
        mysqlTest(tableName, type, typeSize, updateColumn);
    }
}

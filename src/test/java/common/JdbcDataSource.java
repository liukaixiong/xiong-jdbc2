package common;

import com.x.jdbc.template.JDBCTemplateSupport;
import com.x.jdbc.model.TTest;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Liukx
 * @create 2017-03-19 15:22
 * @email liukx@elab-plus.com
 **/
public class JdbcDataSource {
    static DataSource dataSource = new JDBCTemplateSupport().getDataSource();

    public static void mxCheckJDBC() {
        String sql = " select\n" +
                "      id\n" +
                "      ,username,name,sex,status,created,time,test_id,love_name\n" +
                "      from t_test\n" +
                "      where\n" +
                "      id <= 1000001";

//        DataSource dataSource = jdbcTemplate.getDataSource();
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        long count = 0;
        try {
            con = dataSource.getConnection();
            ps = (PreparedStatement) con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
                    ResultSet.CONCUR_READ_ONLY);
            ps.setFetchSize(Integer.MIN_VALUE);
            ps.setFetchDirection(ResultSet.FETCH_REVERSE);
//            rs = ps.executeQuery();
//            ResultSetMetaData md = rs.getMetaData(); //获得结果集结构信息,元数据
//            int columnCount = md.getColumnCount();   //获得列数
//            List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
//            Date checkStart = (Date) new java.util.Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//            System.out.println("明细校验开始时间：" + dateFormat.format(checkStart));
//
//            while (rs.next()) {
//
//            }
            Long start = System.currentTimeMillis();
            rs = ps.executeQuery(sql);
            Long end = System.currentTimeMillis();
            Long time = end - start;
            System.out.println("查询耗时:" + time);

            List<TTest> list = new ArrayList<TTest>();
            Long s = System.currentTimeMillis();
            while (rs.next()) {
                TTest t = new TTest();
                t.setUsername(rs.getString(2));
//                t.setId(rs.getInt(1));
                t.setName(rs.getString(3));
                t.setSex(rs.getString(4));
                t.setStatus(rs.getString(5));
                t.setTime(rs.getDate(6));
                t.setCreated(rs.getDate(7));
                t.setTest_id(rs.getString(8));
                t.setLove_name(rs.getString(9));
                list.add(t);
            }
            Long e = System.currentTimeMillis();
            System.out.println(" 手动转对象耗时 : " + (e - s) + "\t 结果集大小:" + list.size());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("异常..");
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (con != null) {
                    con.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Long start = System.currentTimeMillis();
        mxCheckJDBC();
        Long end = System.currentTimeMillis();
        System.out.println("一共耗时 : "+(end - start));
    }
}

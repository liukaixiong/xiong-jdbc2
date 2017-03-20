package ObjectDuibi;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author Liukx
 * @create 2017-03-17 11:52
 * @email liukx@elab-plus.com
 **/
public class JDBCTemplate2 extends JdbcTemplate {

    public <T> List<T> queryForList4(String sql, Object[] args, Class<T> elementType) throws DataAccessException {
        return query(sql, args, new RowMapper4(elementType));
    }

    public <T> List<T> queryForList3(String sql, Object[] args, Class<T> elementType) throws DataAccessException {
        return query(sql, args, new RowMapper3(elementType));
    }

    public <T> List<T> queryForList2(String sql, Object[] args, Class<T> elementType) throws DataAccessException {
        return query(sql, args, new RowMapper2(elementType));
    }

    public <T> List<T> queryForList5(String sql, Object[] args, Class<T> elementType) throws DataAccessException {
        return query(sql, args, new RowMapper5(elementType));
    }
    public <T> List<T> queryForList6(String sql, Object[] args, Class<T> elementType) throws DataAccessException {
        return query(sql, args, new RowMapper6(elementType));
    }

    public <T> List<T> queryForList7(String sql, Object[] args, Class<T> elementType) throws DataAccessException {
        return query(sql, args, new RowMapper7(elementType));
    }

    public <T> List<T> query(String sql, Object[] args, RowMapper<T> rowMapper) throws DataAccessException {
        return query(sql, args, new CustomerInvokeObject<T>(rowMapper));
    }

    public <T> T query(
            PreparedStatementCreator psc, final PreparedStatementSetter pss, final ResultSetExtractor<T> rse)
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
                    System.out.println(" =========> 查询耗时 : "+(System.currentTimeMillis() - s));
                    ResultSet rsToUse = rs;
                    Long start = System.currentTimeMillis();
                    T t = rse.extractData(rsToUse);
                    Long end = System.currentTimeMillis();
                    Long time = end - start;
                    System.out.println(" ---反射耗时: " + time+"\t size - ");
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
}

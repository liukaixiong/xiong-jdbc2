package ObjectDuibi;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Liukx
 * @create 2017-03-17 11:40
 * @email liukx@elab-plus.com
 **/
public class CustomerInvokeObject<T> implements ResultSetExtractor<List<T>> {
    private final RowMapper<T> rowMapper;

    public CustomerInvokeObject(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    public List<T> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<T> list = new ArrayList<T>();
        try {
            System.out.println("初始化执行反射...");
            int rowNum = 0;
            Long start = System.currentTimeMillis();
            while (rs.next()) {
                list.add(rowMapper.mapRow(rs, rowNum));
            }
            Long end = System.currentTimeMillis();
            Long time = end - start;
            System.out.println(" ----------------------------- 参数反射耗时: [[[[[[[[[[[[" + time + "]]]]]]]]]]]");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }


}

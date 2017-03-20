package mapping;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 实体对应数据库字段,取消反射
 *
 * @author Liukx
 * @create 2017-03-19 13:51
 * @email liukx@elab-plus.com
 **/
public interface ColumnMapping {
    /**
     * 由数据库对应的实体模型去实现,将ResultSet手动set进去
     *
     * @param rs
     * @throws SQLException
     */
    public void mappingColumn(ResultSet rs) throws SQLException;

}

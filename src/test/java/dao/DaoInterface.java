package dao;

import com.x.jdbc.model.TTest;

import java.util.List;

/**
 * @author Liukx
 * @create 2018-01-03 13:33
 * @email liukx@elab-plus.com
 **/
public interface DaoInterface {
    public List<TTest> selectByExample(TTest test);

    public List<TTest> selectByName(String id, String userName);
}

package dao;

import com.x.jdbc.template.IBaseDaoSupport;
import model.TTest;
import model.TTestExample;

import java.util.List;

/**
 * @author Liukx
 * @create 2018-01-03 13:33
 * @email liukx@elab-plus.com
 **/
public interface DaoInterface extends IBaseDaoSupport<TTest, TTestExample> {
    public List<TTest> selectByExample(TTest test);

    public List<TTest> selectByName(String id, String userName);

    public TTest selectById(String id);

}

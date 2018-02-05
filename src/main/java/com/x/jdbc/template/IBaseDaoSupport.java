package com.x.jdbc.template;/**
 * Created by liukx on 2018/1/4.
 */

import java.util.List;

/**
 * 单表公共操作处理
 *
 * @author Liukx
 * @create 2018-01-04 11:18
 * @email liukx@elab-plus.com
 **/
public interface IBaseDaoSupport<T, E> {

    public int insert(T obj) throws Exception;

    public int updateById(T obj) throws Exception;

    public int delete(T obj) throws Exception;

    public T selectByObject(T obj) throws Exception;

    public List<T> selectByList(T obj) throws Exception;

    public List<T> find(E queryRule) throws Exception;

}

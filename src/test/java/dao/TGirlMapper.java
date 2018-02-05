package dao;

import model.TGirl;
import model.TGirlExample;

import java.util.List;

public interface TGirlMapper {
    long countByExample(TGirlExample example);

    int deleteByExample(TGirlExample example);

    int deleteByPrimaryKey(Integer id);

    int insert(TGirl record);

    int insertSelective(TGirl record);

    List<TGirl> selectByExample(TGirlExample example);

    TGirl selectByPrimaryKey(Integer id);

    int updateByExampleSelective(TGirl record, TGirlExample example);

    int updateByExample( TGirl record,TGirlExample example);

    int updateByPrimaryKeySelective(TGirl record);

    int updateByPrimaryKey(TGirl record);
}
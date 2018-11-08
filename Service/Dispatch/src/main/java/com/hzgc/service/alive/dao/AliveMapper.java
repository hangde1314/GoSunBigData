package com.hzgc.service.alive.dao;

import com.hzgc.service.alive.model.Alive;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Param;

import java.util.List;
@CacheNamespace
public interface AliveMapper {
    int deleteByPrimaryKey(String id);

    int insert(Alive record);

    int insertSelective(Alive record);

    Alive selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(Alive record);

    int updateByPrimaryKeyWithBLOBs(Alive record);

    int updateByPrimaryKey(Alive record);

    List<Alive> searchAliveInfo(@Param("name") String name);
}
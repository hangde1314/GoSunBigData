package com.hzgc.cloud.community.dao;

import com.hzgc.cloud.community.model.ImsiFilter;
import org.apache.ibatis.annotations.CacheNamespace;

@CacheNamespace
public interface ImsiFilterMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ImsiFilter record);

    int insertSelective(ImsiFilter record);

    ImsiFilter selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ImsiFilter record);

    int updateByPrimaryKey(ImsiFilter record);
}
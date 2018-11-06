package com.hzgc.service.white.dao;

import com.hzgc.service.white.model.WhiteInfo;

import java.util.List;

public interface WhiteInfoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(WhiteInfo record);

    int insertSelective(WhiteInfo record);

    WhiteInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(WhiteInfo record);

    int updateByPrimaryKeyWithBLOBs(WhiteInfo record);

    int updateByPrimaryKey(WhiteInfo record);

    List<WhiteInfo> selectByWhiteId(String whiteid);

    WhiteInfo selectPictureById(Long id);
}
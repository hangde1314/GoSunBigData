package com.hzgc.service.people.dao;

import com.hzgc.service.community.param.AffirmOperationDTO;
import com.hzgc.service.people.model.People;
import com.hzgc.service.people.param.FilterField;
import org.apache.ibatis.annotations.CacheNamespace;
import org.apache.ibatis.annotations.Options;

import java.sql.Timestamp;
import java.util.List;

@CacheNamespace
public interface PeopleMapper {
    int deleteByPrimaryKey(String id);

    int insert(People record);

    int insertSelective(People record);

    People selectByPrimaryKey(String id);

    People selectByIdCard(String idCard);

    int updateByPrimaryKeySelective(People record);

    int updateByPrimaryKey(People record);

    List<People> searchPeople(FilterField field);

    List<Long> getCommunityIdsById(List<Long> communityIds);

    int countCommunityPeople(Long community);

    int countImportantPeople(Long community);

    int countCarePeople(Long community);

    List<People> searchCommunityPeople(Long community);

    List<People> searchImportantPeople(Long community);

    List<People> searchCarePeople(Long community);

    List<People> searchNewPeople(Long community);

    List<People> searchOutPeople(Long community);

    People searchCommunityPeopleInfo(String id);

    People searchPeopleByIdCard(String idcard);

    Timestamp getLastTime(String id);

    Integer deleteCommunityByPeopleId(String id);

    Integer insertCommunityByPeopleId(AffirmOperationDTO param);

    List<String> getImportantPeopleId(List<Long> communityIds);
}
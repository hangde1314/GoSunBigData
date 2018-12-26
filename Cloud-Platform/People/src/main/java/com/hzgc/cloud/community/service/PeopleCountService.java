package com.hzgc.cloud.community.service;

import com.github.pagehelper.PageHelper;
import com.hzgc.cloud.community.dao.NewPeopleMapper;
import com.hzgc.cloud.community.dao.OutPeopleMapper;
import com.hzgc.cloud.community.model.Count;
import com.hzgc.cloud.community.param.*;
import com.hzgc.cloud.people.dao.PeopleMapper;
import com.hzgc.cloud.people.fields.Flag;
import com.hzgc.cloud.people.model.People;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PeopleCountService {
    @Autowired
    @SuppressWarnings("unused")
    private PeopleMapper peopleMapper;

    @Autowired
    @SuppressWarnings("unused")
    private NewPeopleMapper newPeopleMapper;

    @Autowired
    @SuppressWarnings("unused")
    private OutPeopleMapper outPeopleMapper;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public PeopleCountVO countCommunityPeople(Long communityId) {
        PeopleCountVO vo = new PeopleCountVO();
        vo.setCommunityPeoples(peopleMapper.countCommunityPeople(communityId));
        // 重点人员统计条件：1.当前小区；2.符合重点标签；3.此人员须有照片
        vo.setImportantPeoples(peopleMapper.countImportantPeople(communityId));
        // 关爱人员统计条件：1.当前小区；2.符合关爱标签；3.此人员须有照片
        vo.setCarePeoples(peopleMapper.countCarePeople(communityId));
        vo.setNewPeoples(newPeopleMapper.countNewPeople(communityId));
        vo.setOutPeoples(outPeopleMapper.countOutPeople(communityId));
        return vo;
    }

    public GridPeopleCount countGridPeople(Long gridCode) {
        GridPeopleCount gridPeopleCount = new GridPeopleCount();
        int count = peopleMapper.countGridPeople(gridCode);
        gridPeopleCount.setPeopleCount(count);
        List<GridFlagCount> counts = new ArrayList<>();
        GridFlagCount flag_0 = new GridFlagCount();
        flag_0.setFlag("矫正人员");
        counts.add(flag_0);
        GridFlagCount flag_1 = new GridFlagCount();
        flag_1.setFlag("刑释解戒");
        counts.add(flag_1);
        GridFlagCount flag_2 = new GridFlagCount();
        flag_2.setFlag("精神病人");
        counts.add(flag_2);
        GridFlagCount flag_3 = new GridFlagCount();
        flag_3.setFlag("吸毒人员");
        counts.add(flag_3);
        GridFlagCount flag_4 = new GridFlagCount();
        flag_4.setFlag("境外人员");
        counts.add(flag_4);
        GridFlagCount flag_5 = new GridFlagCount();
        flag_5.setFlag("艾滋病人");
        counts.add(flag_5);
        GridFlagCount flag_6 = new GridFlagCount();
        flag_6.setFlag("重点青少年");
        counts.add(flag_6);
        GridFlagCount flag_7 = new GridFlagCount();
        flag_7.setFlag("留守人员");
        counts.add(flag_7);
        GridFlagCount flag_8 = new GridFlagCount();
        flag_8.setFlag("涉军人员");
        counts.add(flag_8);
        GridFlagCount flag_9 = new GridFlagCount();
        flag_9.setFlag("信访人员");
        counts.add(flag_9);
        GridFlagCount flag_10 = new GridFlagCount();
        flag_10.setFlag("邪教人员");
        counts.add(flag_10);
        // 人员统计条件：1.当前网格；2.符合当前标签；3.此人员须有照片
        List<Count> flagCount = peopleMapper.countFlagPeople(gridCode);
        if (flagCount != null && flagCount.size() > 0){
            for (Count c : flagCount){
                for (GridFlagCount gridFlagCount : counts){
                    if (Flag.getFlag(c.getFlagid()).equals(gridFlagCount.getFlag())){
                        gridFlagCount.setCount(c.getCount());
                    }
                }
            }
        }
        gridPeopleCount.setFlagCount(counts);
        return gridPeopleCount;
    }

    public List<PeopleVO> searchCommunityPeople(PeopleDTO param) {
        PageHelper.offsetPage(param.getStart(), param.getLimit());
        List<People> peopleList = peopleMapper.searchCommunityPeople(param.getCommunityId());
        return this.shift(peopleList);
    }

    // 重点人员查询条件：1.当前小区；2.符合重点标签；3.此人员须有照片
    public List<PeopleVO> searchCommunityImportantPeople(PeopleDTO param) {
        PageHelper.offsetPage(param.getStart(), param.getLimit());
        List<People> peopleList = peopleMapper.searchImportantPeople(param);
        return this.shift(peopleList);
    }

    // 关爱人员查询条件：1.当前小区；2.符合关爱标签；3.此人员须有照片
    public List<PeopleVO> searchCommunityCarePeople(PeopleDTO param) {
        PageHelper.offsetPage(param.getStart(), param.getLimit());
        List<People> peopleList = peopleMapper.searchCarePeople(param.getCommunityId());
        return this.shift(peopleList);
    }

    public List<PeopleVO> searchCommunityNewPeople(PeopleDTO param) {
        PageHelper.offsetPage(param.getStart(), param.getLimit());
        List<People> peopleList = peopleMapper.searchNewPeople(param.getCommunityId());
        return this.shift(peopleList);
    }

    public List<PeopleVO> searchCommunityOutPeople(PeopleDTO param) {
        PageHelper.offsetPage(param.getStart(), param.getLimit());
        List<People> peopleList = peopleMapper.searchOutPeople(param.getCommunityId());
        return this.shift(peopleList);
    }

    private List<PeopleVO> shift(List<People> peopleList) {
        List<PeopleVO> voList = new ArrayList<>();
        if (peopleList != null && peopleList.size() > 0) {
            for (People people : peopleList) {
                PeopleVO vo = new PeopleVO();
                vo.setId(people.getId());
                vo.setIdCard(people.getIdcard());
                vo.setName(people.getName());
                if (people.getLasttime() != null) {
                    vo.setLastTime(sdf.format(people.getLasttime()));
                }
                vo.setPictureId(people.getPictureId());
                voList.add(vo);
            }
        }
        return voList;
    }
}

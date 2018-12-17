package com.hzgc.service.community.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hzgc.common.service.api.service.InnerService;
import com.hzgc.common.service.api.service.PlatformService;
import com.hzgc.common.service.response.ResponseResult;
import com.hzgc.common.util.basic.ImsiUtil;
import com.hzgc.common.util.basic.ListUtil;
import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.jniface.FaceAttribute;
import com.hzgc.jniface.FaceUtil;
import com.hzgc.service.community.dao.*;
import com.hzgc.service.community.model.*;
import com.hzgc.service.community.param.*;
import com.hzgc.service.people.dao.*;
import com.hzgc.service.people.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class CommunityService {
    @Autowired
    @SuppressWarnings("unused")
    private PeopleMapper peopleMapper;

    @Autowired
    @SuppressWarnings("unused")
    private FlagMapper flagMapper;

    @Autowired
    @SuppressWarnings("unused")
    private ImsiMapper imsiMapper;

    @Autowired
    @SuppressWarnings("unused")
    private PhoneMapper phoneMapper;

    @Autowired
    @SuppressWarnings("unused")
    private HouseMapper houseMapper;

    @Autowired
    @SuppressWarnings("unused")
    private CarMapper carMapper;

    @Autowired
    @SuppressWarnings("unused")
    private PictureMapper pictureMapper;

    @Autowired
    @SuppressWarnings("unused")
    private FusionImsiMapper fusionImsiMapper;

    @Autowired
    @SuppressWarnings("unused")
    private NewPeopleMapper newPeopleMapper;

    @Autowired
    @SuppressWarnings("unused")
    private OutPeopleMapper outPeopleMapper;

    @Autowired
    @SuppressWarnings("unused")
    private RecognizeRecordMapper recognizeRecordMapper;

    @Autowired
    @SuppressWarnings("unused")
    private DeviceRecognizeMapper deviceRecognizeMapper;

    @Autowired
    @SuppressWarnings("unused")
    private Count24HourMapper count24HourMapper;

    @Autowired
    @SuppressWarnings("unused")
    private ImsiAllMapper imsiAllMapper;

    @Autowired
    @SuppressWarnings("unused")
    private CarRecognizeMapper carRecognizeMapper;

    @Autowired
    @SuppressWarnings("unused")
    private PlatformService platformService;

    @Autowired
    @SuppressWarnings("unused")
    private InnerService innerService;

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

    public List<NewAndOutPeopleCounVO> countCommunityNewAndOutPeople(NewAndOutPeopleCountDTO param) {
        List<CountCommunityPeople> totalNewCount = newPeopleMapper.countTotalNewPeople(param);
        List<CountCommunityPeople> totalOutCount = outPeopleMapper.countTotalOutPeople(param);
        List<CountCommunityPeople> confirmNewCount = newPeopleMapper.countConfirmNewPeople(param);
        List<CountCommunityPeople> confirmOutCount = outPeopleMapper.countConfirmOutPeople(param);
        List<NewAndOutPeopleCounVO> voList = new ArrayList<>();
        for (Long communityId : param.getCommunityIdList()) {
            NewAndOutPeopleCounVO vo = new NewAndOutPeopleCounVO();
            vo.setCommunityId(communityId);
            vo.setCommunityName(platformService.getCommunityName(communityId));
            vo.setMonth(param.getMonth());
            for (CountCommunityPeople suggestNew : totalNewCount) {
                if (suggestNew.getCommunity().equals(communityId)) {
                    vo.setSuggestNewCount(suggestNew.getCount());
                    break;
                }
            }
            for (CountCommunityPeople suggestOut : totalOutCount) {
                if (suggestOut.getCommunity().equals(communityId)) {
                    vo.setSuggestOutCount(suggestOut.getCount());
                    break;
                }
            }
            for (CountCommunityPeople confirmNew : confirmNewCount) {
                if (confirmNew.getCommunity().equals(communityId)) {
                    vo.setConfirmNewCount(confirmNew.getCount());
                    break;
                }
            }
            for (CountCommunityPeople confirmOut : confirmOutCount) {
                if (confirmOut.getCommunity().equals(communityId)) {
                    vo.setConfirmOutCount(confirmOut.getCount());
                    break;
                }
            }
            if (!(vo.getSuggestNewCount() == 0 && vo.getSuggestOutCount() == 0)) {
                voList.add(vo);
            }
        }
        log.info("Community new and out people total result: " + JacksonUtil.toJson(voList));
        List<NewAndOutPeopleCounVO> vos = ListUtil.pageSplit(voList, param.getStart(), param.getLimit());
        log.info("Count community new and out people result: " + JacksonUtil.toJson(vos));
        return vos;
    }

    public NewAndOutPeopleSearchVO searchCommunityNewAndOutPeople(NewAndOutPeopleSearchDTO param) {
        List<NewAndOutPeopleSearch> newAndOutPeopleSearchList = new ArrayList<>();
        // 小区迁入人口查询（疑似与确认）
        if (param.getType() == 0) {
            Page page = PageHelper.offsetPage(param.getStart(), param.getLimit());
            List<NewPeople> list = newPeopleMapper.searchCommunityNewPeople(param);
            PageInfo info = new PageInfo(page.getResult());
            if (list != null && list.size() > 0) {
                for (NewPeople people : list) {
                    NewAndOutPeopleSearch vo = new NewAndOutPeopleSearch();
                    vo.setPeopleId(people.getPeopleid());
                    vo.setCommunityId(param.getCommunityId());
                    vo.setMonth(param.getMonth());
                    vo.setType(param.getType());
                    // 未确认迁入
                    if (people.getIsconfirm() == 1) {
                        vo.setIsconfirm(2);
                        if (people.getFlag() == 1) {
                            vo.setFlag(0);
                            // 未确认迁入人口:预实名
                            vo.setPicture(getPictureIdByPeopleId(people.getPeopleid()));
                        }
                        if (people.getFlag() == 2) {
                            vo.setFlag(1);
                            // 未确认迁入人口:新增
                            vo.setSul(innerService.httpHostNameToIp(getSurlByPeopleId(people.getPeopleid())).getHttp_ip());
                        }
                    }
                    // 已确认迁入
                    if (people.getIsconfirm() == 2) {
                        vo.setIsconfirm(0);
                        if (people.getFlag() == 1) {
                            vo.setFlag(0);
                        }
                        if (people.getFlag() == 2) {
                            vo.setFlag(1);
                        }
                        vo.setPicture(getPictureIdByPeopleId(people.getPeopleid()));
                    }
                    // 已确认未迁入
                    if (people.getIsconfirm() == 3) {
                        vo.setIsconfirm(1);
                        if (people.getFlag() == 1) {
                            vo.setFlag(0);
                            // 已确认未迁入人口:预实名
                            vo.setPicture(getPictureIdByPeopleId(people.getPeopleid()));
                        }
                        if (people.getFlag() == 2) {
                            vo.setFlag(1);
                            // 已确认未迁入人口:新增
                            vo.setSul(innerService.httpHostNameToIp(getSurlByPeopleId(people.getPeopleid())).getHttp_ip());
                        }
                    }
                    newAndOutPeopleSearchList.add(vo);
                }
                NewAndOutPeopleSearchVO vo = new NewAndOutPeopleSearchVO();
                vo.setTotalNum((int) info.getTotal());
                vo.setVoList(newAndOutPeopleSearchList);
                return vo;
            }
        }
        // 小区迁出人口查询（疑似与确认）
        if (param.getType() == 1) {
            Page page = PageHelper.offsetPage(param.getStart(), param.getLimit());
            List<OutPeople> list = outPeopleMapper.searchCommunityOutPeople(param);
            PageInfo info = new PageInfo(page.getResult());
            if (list != null && list.size() > 0) {
                for (OutPeople people : list) {
                    NewAndOutPeopleSearch vo = new NewAndOutPeopleSearch();
                    vo.setPeopleId(people.getPeopleid());
                    vo.setCommunityId(param.getCommunityId());
                    vo.setMonth(param.getMonth());
                    vo.setPicture(getPictureIdByPeopleId(people.getPeopleid()));
                    vo.setType(param.getType());
                    if (people.getIsconfirm() == 1) {
                        vo.setIsconfirm(5);
                    }
                    if (people.getIsconfirm() == 2) {
                        vo.setIsconfirm(3);
                    }
                    if (people.getIsconfirm() == 3) {
                        vo.setIsconfirm(4);
                    }
                    newAndOutPeopleSearchList.add(vo);
                }
            }
            NewAndOutPeopleSearchVO vo = new NewAndOutPeopleSearchVO();
            vo.setTotalNum((int) info.getTotal());
            vo.setVoList(newAndOutPeopleSearchList);
            return vo;
        }
        return new NewAndOutPeopleSearchVO();
    }

    private Long getPictureIdByPeopleId(String peopleId) {
        return pictureMapper.getPictureIdByPeopleId(peopleId);
    }

    private String getSurlByPeopleId(String peopleId) {
        return recognizeRecordMapper.getSurlByPeopleId(peopleId);
    }

    public CommunityPeopleInfoVO searchCommunityPeopleInfo(String peopleId) {
        CommunityPeopleInfoVO vo = new CommunityPeopleInfoVO();
        People people = peopleMapper.searchCommunityPeopleInfo(peopleId);
        if (people != null) {
            vo.setId(people.getId());
            vo.setName(people.getName());
            vo.setIdCard(people.getIdcard());
            vo.setSex(people.getSex());
            vo.setBirthday(people.getBirthday());
            vo.setBirthplace(people.getBirthplace());
            vo.setAddress(people.getAddress());
            vo.setPictureId(people.getPictureId());
        }
        return vo;
    }

    public CommunityPeopleInfoVO searchPeopleByIdCard(String idCard) {
        CommunityPeopleInfoVO vo = new CommunityPeopleInfoVO();
        People people = peopleMapper.searchPeopleByIdCard(idCard);
        if (people != null) {
            vo.setId(people.getId());
            vo.setName(people.getName());
            vo.setSex(people.getSex());
            vo.setIdCard(people.getIdcard());
            vo.setBirthday(people.getBirthday());
            vo.setBirthplace(people.getBirthplace());
            vo.setAddress(people.getAddress());
            if (StringUtils.isNotBlank(people.getId())) {
                Long pictureId = getPictureIdByPeopleId(people.getId());
                vo.setPictureId(pictureId);
            }
        }
        return vo;
    }

    public CaptureDetailsVO searchCommunityNewPeopleCaptureDetails(CaptureDetailsDTO param) {
        CaptureDetailsVO vo = new CaptureDetailsVO();
        // 小区迁入人口抓拍详情:设备抓拍统计
        List<CaptureDeviceCount> deviceCountList = new ArrayList<>();
        List<DeviceRecognize> deviceRecognizes = deviceRecognizeMapper.countCommunityNewPeopleCapture(param);
        if (deviceRecognizes != null && deviceRecognizes.size() > 0) {
            for (DeviceRecognize deviceRecognize : deviceRecognizes) {
                CaptureDeviceCount captureDeviceCount = new CaptureDeviceCount();
                String deviceName = platformService.getCameraDeviceName(deviceRecognize.getDeviceid());
                if (StringUtils.isBlank(deviceName)) {
                    deviceName = platformService.getImsiDeviceName(deviceRecognize.getDeviceid());
                }
                captureDeviceCount.setDeviceName(deviceName != null ? deviceName : deviceRecognize.getDeviceid());
                captureDeviceCount.setCount(deviceRecognize.getCount());
                deviceCountList.add(captureDeviceCount);
            }
        }
        vo.setDeviceCountList(deviceCountList);
        // 小区迁入人口抓拍详情:24小时统计(30天总和)
        List<CaptureHourCount> hourCountList = new ArrayList<>();
        List<Count24Hour> count24Hours = count24HourMapper.countCommunityNewPeopleCapture(param);
        List<String> hourList = new ArrayList<>();
        hourList.add("00");hourList.add("01");hourList.add("02");hourList.add("03");hourList.add("04");hourList.add("05");
        hourList.add("06");hourList.add("07");hourList.add("08");hourList.add("09");hourList.add("10");hourList.add("11");
        hourList.add("12");hourList.add("13");hourList.add("14");hourList.add("15");hourList.add("16");hourList.add("17");
        hourList.add("18");hourList.add("19");hourList.add("20");hourList.add("21");hourList.add("22");hourList.add("23");
        for (String hour : hourList) {
            CaptureHourCount count = new CaptureHourCount();
            count.setHour(hour);
            if (count24Hours != null && count24Hours.size() > 0) {
                for (Count24Hour count24Hour : count24Hours) {
                    if (hour.equals(count24Hour.getHour())) {
                        count.setCount(count24Hour.getCount());
                    }
                }
                hourCountList.add(count);
            }
        }
        vo.setHourCountList(hourCountList);
        log.info("hourCountList:" + JacksonUtil.toJson(hourCountList));
        // 小区迁入人口抓拍详情:人员抓拍列表
        Page page = PageHelper.offsetPage(param.getStart(), param.getLimit());
        List<RecognizeRecord> records = recognizeRecordMapper.searchCommunityNewPeopleCaptureData(param);
        PageInfo pageInfo = new PageInfo(page.getResult());
        int total = (int) pageInfo.getTotal();
        CapturePeopleCount capturePeopleCount = new CapturePeopleCount();
        capturePeopleCount.setTotal(total);
        List<CapturePictureInfo> infoList = new ArrayList<>();
        for (RecognizeRecord record : records) {
            CapturePictureInfo info = new CapturePictureInfo();
            info.setDeviceId(record.getDeviceid());
            info.setDeviceName(platformService.getCameraDeviceName(record.getDeviceid()));
            info.setSurl(innerService.httpHostNameToIp(record.getSurl()).getHttp_ip());
            info.setBurl(innerService.httpHostNameToIp(record.getBurl()).getHttp_ip());
            info.setCaptureTime(sdf.format(record.getCapturetime()));
            infoList.add(info);
        }
        capturePeopleCount.setPictureInfos(infoList);
        vo.setPeopleCount(capturePeopleCount);
        return vo;
    }

    public OutPeopleLastCaptureVO searchCommunityOutPeopleLastCapture(String peopleId) {
        OutPeopleLastCaptureVO vo = new OutPeopleLastCaptureVO();
        RecognizeRecord record = recognizeRecordMapper.searchCommunityOutPeopleLastCapture(peopleId);
        if (record != null) {
            vo.setDeviceId(record.getDeviceid());
            vo.setDeviceName(platformService.getCameraDeviceName(record.getDeviceid()));
            vo.setPicture(innerService.httpHostNameToIp(record.getSurl()).getHttp_ip());
            vo.setLastTime(sdf.format(record.getCapturetime()));
            long now = new Date().getTime();
            int day = Math.toIntExact((now - record.getCapturetime().getTime()) / (24 * 60 * 60 * 1000));
            vo.setLastDay(day);
        }
        return vo;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer communityAffirmOut(AffirmOperationDTO param) {
        // 已确认迁出
        if (param.getIsconfirm() == 2) {
            Integer delete = peopleMapper.deleteCommunityByPeopleId(param.getPeopleId());
            if (delete != 1) {
                log.info("Affirm out operation failed");
                return 0;
            }
            Integer update = outPeopleMapper.updateIsconfirm(param);
            if (update != 1) {
                log.info("Affirm out operation failed");
                return 0;
            }
        } else if (param.getIsconfirm() == 3) {     // 已确认未迁出
            Integer update = outPeopleMapper.updateIsconfirm(param);
            if (update != 1) {
                log.info("Affirm out operation failed");
                return 0;
            }
        } else {
            log.info("Affirm out operation failed, because param: isconfirm error");
            return 0;
        }
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer communityAffirmNew(AffirmOperationDTO param) {
        // 已确认迁入
        if (param.getIsconfirm() == 2) {
            // 已确认迁入:预实名
            if (param.getFlag() == 0) {
                Integer integer = peopleMapper.insertCommunityByPeopleId(param);
                if (integer != 1) {
                    log.info("Affirm new operation failed");
                    return 0;
                }
                Integer update = newPeopleMapper.updateIsconfirm(param);
                if (update != 1) {
                    log.info("Affirm new operation failed");
                    return 0;
                }
            } else if (param.getFlag() == 1) {    // 已确认迁入:新增
                Integer update = newPeopleMapper.updateIsconfirm(param);
                if (update != 1) {
                    log.info("Affirm new operation failed");
                    return 0;
                }
            } else {
                log.info("Affirm new operation failed, because param: flag error");
                return 0;
            }
        } else if (param.getIsconfirm() == 3) {     // 已确认未迁入
            Integer update = newPeopleMapper.updateIsconfirm(param);
            if (update != 1) {
                log.info("Affirm new operation failed");
                return 0;
            }
        } else {
            log.info("Affirm new operation failed, because param: isconfirm error");
            return 0;
        }
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer communityAffirmNew_newPeopleHandle(NewPeopleHandleDTO param) {
        People people = peopleMapper.selectByPrimaryKey(param.getSearchPeopleId());
        int status_delete = peopleMapper.deleteByPrimaryKey(param.getSearchPeopleId());
        if (status_delete != 1) {
            log.error("Delete people info failed, people info:" + JacksonUtil.toJson(people));
            return 0;
        }
        People people_insert = new People();
        people_insert.setId(param.getNewPeopleId());
        people_insert.setName(people.getName());
        people_insert.setIdcard(people.getIdcard());
        people_insert.setRegion(people.getRegion());
        people_insert.setHousehold(people.getHousehold());
        people_insert.setAddress(people.getAddress());
        people_insert.setSex(people.getSex());
        people_insert.setAge(people.getAge());
        people_insert.setBirthday(people.getBirthday());
        people_insert.setPolitic(people.getPolitic());
        people_insert.setEdulevel(people.getEdulevel());
        people_insert.setJob(people.getJob());
        people_insert.setBirthplace(people.getBirthplace());
        people_insert.setCommunity(param.getCommunityId());
        people_insert.setLasttime(people.getLasttime());
        people_insert.setCreatetime(people.getCreatetime());
        people_insert.setUpdatetime(new Date());
        int status_insert = peopleMapper.insertSelective(people_insert);
        if (status_insert != 1) {
            log.error("Insert people info failed, people info:" + JacksonUtil.toJson(people_insert));
            return 0;
        }
        if (people.getFlag() != null && people.getFlag().size() > 0) {
            for (Flag flag : people.getFlag()) {
                if (flag != null) {
                    Flag update = new Flag();
                    update.setId(flag.getId());
                    update.setPeopleid(param.getNewPeopleId());
                    int update_status = flagMapper.updateByPrimaryKeySelective(update);
                    if (update_status != 1) {
                        log.error("Update people info: t_flag failed, flag info:" + JacksonUtil.toJson(flag));
                        return 0;
                    }
                } else {
                    log.error("Get flag info failed");
                }
            }
        }
        if (people.getImsi() != null && people.getImsi().size() > 0) {
            for (Imsi imsi : people.getImsi()) {
                if (imsi != null) {
                    Imsi update = new Imsi();
                    update.setId(imsi.getId());
                    update.setPeopleid(param.getNewPeopleId());
                    int update_status = imsiMapper.updateByPrimaryKeySelective(update);
                    if (update_status != 1) {
                        log.error("Update people info: t_imsi failed, imsi info:" + JacksonUtil.toJson(imsi));
                        return 0;
                    }
                } else {
                    log.error("Get imsi info failed");
                }
            }
        }
        if (people.getPhone() != null && people.getPhone().size() > 0) {
            for (Phone phone : people.getPhone()) {
                if (phone != null) {
                    Phone update = new Phone();
                    update.setId(phone.getId());
                    update.setPeopleid(param.getNewPeopleId());
                    int update_status = phoneMapper.updateByPrimaryKeySelective(update);
                    if (update_status != 1) {
                        log.error("Update people info: t_phone failed, phone info:" + JacksonUtil.toJson(phone));
                        return 0;
                    }
                } else {
                    log.error("Get phone info failed");
                }
            }
        }
        if (people.getHouse() != null && people.getHouse().size() > 0) {
            for (House house : people.getHouse()) {
                if (house != null) {
                    House update = new House();
                    update.setId(house.getId());
                    update.setPeopleid(param.getNewPeopleId());
                    int update_status = houseMapper.updateByPrimaryKeySelective(update);
                    if (update_status != 1) {
                        log.error("Update people info: t_house failed, house info:" + JacksonUtil.toJson(house));
                        return 0;
                    }
                } else {
                    log.error("Get house info failed");
                }
            }
        }
        if (people.getCar() != null && people.getCar().size() > 0) {
            for (Car car : people.getCar()) {
                if (car != null) {
                    Car update = new Car();
                    update.setId(car.getId());
                    update.setPeopleid(param.getNewPeopleId());
                    int update_status = carMapper.updateByPrimaryKeySelective(update);
                    if (update_status != 1) {
                        log.error("Update people info: t_car failed, car info:" + JacksonUtil.toJson(car));
                        return 0;
                    }
                } else {
                    log.error("Get car info failed");
                }
            }
        }
        if (people.getPicture() != null && people.getPicture().size() > 0) {
            for (PictureWithBLOBs picture : people.getPicture()) {
                if (picture != null) {
                    PictureWithBLOBs update = new PictureWithBLOBs();
                    update.setId(picture.getId());
                    update.setPeopleid(param.getNewPeopleId());
                    int update_status = pictureMapper.updateByPrimaryKeySelective(update);
                    if (update_status != 1) {
                        log.error("Update people info: t_picture failed, car picture:" + JacksonUtil.toJson(picture));
                        return 0;
                    }
                } else {
                    log.error("Get picture info failed");
                }
            }
        }
        PictureWithBLOBs picture = new PictureWithBLOBs();
        picture.setPeopleid(param.getNewPeopleId());
        byte[] bytes = FaceUtil.base64Str2BitFeature(param.getCapturePicture());
        picture.setIdcardpic(bytes);
        FaceAttribute faceAttribute = innerService.faceFeautreCheck(param.getCapturePicture()).getFeature();
        if (faceAttribute == null || faceAttribute.getFeature() == null || faceAttribute.getBitFeature() == null) {
            log.error("Face feature extract failed, insert picture to t_picture failed");
            throw new RuntimeException("Face feature extract failed, insert picture to t_picture failed");
        }
        picture.setFeature(FaceUtil.floatFeature2Base64Str(faceAttribute.getFeature()));
        picture.setBitfeature(FaceUtil.bitFeautre2Base64Str(faceAttribute.getBitFeature()));
        int insertStatus = pictureMapper.insertSelective(picture);
        if (insertStatus != 1) {
            log.info("Insert people, but insert picture to t_picture failed");
            return 0;
        }
        return 1;
    }

    public ResponseResult<List<PeopleCaptureVO>> searchCapture1Month(PeopleCaptureDTO param) {
        List<PeopleCaptureVO> voList = new ArrayList<>();
        Page page = PageHelper.offsetPage(param.getStart(), param.getLimit(), true);
        List<RecognizeRecord> records = recognizeRecordMapper.searchCapture1Month(param);
        PageInfo info = new PageInfo(page.getResult());
        if (records != null && records.size() > 0) {
            for (RecognizeRecord record : records) {
                if (record != null) {
                    PeopleCaptureVO vo = new PeopleCaptureVO();
                    switch (param.getSearchType()) {
                        case 0:
                            // 返回:人脸抓拍、IMSI码、车辆全部抓拍识别记录
                            switch (record.getType()) {
                                case 1:
                                    // 人脸抓拍识别记录
                                    vo.setCaptureType(0);
                                    vo.setRecordId(record.getId());
                                    vo.setCaptureTime(sdf.format(record.getCapturetime()));
                                    vo.setCameraDeviceId(platformService.getCameraDeviceName(record.getDeviceid()));
                                    vo.setBurl(innerService.httpHostNameToIp(record.getBurl()).getHttp_ip());
                                    vo.setSurl(innerService.httpHostNameToIp(record.getSurl()).getHttp_ip());
                                    break;
                                case 2:
                                    // IMSI码识别记录
                                    vo.setCaptureType(1);
                                    vo.setRecordId(record.getId());
                                    vo.setCaptureTime(sdf.format(record.getCapturetime()));
                                    vo.setImsiDeviceId(platformService.getImsiDeviceName(record.getDeviceid()));
                                    vo.setImsi(ImsiUtil.toMac(record.getImsi()));
                                    break;
                                case 3:
                                    // 车辆抓拍识别记录
                                    vo.setCaptureType(2);
                                    vo.setRecordId(record.getId());
                                    vo.setCaptureTime(sdf.format(record.getCapturetime()));
                                    vo.setCameraDeviceId(platformService.getCameraDeviceName(record.getDeviceid()));
                                    vo.setPlate(record.getPlate());
                                    vo.setBurl(innerService.httpHostNameToIp(record.getBurl()).getHttp_ip());
                                    vo.setSurl(innerService.httpHostNameToIp(record.getSurl()).getHttp_ip());
                                    break;
                                default:
                                    break;
                            }
                            voList.add(vo);
                            break;
                        case 1:
                            // 返回:人脸抓拍识别记录
                            if (record.getType() == 1){
                                // 人脸抓拍识别记录
                                vo.setCaptureType(0);
                                vo.setRecordId(record.getId());
                                vo.setCaptureTime(sdf.format(record.getCapturetime()));
                                vo.setCameraDeviceId(platformService.getCameraDeviceName(record.getDeviceid()));
                                vo.setBurl(innerService.httpHostNameToIp(record.getBurl()).getHttp_ip());
                                vo.setSurl(innerService.httpHostNameToIp(record.getSurl()).getHttp_ip());
                                voList.add(vo);
                            }
                            break;
                        case 2:
                            // 返回:IMSI码识别记录
                            if (record.getType() == 2){
                                vo.setCaptureType(1);
                                vo.setRecordId(record.getId());
                                vo.setCaptureTime(sdf.format(record.getCapturetime()));
                                vo.setImsiDeviceId(platformService.getImsiDeviceName(record.getDeviceid()));
                                vo.setImsi(ImsiUtil.toMac(record.getImsi()));
                                voList.add(vo);
                            }
                            break;
                        case 3:
                            // 返回:车辆抓拍识别记录
                            if (record.getType() == 3){
                                vo.setCaptureType(2);
                                vo.setRecordId(record.getId());
                                vo.setCaptureTime(sdf.format(record.getCapturetime()));
                                vo.setCameraDeviceId(platformService.getCameraDeviceName(record.getDeviceid()));
                                vo.setPlate(record.getPlate());
                                vo.setBurl(innerService.httpHostNameToIp(record.getBurl()).getHttp_ip());
                                vo.setSurl(innerService.httpHostNameToIp(record.getSurl()).getHttp_ip());
                                voList.add(vo);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        return ResponseResult.init(voList, info.getTotal());
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer deleteRecognizeRecord(List<String> idList) {
        for (String id : idList) {
            int status = recognizeRecordMapper.deleteByPrimaryKey(id);
            if (status != 1) {
                throw new RuntimeException("删除抓拍识别记录失败！");
            }
        }
        return 1;
    }

    public List<PeopleCaptureVO> searchPeopleTrack1Month(String peopleId) {
        List<PeopleCaptureVO> voList = new ArrayList<>();
        List<RecognizeRecord> records = recognizeRecordMapper.searchPeopleTrack1Month(peopleId);
        if (records != null && records.size() > 0) {
            for (RecognizeRecord record : records) {
                if (record != null) {
                    PeopleCaptureVO vo = new PeopleCaptureVO();
                    switch (record.getType()) {
                        case 1:
                            // 人脸抓拍识别记录
                            vo.setCaptureType(0);
                            vo.setRecordId(record.getId());
                            vo.setCaptureTime(sdf.format(record.getCapturetime()));
                            vo.setCameraDeviceId(record.getDeviceid());
                            vo.setBurl(innerService.httpHostNameToIp(record.getBurl()).getHttp_ip());
                            vo.setSurl(innerService.httpHostNameToIp(record.getSurl()).getHttp_ip());
                            break;
                        case 2:
                            // IMSI码识别记录
                            vo.setCaptureType(1);
                            vo.setRecordId(record.getId());
                            vo.setCaptureTime(sdf.format(record.getCapturetime()));
                            vo.setImsiDeviceId(record.getDeviceid());
                            vo.setImsi(record.getImsi());
                            break;
                        case 3:
                            // 车辆抓拍识别记录
                            vo.setCaptureType(2);
                            vo.setRecordId(record.getId());
                            vo.setCaptureTime(sdf.format(record.getCapturetime()));
                            vo.setCameraDeviceId((record.getDeviceid()));
                            vo.setPlate(record.getPlate());
                            vo.setBurl(innerService.httpHostNameToIp(record.getBurl()).getHttp_ip());
                            vo.setSurl(innerService.httpHostNameToIp(record.getSurl()).getHttp_ip());
                            break;
                        default:
                            break;
                    }
                    voList.add(vo);
                }
            }
        }
        return voList;
    }

    public List<PeopleCaptureCountVO> countDeviceCaptureNum1Month(String peopleId) {
        List<PeopleCaptureCountVO> voList = new ArrayList<>();
        List<DeviceRecognize> deviceRecognizeList = deviceRecognizeMapper.countDeviceCaptureNum1Month(peopleId);
        if (deviceRecognizeList != null && deviceRecognizeList.size() > 0) {
            List<String> deviceIdList = new ArrayList<>();
            for (DeviceRecognize device : deviceRecognizeList) {
                String deviceId = device.getDeviceid();
                if (StringUtils.isNotBlank(deviceId) && !deviceIdList.contains(deviceId)) {
                    deviceIdList.add(deviceId);
                }
            }
            if (deviceIdList.size() > 0) {
                for (String deviceId : deviceIdList) {
                    PeopleCaptureCountVO vo = new PeopleCaptureCountVO();
                    vo.setDeviceId(deviceId);
                    for (DeviceRecognize deviceRecognize : deviceRecognizeList) {
                        if (deviceId.equals(deviceRecognize.getDeviceid())) {
                            vo.setCount(vo.getCount() + deviceRecognize.getCount());
                        }
                    }
                    voList.add(vo);
                }
            }
        }
        return voList;
    }

    public List<PeopleCaptureCountVO> countCaptureNum3Month(String peopleId) {
        List<PeopleCaptureCountVO> voList = new ArrayList<>();
        List<DeviceRecognize> deviceRecognizeList = deviceRecognizeMapper.countCaptureNum3Month(peopleId);
        if (deviceRecognizeList != null && deviceRecognizeList.size() > 0) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            int year_end = cal.get(Calendar.YEAR);
            int month_end = cal.get(Calendar.MONTH) + 1;
            int date_end = cal.get(Calendar.DATE);
            String endDate = year_end + "-" + month_end + "-" + date_end;
            cal.add(Calendar.MONTH, -2);
            cal.add(Calendar.DATE, -date_end + 1);
            int year_start = cal.get(Calendar.YEAR);
            int month_start = cal.get(Calendar.MONTH) + 1;
            int date_start = cal.get(Calendar.DATE);
            String startDate = year_start + "-"
                    + (month_start >= 10 ? month_start : "0" + month_start) + "-"
                    + (date_start >= 10 ? date_start : "0" + date_start);
            List<String> dateList = new ArrayList<>();
            dateList.add(startDate);
            try {
                long time = dateFormat.parse(startDate).getTime();
                for (int i = 0; i < 100; i++) {
                    time = time + (24 * 60 * 60 * 1000);
                    if (time > dateFormat.parse(endDate).getTime()) {
                        break;
                    }
                    dateList.add(dateFormat.format(time));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (dateList.size() > 0) {
                for (String date : dateList) {
                    PeopleCaptureCountVO vo = new PeopleCaptureCountVO();
                    vo.setDate(date);
                    for (DeviceRecognize deviceRecognize : deviceRecognizeList) {
                        if (date.replace("-", "").equals(deviceRecognize.getCurrenttime())) {
                            vo.setCount(vo.getCount() + deviceRecognize.getCount());
                        }
                    }
                    voList.add(vo);
                }
            }
        }
        return voList;
    }

    public ImportantRecognizeVO importantPeopleRecognize(ImportantRecognizeDTO param) {
        ImportantRecognizeVO vo = new ImportantRecognizeVO();
        List<Long> communityIds = platformService.getCommunityIdsById(param.getRegionId());
        if (communityIds == null || communityIds.size() == 0) {
            log.info("Search community ids by region id is null, so return null");
            return null;
        }
        log.info("Search community ids by region id is:" + JacksonUtil.toJson(communityIds));
        ImportantRecognizeSearchParam search = new ImportantRecognizeSearchParam();
        search.setSearchType(param.getSearchType());
        search.setCommunityIds(communityIds);
        try {
            Timestamp startTime = new Timestamp(sdf.parse(param.getStartTime()).getTime());
            Timestamp endTime = new Timestamp(sdf.parse(param.getEndTime()).getTime());
            search.setStartTime(startTime);
            search.setEndTime(endTime);
        } catch (ParseException e) {
            log.error("Date parse error, because param time error, so return null");
            return null;
        }
        log.info("Search community important people recognize param:" + JacksonUtil.toJson(search));
        Page page = PageHelper.offsetPage(param.getStart(), param.getLimit());
        List<ImportantPeopleRecognize> recognizes = recognizeRecordMapper.getImportantRecognizeRecord(search);
        PageInfo info = new PageInfo(page.getResult());
        if (recognizes == null || recognizes.size() == 0) {
            log.info("Search community important people recognize is null, so return null");
            return null;
        }
        List<ImportantPeopleRecognizeVO> voList = new ArrayList<>();
        for (ImportantPeopleRecognize recognize : recognizes) {
            ImportantPeopleRecognizeVO importantPeopleRecognize = new ImportantPeopleRecognizeVO();
            importantPeopleRecognize.setId(recognize.getPeopleId());
            importantPeopleRecognize.setType(recognize.getType());
            importantPeopleRecognize.setName(recognize.getName());
            importantPeopleRecognize.setIdCard(recognize.getIdCard());
            importantPeopleRecognize.setLastTime(recognize.getLastTime() != null ? sdf.format(recognize.getLastTime()) : "");
            //当搜索类型不是0的时候为识别类型,识别类型数据库pictureid为空,所以需要手动查询一张图片出来
            if (param.getSearchType() == 0) {
                importantPeopleRecognize.setPeoplePictureId(recognize.getPictureId());
                importantPeopleRecognize.setPictureId(recognize.getPictureId());
            } else {
                Long picId = pictureMapper.getPictureIdByPeopleId(recognize.getPeopleId());
                importantPeopleRecognize.setPeoplePictureId(picId);
                importantPeopleRecognize.setPictureId(picId);
            }
            if (recognize.getType() == 1 || recognize.getType() == 3) {
                importantPeopleRecognize.setDeviceId(recognize.getDeviceId());
                importantPeopleRecognize.setDeviceName(platformService.getCameraDeviceName(recognize.getDeviceId()));
            } else if (recognize.getType() == 2) {
                importantPeopleRecognize.setDeviceId(platformService.getImsiDeviceId(recognize.getDeviceId()));
                importantPeopleRecognize.setDeviceName(platformService.getImsiDeviceName(recognize.getDeviceId()));
            }
            importantPeopleRecognize.setCaptureTime(sdf.format(recognize.getCaptureTime()));
            List<Car> cars = recognize.getCar();
            List<String> carList = new ArrayList<>();
            for (Car car : cars) {
                carList.add(car.getCar());
            }
            importantPeopleRecognize.setCar(carList);
            List<Imsi> imsis = recognize.getImac();
            List<String> imsiList = new ArrayList<>();
            for (Imsi imsi : imsis) {
                imsiList.add(ImsiUtil.toMac(imsi.getImsi()));
            }
            importantPeopleRecognize.setImac(imsiList);
            List<Flag> flags = recognize.getFlag();
            List<com.hzgc.service.people.param.Flag> flagList = new ArrayList<>();
            for (Flag flag : flags) {
                com.hzgc.service.people.param.Flag ff = new com.hzgc.service.people.param.Flag();
                ff.setId(flag.getFlagid());
                ff.setFlag(flag.getFlag());
                flagList.add(ff);
            }
            importantPeopleRecognize.setFlag(flagList);
            if (recognize.getType() == 1 || recognize.getType() == 3) {
                importantPeopleRecognize.setBurl(innerService.httpHostNameToIp(recognize.getBurl()).getHttp_ip());
                importantPeopleRecognize.setSurl(innerService.httpHostNameToIp(recognize.getSurl()).getHttp_ip());
            }
            importantPeopleRecognize.setSimilarity(recognize.getSimilarity());
            importantPeopleRecognize.setImsi(ImsiUtil.toMac(recognize.getImsi()));
            importantPeopleRecognize.setMac(recognize.getMac());
            importantPeopleRecognize.setPlate(recognize.getPlate());
            voList.add(importantPeopleRecognize);
        }
        switch (param.getSearchType()) {
            case 0:
                vo.setImportantPeopleCaptureList(voList);
                break;
            case 1:
                vo.setImportantPeopleRecognizeList(voList);
                break;
            case 2:
                vo.setImportantPeopleRecordList(voList);
                break;
            default:
                log.error("param search type error");
                return null;
        }
        vo.setTotalNum((int) info.getTotal());
        return vo;
    }

    public List<ImportantPeopleRecognizeHistoryVO> importantPeopleRecognizeHistory(Long regionId) {
        List<ImportantPeopleRecognizeHistoryVO> voList = new ArrayList<>();
        List<Long> communityIds = platformService.getCommunityIdsById(regionId);
        if (communityIds == null || communityIds.size() == 0) {
            log.info("Search community ids by region id is null, so return null");
            return null;
        }
        log.info("Search community ids by region id is:" + JacksonUtil.toJson(communityIds));
        List<ImportantPeopleRecognizeHistory> histories = recognizeRecordMapper.getImportantPeopleRecognizeHistory(communityIds);
        if (histories == null || histories.size() == 0) {
            log.info("Search community important people recognize history is null, so return null");
            return null;
        }
        for (ImportantPeopleRecognizeHistory history : histories){
            ImportantPeopleRecognizeHistoryVO vo = new ImportantPeopleRecognizeHistoryVO();
            vo.setPeopleId(history.getPeopleId());
            vo.setName(history.getName());
            vo.setIdcard(history.getIdcard());
            vo.setAge(history.getAge());
            vo.setSex(history.getSex());
            vo.setBirthplace(history.getBirthplace());
            vo.setAddress(history.getAddress());
            List<Phone> phones = history.getPhones();
            if (phones != null && phones.size() > 0){
                List<String> phoneList = new ArrayList<>();
                for (Phone phone : phones) {
                    phoneList.add(phone.getPhone());
                }
                vo.setPhone(phoneList);
            }
            List<Car> cars = history.getCars();
            if (cars != null && cars.size() > 0){
                List<String> carList = new ArrayList<>();
                for (Car car : cars) {
                    carList.add(car.getCar());
                }
                vo.setCar(carList);
            }
            List<Imsi> imsis = history.getImsis();
            if (imsis != null && imsis.size() > 0){
                List<String> imsiList = new ArrayList<>();
                for (Imsi imsi : imsis) {
                    imsiList.add(ImsiUtil.toMac(imsi.getImsi()));
                }
                vo.setMac(imsiList);
            }
            vo.setFlag(history.getType());
            //当type等于1的时候：为人脸识别类型；不等于1的时候，数据库pictureid为空,所以需要手动查询一张图片出来
            if (history.getType() == 1) {
                vo.setPictureId(history.getPictureId());
            } else {
                Long picId = pictureMapper.getPictureIdByPeopleId(history.getPeopleId());
                vo.setPictureId(picId);
            }
            vo.setImsi(history.getImsi());
            vo.setPlate(history.getPlate());
            vo.setBurl(innerService.httpHostNameToIp(history.getBurl()).getHttp_ip());
            vo.setSurl(innerService.httpHostNameToIp(history.getSurl()).getHttp_ip());
            vo.setTime(sdf.format(history.getCaptureTime()));
            vo.setCommunityId(history.getCommunity());
            vo.setCommunity(platformService.getCommunityName(history.getCommunity()));
            vo.setDeviceId(history.getDeviceId());
            if (history.getType() == 1 || history.getType() == 3){
                vo.setDeviceId(history.getDeviceId());
                vo.setDeviceName(platformService.getCameraDeviceName(history.getDeviceId()));
            }
            if (history.getType() == 2){
                vo.setDeviceId(platformService.getImsiDeviceId(history.getDeviceId()));
                vo.setDeviceName(platformService.getImsiDeviceName(history.getDeviceId()));
            }
            voList.add(vo);
        }
        return voList;
    }
}

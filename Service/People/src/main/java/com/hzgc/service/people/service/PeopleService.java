package com.hzgc.service.people.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hzgc.common.service.api.service.InnerService;
import com.hzgc.common.service.api.service.PlatformService;
import com.hzgc.common.service.peoman.SyncPeopleManager;
import com.hzgc.common.util.basic.ImsiUtil;
import com.hzgc.common.util.basic.UuidUtil;
import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.jniface.FaceAttribute;
import com.hzgc.jniface.FaceUtil;
import com.hzgc.jniface.PictureData;
import com.hzgc.service.community.dao.NewPeopleMapper;
import com.hzgc.service.community.dao.OutPeopleMapper;
import com.hzgc.service.community.dao.RecognizeRecordMapper;
import com.hzgc.service.people.dao.*;
import com.hzgc.service.people.fields.Flag;
import com.hzgc.service.people.model.*;
import com.hzgc.service.people.param.*;
import com.hzgc.service.people.util.IdCardUtil;
import com.hzgc.service.people.util.PeopleExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class PeopleService {
    @Autowired
    @SuppressWarnings("unused")
    private PeopleMapper peopleMapper;

    @Autowired
    @SuppressWarnings("unused")
    private CarMapper carMapper;

    @Autowired
    @SuppressWarnings("unused")
    private FlagMapper flagMapper;

    @Autowired
    @SuppressWarnings("unused")
    private HouseMapper houseMapper;

    @Autowired
    @SuppressWarnings("unused")
    private ImsiMapper imsiMapper;

    @Autowired
    @SuppressWarnings("unused")
    private ImeiMapper imeiMapper;

    @Autowired
    @SuppressWarnings("unused")
    private PhoneMapper phoneMapper;

    @Autowired
    @SuppressWarnings("unused")
    private PictureMapper pictureMapper;

    @Autowired
    @SuppressWarnings("unused")
    private PlatformService platformService;

    @Autowired
    @SuppressWarnings("unused")
    private InnerService innerService;

    @Autowired
    private NewPeopleMapper newPeopleMapper;

    @Autowired
    @SuppressWarnings("unused")
    private OutPeopleMapper outPeopleMapper;

    @Autowired
    @SuppressWarnings("unused")
    private RecognizeRecordMapper recognizeRecordMapper;

    @Autowired
    @SuppressWarnings("unused")
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${people.kafka.topic}")
    @NotNull
    @SuppressWarnings("unused")
    private String topic;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private void sendKafka(String key, Object data) {
        kafkaTemplate.send(topic, key, JacksonUtil.toJson(data));
    }

    public boolean CheckIdCard(String idCard) {
        People people = peopleMapper.searchPeopleByIdCard(idCard);
        return people != null;
    }

    @Transactional(rollbackFor = Exception.class)
    public String insertPeople(PeopleDTO peopleDTO) {
        People people = peopleDTO.peopleDTOShift_insert(peopleDTO);
        log.info("Start insert people info, param is:" + JacksonUtil.toJson(people));
        int insertStatus = peopleMapper.insertSelective(people);
        if (insertStatus != 1) {
            log.error("Insert people to t_people failed");
            throw new RuntimeException("添加人口信息失败");
        }
        log.info("Insert people to t_people successfully");
        if (peopleDTO.getIdCardPic() != null && peopleDTO.getIdCardPic().size() > 0) {
            for (String photo : peopleDTO.getIdCardPic()) {
                PictureWithBLOBs picture = new PictureWithBLOBs();
                picture.setPeopleid(people.getId());
                picture.setIdcardpic(FaceUtil.base64Str2BitFeature(photo));
                PictureData pictureData = innerService.faceFeautreCheck(photo);
                if (pictureData == null) {
                    log.error("Face feature extract is null");
                    throw new RuntimeException("照片特征提取失败");
                }
                FaceAttribute faceAttribute = pictureData.getFeature();
                if (faceAttribute == null || faceAttribute.getFeature() == null || faceAttribute.getBitFeature() == null) {
                    log.error("Face feature extract failed");
                    throw new RuntimeException("照片特征提取失败");
                }
                picture.setFeature(FaceUtil.floatFeature2Base64Str(faceAttribute.getFeature()));
                picture.setBitfeature(FaceUtil.bitFeautre2Base64Str(faceAttribute.getBitFeature()));
                int status = pictureMapper.insertSelective(picture);
                if (status != 1) {
                    log.error("Insert idCard picture to t_picture failed");
                    throw new RuntimeException("添加证件照片失败");
                }
            }
            log.info("Insert idCard picture to t_picture successfully");
        }
        if (peopleDTO.getCapturePic() != null && peopleDTO.getCapturePic().size() > 0) {
            for (String photo : peopleDTO.getCapturePic()) {
                PictureWithBLOBs picture = new PictureWithBLOBs();
                picture.setPeopleid(people.getId());
                picture.setCapturepic(FaceUtil.base64Str2BitFeature(photo));
                PictureData pictureData = innerService.faceFeautreCheck(photo);
                if (pictureData == null) {
                    log.error("Face feature extract is null");
                    throw new RuntimeException("照片特征提取失败");
                }
                FaceAttribute faceAttribute = pictureData.getFeature();
                if (faceAttribute == null || faceAttribute.getFeature() == null || faceAttribute.getBitFeature() == null) {
                    log.error("Face feature extract failed");
                    throw new RuntimeException("照片特征提取失败");
                }
                picture.setFeature(FaceUtil.floatFeature2Base64Str(faceAttribute.getFeature()));
                picture.setBitfeature(FaceUtil.bitFeautre2Base64Str(faceAttribute.getBitFeature()));
                int status = pictureMapper.insertSelective(picture);
                if (status != 1) {
                    log.info("Insert capture picture to t_picture failed");
                    throw new RuntimeException("添加实采照片失败");
                }
            }
            log.info("Insert capture picture to t_picture successfully");
        }
        if (peopleDTO.getFlagId() != null && peopleDTO.getFlagId().size() > 0) {
            for (Integer integer : peopleDTO.getFlagId()) {
                com.hzgc.service.people.model.Flag flag = new com.hzgc.service.people.model.Flag();
                flag.setPeopleid(people.getId());
                flag.setFlagid(integer);
                flag.setFlag(Flag.getFlag(integer));
                int status = flagMapper.insertSelective(flag);
                if (status != 1) {
                    log.error("Insert flag to t_flag failed");
                    throw new RuntimeException("添加标签信息失败");
                }
            }
            log.info("Insert flag to t_flag successfully");
        }
        if (peopleDTO.getImsi() != null && peopleDTO.getImsi().size() > 0) {
            for (String s : peopleDTO.getImsi()) {
                Imsi imsi = new Imsi();
                imsi.setPeopleid(people.getId());
                imsi.setImsi(s);
                int status = imsiMapper.insertSelective(imsi);
                if (status != 1) {
                    log.info("Insert imsi to t_imsi failed");
                    throw new RuntimeException("添加IMSI信息失败");
                }
            }
            log.info("Insert imsi to t_imsi successfully");
        }
        if (peopleDTO.getPhone() != null && peopleDTO.getPhone().size() > 0) {
            for (String s : peopleDTO.getPhone()) {
                Phone phone = new Phone();
                phone.setPeopleid(people.getId());
                phone.setPhone(s);
                int status = phoneMapper.insertSelective(phone);
                if (status != 1) {
                    log.info("Insert phone to t_phone failed");
                    throw new RuntimeException("添加联系方式失败");
                }
            }
            log.info("Insert phone to t_phone successfully");
        }
        if (peopleDTO.getHouse() != null && peopleDTO.getHouse().size() > 0) {
            for (String s : peopleDTO.getHouse()) {
                House house = new House();
                house.setPeopleid(people.getId());
                house.setHouse(s);
                int status = houseMapper.insertSelective(house);
                if (status != 1) {
                    log.info("Insert house to t_house failed");
                    throw new RuntimeException("添加房产信息失败");
                }
            }
            log.info("Insert house to t_house successfully");
        }
        if (peopleDTO.getCar() != null && peopleDTO.getCar().size() > 0) {
            for (String s : peopleDTO.getCar()) {
                Car car = new Car();
                car.setPeopleid(people.getId());
                car.setCar(s);
                int status = carMapper.insertSelective(car);
                if (status != 1) {
                    log.info("Insert car to t_car failed");
                    throw new RuntimeException("添加车辆信息失败");
                }
            }
            log.info("Insert car to t_car successfully");
        }
        log.info("Insert people info successfully");
        boolean b1 = peopleDTO.getIdCardPic() != null && peopleDTO.getIdCardPic().size() > 0;
        boolean b2 = peopleDTO.getCapturePic() != null && peopleDTO.getCapturePic().size() > 0;
        if (b1 || b2) {
            SyncPeopleManager manager = new SyncPeopleManager();
            manager.setType("2");
            manager.setPersonid(people.getId());
            this.sendKafka("ADD", manager);
        }
        return people.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer deletePeople(String id) {
        int status = peopleMapper.deleteByPrimaryKey(id);
        if (status != 1) {
            log.error("Delete info from t_people failed");
            throw new RuntimeException("删除人员信息失败");
        }
        phoneMapper.delete(id);
        carMapper.delete(id);
        houseMapper.delete(id);
        imsiMapper.delete(id);
        flagMapper.delete(id);
        pictureMapper.delete(id);
        newPeopleMapper.delete(id);
        outPeopleMapper.delete(id);
        recognizeRecordMapper.delete(id);
        imeiMapper.delete(id);
        SyncPeopleManager manager = new SyncPeopleManager();
        manager.setType("4");
        manager.setPersonid(id);
        this.sendKafka("DELETE", manager);
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer updatePeople(PeopleDTO peopleDTO) {
        People people = peopleDTO.peopleDTOShift_update(peopleDTO);
        log.info("Start update t_people, param is:" + JacksonUtil.toJson(people));
        int status_people_update = peopleMapper.updateByPrimaryKeySelective(people);
        if (status_people_update != 1) {
            log.error("Update people to t_people failed");
            throw new RuntimeException("修改人口信息失败");
        }
        log.info("Update people to t_people successfully");
        List<Long> t_flag_ids = flagMapper.selectIdByPeopleId(people.getId());
        if (t_flag_ids != null && t_flag_ids.size() > 0) {
            for (Long id : t_flag_ids) {
                int status = flagMapper.deleteByPrimaryKey(id);
                if (status != 1) {
                    log.error("Update people, but delete flag from t_flag failed, t_flag.id: " + id);
                    throw new RuntimeException("修改标签信息失败");
                }
            }
        }
        if (peopleDTO.getFlagId() != null && peopleDTO.getFlagId().size() > 0) {
            for (Integer integer : peopleDTO.getFlagId()) {
                com.hzgc.service.people.model.Flag flag = new com.hzgc.service.people.model.Flag();
                flag.setPeopleid(people.getId());
                flag.setFlagid(integer);
                flag.setFlag(Flag.getFlag(integer));
                int status = flagMapper.insertSelective(flag);
                if (status != 1) {
                    log.error("Update people, but insert flag to t_flag failed, flag:" + integer);
                    throw new RuntimeException("修改标签信息失败");
                }
            }
        }
        List<Long> t_imsi_ids = imsiMapper.selectIdByPeopleId(people.getId());
        for (Long id : t_imsi_ids) {
            int status = imsiMapper.deleteByPrimaryKey(id);
            if (status != 1) {
                log.error("Update people, but delete imsi from t_imsi failed, t_imsi.id: " + id);
                throw new RuntimeException("修改IMIS信息失败");
            }
        }
        if (peopleDTO.getImsi() != null && peopleDTO.getImsi().size() > 0) {
            for (String s : peopleDTO.getImsi()) {
                Imsi imsi = new Imsi();
                imsi.setPeopleid(people.getId());
                imsi.setImsi(s);
                int insertStatus = imsiMapper.insertSelective(imsi);
                if (insertStatus != 1) {
                    log.error("Update people, but insert imsi to t_imsi failed, imsi: " + s);
                    throw new RuntimeException("修改IMIS信息失败");
                }
            }
        }
        List<Long> t_phone_ids = phoneMapper.selectIdByPeopleId(people.getId());
        for (Long id : t_phone_ids) {
            int status = phoneMapper.deleteByPrimaryKey(id);
            if (status != 1) {
                log.error("Update people, but delete phone from t_phone failed, t_phone.id: " + id);
                throw new RuntimeException("修改联系方式失败");
            }
        }
        if (peopleDTO.getPhone() != null && peopleDTO.getPhone().size() > 0) {
            for (String s : peopleDTO.getPhone()) {
                Phone phone = new Phone();
                phone.setPeopleid(people.getId());
                phone.setPhone(s);
                int insertStatus = phoneMapper.insertSelective(phone);
                if (insertStatus != 1) {
                    log.info("Update people, but insert phone to t_phone failed, phone: " + s);
                    throw new RuntimeException("修改联系方式失败");
                }
            }
        }
        List<Long> t_house_ids = houseMapper.selectIdByPeopleId(people.getId());
        for (Long id : t_house_ids) {
            int status = houseMapper.deleteByPrimaryKey(id);
            if (status != 1) {
                log.info("Update people, but delete house from t_house failed, t_house.id: " + id);
                throw new RuntimeException("修改房产信息失败");
            }
        }
        if (peopleDTO.getHouse() != null && peopleDTO.getHouse().size() > 0) {
            for (String s : peopleDTO.getHouse()) {
                House house = new House();
                house.setPeopleid(people.getId());
                house.setHouse(s);
                int insertStatus = houseMapper.insertSelective(house);
                if (insertStatus != 1) {
                    log.error("Update people, but insert house to t_house failed, house: " + s);
                    throw new RuntimeException("修改房产信息失败");
                }
            }
        }
        List<Long> t_car_ids = carMapper.selectIdByPeopleId(people.getId());
        for (Long id : t_car_ids) {
            int status = carMapper.deleteByPrimaryKey(id);
            if (status != 1) {
                log.error("Update people, but delete car from t_car failed, t_car.id: " + id);
                throw new RuntimeException("修改车辆信息失败");
            }
        }
        if (peopleDTO.getCar() != null && peopleDTO.getCar().size() > 0) {
            for (String s : peopleDTO.getCar()) {
                Car car = new Car();
                car.setPeopleid(people.getId());
                car.setCar(s);
                int insertStatus = carMapper.insertSelective(car);
                if (insertStatus != 1) {
                    log.info("Update people, but insert car to t_car failed, car: " + s);
                    throw new RuntimeException("修改车辆信息失败");
                }
            }
        }
        log.info("Update people info successfully");
        return 1;
    }

    public int insertMentalPatient(MentalPatientDTO mentalPatientDTO) {
        Imei imei = mentalPatientDTO.mentalPatientDTOShift_insert(mentalPatientDTO);
        return imeiMapper.insertSelective(imei);
    }

    public int updateMentalPatient(MentalPatientDTO mentalPatientDTO) {
        Imei imei = mentalPatientDTO.mentalPatientDTOShift_insert(mentalPatientDTO);
        return imeiMapper.updateByPrimaryKeySelective(imei);
    }

    public ImeiVO selectMentalPatientByPeopleId(String peopleId) {
        ImeiVO vo = new ImeiVO();
        Imei imei = imeiMapper.selectByPeopleId(peopleId);
        vo.setId(imei.getId());
        vo.setPeopleId(imei.getPeopleid());
        vo.setImei(imei.getImei());
        vo.setGuardianName(imei.getGuardianname());
        vo.setGuardianPhone(imei.getGuardianphone());
        vo.setCadresName(imei.getCadresname());
        vo.setCadresPhone(imei.getCadresphone());
        vo.setPoliceName(imei.getPolicename());
        vo.setPolicePhone(imei.getPolicephone());
        return vo;
    }

    /**
     * 添加人口库照片信息
     *
     * @param dto 添加照片信息
     * @return 成功状态 1：修改成功, 0：修改失败
     */
    public int insertPicture(PictureDTO dto) {
        PictureWithBLOBs picture = new PictureWithBLOBs();
        picture.setPeopleid(dto.getPeopleId());
        byte[] bytes = FaceUtil.base64Str2BitFeature(dto.getPicture());
        if (dto.getType() == 0) {
            picture.setIdcardpic(bytes);
        }
        if (dto.getType() == 1) {
            picture.setCapturepic(bytes);
        }
        PictureData pictureData = innerService.faceFeautreCheck(dto.getPicture());
        if (pictureData == null) {
            log.error("Face feature extract is null");
            return 0;
        }
        FaceAttribute faceAttribute = pictureData.getFeature();
        if (faceAttribute == null || faceAttribute.getFeature() == null || faceAttribute.getBitFeature() == null) {
            log.error("Face feature extract failed");
            return 0;
        }
        picture.setFeature(FaceUtil.floatFeature2Base64Str(faceAttribute.getFeature()));
        picture.setBitfeature(FaceUtil.bitFeautre2Base64Str(faceAttribute.getBitFeature()));
        int status = pictureMapper.insertSelective(picture);
        if (status != 1) {
            log.info("Insert picture to t_picture failed");
            return 0;
        }
        SyncPeopleManager manager = new SyncPeopleManager();
        manager.setType("2");
        manager.setPersonid(dto.getPeopleId());
        this.sendKafka("ADD", manager);
        return 1;
    }

    /**
     * 修改人口库照片信息
     *
     * @param dto 修改照片信息
     * @return 成功状态 1：修改成功, 0：修改失败
     */
    public int updatePicture(PictureDTO dto) {
        PictureWithBLOBs picture = new PictureWithBLOBs();
        picture.setId(dto.getPictureId());
        picture.setPeopleid(dto.getPeopleId());
        byte[] bytes = FaceUtil.base64Str2BitFeature(dto.getPicture());
        if (dto.getType() == 0) {
            picture.setIdcardpic(bytes);
        }
        if (dto.getType() == 1) {
            picture.setCapturepic(bytes);
        }
        PictureData pictureData = innerService.faceFeautreCheck(dto.getPicture());
        if (pictureData == null) {
            log.error("Face feature extract is null");
            return 0;
        }
        FaceAttribute faceAttribute = pictureData.getFeature();
        if (faceAttribute == null || faceAttribute.getFeature() == null || faceAttribute.getBitFeature() == null) {
            log.error("Face feature extract failed");
            return 0;
        }
        picture.setFeature(FaceUtil.floatFeature2Base64Str(faceAttribute.getFeature()));
        picture.setBitfeature(FaceUtil.bitFeautre2Base64Str(faceAttribute.getBitFeature()));
        int status = pictureMapper.updateByPrimaryKeyWithBLOBs(picture);
        if (status != 1) {
            log.info("Update picture to t_picture failed");
            return 0;
        }
        SyncPeopleManager manager = new SyncPeopleManager();
        manager.setType("3");
        manager.setPersonid(dto.getPeopleId());
        this.sendKafka("UPDATE", manager);
        return 1;
    }

    /**
     * 删除人口库照片信息
     *
     * @param dto 删除照片信息
     * @return 成功状态 1：修改成功, 0：修改失败
     */
    public int deletePicture(PictureDTO dto) {
        int status = pictureMapper.deleteByPrimaryKey(dto.getPictureId());
        if (status != 1) {
            log.info("Delete picture to t_picture failed, picture id:" + dto.getPictureId());
            return 0;
        }
        SyncPeopleManager manager = new SyncPeopleManager();
        manager.setType("4");
        manager.setPersonid(dto.getPeopleId());
        this.sendKafka("DELETE", manager);
        return 1;
    }

    /**
     * 根据照片ID查询照片
     *
     * @param pictureId 照片ID
     * @return byte[] 照片
     */
    public byte[] searchPictureByPicId(Long pictureId) {
        PictureWithBLOBs picture = pictureMapper.selectPictureById(pictureId);
        if (picture != null) {
            if (picture.getIdcardpic() != null) {
                return picture.getIdcardpic();
            } else {
                return picture.getCapturepic();
            }
        }
        return null;
    }

    public PictureVO searchPictureByPeopleId(String peopleId) {
        PictureVO pictureVO = new PictureVO();
        List<PictureWithBLOBs> pictures = pictureMapper.selectPictureByPeopleId(peopleId);
        if (pictures != null && pictures.size() > 0) {
            List<Long> pictureIds = new ArrayList<>();
            List<Long> idcardPictureIds = new ArrayList<>();
            List<Long> capturePictureIds = new ArrayList<>();
            for (PictureWithBLOBs picture : pictures) {
                if (picture != null) {
                    pictureIds.add(picture.getId());
                    byte[] idcardPic = picture.getIdcardpic();
                    if (idcardPic != null && idcardPic.length > 0) {
                        idcardPictureIds.add(picture.getId());
                    } else {
                        capturePictureIds.add(picture.getId());
                    }
                }
            }
            pictureVO.setPictureIds(pictureIds);
            pictureVO.setIdcardPics(idcardPictureIds);
            pictureVO.setCapturePics(capturePictureIds);
        }
        return pictureVO;
    }

    /**
     * 根据ID查询人口信息
     *
     * @param peopleId 人员全局ID
     * @return peopleVO
     */
    public PeopleVO selectByPeopleId(String peopleId) {
        People people = peopleMapper.selectByPrimaryKey(peopleId);
        PeopleVO peopleVO = new PeopleVO();
        ImeiVO imeiVO = new ImeiVO();
        if (people != null) {
            peopleVO.setId(people.getId());
            peopleVO.setName(people.getName());
            peopleVO.setIdCard(people.getIdcard());
            peopleVO.setRegionId(people.getRegion());
            peopleVO.setRegion(platformService.getRegionName(people.getRegion()));
            peopleVO.setHousehold(people.getHousehold());
            peopleVO.setAddress(people.getAddress());
            peopleVO.setSex(people.getSex());
            peopleVO.setAge(IdCardUtil.getAge(people.getIdcard()));
            peopleVO.setBirthday(IdCardUtil.getBirthday(people.getIdcard()));
            peopleVO.setPolitic(people.getPolitic());
            peopleVO.setEduLevel(people.getEdulevel());
            peopleVO.setJob(people.getJob());
            peopleVO.setBirthplace(people.getBirthplace());
            if (people.getImei() != null){
                peopleVO.setImeiVO(imeiVO.imeiToImeiVO(people));
            }
            if (people.getCommunity() != null) {
                peopleVO.setCommunity(people.getCommunity());
                peopleVO.setCommunityName(platformService.getCommunityName(people.getCommunity()));
            }
            if (people.getLasttime() != null) {
                peopleVO.setLastTime(sdf.format(people.getLasttime()));
            }
            if (people.getCreatetime() != null) {
                peopleVO.setCreateTime(sdf.format(people.getCreatetime()));
            }
            if (people.getUpdatetime() != null) {
                peopleVO.setUpdateTime(sdf.format(people.getUpdatetime()));
            }
            if (people.getFlag() != null && people.getFlag().size() > 0) {
                List<com.hzgc.service.people.model.Flag> flags = people.getFlag();
                List<com.hzgc.service.people.param.Flag> flagList = new ArrayList<>();
                for (com.hzgc.service.people.model.Flag flag : flags) {
                    com.hzgc.service.people.param.Flag flagVO = new com.hzgc.service.people.param.Flag();
                    flagVO.setId(flag.getFlagid());
                    flagVO.setFlag(flag.getFlag());
                    flagList.add(flagVO);
                }
                peopleVO.setFlag(flagList);
            }
            if (people.getImsi() != null && people.getImsi().size() > 0) {
                List<Imsi> imsis = people.getImsi();
                List<String> imsiList = new ArrayList<>();
                List<String> imacList = new ArrayList<>();
                for (Imsi imsi : imsis) {
                    imsiList.add(imsi.getImsi());
                    imacList.add(ImsiUtil.toMac(imsi.getImsi()));
                }
                peopleVO.setImsi(imsiList);
                peopleVO.setImac(imacList);
            }
            if (people.getPhone() != null && people.getPhone().size() > 0) {
                List<Phone> phones = people.getPhone();
                List<String> phoneList = new ArrayList<>();
                for (Phone phone : phones) {
                    phoneList.add(phone.getPhone());
                }
                peopleVO.setPhone(phoneList);
            }
            if (people.getHouse() != null && people.getHouse().size() > 0) {
                List<House> houses = people.getHouse();
                List<String> houseList = new ArrayList<>();
                for (House house : houses) {
                    houseList.add(house.getHouse());
                }
                peopleVO.setHouse(houseList);
            }
            if (people.getCar() != null && people.getCar().size() > 0) {
                List<Car> cars = people.getCar();
                List<String> carList = new ArrayList<>();
                for (Car car : cars) {
                    carList.add(car.getCar());
                }
                peopleVO.setCar(carList);
            }
            List<PictureWithBLOBs> pictures = people.getPicture();
            if (pictures != null && pictures.size() > 0) {
                peopleVO.setPictureId(pictures.get(0).getId());
                List<Long> idcardPictureIds = new ArrayList<>();
                List<Long> capturePictureIds = new ArrayList<>();
                for (PictureWithBLOBs picture : pictures) {
                    byte[] idcardPic = picture.getIdcardpic();
                    if (idcardPic != null && idcardPic.length > 0) {
                        idcardPictureIds.add(picture.getId());
                    } else {
                        capturePictureIds.add(picture.getId());
                    }
                }
                peopleVO.setIdcardPictureIds(idcardPictureIds);
                peopleVO.setCapturePictureIds(capturePictureIds);
            }
        }
        return peopleVO;
    }

    /**
     * 根据精神病手环ID(IMEI)查询人口信息
     *
     * @param imeiId 精神病手环ID
     * @return peopleVO
     */
    public PeopleVO selectByImeiId(String imeiId) {
        PeopleVO peopleVO = new PeopleVO();
        String peopleId = imeiMapper.selectPeopleIdByImei(imeiId);
        if (StringUtils.isNotBlank(peopleId)){
            peopleVO = this.selectByPeopleId(peopleId);
        }
        return peopleVO;
    }

    /**
     * 查询人员对象
     *
     * @param param 查询过滤字段封装
     * @return SearchPeopleVO 查询返回参数封装
     */
    public SearchPeopleVO searchPeople(SearchPeopleDTO param) {
        SearchPeopleVO vo = new SearchPeopleVO();
        List<PeopleVO> list = new ArrayList<>();
        Page page = PageHelper.offsetPage(param.getStart(), param.getLimit(), true);
        List<People> peoples = peopleMapper.searchPeople(param);
        PageInfo info = new PageInfo(page.getResult());
        int total = (int) info.getTotal();
        vo.setTotal(total);
        if (peoples != null && peoples.size() > 0) {
            for (People people : peoples) {
                PeopleVO peopleVO = new PeopleVO();
                ImeiVO imeiVO = new ImeiVO();
                if (people != null) {
                    peopleVO.setId(people.getId());
                    peopleVO.setName(people.getName());
                    peopleVO.setIdCard(people.getIdcard());
                    peopleVO.setRegionId(people.getRegion());
                    peopleVO.setRegion(platformService.getRegionName(people.getRegion()));
                    peopleVO.setHousehold(people.getHousehold());
                    peopleVO.setAddress(people.getAddress());
                    peopleVO.setSex(people.getSex());
                    peopleVO.setAge(IdCardUtil.getAge(people.getIdcard()));
                    peopleVO.setBirthday(IdCardUtil.getBirthday(people.getIdcard()));
                    peopleVO.setPolitic(people.getPolitic());
                    peopleVO.setEduLevel(people.getEdulevel());
                    peopleVO.setJob(people.getJob());
                    peopleVO.setBirthplace(people.getBirthplace());
                    if (people.getImei() != null){
                        peopleVO.setImeiVO(imeiVO.imeiToImeiVO(people));
                    }
                    if (people.getCommunity() != null) {
                        peopleVO.setCommunity(people.getCommunity());
                        peopleVO.setCommunityName(platformService.getCommunityName(people.getCommunity()));
                    }
                    if (people.getLasttime() != null) {
                        peopleVO.setLastTime(sdf.format(people.getLasttime()));
                    }
                    if (people.getCreatetime() != null) {
                        peopleVO.setCreateTime(sdf.format(people.getCreatetime()));
                    }
                    if (people.getUpdatetime() != null) {
                        peopleVO.setUpdateTime(sdf.format(people.getUpdatetime()));
                    }
                    if (people.getFlag() != null && people.getFlag().size() > 0) {
                        List<com.hzgc.service.people.model.Flag> flags = people.getFlag();
                        List<com.hzgc.service.people.param.Flag> flagList = new ArrayList<>();
                        for (com.hzgc.service.people.model.Flag flag : flags) {
                            com.hzgc.service.people.param.Flag flagVO = new com.hzgc.service.people.param.Flag();
                            flagVO.setId(flag.getFlagid());
                            flagVO.setFlag(flag.getFlag());
                            flagList.add(flagVO);
                        }
                        peopleVO.setFlag(flagList);
                    }
                    if (people.getPicture() != null && people.getPicture().size() > 0) {
                        PictureWithBLOBs picture = people.getPicture().get(0);
                        peopleVO.setPictureId(picture.getId());
                    }
                    list.add(peopleVO);
                }
            }
        }
        vo.setPeopleVOList(list);
        return vo;
    }

    public List<Long> searchCommunityIdsById(Long id) {
        List<Long> communityIds = platformService.getCommunityIdsById(id);
        log.info("Search platform service, community id list:" + JacksonUtil.toJson(communityIds));
        List<Long> list = new ArrayList<>();
        if (communityIds != null && communityIds.size() > 0) {
            list = peopleMapper.getCommunityIdsById(communityIds);
        }
        return list;
    }

    public PeopleVO searchPeopleByIdCard(String idCard) {
        People people = peopleMapper.selectByIdCard(idCard);
        PeopleVO peopleVO = new PeopleVO();
        if (people != null) {
            peopleVO.setId(people.getId());
            peopleVO.setName(people.getName());
            peopleVO.setIdCard(people.getIdcard());
            peopleVO.setRegionId(people.getRegion());
            peopleVO.setAddress(people.getAddress());
            peopleVO.setSex(people.getSex());
            peopleVO.setAge(IdCardUtil.getAge(people.getIdcard()));
            peopleVO.setBirthday(IdCardUtil.getBirthday(people.getIdcard()));
            peopleVO.setPolitic(people.getPolitic());
            peopleVO.setEduLevel(people.getEdulevel());
            peopleVO.setJob(people.getJob());
            peopleVO.setBirthplace(people.getBirthplace());
            peopleVO.setCommunity(people.getCommunity());
            if (people.getPhone() != null && people.getPhone().size() > 0) {
                List<String> phoneList = new ArrayList<>();
                for (Phone phone : people.getPhone()) {
                    phoneList.add(phone.getPhone());
                }
                peopleVO.setPhone(phoneList);
            }
        }
        return peopleVO;
    }

    public Integer excelImport(MultipartFile file) {
        PeopleExcelUtils excelUtils = new PeopleExcelUtils(file);
        Map<Integer, Map<Integer, Object>> excelMap = null;
        try {
            excelMap = excelUtils.readExcelContent();
        } catch (Exception e) {
            log.error("Import excel data failed, because read excel error");
            e.printStackTrace();
        }
        if (excelMap == null || excelMap.size() == 0) {
            return 0;
        }

        List<PeopleDTO> peopleDTOList = new ArrayList<>();
        for (int i = 1; i <= excelMap.size(); i++) {
            Map<String, Long> regionMap = platformService.getAllRegionId();
            Set<String> regionKeys = regionMap.keySet();
            List<String> regionNames = new ArrayList<>(regionKeys);
            Map<String, Long> communityMap = platformService.getAllCommunityId();
            Set<String> communityKeys = communityMap.keySet();
            List<String> communityNames = new ArrayList<>(communityKeys);
            Map<Integer, Object> map = excelMap.get(i);
            PeopleDTO peopleDTO = new PeopleDTO();
            if (map.get(0) != null && !"".equals(map.get(0))) {
                peopleDTO.setName(String.valueOf(map.get(0)));
            } else {
                log.error("Import excel data failed, because name is error, please check line: " + i);
                return 0;
            }
            if (map.get(1) != null && !"".equals(map.get(1)) &&
                    PeopleExcelUtils.isIdCard(String.valueOf(map.get(1)))) {
                peopleDTO.setIdCard(String.valueOf(map.get(1)));
            } else {
                log.error("Import excel data failed, because idCard is error, please check line: " + i);
                return 0;
            }
            if (map.get(2) != null && !"".equals(map.get(2))
                    && regionNames.contains(String.valueOf(map.get(2)))) {
                peopleDTO.setRegion(regionMap.get(String.valueOf(map.get(2))));
            } else {
                log.error("Import excel data failed, because region is error, please check line: " + i);
                return 0;
            }
            if (map.get(3) != null && !"".equals(map.get(3))) {
                if (communityNames.contains(String.valueOf(map.get(3)))) {
                    peopleDTO.setCommunity(communityMap.get(String.valueOf(map.get(3))));
                } else {
                    log.error("Import excel data failed, because community is error, please check line: " + i);
                    return 0;
                }
            }
            if (map.get(4) != null && !"".equals(map.get(4))) {
                peopleDTO.setSex(String.valueOf(map.get(4)));
            }
            if (map.get(5) != null && !"".equals(map.get(5))) {
                peopleDTO.setJob(String.valueOf(map.get(5)));
            }
            if (map.get(6) != null && !"".equals(map.get(6))) {
                peopleDTO.setBirthday(String.valueOf(map.get(6)));
            }
            if (map.get(7) != null && !"".equals(map.get(7))) {
                peopleDTO.setAddress(String.valueOf(map.get(7)));
            }
            if (map.get(8) != null && !"".equals(map.get(8))) {
                peopleDTO.setBirthplace(String.valueOf(map.get(8)));
            }
            if (map.get(9) != null && !"".equals(map.get(9))) {
                peopleDTO.setPolitic(String.valueOf(map.get(9)));
            }
            if (map.get(10) != null && !"".equals(map.get(10))) {
                peopleDTO.setEduLevel(String.valueOf(10));
            }

            peopleDTOList.add(peopleDTO);
        }
        log.info("Excel data conversion is completed, start insert into t_people table");
        Integer status = this.excelImport(peopleDTOList);
        if (status != 1) {
            log.error("Import excel data failed, because insert into t_people table failed");
            return 0;
        }
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    private Integer excelImport(List<PeopleDTO> peopleDTOList) {
        for (PeopleDTO peopleDTO : peopleDTOList) {
            peopleDTO.setId(UuidUtil.getUuid());
            String id = this.insertPeople(peopleDTO);
            if (StringUtils.isBlank(id)) {
                throw new RuntimeException("Insert into t_people table failed");
            }
        }
        return 1;
    }
}

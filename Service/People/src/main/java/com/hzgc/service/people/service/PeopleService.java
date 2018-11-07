package com.hzgc.service.people.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.hzgc.common.service.api.service.InnerService;
import com.hzgc.common.service.api.service.PlatformService;
import com.hzgc.common.service.peoman.SyncPeopleManager;
import com.hzgc.common.util.basic.UuidUtil;
import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.jniface.FaceAttribute;
import com.hzgc.jniface.FaceUtil;
import com.hzgc.service.people.dao.*;
import com.hzgc.service.people.fields.Flag;
import com.hzgc.service.people.model.*;
import com.hzgc.service.people.param.*;
import com.hzgc.service.people.util.PeopleExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    @SuppressWarnings("unused")
    //Spring-kafka-template
    private KafkaTemplate<String, String> kafkaTemplate;

    private final static String TOPIC = "PeoMan-Inner";

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private final static String IDCARD_PIC = "idcardpic";

    private final static String CAPTURE_PIC = "capturepic";

    private void sendKafka(String key, Object data) {
        kafkaTemplate.send(TOPIC, key, JacksonUtil.toJson(data));
    }

    public boolean CheckIdCard(String idCard) {
        People people = peopleMapper.searchPeopleByIdCard(idCard);
        return people != null;
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnMessage insertPeople(PeopleDTO peopleDTO) {
        People people = peopleDTO.peopleDTOShift_insert(peopleDTO);
        log.info("Start Insert people info, param is:" + JacksonUtil.toJson(people));
        Integer status = people_insert(people);
        if (status != 1) {
            log.info("Insert people to t_people failed");
            ReturnMessage message = new ReturnMessage();
            message.setStatus(0);
            message.setMessage("添加人口信息失败");
            return message;
        }
        log.info("Insert people to t_people successfully");
        if (peopleDTO.getFlagId() != null || peopleDTO.getIdCardPic() != null || peopleDTO.getCapturePic() != null ||
                peopleDTO.getImsi() != null || peopleDTO.getPhone() != null || peopleDTO.getHouse() != null ||
                peopleDTO.getCar() != null) {
            if (peopleDTO.getFlagId() != null && peopleDTO.getFlagId().size() > 0) {
                Integer insertStatus = people_flag_insert(people.getId(), peopleDTO.getFlagId());
                if (insertStatus == 1) {
                    log.info("Insert flag to t_flag successfully");
                } else {
                    log.info("Insert flag to t_flag failed");
                    ReturnMessage message = new ReturnMessage();
                    message.setStatus(0);
                    message.setMessage("添加人口标签表失败");
                    return message;
                }
            }
            if (peopleDTO.getIdCardPic() != null && peopleDTO.getIdCardPic().size() > 0) {
                Integer insertStatus = people_picture_insert(people.getId(), PeopleService.IDCARD_PIC,
                        peopleDTO.getIdCardPic());
                if (insertStatus == 1) {
                    log.info("Insert idCard pic to t_picture successfully");
                } else {
                    log.info("Insert idCard pic to t_picture failed");
                    ReturnMessage message = new ReturnMessage();
                    message.setStatus(0);
                    message.setMessage("添加人口证件照片表失败");
                    return message;
                }
            }
            if (peopleDTO.getCapturePic() != null && peopleDTO.getCapturePic().size() > 0) {
                Integer insertStatus = people_picture_insert(people.getId(), PeopleService.CAPTURE_PIC,
                        peopleDTO.getCapturePic());
                if (insertStatus == 1) {
                    log.info("Insert capture pic to t_picture successfully");
                } else {
                    log.info("Insert capture pic to t_picture failed");
                    ReturnMessage message = new ReturnMessage();
                    message.setStatus(0);
                    message.setMessage("添加人口实采照片表失败");
                    return message;
                }
            }
            if (peopleDTO.getImsi() != null && peopleDTO.getImsi().size() > 0) {
                Integer insertStatus = people_imsi_insert(people.getId(), peopleDTO.getImsi());
                if (insertStatus == 1) {
                    log.info("Insert imsi to t_imsi successfully");
                } else {
                    log.info("Insert imsi to t_imsi failed");
                    ReturnMessage message = new ReturnMessage();
                    message.setStatus(0);
                    message.setMessage("添加人口imsi表失败");
                    return message;
                }
            }
            if (peopleDTO.getPhone() != null && peopleDTO.getPhone().size() > 0) {
                Integer insertStatus = people_phone_insert(people.getId(), peopleDTO.getPhone());
                if (insertStatus == 1) {
                    log.info("Insert phone to t_phone successfully");
                } else {
                    log.info("Insert phone to t_phone failed");
                    ReturnMessage message = new ReturnMessage();
                    message.setStatus(0);
                    message.setMessage("添加人口联系方式表失败");
                    return message;
                }
            }
            if (peopleDTO.getHouse() != null && peopleDTO.getHouse().size() > 0) {
                Integer insertStatus = people_house_insert(people.getId(), peopleDTO.getHouse());
                if (insertStatus == 1) {
                    log.info("Insert house to t_house successfully");
                } else {
                    log.info("Insert house to t_house failed");
                    ReturnMessage message = new ReturnMessage();
                    message.setStatus(0);
                    message.setMessage("添加人口房产信息表失败");
                    return message;
                }
            }
            if (peopleDTO.getCar() != null && peopleDTO.getCar().size() > 0) {
                Integer insertStatus = people_car_insert(people.getId(), peopleDTO.getCar());
                if (insertStatus == 1) {
                    log.info("Insert car to t_car successfully");
                } else {
                    log.info("Insert car to t_car failed");
                    ReturnMessage message = new ReturnMessage();
                    message.setStatus(0);
                    message.setMessage("添加人口车辆信息表失败");
                    return message;
                }
            }
        }
        log.info("Insert people info successfully");
        SyncPeopleManager manager = new SyncPeopleManager();
        manager.setType("2");
        manager.setPersonid(people.getId());
        this.sendKafka("ADD", manager);
        ReturnMessage message = new ReturnMessage();
        message.setStatus(1);
        message.setMessage("添加成功");
        return message;
    }

    @Transactional(rollbackFor = Exception.class)
    public ReturnMessage updatePeople(PeopleDTO peopleDTO) {
        People people = peopleDTO.peopleDTOShift_update(peopleDTO);
        log.info("Start update t_people, param is:" + JacksonUtil.toJson(people));
        int status_people_update = peopleMapper.updateByPrimaryKeySelective(people);
        if (status_people_update != 1) {
            log.info("Update t_people failed");
            ReturnMessage message = new ReturnMessage();
            message.setStatus(0);
            message.setMessage("修改人口失败");
            return message;
        }
        log.info("Update people to t_people successfully");
        List<Long> t_flag_ids = flagMapper.selectIdByPeopleId(people.getId());
        if (t_flag_ids != null && t_flag_ids.size() > 0) {
            for (Long id : t_flag_ids) {
                int status = flagMapper.deleteByPrimaryKey(id);
                if (status != 1) {
                    log.error("Update people, but delete flag from t_flag failed, t_flag.id: " + id);
                    ReturnMessage message = new ReturnMessage();
                    message.setStatus(0);
                    message.setMessage("修改人口标签表失败");
                    return message;
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
                    ReturnMessage message = new ReturnMessage();
                    message.setStatus(0);
                    message.setMessage("修改人口照片表失败");
                    return message;
                }
            }
        }
        List<Long> t_imsi_ids = imsiMapper.selectIdByPeopleId(people.getId());
        for (Long id : t_imsi_ids) {
            int status = imsiMapper.deleteByPrimaryKey(id);
            if (status != 1) {
                log.error("Update people, but delete imsi from t_imsi failed, t_imsi.id: " + id);
                ReturnMessage message = new ReturnMessage();
                message.setStatus(0);
                message.setMessage("修改人口标签表失败");
                return message;
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
                    ReturnMessage message = new ReturnMessage();
                    message.setStatus(0);
                    message.setMessage("修改人口标签表失败");
                    return message;
                }
            }

        }
        List<Long> t_phone_ids = phoneMapper.selectIdByPeopleId(people.getId());
        for (Long id : t_phone_ids) {
            int status = phoneMapper.deleteByPrimaryKey(id);
            if (status != 1) {
                log.error("Update people, but delete phone from t_phone failed, t_phone.id: " + id);
                ReturnMessage message = new ReturnMessage();
                message.setStatus(0);
                message.setMessage("修改人口联系方式表失败");
                return message;
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
                    ReturnMessage message = new ReturnMessage();
                    message.setStatus(0);
                    message.setMessage("修改人口联系方式表失败");
                    return message;
                }
            }
        }
        List<Long> t_house_ids = houseMapper.selectIdByPeopleId(people.getId());
        for (Long id : t_house_ids) {
            int status = houseMapper.deleteByPrimaryKey(id);
            if (status != 1) {
                log.info("Update people, but delete house from t_house failed, t_house.id: " + id);
                ReturnMessage message = new ReturnMessage();
                message.setStatus(0);
                message.setMessage("修改人口房产信息表失败");
                return message;
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
                    ReturnMessage message = new ReturnMessage();
                    message.setStatus(0);
                    message.setMessage("修改人口房产信息表失败");
                    return message;
                }
            }
        }
        List<Long> t_car_ids = carMapper.selectIdByPeopleId(people.getId());
        for (Long id : t_car_ids) {
            int status = carMapper.deleteByPrimaryKey(id);
            if (status != 1) {
                log.error("Update people, but delete car from t_car failed, t_car.id: " + id);
                ReturnMessage message = new ReturnMessage();
                message.setStatus(0);
                message.setMessage("修改人口车辆信息表失败");
                return message;
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
                    ReturnMessage message = new ReturnMessage();
                    message.setStatus(0);
                    message.setMessage("修改人口车辆信息表失败");
                    return message;
                }
            }
        }
        List<Long> t_picture_ids = pictureMapper.selectIdByPeopleId(people.getId());
        for (Long id : t_picture_ids) {
            int status = pictureMapper.deleteByPrimaryKey(id);
            if (status != 1) {
                log.error("Update people, but delete picture from t_picture failed, t_picture.id: " + id);
                ReturnMessage message = new ReturnMessage();
                message.setStatus(0);
                message.setMessage("修改人口照片表失败");
                return message;
            }
        }
        if (peopleDTO.getIdCardPic() != null && peopleDTO.getIdCardPic().size() > 0) {
            for (String photo : peopleDTO.getIdCardPic()) {
                PictureWithBLOBs picture = new PictureWithBLOBs();
                picture.setPeopleid(people.getId());
                picture.setIdcardpic(FaceUtil.base64Str2BitFeature(photo));
                FaceAttribute faceAttribute = innerService.faceFeautreCheck(photo).getFeature();
                if (faceAttribute == null || faceAttribute.getFeature() == null || faceAttribute.getBitFeature() == null) {
                    log.error("Update people, but face feature extract failed, insert idCard picture to t_picture failed");
                    throw new RuntimeException("Face feature extract failed, insert idCard picture to t_picture failed");
                }
                picture.setFeature(FaceUtil.floatFeature2Base64Str(faceAttribute.getFeature()));
                picture.setBitfeature(FaceUtil.bitFeautre2Base64Str(faceAttribute.getBitFeature()));
                int status = pictureMapper.insertSelective(picture);
                if (status != 1) {
                    log.error("Update people, but insert idCard picture to t_picture failed");
                    ReturnMessage message = new ReturnMessage();
                    message.setStatus(0);
                    message.setMessage("修改人口照片表失败");
                    return message;
                }
            }
        }
        if (peopleDTO.getCapturePic() != null && peopleDTO.getCapturePic().size() > 0) {
            for (String photo : peopleDTO.getCapturePic()) {
                PictureWithBLOBs picture = new PictureWithBLOBs();
                picture.setPeopleid(people.getId());
                picture.setIdcardpic(FaceUtil.base64Str2BitFeature(photo));
                FaceAttribute faceAttribute = innerService.faceFeautreCheck(photo).getFeature();
                if (faceAttribute == null || faceAttribute.getFeature() == null || faceAttribute.getBitFeature() == null) {
                    log.error("Update people, but face feature extract failed, insert capture picture to t_picture failed");
                    throw new RuntimeException("Face feature extract failed, insert capture picture to t_picture failed");
                }
                picture.setFeature(FaceUtil.floatFeature2Base64Str(faceAttribute.getFeature()));
                picture.setBitfeature(FaceUtil.bitFeautre2Base64Str(faceAttribute.getBitFeature()));
                int status = pictureMapper.insertSelective(picture);
                if (status != 1) {
                    log.error("Update people, but insert capture picture to t_picture failed");
                    ReturnMessage message = new ReturnMessage();
                    message.setStatus(0);
                    message.setMessage("修改人口照片表失败");
                    return message;
                }
            }
        }
        log.info("Update people info successfully");
        SyncPeopleManager manager = new SyncPeopleManager();
        manager.setType("3");
        manager.setPersonid(people.getId());
        this.sendKafka("UPDATE", manager);
        ReturnMessage message = new ReturnMessage();
        message.setStatus(1);
        message.setMessage("修改成功");
        return message;
    }

    private Integer people_insert(People people) {
        return peopleMapper.insertSelective(people);
    }

    private Integer people_flag_insert(String peopleId, List<Integer> flags) {
        for (Integer integer : flags) {
            com.hzgc.service.people.model.Flag flag = new com.hzgc.service.people.model.Flag();
            flag.setPeopleid(peopleId);
            flag.setFlagid(integer);
            flag.setFlag(Flag.getFlag(integer));
            int status = flagMapper.insertSelective(flag);
            if (status != 1) {
                log.info("Insert people, but insert t_flag failed");
                return 0;
            }
        }
        return 1;
    }

    private Integer people_picture_insert(String peopleId, String picType, List<String> pics) {
        for (String photo : pics) {
            PictureWithBLOBs picture = new PictureWithBLOBs();
            picture.setPeopleid(peopleId);
            byte[] bytes = FaceUtil.base64Str2BitFeature(photo);
            if (IDCARD_PIC.equals(picType)) {
                picture.setIdcardpic(bytes);
            }
            if (CAPTURE_PIC.equals(picType)) {
                picture.setCapturepic(bytes);
            }
            FaceAttribute faceAttribute = innerService.faceFeautreCheck(photo).getFeature();
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
        }
        return 1;
    }

    private Integer people_imsi_insert(String peopleId, List<String> imsis) {
        for (String s : imsis) {
            Imsi imsi = new Imsi();
            imsi.setPeopleid(peopleId);
            imsi.setImsi(s);
            int status = imsiMapper.insertSelective(imsi);
            if (status != 1) {
                log.info("Insert people, but insert imsi to t_imsi failed");
                return 0;
            }
        }
        return 1;
    }

    private Integer people_phone_insert(String peopleId, List<String> phones) {
        for (String s : phones) {
            Phone phone = new Phone();
            phone.setPeopleid(peopleId);
            phone.setPhone(s);
            int status = phoneMapper.insertSelective(phone);
            if (status != 1) {
                log.info("Insert people, but insert phone to t_phone failed");
                return 0;
            }
        }
        return 1;
    }

    private Integer people_house_insert(String peopleId, List<String> houses) {
        for (String s : houses) {
            House house = new House();
            house.setPeopleid(peopleId);
            house.setHouse(s);
            int status = houseMapper.insertSelective(house);
            if (status != 1) {
                log.info("Insert people, but insert house to t_house failed");
                return 0;
            }
        }
        return 1;
    }

    private Integer people_car_insert(String peopleId, List<String> cars) {
        for (String s : cars) {
            Car car = new Car();
            car.setPeopleid(peopleId);
            car.setCar(s);
            int status = carMapper.insertSelective(car);
            if (status != 1) {
                log.info("Insert people, but insert car to t_car failed");
                return 0;
            }
        }
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
        if (people != null) {
            peopleVO.setId(people.getId());
            peopleVO.setName(people.getName());
            peopleVO.setIdCard(people.getIdcard());
            peopleVO.setRegionId(people.getRegion());
            peopleVO.setRegion(platformService.getRegionName(people.getRegion()));
            peopleVO.setHousehold(people.getHousehold());
            peopleVO.setAddress(people.getAddress());
            peopleVO.setSex(people.getSex());
            peopleVO.setAge(people.getAge());
            peopleVO.setBirthday(people.getBirthday());
            peopleVO.setPolitic(people.getPolitic());
            peopleVO.setEduLevel(people.getEdulevel());
            peopleVO.setJob(people.getJob());
            peopleVO.setBirthplace(people.getBirthplace());
            peopleVO.setCommunity(people.getCommunity());
            peopleVO.setCommunityName(platformService.getCommunityName(people.getCommunity()));
            if (people.getLasttime() != null) {
                peopleVO.setLastTime(sdf.format(people.getLasttime()));
            }
            if (people.getCreatetime() != null) {
                peopleVO.setCreateTime(sdf.format(people.getCreatetime()));
            }
            if (people.getUpdatetime() != null) {
                peopleVO.setUpdateTime(sdf.format(people.getUpdatetime()));
            }
            List<com.hzgc.service.people.model.Flag> flags = people.getFlag();
            List<Integer> flagIdList = new ArrayList<>();
            for (com.hzgc.service.people.model.Flag flag : flags) {
                flagIdList.add(flag.getFlagid());
            }
            peopleVO.setFlag(flagIdList);
            List<Imsi> imsis = people.getImsi();
            List<String> imsiList = new ArrayList<>();
            for (Imsi imsi : imsis) {
                imsiList.add(imsi.getImsi());
            }
            peopleVO.setImsi(imsiList);
            List<Phone> phones = people.getPhone();
            List<String> phoneList = new ArrayList<>();
            for (Phone phone : phones) {
                phoneList.add(phone.getPhone());
            }
            peopleVO.setPhone(phoneList);
            List<House> houses = people.getHouse();
            List<String> houseList = new ArrayList<>();
            for (House house : houses) {
                houseList.add(house.getHouse());
            }
            peopleVO.setHouse(houseList);
            List<Car> cars = people.getCar();
            List<String> carList = new ArrayList<>();
            for (Car car : cars) {
                carList.add(car.getCar());
            }
            peopleVO.setCar(carList);
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
     * 查询人员对象
     *
     * @param field 查询过滤字段封装
     * @return SearchPeopleVO 查询返回参数封装
     */
    public SearchPeopleVO searchPeople(FilterField field) {
        SearchPeopleVO vo = new SearchPeopleVO();
        List<PeopleVO> list = new ArrayList<>();
        Page page = PageHelper.offsetPage(field.getStart(), field.getLimit(), true);
        List<People> peoples = peopleMapper.searchPeople(field);
        PageInfo info = new PageInfo(page.getResult());
        int total = (int) info.getTotal();
        vo.setTotal(total);
        if (peoples != null && peoples.size() > 0) {
            for (People people : peoples) {
                PeopleVO peopleVO = new PeopleVO();
                if (people != null) {
                    peopleVO.setId(people.getId());
                    peopleVO.setName(people.getName());
                    peopleVO.setIdCard(people.getIdcard());
                    peopleVO.setRegion(platformService.getRegionName(people.getRegion()));
                    peopleVO.setHousehold(people.getHousehold());
                    peopleVO.setAddress(people.getAddress());
                    peopleVO.setSex(people.getSex());
                    peopleVO.setAge(people.getAge());
                    peopleVO.setBirthday(people.getBirthday());
                    peopleVO.setPolitic(people.getPolitic());
                    peopleVO.setEduLevel(people.getEdulevel());
                    peopleVO.setJob(people.getJob());
                    peopleVO.setBirthplace(people.getBirthplace());
                    peopleVO.setCommunity(people.getCommunity());
                    peopleVO.setCommunityName(platformService.getCommunityName(people.getCommunity()));
                    if (people.getLasttime() != null) {
                        peopleVO.setLastTime(sdf.format(people.getLasttime()));
                    }
                    if (people.getCreatetime() != null) {
                        peopleVO.setCreateTime(sdf.format(people.getCreatetime()));
                    }
                    if (people.getUpdatetime() != null) {
                        peopleVO.setUpdateTime(sdf.format(people.getUpdatetime()));
                    }
                    List<com.hzgc.service.people.model.Flag> flags = people.getFlag();
                    List<Integer> flagIdList = new ArrayList<>();
                    for (com.hzgc.service.people.model.Flag flag : flags) {
                        flagIdList.add(flag.getFlagid());
                    }
                    peopleVO.setFlag(flagIdList);
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
        return peopleMapper.getCommunityIdsById(communityIds);
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
            peopleVO.setAge(people.getAge());
            peopleVO.setBirthday(people.getBirthday());
            peopleVO.setPolitic(people.getPolitic());
            peopleVO.setEduLevel(people.getEdulevel());
            peopleVO.setJob(people.getJob());
            peopleVO.setBirthplace(people.getBirthplace());
            peopleVO.setCommunity(people.getCommunity());
            List<Phone> phones = people.getPhone();
            List<String> phoneList = new ArrayList<>();
            for (Phone phone : phones) {
                phoneList.add(phone.getPhone());
            }
            peopleVO.setPhone(phoneList);
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
                    && regionNames.contains(String.valueOf(map.get(2)))){
                peopleDTO.setRegion(regionMap.get(String.valueOf(map.get(2))));
            } else {
                log.error("Import excel data failed, because region is error, please check line: " + i);
                return 0;
            }
            if (map.get(3) != null && !"".equals(map.get(3))){
                if (communityNames.contains(String.valueOf(map.get(3)))){
                    peopleDTO.setCommunity(communityMap.get(String.valueOf(map.get(3))));
                }else {
                    log.error("Import excel data failed, because community is error, please check line: " + i);
                    return 0;
                }
            }
            if (map.get(4) != null && !"".equals(map.get(4))) {
                peopleDTO.setSex(String.valueOf(map.get(4)));
            }
            if (map.get(5) != null && !"".equals(map.get(5))) {
                peopleDTO.setAge(Float.valueOf(String.valueOf(map.get(5))).intValue());
            }
            if (map.get(6) != null && !"".equals(map.get(6))) {
                peopleDTO.setJob(String.valueOf(map.get(6)));
            }
            if (map.get(7) != null && !"".equals(map.get(7))) {
                peopleDTO.setBirthday(String.valueOf(map.get(7)));
            }
            if (map.get(8) != null && !"".equals(map.get(8))) {
                peopleDTO.setAddress(String.valueOf(map.get(8)));
            }
            if (map.get(9) != null && !"".equals(map.get(9))) {
                peopleDTO.setHousehold(String.valueOf(map.get(9)));
            }
            if (map.get(10) != null && !"".equals(map.get(10))) {
                peopleDTO.setBirthplace(String.valueOf(map.get(10)));
            }
            if (map.get(11) != null && !"".equals(map.get(11))) {
                peopleDTO.setPolitic(String.valueOf(map.get(11)));
            }
            if (map.get(12) != null && !"".equals(map.get(12))) {
                peopleDTO.setEduLevel(String.valueOf(12));
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
            ReturnMessage message = this.insertPeople(peopleDTO);
            if (message == null || message.getStatus() != 1) {
                throw new RuntimeException("Insert into t_people table failed");
            }
        }
        return 1;
    }
}

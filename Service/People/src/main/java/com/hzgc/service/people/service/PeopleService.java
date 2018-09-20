package com.hzgc.service.people.service;

import com.github.pagehelper.PageHelper;
import com.hzgc.jniface.FaceAttribute;
import com.hzgc.jniface.FaceFunction;
import com.hzgc.jniface.FaceJNI;
import com.hzgc.jniface.PictureFormat;
import com.hzgc.service.people.dao.*;
import com.hzgc.service.people.model.*;
import com.hzgc.service.people.model.Flag;
import com.hzgc.service.people.param.FilterField;
import com.hzgc.service.people.param.PeopleVO;
import com.hzgc.service.people.param.PictureVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class PeopleService {
    @Autowired
    private CarMapper carMapper;
    @Autowired
    private FlagMapper flagMapper;
    @Autowired
    private HouseMapper houseMapper;
    @Autowired
    private ImsiMapper imsiMapper;
    @Autowired
    private PeopleMapper peopleMapper;
    @Autowired
    private PhoneMapper phoneMapper;
    @Autowired
    private PictureMapper pictureMapper;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public final static String IDCARD_PIC = "idcardpic";
    public final static String CAPTURE_PIC = "capturepic";

    public Integer people_insert(People people) {
        return peopleMapper.insertSelective(people);
    }

    public Integer people_update(People people) {
        return peopleMapper.updateByPrimaryKeySelective(people);
    }

    public Integer people_flag_insert(String peopleId, List<Integer> flags) {
        for (Integer integer : flags) {
            Flag flag = new Flag();
            flag.setPeopleid(peopleId);
            flag.setFlagid(integer);
            flag.setFlag(com.hzgc.service.people.fields.Flag.getFlag(integer));
            int status = flagMapper.insertSelective(flag);
            if (status != 1) {
                log.info("Insert people, but insert t_flag failed");
                return 0;
            }
        }
        return 1;
    }

    public Integer people_flag_update(String peopleId, List<Integer> flags) {
        List<Long> idList = flagMapper.selectIdByPeopleId(peopleId);
        for (Long id : idList) {
            int status = flagMapper.deleteByPrimaryKey(id);
            if (status != 1) {
                log.info("Update people, but delete t_flag failed, id: " + id);
                return 0;
            }
        }
        for (Integer integer : flags) {
            Flag flag = new Flag();
            flag.setPeopleid(peopleId);
            flag.setFlagid(integer);
            flag.setFlag(com.hzgc.service.people.fields.Flag.getFlag(integer));
            int insertStatus = flagMapper.insertSelective(flag);
            if (insertStatus != 1) {
                log.info("Update people, but insert flag to t_flag failed, flag:" + integer);
                return 0;
            }
        }
        return 1;
    }

    public Integer people_picture_insert(String peopleId, String picType, List<byte[]> pics) {
        for (byte[] bytes : pics) {
            PictureWithBLOBs picture = new PictureWithBLOBs();
            picture.setPeopleid(peopleId);
            if (IDCARD_PIC.equals(picType)) {
                picture.setIdcardpic(bytes);
            }
            if (CAPTURE_PIC.equals(picType)) {
                picture.setCapturepic(bytes);
            }
            FaceAttribute faceAttribute = FaceJNI.faceFeatureExtract(bytes, PictureFormat.JPG);
            if (faceAttribute == null || faceAttribute.getFeature() == null || faceAttribute.getBitFeature() == null) {
                log.info("Face feature extract failed, insert picture to t_picture failed");
                return 0;
            }
            picture.setFeature(FaceFunction.floatFeature2Base64Str(faceAttribute.getFeature()));
            picture.setBitfeature(FaceFunction.bitFeautre2Base64Str(faceAttribute.getBitFeature()));
            int insertStatus = pictureMapper.insertSelective(picture);
            if (insertStatus != 1) {
                log.info("Insert people, but insert picture to t_picture failed");
                return 0;
            }
        }
        return 1;
    }

    public Integer people_picture_update(String peopleId, String picType, List<byte[]> pics) {
        List<Long> idList = pictureMapper.selectIdByPeopleId(peopleId);
        for (Long id : idList) {
            int status = pictureMapper.deleteByPrimaryKey(id);
            if (status != 1) {
                log.info("Update people, but delete t_picture failed, id: " + id);
                return 0;
            }
        }
        for (byte[] bytes : pics) {
            PictureWithBLOBs picture = new PictureWithBLOBs();
            picture.setPeopleid(peopleId);
            if (IDCARD_PIC.equals(picType)) {
                picture.setIdcardpic(bytes);
            }
            if (CAPTURE_PIC.equals(picType)) {
                picture.setCapturepic(bytes);
            }
            FaceAttribute faceAttribute = FaceJNI.faceFeatureExtract(bytes, PictureFormat.JPG);
            if (faceAttribute == null || faceAttribute.getFeature() == null || faceAttribute.getBitFeature() == null) {
                log.info("Face feature extract failed, insert picture to t_picture failed");
                return 0;
            }
            picture.setFeature(FaceFunction.floatFeature2Base64Str(faceAttribute.getFeature()));
            picture.setBitfeature(FaceFunction.bitFeautre2Base64Str(faceAttribute.getBitFeature()));
            int insertStatus = pictureMapper.insertSelective(picture);
            if (insertStatus != 1) {
                log.info("Update people, but insert picture to t_picture failed");
                return 0;
            }
        }
        return 1;
    }

    public Integer people_imsi_insert(String peopleId, List<String> imsis) {
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

    public Integer people_imsi_update(String peopleId, List<String> imsis) {
        List<Long> idList = imsiMapper.selectIdByPeopleId(peopleId);
        for (Long id : idList) {
            int status = imsiMapper.deleteByPrimaryKey(id);
            if (status != 1) {
                log.info("Update people, but delete t_imsi failed, id: " + id);
                return 0;
            }
        }
        for (String s : imsis) {
            Imsi imsi = new Imsi();
            imsi.setPeopleid(peopleId);
            imsi.setImsi(s);
            int insertStatus = imsiMapper.insertSelective(imsi);
            if (insertStatus != 1) {
                log.info("Update people, but insert imsi to t_imsi failed, imsi: " + s);
                return 0;
            }
        }
        return 1;
    }

    public Integer people_phone_insert(String peopleId, List<String> phones) {
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

    public Integer people_phone_update(String peopleId, List<String> phones) {
        List<Long> idList = phoneMapper.selectIdByPeopleId(peopleId);
        for (Long id : idList) {
            int status = phoneMapper.deleteByPrimaryKey(id);
            if (status != 1) {
                log.info("Update people, but delete t_phone failed, id: " + id);
                return 0;
            }
        }
        for (String s : phones) {
            Phone phone = new Phone();
            phone.setPeopleid(peopleId);
            phone.setPhone(s);
            int insertStatus = phoneMapper.insertSelective(phone);
            if (insertStatus != 1) {
                log.info("Update people, but insert phone to t_phone failed, phone: " + s);
                return 0;
            }
        }
        return 1;
    }

    public Integer people_house_insert(String peopleId, List<String> houses) {
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

    public Integer people_house_update(String peopleId, List<String> houses) {
        List<Long> idList = houseMapper.selectIdByPeopleId(peopleId);
        for (Long id : idList) {
            int status = houseMapper.deleteByPrimaryKey(id);
            if (status != 1) {
                log.info("Update people, but delete t_house failed, id: " + id);
                return 0;
            }
        }
        for (String s : houses) {
            House house = new House();
            house.setPeopleid(peopleId);
            house.setHouse(s);
            int insertStatus = houseMapper.insertSelective(house);
            if (insertStatus != 1) {
                log.info("Update people, but insert house to t_house failed, house: " + s);
                return 0;
            }
        }
        return 1;
    }

    public Integer people_car_insert(String peopleId, List<String> cars) {
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

    public Integer people_car_update(String peopleId, List<String> cars) {
        List<Long> idList = carMapper.selectIdByPeopleId(peopleId);
        for (Long id : idList) {
            int status = carMapper.deleteByPrimaryKey(id);
            if (status != 1) {
                log.info("Update people, but delete t_car failed, id: " + id);
                return 0;
            }
        }
        for (String s : cars) {
            Car car = new Car();
            car.setPeopleid(peopleId);
            car.setCar(s);
            int insertStatus = carMapper.insertSelective(car);
            if (insertStatus != 1) {
                log.info("Update people, but insert car to t_car failed, car: " + s);
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
            List<byte[]> idcardPics = new ArrayList<>();
            List<byte[]> capturePics = new ArrayList<>();
            for (PictureWithBLOBs picture : pictures) {
                if (picture != null) {
                    byte[] idcardPic = picture.getIdcardpic();
                    if (idcardPic != null && idcardPic.length > 0) {
                        idcardPics.add(idcardPic);
                    } else {
                        capturePics.add(picture.getCapturepic());
                    }
                }
            }
            pictureVO.setIdcardPics(idcardPics);
            pictureVO.setCapturePics(capturePics);
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
            peopleVO.setRegion(String.valueOf(people.getRegion()));
            peopleVO.setHousehold(people.getHousehold());
            peopleVO.setAddress(people.getAddress());
            peopleVO.setSex(people.getSex());
            peopleVO.setAge(people.getAge());
            peopleVO.setBirthday(people.getBirthday());
            peopleVO.setPolitic(people.getPolitic());
            peopleVO.setEduLevel(people.getEdulevel());
            peopleVO.setJob(people.getJob());
            peopleVO.setBirthplace(people.getBirthplace());
            peopleVO.setCommunity(String.valueOf(people.getCommunity()));
            peopleVO.setImportant(people.getImportant());
            peopleVO.setCare(people.getCare());
            if (people.getLasttime() != null) {
                peopleVO.setLastTime(sdf.format(people.getLasttime()));
            }
            if (people.getCreatetime() != null) {
                peopleVO.setCreateTime(sdf.format(people.getCreatetime()));
            }
            if (people.getUpdatetime() != null) {
                peopleVO.setUpdateTime(sdf.format(people.getUpdatetime()));
            }
            List<Flag> flags = people.getFlag();
            List<Integer> flagIdList = new ArrayList<>();
            for (Flag flag : flags) {
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
                List<byte[]> idcardPictureList = new ArrayList<>();
                List<byte[]> capturePictureList = new ArrayList<>();
                for (PictureWithBLOBs picture : pictures) {
                    if (picture.getIdcardpic() != null) {
                        idcardPictureList.add(picture.getIdcardpic());
                    } else {
                        capturePictureList.add(picture.getCapturepic());
                    }
                }
                peopleVO.setIdcardPicture(idcardPictureList);
                peopleVO.setCapturePicture(capturePictureList);
            }
        }
        return peopleVO;
    }

    /**
     * 查询人员对象
     *
     * @param field 查询过滤字段封装
     * @param start 起始行数
     * @param limit 分页行数
     * @return peopleVO 查询返回参数封装
     */
    public List<PeopleVO> searchPeople(FilterField field, int start, int limit) {
        List<PeopleVO> list = new ArrayList<>();
        PageHelper.offsetPage(start, limit);
        List<People> peoples = peopleMapper.searchPeople(field);
        if (peoples != null && peoples.size() > 0) {
            for (People people : peoples) {
                PeopleVO peopleVO = new PeopleVO();
                if (people != null) {
                    peopleVO.setId(people.getId());
                    peopleVO.setName(people.getName());
                    peopleVO.setIdCard(people.getIdcard());
                    peopleVO.setRegion(String.valueOf(people.getRegion()));
                    peopleVO.setHousehold(people.getHousehold());
                    peopleVO.setAddress(people.getAddress());
                    peopleVO.setSex(people.getSex());
                    peopleVO.setAge(people.getAge());
                    peopleVO.setBirthday(people.getBirthday());
                    peopleVO.setPolitic(people.getPolitic());
                    peopleVO.setEduLevel(people.getEdulevel());
                    peopleVO.setJob(people.getJob());
                    peopleVO.setBirthplace(people.getBirthplace());
                    peopleVO.setCommunity(String.valueOf(people.getCommunity()));
                    peopleVO.setImportant(people.getImportant());
                    peopleVO.setCare(people.getCare());
                    if (people.getLasttime() != null) {
                        peopleVO.setLastTime(sdf.format(people.getLasttime()));
                    }
                    if (people.getCreatetime() != null) {
                        peopleVO.setCreateTime(sdf.format(people.getCreatetime()));
                    }
                    if (people.getUpdatetime() != null) {
                        peopleVO.setUpdateTime(sdf.format(people.getUpdatetime()));
                    }
                    List<Flag> flags = people.getFlag();
                    List<Integer> flagIdList = new ArrayList<>();
                    for (Flag flag : flags) {
                        flagIdList.add(flag.getFlagid());
                    }
                    peopleVO.setFlag(flagIdList);
                    if (people.getPicture() != null && people.getPicture().size() > 0) {
                        PictureWithBLOBs picture = people.getPicture().get(0);
                        if (picture.getIdcardpic() != null) {
                            peopleVO.setPicture(picture.getIdcardpic());
                        } else {
                            peopleVO.setPicture(picture.getCapturepic());
                        }
                    }
                    list.add(peopleVO);
                }
            }
        }
        return list;
    }
}

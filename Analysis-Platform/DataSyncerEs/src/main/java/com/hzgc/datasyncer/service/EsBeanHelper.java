package com.hzgc.datasyncer.service;

import com.hzgc.common.collect.bean.CarObject;
import com.hzgc.common.collect.bean.FaceObject;
import com.hzgc.common.collect.bean.PersonObject;
import com.hzgc.common.service.api.bean.CameraQueryDTO;
import com.hzgc.common.service.api.bean.Region;
import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.datasyncer.bean.EsCarObject;
import com.hzgc.datasyncer.bean.EsFaceObject;
import com.hzgc.datasyncer.bean.EsPersonObject;
import com.hzgc.jniface.FaceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
class EsBeanHelper {
    @Autowired
    @SuppressWarnings("unused")
    private CameraCacheHelper cameraCacheHelper;

    @Autowired
    @SuppressWarnings("unused")
    private RegionCacheHelper regionCacheHelper;

    List<EsFaceObject> faceBeanConvert(List<FaceObject> objectList) {
        return objectList.stream()
                .map(faceObject -> {
                    if (faceObject.getId() != null) {
                        EsFaceObject esFaceObject = new EsFaceObject();
                        esFaceObject.setId(faceObject.getId());
                        CameraQueryDTO info = cameraCacheHelper.getInfo(faceObject.getIpcId());
                        if (info != null) {
                            esFaceObject.setCommunityid(info.getCommunityId().toString());
                            Long districtId = info.getDistrictId();
                            if (districtId != null) {
                                Region region = regionCacheHelper.getInfo(districtId);
                                if (region != null) {
                                    esFaceObject.setProvinceid(region.getProvinceId().toString());
                                    esFaceObject.setCityid(region.getCityId().toString());
                                    esFaceObject.setRegionid(districtId.toString());
                                } else {
                                    log.error("Region id is not found, region is is:{}", districtId.toString());
                                }
                            }
                        } else {
                            log.error("Camera ipc id is not found, ipc id is:{}", faceObject.getIpcId());
                        }
                        esFaceObject.setAge(esFaceObject.getAge());
                        esFaceObject.setTimestamp(faceObject.getTimeStamp());
                        esFaceObject.setIpcid(faceObject.getIpcId());
                        esFaceObject.setHostname(faceObject.getHostname());
                        esFaceObject.setBabsolutepath(faceObject.getbAbsolutePath());
                        esFaceObject.setSabsolutepath(faceObject.getsAbsolutePath());
                        esFaceObject.setEyeglasses(faceObject.getAttribute().getEyeglasses());
                        esFaceObject.setGender(faceObject.getAttribute().getGender());
                        esFaceObject.setAge(faceObject.getAttribute().getAge());
                        esFaceObject.setMask(faceObject.getAttribute().getMask());
                        esFaceObject.setHuzi(faceObject.getAttribute().getHuzi());
                        esFaceObject.setSharpness(faceObject.getAttribute().getSharpness());
                        esFaceObject.setFeature(FaceUtil.floatFeature2Base64Str(faceObject.getAttribute().getFeature()));
                        esFaceObject.setBitfeature(faceObject.getAttribute().getBitFeature());
                        return esFaceObject;
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    List<EsPersonObject> personBeanConvert(List<PersonObject> objectList) {
        return objectList.stream()
                .map(personObject -> {
                    if (personObject.getId() != null) {
                        EsPersonObject esPersonObject = new EsPersonObject();
                        esPersonObject.setId(personObject.getId());
                        esPersonObject.setProvinceid(null);
                        esPersonObject.setCityid(null);
                        esPersonObject.setRegionid(null);
                        esPersonObject.setIpcid(personObject.getIpcId());
                        CameraQueryDTO info = cameraCacheHelper.getInfo(personObject.getIpcId());
                        if (info != null) {
                            esPersonObject.setCommunityid(info.getCommunityId().toString());
                            Long districtId = info.getDistrictId();
                            if (districtId != null) {
                                Region region = regionCacheHelper.getInfo(districtId);
                                if (region != null) {
                                    esPersonObject.setProvinceid(region.getProvinceId().toString());
                                    esPersonObject.setCityid(region.getCityId().toString());
                                    esPersonObject.setRegionid(districtId.toString());
                                } else {
                                    log.error("Region id is not found, region is is:{}", districtId.toString());
                                }
                            }
                        } else {
                            log.error("Camera ipc id is not found, ipc id is:{}", personObject.getIpcId());
                        }
                        esPersonObject.setTimestamp(personObject.getTimeStamp());
                        esPersonObject.setHostname(personObject.getHostname());
                        esPersonObject.setBabsolutepath(personObject.getbAbsolutePath());
                        esPersonObject.setSabsolutepath(personObject.getsAbsolutePath());
                        esPersonObject.setAge(personObject.getAttribute().getAge_code());
                        esPersonObject.setBaby(personObject.getAttribute().getBaby_code());
                        esPersonObject.setBag(personObject.getAttribute().getBag_code());
                        esPersonObject.setBottomcolor(personObject.getAttribute().getBottomcolor_code());
                        esPersonObject.setBottomtype(personObject.getAttribute().getBottomtype_code());
                        esPersonObject.setHat(personObject.getAttribute().getHat_code());
                        esPersonObject.setHair(personObject.getAttribute().getHair_code());
                        esPersonObject.setKnapsack(personObject.getAttribute().getKnapsack_code());
                        esPersonObject.setMessengerbag(personObject.getAttribute().getMessengerbag_code());
                        esPersonObject.setOrientation(personObject.getAttribute().getOrientation_code());
                        esPersonObject.setSex(personObject.getAttribute().getSex_code());
                        esPersonObject.setShoulderbag(personObject.getAttribute().getShoulderbag_code());
                        esPersonObject.setUmbrella(personObject.getAttribute().getUmbrella_code());
                        esPersonObject.setUppercolor(personObject.getAttribute().getUppercolor_code());
                        esPersonObject.setUppertype(personObject.getAttribute().getUppertype_code());
                        esPersonObject.setCartype(personObject.getAttribute().getCar_type());
                        return esPersonObject;
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    List<EsCarObject> carBeanConvert(List<CarObject> objectList) {
        return objectList.stream()
                .map(carObject -> {
                    if (carObject.getId() != null) {
                        EsCarObject esCarObject = new EsCarObject();
                        esCarObject.setId(carObject.getId());
                        esCarObject.setProvinceid(null);
                        esCarObject.setCityid(null);
                        esCarObject.setRegionid(null);
                        esCarObject.setIpcid(carObject.getIpcId());
                        CameraQueryDTO info = cameraCacheHelper.getInfo(carObject.getIpcId());
                        if (info != null) {
                            esCarObject.setCommunityid(info.getCommunityId().toString());
                            Long districtId = info.getDistrictId();
                            if (districtId != null) {
                                Region region = regionCacheHelper.getInfo(districtId);
                                if (region != null) {
                                    esCarObject.setProvinceid(region.getProvinceId().toString());
                                    esCarObject.setCityid(region.getCityId().toString());
                                    esCarObject.setRegionid(districtId.toString());
                                } else {
                                    log.error("Region id is not found, region is is:{}", districtId.toString());
                                }
                            }
                        } else {
                            log.error("Camera ipc id is not found, ipc id is:{}", carObject.getIpcId());
                        }
                        esCarObject.setTimestamp(carObject.getTimeStamp());
                        esCarObject.setHostname(carObject.getHostname());
                        esCarObject.setBabsolutepath(carObject.getbAbsolutePath());
                        esCarObject.setSabsolutepath(carObject.getsAbsolutePath());
                        esCarObject.setVehicle_object_type(carObject.getAttribute().getVehicle_object_type());
                        esCarObject.setBelt_maindriver(carObject.getAttribute().getBelt_maindriver());
                        esCarObject.setBelt_codriver(carObject.getAttribute().getBelt_codriver());
                        esCarObject.setBrand_name(carObject.getAttribute().getBrand_name());
                        esCarObject.setCall_code(carObject.getAttribute().getCall_code());
                        esCarObject.setVehicle_color(carObject.getAttribute().getVehicle_color());
                        esCarObject.setCrash_code(carObject.getAttribute().getCrash_code());
                        esCarObject.setDanger_code(carObject.getAttribute().getDanger_code());
                        esCarObject.setMarker_code(carObject.getAttribute().getMarker_code());
                        esCarObject.setPlate_schelter_code(carObject.getAttribute().getPlate_schelter_code());
                        esCarObject.setPlate_flag_code(carObject.getAttribute().getPlate_flag_code());
                        esCarObject.setPlate_licence(carObject.getAttribute().getPlate_licence());
                        esCarObject.setPlate_destain_code(carObject.getAttribute().getPlate_destain_code());
                        esCarObject.setPlate_color_code(carObject.getAttribute().getPlate_color_code());
                        esCarObject.setPlate_type_code(carObject.getAttribute().getPlate_type_code());
                        esCarObject.setRack_code(carObject.getAttribute().getRack_code());
                        esCarObject.setSunroof_code(carObject.getAttribute().getSunroof_code());
                        esCarObject.setVehicle_type(carObject.getAttribute().getVehicle_type());
                        return esCarObject;
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    <T> List<T> getObjectList(List<ConsumerRecord<String, String>> consumerRecordList, Class<T> type) {
        if (consumerRecordList != null && consumerRecordList.size() > 0) {
            return consumerRecordList.stream()
                    .map(consumerRecord -> JacksonUtil.toObject(consumerRecord.value(), type))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } else {
            log.error("Receive record number is null, type is:{}", type.getName());
            return new ArrayList<>();
        }
    }
}

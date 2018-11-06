package com.hzgc.common.service.api.service;

import com.hzgc.common.service.api.bean.*;
import com.hzgc.common.util.json.JacksonUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Service
@Slf4j
public class PlatformService {
    @Resource(name = "platform")
    @SuppressWarnings("unused")
    private RestTemplate restTemplate;

    /**
     * 获取区域名称
     *
     * @param id 区域ID
     * @return 区域名称
     */
    public String getRegionName(Long id) {
        if (id != null) {
            log.debug("Method:getRegionName, id:" + id);
            List<Long> ids = new ArrayList<>();
            ids.add(id);
            List<Region> regions = getRegionByIds(ids);
            if (regions != null && regions.size() > 0 && regions.get(0) != null) {
                return regions.get(0).getMergerName();
            } else {
                log.info("Get region info failed, because result is null");
            }
        } else {
            log.error("Method:getRegionName, id is null");
        }
        return null;
    }

    /**
     * 获取社区名称
     *
     * @param id 社区ID
     * @return 社区名称
     */
    public String getCommunityName(Long id) {
        if (id != null) {
            log.debug("Method:getCommunityName, id:" + id);
            List<Long> ids = new ArrayList<>();
            ids.add(id);
            List<Region> regions = getRegionByIds(ids);
            if (regions != null && regions.size() > 0 && regions.get(0) != null) {
                return regions.get(0).getName();
            } else {
                log.info("Get community info failed, because result is null");
            }
        } else {
            log.error("Method:getCommunityName, id is null");
        }
        return null;
    }

    /**
     * 获取所有区域ID
     *
     * @return 区域ID集合
     */
    public Map<String, Long> getAllRegionId() {
        AreaCriteria areaCriteria = new AreaCriteria();
        Map<String, Long> map = new HashMap<>();
        areaCriteria.setId(1L);
        areaCriteria.setLevel("district");
        AreaSDTO areaSDTO = restTemplate.postForObject("http://platform:8888/api/v1/region/internal/region/all/query_region_info", areaCriteria, AreaSDTO.class);
        List<AreaDTO> areaDTOs = areaSDTO.getAreaDTOs();
        for (AreaDTO areaDTO : areaDTOs){
            String provinceName = areaDTO.getProvinceName();
            String cityName = areaDTO.getCityName();
            String districtName = areaDTO.getDistrictName();
            String region = provinceName + cityName + districtName;
            map.put(region,areaDTO.getId());
        }
        return map;
    }

    /**
     * 获取所有小区ID
     *
     * @return 小区ID集合
     */
    public Map<String, Long> getAllCommunityId() {
        Map<String, Long> map = new HashMap<>();
        AreaCriteria areaCriteria = new AreaCriteria();
        areaCriteria.setId(1L);
        areaCriteria.setLevel("region");
        AreaSDTO areaSDTO = restTemplate.postForObject("http://platform:8888/api/v1/region/internal/region/all/query_region_info", areaCriteria, AreaSDTO.class);
        List<AreaDTO> areaDTOs = areaSDTO.getAreaDTOs();
        for (AreaDTO areaDTO : areaDTOs){
            String mergerName = areaDTO.getMergerName();
            Long id = areaDTO.getId();
            map.put(mergerName, id);
        }
        return map;
    }

    /**
     * 获取省/市/区范围内的所有社区ID列表
     *
     * @param id 省/市/区 ID
     * @return 社区ID列表
     */
    public List<Long> getCommunityIdsById(Long id) {
        if (id != null) {
            log.debug("Method:getCommunityIdsById, id:" + id);
            AreaCriteria criteria = new AreaCriteria();
            criteria.setId(id);
            criteria.setLevel("region");
            AreaSDTO areaSDTO = restTemplate.postForObject(
                    "http://platform:8888/api/v1/region/internal/region/all/query_region_info",
                    criteria,AreaSDTO.class);
            if (areaSDTO != null && areaSDTO.getAreaDTOs() != null) {
                List<AreaDTO> areaDTOs = areaSDTO.getAreaDTOs();
                List<Long> communityIds = new ArrayList<>();
                for (AreaDTO areaDTO : areaDTOs){
                    if (areaDTO != null){
                        communityIds.add(areaDTO.getId());
                    }
                }
                return communityIds;
            } else {
                log.info("Get community ids by id failed, because result is null");
            }
        } else {
            log.error("Method:getCommunityIdsById, regionId is null");
        }
        return null;
    }

    public String getCameraDeviceName(String deviceId) {
        if (deviceId != null) {
            log.debug("Method:getCameraDeviceName, deviceId:" + deviceId);
            List<String> ids = new ArrayList<>();
            ids.add(deviceId);
            Map<String, CameraQueryDTO> map = getCameraInfoByBatchIpc(ids);
            if (map != null && map.size() > 0 && map.get(deviceId) != null) {
                return map.get(deviceId).getCameraName();
            } else {
                log.info("Get camera device info failed, because result is null");
            }
        } else {
            log.error("Method:getCameraDeviceName, id is null");
        }
        return null;
    }

    public String getImsiDeviceName(String deviceId) {
        if (deviceId != null) {
            log.debug("Method:getImsiDeviceName, deviceId:" + deviceId);
            List<String> ids = new ArrayList<>();
            ids.add(deviceId);
            Map<String, DetectorQueryDTO> map = getImsiDeviceInfoByBatchId(ids);
            if (map != null && map.size() > 0 && map.get(deviceId) != null) {
                return map.get(deviceId).getDetectorName();
            } else {
                log.info("Get imsi device info failed, because result is null");
            }
        } else {
            log.error("Method:getImsiDeviceName, id is null");
        }
        return null;
    }

    private Map<String, DetectorQueryDTO> getImsiDeviceInfoByBatchId(List<String> idList) {
        if (idList != null) {
            log.debug("Method:getImsiDeviceInfoByBatchId, id list is:" + Arrays.toString(idList.toArray()));
            ParameterizedTypeReference<Map<String, DetectorQueryDTO>> parameterizedTypeReference =
                    new ParameterizedTypeReference<Map<String, DetectorQueryDTO>>() {
                    };
            ResponseEntity<Map<String, DetectorQueryDTO>> responseEntity =
                    restTemplate.exchange("http://platform:8888/api/v1/device/internal/detectors/query_detector_by_sns", HttpMethod.POST,
                            new HttpEntity<>(idList), parameterizedTypeReference);
            return responseEntity.getBody();
        } else {
            log.error("Method:getImsiDeviceInfoByBatchId, id list is null");
            return new HashMap<>();
        }
    }


    /**
     * 获取区域/社区信息
     *
     * @param ids 区域/社区 ID列表
     * @return 获取区域/社区信息
     */
    private List<Region> getRegionByIds(List<Long> ids) {
        if (ids != null && ids.size() > 0) {
            log.debug("Method:getRegionByIds, ids:" + Arrays.toString(ids.toArray()));
            ParameterizedTypeReference<Region[]> parameterizedTypeReference =
                    new ParameterizedTypeReference<Region[]>() {
                    };
            ResponseEntity<Region[]> responseEntity = restTemplate.exchange("http://platform:8888/api/v1/region/internal/region/query_region_info_by_ids",
                    HttpMethod.POST, new HttpEntity<>(ids), parameterizedTypeReference);
            return responseEntity.getBody() != null ? Arrays.asList(responseEntity.getBody()) : null;
        } else {
            log.error("Method:getRegionByIds, ids is null");
            return new ArrayList<>();
        }
    }

    public Map<String, DetectorQueryDTO> getInfoByBatchSn(List<String> snList){
        List<String> snListTemp = new ArrayList<>();
        if(snList != null && snList.size() > 0){
            Map<String, DetectorQueryDTO> returnResult = new HashMap<>();
            for(String sn : snList){
                DetectorQueryDTO detectorQuery = DetectorQueryDTOSingleton.getInstance().getDetectorQueryDTO(sn);
                if(detectorQuery != null){
                    returnResult.put(sn, detectorQuery);
                } else {
                    snListTemp.add(sn);
                }
            }
            if(snListTemp.size() > 0){
                log.debug("Method:getDetectorInfoByBatchIpc, ipc list is:" + Arrays.toString(snListTemp.toArray()));
                Map<String, DetectorQueryDTO> searchMap = getImsiDeviceInfoByBatchId(snListTemp);
                for(String sn : searchMap.keySet()){
                    DetectorQueryDTO searchdetector = searchMap.get(sn);
                    if (searchdetector != null) {
                        returnResult.put(sn, searchdetector);
                        DetectorQueryDTOSingleton.getInstance().setDetectorQueryDTO(sn, searchdetector);
                    }
                }
            }
            return returnResult;
        } else if(snList != null){
            return new HashMap<>();
        } else {
            log.error("Method:getDetectorInfoByBatchIpc, ipc list is null");
            return new HashMap<>();
        }
    }

    public Map<String, CameraQueryDTO> getCameraInfoByBatchIpc(List<String> ipcList) {
        ParameterizedTypeReference<Map<String, CameraQueryDTO>> parameterizedTypeReference =
                new ParameterizedTypeReference<Map<String, CameraQueryDTO>>() {
                };
        if (ipcList != null && ipcList.size() > 0) {
            Map<String, CameraQueryDTO> returnResult = new HashMap<>();
            for (String ipcId : ipcList) {
                if (ipcId != null) {
                    CameraQueryDTO cameraQuery = CameraQueryDTOSingleton.getInstance().getCameraQueryDTO(ipcId);
                    if (cameraQuery != null) {
                        returnResult.put(ipcId, cameraQuery);
                    } else {
                        log.debug("Method:getCameraInfoByBatchIpc, ipc list is:" + Arrays.toString(ipcList.toArray()));
                        ResponseEntity<Map<String, CameraQueryDTO>> responseEntity =
                                restTemplate.exchange("http://platform:8888/api/v1/device/internal/cameras/query_camera_by_codes",
                                        HttpMethod.POST,
                                        new HttpEntity<>(Collections.singletonList(ipcId)),
                                        parameterizedTypeReference);
                        Map<String, CameraQueryDTO> searchMap = responseEntity.getBody();
                        CameraQueryDTO searchCamera = searchMap.get(ipcId);
                        if (searchCamera != null) {
                            returnResult.put(ipcId, searchCamera);
                            CameraQueryDTOSingleton.getInstance().setCameraQueryDTO(ipcId, searchCamera);
                        }
                    }
                }
            }
            return returnResult;
        } else if (ipcList != null) {
            ResponseEntity<Map<String, CameraQueryDTO>> responseEntity =
                    restTemplate.exchange("http://platform:8888/api/v1/device/internal/cameras/query_camera_by_codes",
                            HttpMethod.POST,
                            new HttpEntity<>(ipcList),
                            parameterizedTypeReference);
            return responseEntity.getBody();
        } else {
            log.error("Method:getCameraInfoByBatchIpc, ipc list is null");
            return new HashMap<>();
        }
    }
}

class DetectorQueryDTOSingleton {
    private Map<String, DetectorQueryInfo> xxxQueryInfoMap = new ConcurrentHashMap<>();
    private static DetectorQueryDTOSingleton instance = new DetectorQueryDTOSingleton();
    private DetectorQueryDTOSingleton(){

    }
    public static DetectorQueryDTOSingleton getInstance() {
        return instance;
    }
    DetectorQueryDTO getDetectorQueryDTO(String sn){
        if(sn != null && sn.length() > 0){
            DetectorQueryInfo detectorQueryInfo = xxxQueryInfoMap.get(sn);
            if (detectorQueryInfo != null) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - detectorQueryInfo.getTimeStamp() <= 15000) {
                    return detectorQueryInfo.getDetectorQueryDTO();
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    void setDetectorQueryDTO(String sn, DetectorQueryDTO detectorQueryDTO) {
        if (sn != null && sn.length() > 0 && detectorQueryDTO != null) {
            DetectorQueryInfo detectorQueryInfo = new DetectorQueryInfo();
            detectorQueryInfo.setTimeStamp(System.currentTimeMillis());
            detectorQueryInfo.setDetectorQueryDTO(detectorQueryDTO);
            detectorQueryInfo.setSn(sn);
            xxxQueryInfoMap.put(sn, detectorQueryInfo);
        }
    }

    @Data
    static class DetectorQueryInfo {
        private String sn;
        private long timeStamp;
        DetectorQueryDTO detectorQueryDTO;
    }
}

class CameraQueryDTOSingleton {
    private Map<String, CameraQueryInfo> cameraQueryDTOMap = new ConcurrentHashMap<>();
    private static CameraQueryDTOSingleton instance = new CameraQueryDTOSingleton();

    private CameraQueryDTOSingleton() {
    }

    public static CameraQueryDTOSingleton getInstance() {
        return instance;
    }

    CameraQueryDTO getCameraQueryDTO(String ipcId) {
        if (ipcId != null && ipcId.length() > 0) {
            CameraQueryInfo cameraQueryInfo = cameraQueryDTOMap.get(ipcId);
            if (cameraQueryInfo != null) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - cameraQueryInfo.getTimeStamp() <= 15000) {
                    return cameraQueryInfo.getCameraQueryDTO();
                } else {
                    return null;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    void setCameraQueryDTO(String ipcId, CameraQueryDTO cameraQueryDTO) {
        if (ipcId != null && ipcId.length() > 0 && cameraQueryDTO != null) {
            CameraQueryInfo cameraQueryInfo = new CameraQueryInfo();
            cameraQueryInfo.setTimeStamp(System.currentTimeMillis());
            cameraQueryInfo.setCameraQueryDTO(cameraQueryDTO);
            cameraQueryInfo.setIpcId(ipcId);
            cameraQueryDTOMap.put(ipcId, cameraQueryInfo);
        }
    }

    @Data
    static class CameraQueryInfo {
        private String ipcId;
        private long timeStamp;
        CameraQueryDTO cameraQueryDTO;
    }
}
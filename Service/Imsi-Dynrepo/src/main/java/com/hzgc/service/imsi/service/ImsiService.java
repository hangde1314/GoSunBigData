package com.hzgc.service.imsi.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.hzgc.common.service.api.bean.DetectorQueryDTO;
import com.hzgc.common.service.api.service.PlatformService;
import com.hzgc.common.service.imsi.ImsiInfo;
import com.hzgc.common.service.response.ResponseResult;
import com.hzgc.common.util.json.JacksonUtil;
import com.hzgc.service.imsi.dao.ImsiDao;
import com.hzgc.service.imsi.dao.ImsiInfoMapper;
import com.hzgc.service.imsi.model.ImsiVO;
import com.hzgc.service.imsi.model.SearchImsiDTO;
import com.hzgc.service.imsi.model.SearchImsiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ImsiService {

    @Autowired
    private ImsiDao imsiDao;

    @Autowired
    private ImsiInfoMapper imsiInfoMapper;

    @Autowired
    private PlatformService platformService;

    public List <ImsiInfo> queryByTime(Long time) {
        return imsiDao.queryByTime(time);
    }

    public ResponseResult<List<ImsiVO>> searchIMSI(SearchImsiDTO searchImsiDTO) {
        SearchImsiParam param = new SearchImsiParam();
        param.setSearchType(searchImsiDTO.getSearchType());
        param.setSearchVal(searchImsiDTO.getSearchVal());
        if (StringUtils.isNotBlank(searchImsiDTO.getCellid())){
            List<Long> list = new ArrayList<>();
            list.add(Long.valueOf(searchImsiDTO.getCellid()));
            param.setCommunityIds(list);
        }else {
            if (StringUtils.isNotBlank(searchImsiDTO.getLac())){
                List<Long> communityIds = platformService.getCommunityIdsById(Long.valueOf(searchImsiDTO.getLac()));
                if (communityIds == null || communityIds.size() == 0){
                    log.info("Search community ids by region id is null, so return null, region id:" + searchImsiDTO.getLac());
                    return ResponseResult.error(3,"根据区域ID获取小区列表失败！");
                }
                param.setCommunityIds(communityIds);
            }
        }
        log.info("Start search imsi info, search param:" + JacksonUtil.toJson(param));
        List<ImsiVO> imsiVOList = new ArrayList<>();
        Page page = PageHelper.offsetPage(searchImsiDTO.getStart(), searchImsiDTO.getLimit(), true);
        List<ImsiInfo> imsiInfoList = imsiInfoMapper.searchIMSI(param);
        for (ImsiInfo imsiInfo : imsiInfoList){
            ImsiVO imsiVO = new ImsiVO();
            imsiVO.setCellid(imsiInfo.getCellid());
            imsiVO.setControlsn(imsiInfo.getControlsn());
            imsiVO.setImsi(imsiInfo.getImsi());
            imsiVO.setLac(imsiInfo.getLac());
            imsiVO.setTime(imsiInfo.getTime());
            List<String> idList = new ArrayList<>();
            idList.add(imsiInfo.getControlsn());
            DetectorQueryDTO detectorQueryDTO = platformService.getImsiDeviceInfoByBatchId(idList).get(imsiInfo.getControlsn());
            if (detectorQueryDTO != null){
                imsiVO.setCommunityName(detectorQueryDTO.getCommunity());
                imsiVO.setRegionName(detectorQueryDTO.getRegion());
            }
            imsiVO.setDeviceName(platformService.getImsiDeviceName(imsiInfo.getControlsn()));
            imsiVOList.add(imsiVO);
        }
        return ResponseResult.init(imsiVOList, page.getTotal());
    }
}

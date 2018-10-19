package com.hzgc.common.service.faceattribute.service;

import com.hzgc.common.service.faceattribute.bean.Attribute;
import com.hzgc.common.service.faceattribute.bean.AttributeValue;
import com.hzgc.common.service.faceattribute.bean.Logistic;
import com.hzgc.common.service.faceattribute.enumclass.*;
import com.hzgc.seemmo.service.ReadCarInfo;

import javax.rmi.CORBA.Tie;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 人/车属性查询
 */
public class AttributeService {

    /**
     * 人/车属性查询
     *
     * @return 属性对象列表
     */
    public List<Attribute> getAttribute() {
        List<Attribute> attributeList = new ArrayList<>();
            Attribute gender = new Attribute();
            gender.setIdentify(Gender.class.getSimpleName());
            gender.setDesc("性别");
            gender.setLogistic(Logistic.OR);
            List<AttributeValue> genderValueList = new ArrayList<>();
            for (Gender gend : Gender.values()) {
                AttributeValue genderValue = new AttributeValue();
                genderValue.setValue(gend.ordinal());
                genderValue.setDesc(Gender.getDesc(gend));
                genderValueList.add(genderValue);
            }
            gender.setValues(genderValueList);
            attributeList.add(gender);

            Attribute mask = new Attribute();
            mask.setIdentify(Mask.class.getSimpleName());
            mask.setDesc("口罩");
            mask.setLogistic(Logistic.OR);
            List<AttributeValue> maskValueList = new ArrayList<>();
            for (Mask m : Mask.values()) {
                AttributeValue tieValue = new AttributeValue();
                tieValue.setValue(m.ordinal());
                tieValue.setDesc(Mask.getDesc(m));
                maskValueList.add(tieValue);
            }
            mask.setValues(maskValueList);
            attributeList.add(mask);

            Attribute huzi = new Attribute();
            huzi.setIdentify(Huzi.class.getSimpleName());
            huzi.setDesc("胡子");
            huzi.setLogistic(Logistic.OR);
            List<AttributeValue> huziValueList = new ArrayList<>();
            for (Huzi hz : Huzi.values()) {
                AttributeValue huziValue = new AttributeValue();
                huziValue.setValue(hz.ordinal());
                huziValue.setDesc(Huzi.getDesc(hz));
                huziValueList.add(huziValue);
            }
            huzi.setValues(huziValueList);
            attributeList.add(huzi);

            Attribute eyeglasses = new Attribute();
            eyeglasses.setIdentify(Eyeglasses.class.getSimpleName());
            eyeglasses.setDesc("眼镜");
            eyeglasses.setLogistic(Logistic.OR);
            List<AttributeValue> eyeglassesValueList = new ArrayList<>();
            for (Eyeglasses eye : Eyeglasses.values()) {
                AttributeValue eyeglassesValue = new AttributeValue();
                eyeglassesValue.setValue(eye.ordinal());
                eyeglassesValue.setDesc(Eyeglasses.getDesc(eye));
                eyeglassesValueList.add(eyeglassesValue);
            }
            eyeglasses.setValues(eyeglassesValueList);
            attributeList.add(eyeglasses);

        return attributeList;
    }

    /**
     * 车属性查询
     *
     * @return 属性对象列表
     */
    public List<Attribute> getCarAttribute() {
        List<Attribute> carAttributeList = new ArrayList<>();
        Map<String, Map<Integer, String>> vehicleMap = new ReadCarInfo().getVehicleMap();

        //对象类型
        Attribute objectType = new Attribute();
        objectType.setDesc("对象类型");
        objectType.setLogistic(Logistic.AND);
        List<AttributeValue> objectTypeValueList = new ArrayList<>();
        Map<Integer, String> vehicle_object_type = vehicleMap.get("vehicle_object_type");
        for (Integer key : vehicle_object_type.keySet()){
            String value = vehicle_object_type.get(key);
            AttributeValue objectTypeValue = new AttributeValue();
            objectTypeValue.setDesc(value);
            objectTypeValue.setValue(key);
            objectTypeValueList.add(objectTypeValue);
        }
        objectType.setValues(objectTypeValueList);
        carAttributeList.add(objectType);

        // 车辆特征
        Attribute plateTypeCodeAttribute = new Attribute();
        plateTypeCodeAttribute.setDesc("车辆特征");
        plateTypeCodeAttribute.setLogistic(Logistic.AND);
        List<AttributeValue> plateTypeCodeValueList = new ArrayList<>();
        Map<Integer, String> plate_type_code = vehicleMap.get("plate_type_code");
        for (Integer key : plate_type_code.keySet()){
            String value = plate_type_code.get(key);
            AttributeValue plateTypeCodeValue = new AttributeValue();
            plateTypeCodeValue.setDesc(value);
            plateTypeCodeValue.setValue(key);
            plateTypeCodeValueList.add(plateTypeCodeValue);
        }
        plateTypeCodeAttribute.setValues(plateTypeCodeValueList);
        carAttributeList.add(plateTypeCodeAttribute);

        //车牌颜色
        Attribute plateColorCodeAttribute = new Attribute();
        plateColorCodeAttribute.setDesc("车牌颜色");
        plateColorCodeAttribute.setLogistic(Logistic.AND);
        List<AttributeValue> plateColorCodeValueList = new ArrayList<>();
        Map<Integer, String> plate_color_code = vehicleMap.get("plate_color_code");
        for (Integer key : plate_color_code.keySet()){
            String value = plate_color_code.get(key);
            AttributeValue plateColorCodeValue = new AttributeValue();
            plateColorCodeValue.setDesc(value);
            plateColorCodeValue.setValue(key);
            plateColorCodeValueList.add(plateColorCodeValue);
        }
        plateColorCodeAttribute.setValues(plateColorCodeValueList);
        carAttributeList.add(plateColorCodeAttribute);

        //车牌标识
        Attribute plateFlagCodeAttribute = new Attribute();
        plateFlagCodeAttribute.setDesc("车牌状况");
        plateFlagCodeAttribute.setLogistic(Logistic.AND);
        List<AttributeValue> plateFlagCodeValueList = new ArrayList<>();
        Map<Integer, String> plate_flag_code = vehicleMap.get("plate_flag_code");
        for (Integer key : plate_flag_code.keySet()) {
            String value = plate_flag_code.get(key);
            AttributeValue plateFlagCodeValue = new AttributeValue();
            plateFlagCodeValue.setDesc(value);
            plateFlagCodeValue.setValue(key);
            plateFlagCodeValueList.add(plateFlagCodeValue);
        }
        plateFlagCodeAttribute.setValues(plateFlagCodeValueList);
        carAttributeList.add(plateFlagCodeAttribute);

        //车颜色
        Attribute vehicleColorAttribute = new Attribute();
        vehicleColorAttribute.setDesc("车颜色");
        vehicleColorAttribute.setLogistic(Logistic.AND);
        List<AttributeValue> vehicleColorValueList = new ArrayList<>();
        Map<Integer, String> vehicle_color = vehicleMap.get("vehicle_color");
        for (Integer key : vehicle_color.keySet()) {
            String value = vehicle_color.get(key);
            AttributeValue vehicleColorValue = new AttributeValue();
            vehicleColorValue.setDesc(value);
            vehicleColorValue.setValue(key);
            vehicleColorValueList.add(vehicleColorValue);
        }
        vehicleColorAttribute.setValues(vehicleColorValueList);
        carAttributeList.add(vehicleColorAttribute);

        //车辆类型
        Attribute vehicleTypeAttribute = new Attribute();
        vehicleTypeAttribute.setDesc("车辆类型");
        vehicleTypeAttribute.setLogistic(Logistic.AND);
        List<AttributeValue> vehicleTypeValueList = new ArrayList<>();
        Map<Integer, String> vehicle_type = vehicleMap.get("vehicle_type");
        for (Integer key : vehicle_type.keySet()) {
            String value = vehicle_type.get(key);
            AttributeValue vehicleTypeValue = new AttributeValue();
            vehicleTypeValue.setDesc(value);
            vehicleTypeValue.setValue(key);
            vehicleTypeValueList.add(vehicleTypeValue);
        }
        vehicleTypeAttribute.setValues(vehicleTypeValueList);
        carAttributeList.add(vehicleTypeAttribute);

        //车辆行驶方向
        Attribute mistakeCodeAttribute = new Attribute();
        mistakeCodeAttribute.setDesc("车辆行驶方向");
        mistakeCodeAttribute.setLogistic(Logistic.AND);
        List<AttributeValue> mistakeCodeValueList = new ArrayList<>();
        Map<Integer, String> mistake_code = vehicleMap.get("mistake_code");
        for (Integer key : mistake_code.keySet()) {
            String value = mistake_code.get(key);
            AttributeValue vehicleTypeValue = new AttributeValue();
            vehicleTypeValue.setDesc(value);
            vehicleTypeValue.setValue(key);
            mistakeCodeValueList.add(vehicleTypeValue);
        }
        mistakeCodeAttribute.setValues(mistakeCodeValueList);
        carAttributeList.add(mistakeCodeAttribute);

        //天窗
        Attribute sunroofCodeAttribute = new Attribute();
        sunroofCodeAttribute.setDesc("天窗");
        sunroofCodeAttribute.setLogistic(Logistic.AND);

        List<AttributeValue>sunroofCodeValueList = new ArrayList<>();
        Map<Integer, String> sunroof_code = vehicleMap.get("sunroof_code");
        for (Integer key : sunroof_code.keySet()) {
            String value = sunroof_code.get(key);
            AttributeValue vsunroofCodeValue = new AttributeValue();
            vsunroofCodeValue.setDesc(value);
            vsunroofCodeValue.setValue(key);
            sunroofCodeValueList.add(vsunroofCodeValue);
        }
        sunroofCodeAttribute.setValues(sunroofCodeValueList);
        carAttributeList.add(sunroofCodeAttribute);

        return carAttributeList;
    }


//    public static void main(String[] args) {
//        List<Attribute> carAttribute = new AttributeService().getCarAttribute();
//        System.out.println(JSON.toJSONString(carAttribute));
//    }
}

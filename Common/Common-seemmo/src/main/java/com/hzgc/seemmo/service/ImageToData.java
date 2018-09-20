package com.hzgc.seemmo.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hzgc.seemmo.bean.ImageResult;
import com.hzgc.seemmo.bean.carbean.Vehicle;
import com.hzgc.seemmo.bean.personbean.Person;
import com.hzgc.seemmo.util.CutImageUtil;
import com.hzgc.seemmo.util.JsonUtil;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ImageToData {

    private static CloseableHttpClient httpClient = null;

    //httpClient单例
    private static synchronized CloseableHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = HttpClientBuilder.create().build();
        }
        return httpClient;
    }

    //执行post请求
    public static String executeHttpPost(String url, String imageJsonString) {
        try {
            httpClient = getHttpClient();
            HttpPost httpPost = new HttpPost(url);
            StringEntity stringEntity = new StringEntity(imageJsonString);
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
            CloseableHttpResponse res = httpClient.execute(httpPost);
            if (res.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                return EntityUtils.toString(res.getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //获取数据 0表示获取车的数据，1表示获取人的数据，其他表示获取所有
    @SuppressWarnings("unchecked")
    public static ImageResult getData(String result, String imagePath, String tag, byte[] bytes) {
        JSONObject jsonObject = JsonUtil.stringToJsonObject(result);
        int code = (int) jsonObject.get("code");
        if (0 == code) {
            JSONObject data = (JSONObject) jsonObject.get("data");
            List <JSONObject> imageResults = (List <JSONObject>) data.get("ImageResults");
            ImageResult imageResult = new ImageResult();
            if ("0".equals(tag)) {
                List <Vehicle> vehicleList = ImageToData.getCarData(imageResults, imagePath, bytes);
                imageResult.setVehicleList(vehicleList);
            } else if ("1".equals(tag)) {
                List <Person> personList = ImageToData.getPersonData(imageResults, imagePath, bytes);
                imageResult.setPersonList(personList);
            } else {
                List <Vehicle> vehicleList = ImageToData.getCarData(imageResults, imagePath, bytes);
                List <Person> personList = ImageToData.getPersonData(imageResults, imagePath, bytes);
                imageResult.setVehicleList(vehicleList);
                imageResult.setPersonList(personList);
            }
            return imageResult;
        }
        return null;
    }

    //车辆数据封装
    @SuppressWarnings("unchecked")
    public static List <Vehicle> getCarData(List <JSONObject> imageResults, String imagePath, byte[] bytess) {
        ArrayList <Vehicle> vehicleList = new ArrayList <>();
        if (imageResults.size() > 0) {
            for (JSONObject js : imageResults) {
                List <JSONObject> vehicles = (List <JSONObject>) js.get("Vehicles");
                if (vehicles.size() > 0) {
                    for (JSONObject js1 : vehicles) {
                        Vehicle vehicle_object = new Vehicle();
                        int vehicle_object_type = (int) js1.get("Type");
                        vehicle_object.setVehicle_object_type(String.valueOf(vehicle_object_type));
                        JSONObject recognize = (JSONObject) js1.get("Recognize");
                        JSONObject detect = (JSONObject) js1.get("Detect");
                        JSONObject car = (JSONObject) detect.get("Body");
                        JSONArray rect = (JSONArray) car.get("Rect");
                        JSONObject plate = (JSONObject) recognize.get("Plate");
                        if (null != plate) {
                            int code = (int) plate.get("Code");
                            if (0 == code) {
                                JSONObject color = (JSONObject) plate.get("Color");
                                vehicle_object.setPlate_color_code((String) color.get("Code"));
                                //1是false，2是true
                                if ((boolean) plate.get("Shelter")) {
                                    vehicle_object.setPlate_schelter_code("2");
                                } else {
                                    vehicle_object.setPlate_schelter_code("1");
                                }
                                vehicle_object.setPlate_flag_code(String.valueOf((int) plate.get("Flag")));
                                vehicle_object.setPlate_licence((String) plate.get("Licence"));
                                if ((boolean) plate.get("Destain")) {
                                    vehicle_object.setPlate_destain_code("2");
                                } else {
                                    vehicle_object.setPlate_destain_code("1");
                                }
                                vehicle_object.setPlate_type_code(String.valueOf((int) plate.get("Type")));
                            }
                        }
                        JSONObject crash = (JSONObject) recognize.get("Crash");
                        if (null != crash) {
                            int code = (int) crash.get("Code");
                            if (0 == code) {
                                if ((boolean) crash.get("HasCrash")) {
                                    vehicle_object.setCrash_code("2");
                                } else {
                                    vehicle_object.setCrash_code("1");
                                }
                            }
                        }
                        JSONObject danger = (JSONObject) recognize.get("Danger");
                        if (null != danger) {
                            int code = (int) danger.get("Code");
                            if (0 == code) {
                                if ((boolean) danger.get("HasDanger")) {
                                    vehicle_object.setDanger_code("2");
                                } else {
                                    vehicle_object.setDanger_code("1");
                                }
                            }
                        }
                        JSONObject color = (JSONObject) recognize.get("Color");
                        if (null != color) {
                            int code = (int) color.get("Code");
                            if (0 == code) {
                                List <JSONObject> topList = (List <JSONObject>) color.get("TopList");
                                JSONObject jsonObject1 = topList.get(0);
                                vehicle_object.setVehicle_color((String) jsonObject1.get("Code"));
                            }
                        }
                        JSONObject brand = (JSONObject) recognize.get("Brand");
                        if (null != brand) {
                            int code = (int) brand.get("Code");
                            if (0 == code) {
                                List <JSONObject> topList = (List <JSONObject>) brand.get("TopList");
                                JSONObject jsonObject1 = topList.get(0);
                                vehicle_object.setBrand_name((String) jsonObject1.get("Name"));
                            }
                        }
                        JSONObject sunroof = (JSONObject) recognize.get("Sunroof");
                        if (null != sunroof) {
                            int code = (int) sunroof.get("Code");
                            if (0 == code) {
                                List <JSONObject> topList = (List <JSONObject>) sunroof.get("TopList");
                                JSONObject jsonObject1 = topList.get(0);
                                vehicle_object.setSunroof_code((String) jsonObject1.get("Code"));
                            }
                        }
                        JSONObject call = (JSONObject) recognize.get("Call");
                        if (null != call) {
                            int code = (int) call.get("Code");
                            if (0 == code) {
                                if ((boolean) call.get("HasCall")) {
                                    vehicle_object.setCall_code("2");
                                } else {
                                    vehicle_object.setCall_code("1");
                                }
                            }
                        }
                        JSONObject marker = (JSONObject) recognize.get("Marker");
                        if (null != marker) {
                            int code = (int) marker.get("Code");
                            if (0 == code) {
                                vehicle_object.setMarker_code("2");
                            } else {
                                vehicle_object.setMarker_code("1");
                            }
                        }
                        JSONObject mistake = (JSONObject) recognize.get("Mistake");
                        if (null != mistake) {
                            int code = (int) mistake.get("Code");
                            if (0 == code) {
                                List <JSONObject> topList = (List <JSONObject>) mistake.get("TopList");
                                JSONObject jsonObject1 = topList.get(0);
                                vehicle_object.setMistake_code((String) jsonObject1.get("Code"));
                            }
                        }
                        JSONObject type = (JSONObject) recognize.get("Type");
                        if (null != type) {
                            int code = (int) type.get("Code");
                            if (0 == code) {
                                List <JSONObject> topList = (List <JSONObject>) type.get("TopList");
                                JSONObject jsonObject1 = topList.get(0);
                                vehicle_object.setVehicle_type((String) jsonObject1.get("Code"));
                            }
                        }
                        JSONObject rack = (JSONObject) recognize.get("Rack");
                        if (null != rack) {
                            int code = (int) rack.get("Code");
                            if (0 == code) {
                                List <JSONObject> topList = (List <JSONObject>) rack.get("TopList");
                                JSONObject jsonObject1 = topList.get(0);
                                vehicle_object.setRack_code((String) jsonObject1.get("Code"));
                            }
                        }
                        JSONObject spareTire = (JSONObject) recognize.get("SpareTire");
                        if (null != spareTire) {
                            int code = (int) spareTire.get("Code");
                            if (0 == code) {
                                if ((boolean) spareTire.get("HasSpareTire")) {
                                    vehicle_object.setSparetire_code("2");
                                } else {
                                    vehicle_object.setSparetire_code("1");
                                }
                            }
                        }
                        JSONObject belt = (JSONObject) recognize.get("Belt");
                        if (null != belt) {
                            int code = (int) belt.get("Code");
                            if (0 == code) {
                                JSONObject coDriver = (JSONObject) belt.get("CoDriver");
                                if ((boolean) coDriver.get("NoBelt")) {
                                    vehicle_object.setBelt_codriver("2");
                                } else {
                                    vehicle_object.setBelt_codriver("1");
                                }
                                JSONObject mainDriver = (JSONObject) belt.get("MainDriver");
                                if ((boolean) mainDriver.get("NoBelt")) {
                                    vehicle_object.setBelt_maindriver("2");
                                } else {
                                    vehicle_object.setBelt_maindriver("1");
                                }
                            }
                        }
                        if (null != imagePath && imagePath.length() > 0) {
                            //截图车的图片
                            CutImageUtil cutImageUtil = new CutImageUtil((int) rect.get(0), (int) rect.get(1), (int) rect.get(2), (int) rect.get(3));
                            cutImageUtil.setSrcpath(imagePath);
                            try {
                                byte[] bytes = cutImageUtil.cut();
                                vehicle_object.setVehicle_data(bytes);
                                System.out.println(JSON.toJSONString(vehicle_object));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        if (null != bytess) {
                            //截图车的图片
                            CutImageUtil cutImageUtil = new CutImageUtil((int) rect.get(0), (int) rect.get(1), (int) rect.get(2), (int) rect.get(3));
                            try {
                                byte[] bytes = cutImageUtil.cut(bytess);
                                vehicle_object.setVehicle_data(bytes);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        vehicleList.add(vehicle_object);
                    }
                }
            }
        }
        return vehicleList;
    }

    //行人的数据封装
    @SuppressWarnings("unchecked")
    public static List <Person> getPersonData(List <JSONObject> imageResults, String imagePath, byte[] bytess) {
        ArrayList <Person> personList = new ArrayList <>();
        if (imageResults.size() > 0) {
            for (JSONObject jsonObject : imageResults) {
                List <JSONObject> bikes = (List <JSONObject>) jsonObject.get("Bikes");
                List <JSONObject> pedestrains = (List <JSONObject>) jsonObject.get("Pedestrains");
                if (pedestrains.size() > 0) {
                    for (JSONObject js : pedestrains) {
                        Person person_object = new Person();
                        int car_type = (Integer) js.get("Type");
                        person_object.setCar_type(String.valueOf(car_type));
                        JSONObject person_detect = (JSONObject) js.get("Detect");
                        int person_code = (int) person_detect.get("Code");
                        JSONArray person_rect = null;
                        if (0 == person_code) {
                            JSONObject person_body = (JSONObject) person_detect.get("Body");
                            person_rect = (JSONArray) person_body.get("Rect");
                        }
                        JSONObject recognize = (JSONObject) js.get("Recognize");
                        //行人数据解析
                        personDataAnalysis(recognize, person_object);
                        if (null != imagePath && imagePath.length() > 0) {
                            CutImageUtil cutImageUtil = new CutImageUtil((int) person_rect.get(0), (int) person_rect.get(1), (int) person_rect.get(2), (int) person_rect.get(3));
                            cutImageUtil.setSrcpath(imagePath);
                            try {
                                byte[] bytes = cutImageUtil.cut();
                                person_object.setCar_data(bytes);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        personList.add(person_object);
                    }
                }
                if (bikes.size() > 0) {
                    for (JSONObject js : bikes) {
                        Person person_object = new Person();
                        JSONObject car_detect = (JSONObject) js.get("Detect");
                        int car_code = (int) car_detect.get("Code");
                        JSONArray car_rect = null;
                        if (0 == car_code) {
                            int car_type = (int) js.get("Type");
                            person_object.setCar_type(String.valueOf(car_type));
                            JSONObject car_body = (JSONObject) car_detect.get("Body");
                            car_rect = (JSONArray) car_body.get("Rect");
                        }
                        List <JSONObject> persons = (List <JSONObject>) js.get("Persons");
                        if (persons.size() > 0) {
                            for (JSONObject person : persons) {
                                JSONArray person_rect = null;
                                JSONObject detect = (JSONObject) person.get("Detect");
                                if (null != detect) {
                                    int person_code = (int) detect.get("Code");
                                    if (0 == person_code) {
                                        JSONObject body = (JSONObject) detect.get("Body");
                                        person_rect = (JSONArray) body.get("Rect");
                                    }
                                }
                                JSONObject recognize = (JSONObject) person.get("Recognize");
                                //数据解析
                                personDataAnalysis(recognize, person_object);
                                if (null != imagePath && imagePath.length() > 0) {
                                    CutImageUtil cutImageUtil;
                                    if (car_rect != null && person_rect != null) {
                                        cutImageUtil = new CutImageUtil((int) car_rect.get(0) < (int) person_rect.get(0) ? (int) car_rect.get(0) : (int) person_rect.get(0),
                                                (int) car_rect.get(1) < (int) person_rect.get(1) ? (int) car_rect.get(1) : (int) person_rect.get(1),
                                                (int) car_rect.get(2) > (int) person_rect.get(2) ? (int) car_rect.get(2) : (int) person_rect.get(2),
                                                (int) car_rect.get(3) > (int) person_rect.get(3) ? (int) car_rect.get(3) : (int) person_rect.get(3));
                                        cutImageUtil.setSrcpath(imagePath);
                                    } else {
                                        cutImageUtil = new CutImageUtil((int) person_rect.get(0), (int) person_rect.get(1), (int) person_rect.get(2), (int) person_rect.get(3));
                                        cutImageUtil.setSrcpath(imagePath);
                                    }
                                    try {
                                        byte[] bytes = cutImageUtil.cut();
                                        person_object.setCar_data(bytes);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (null != bytess) {
                                    CutImageUtil cutImageUtil;
                                    if (car_rect != null && person_rect != null) {
                                        cutImageUtil = new CutImageUtil((int) car_rect.get(0) < (int) person_rect.get(0) ? (int) car_rect.get(0) : (int) person_rect.get(0),
                                                (int) car_rect.get(1) < (int) person_rect.get(1) ? (int) car_rect.get(1) : (int) person_rect.get(1),
                                                (int) car_rect.get(2) > (int) person_rect.get(2) ? (int) car_rect.get(2) : (int) person_rect.get(2),
                                                (int) car_rect.get(3) > (int) person_rect.get(3) ? (int) car_rect.get(3) : (int) person_rect.get(3));
                                        cutImageUtil.setSrcpath(imagePath);
                                    } else {
                                        cutImageUtil = new CutImageUtil((int) person_rect.get(0), (int) person_rect.get(1), (int) person_rect.get(2), (int) person_rect.get(3));
                                    }
                                    try {
                                        byte[] bytes = cutImageUtil.cut(bytess);
                                        person_object.setCar_data(bytes);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                personList.add(person_object);
                            }
                        }
                    }
                }
            }
        }
        return personList;
    }

    @SuppressWarnings("unchecked")
    public static void personDataAnalysis(JSONObject recognize, Person person_object) {
        JSONObject shoulderBag = (JSONObject) recognize.get("ShoulderBag");
        if (null != shoulderBag) {
            int code = (int) shoulderBag.get("Code");
            if (0 == code) {
                List <JSONObject> topList = (List <JSONObject>) shoulderBag.get("TopList");
                JSONObject jsonObject1 = topList.get(0);
                person_object.setShoulderbag_code((String) jsonObject1.get("Code"));
            }
        }
        JSONObject bottomType = (JSONObject) recognize.get("BottomType");
        if (null != bottomType) {
            int code = (int) bottomType.get("Code");
            if (0 == code) {
                List <JSONObject> topList = (List <JSONObject>) bottomType.get("TopList");
                JSONObject jsonObject1 = topList.get(0);
                person_object.setBottomtype_code((String) jsonObject1.get("Code"));
            }
        }
        JSONObject umbrella = (JSONObject) recognize.get("Umbrella");
        if (null != umbrella) {
            int code = (int) umbrella.get("Code");
            if (0 == code) {
                List <JSONObject> topList = (List <JSONObject>) umbrella.get("TopList");
                JSONObject jsonObject1 = topList.get(0);
                person_object.setUmbrella_code((String) jsonObject1.get("Code"));
            }
        }
        JSONObject orientation = (JSONObject) recognize.get("Orientation");
        if (null != orientation) {
            int code = (int) orientation.get("Code");
            if (0 == code) {
                List <JSONObject> topList = (List <JSONObject>) orientation.get("TopList");
                JSONObject jsonObject1 = topList.get(0);
                person_object.setOrientation_code((String) jsonObject1.get("Code"));
            }
        }
        JSONObject messengerBag = (JSONObject) recognize.get("MessengerBag");
        if (null != messengerBag) {
            int code = (int) messengerBag.get("Code");
            if (0 == code) {
                List <JSONObject> topList = (List <JSONObject>) messengerBag.get("TopList");
                JSONObject jsonObject1 = topList.get(0);
                person_object.setMessengerbag_code((String) jsonObject1.get("Code"));
            }
        }
        JSONObject upperType = (JSONObject) recognize.get("UpperType");
        if (null != upperType) {
            int code = (int) upperType.get("Code");
            if (0 == code) {
                List <JSONObject> topList = (List <JSONObject>) upperType.get("TopList");
                JSONObject jsonObject1 = topList.get(0);
                person_object.setUppertype_code((String) jsonObject1.get("Code"));
            }
        }
        JSONObject age = (JSONObject) recognize.get("Age");
        if (null != age) {
            int code = (int) age.get("Code");
            if (0 == code) {
                List <JSONObject> topList = (List <JSONObject>) age.get("TopList");
                JSONObject jsonObject1 = topList.get(0);
                person_object.setAge_code((String) jsonObject1.get("Code"));
            }
        }
        JSONObject upperColor = (JSONObject) recognize.get("UpperColor");
        if (null != upperColor) {
            int code = (int) upperColor.get("Code");
            if (0 == code) {
                List <JSONObject> topList = (List <JSONObject>) upperColor.get("TopList");
                JSONObject jsonObject1 = topList.get(0);
                person_object.setUppercolor_code((String) jsonObject1.get("Code"));
            }
        }
        JSONObject sex = (JSONObject) recognize.get("Sex");
        if (null != sex) {
            int code = (int) sex.get("Code");
            if (0 == code) {
                List <JSONObject> topList = (List <JSONObject>) sex.get("TopList");
                JSONObject jsonObject1 = topList.get(0);
                person_object.setSex_code((String) jsonObject1.get("Code"));
            }
        }
        JSONObject hair = (JSONObject) recognize.get("Hair");
        if (null != hair) {
            int code = (int) hair.get("Code");
            if (0 == code) {
                List <JSONObject> topList = (List <JSONObject>) hair.get("TopList");
                JSONObject jsonObject1 = topList.get(0);
                person_object.setHair_code((String) jsonObject1.get("Code"));
            }
        }
        JSONObject bag = (JSONObject) recognize.get("Bag");
        if (null != bag) {
            int code = (int) bag.get("Code");
            if (0 == code) {
                List <JSONObject> topList = (List <JSONObject>) bag.get("TopList");
                JSONObject jsonObject1 = topList.get(0);
                person_object.setBag_code((String) jsonObject1.get("Code"));
            }
        }
        JSONObject knapsack = (JSONObject) recognize.get("Knapsack");
        if (null != knapsack) {
            int code = (int) knapsack.get("Code");
            if (0 == code) {
                List <JSONObject> topList = (List <JSONObject>) knapsack.get("TopList");
                JSONObject jsonObject1 = topList.get(0);
                person_object.setKnapsack_code((String) jsonObject1.get("Code"));
            }
        }
        JSONObject baby = (JSONObject) recognize.get("Baby");
        if (null != baby) {
            int code = (int) baby.get("Code");
            if (0 == code) {
                List <JSONObject> topList = (List <JSONObject>) baby.get("TopList");
                JSONObject jsonObject1 = topList.get(0);
                person_object.setBaby_code((String) jsonObject1.get("Code"));
            }
        }
        JSONObject hat = (JSONObject) recognize.get("Hat");
        if (null != hat) {
            int code = (int) hat.get("Code");
            if (0 == code) {
                List <JSONObject> topList = (List <JSONObject>) hat.get("TopList");
                JSONObject jsonObject1 = topList.get(0);
                person_object.setHat_code((String) jsonObject1.get("Code"));
            }
        }
        JSONObject bottomColor = (JSONObject) recognize.get("BottomColor");
        if (null != bottomColor) {
            int code = (int) bottomColor.get("Code");
            if (0 == code) {
                List <JSONObject> topList = (List <JSONObject>) bottomColor.get("TopList");
                JSONObject jsonObject1 = topList.get(0);
                person_object.setBottomcolor_code((String) jsonObject1.get("Code"));
            }
        }
    }

    /*
     * @param url:算法提取路径
     * @param imagePath:图片路径
     * @param tag:0表示车只提取车属性，1表示提取人属性，其余表示提取所有
     * 获取图片的结构化信息
     * */
    public static ImageResult getImageResult(String url, String imagePath, String tag) {
        String imageJsonString = JsonUtil.objectToJsonString(imagePath);
        String s = ImageToData.executeHttpPost(url, imageJsonString);
        return ImageToData.getData(s, imagePath, tag, null);
    }

    public static ImageResult getImageResult(String url, byte[] bytes, String tag) {
        String imageJsonString = JsonUtil.objectToJsonString(bytes);
        String s = ImageToData.executeHttpPost(url, imageJsonString);
        return ImageToData.getData(s, null, tag, bytes);
    }
}
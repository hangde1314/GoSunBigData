package com.hzgc.datasyncer.bean;

import com.hzgc.datasyncer.esearch.CustomField;
import com.hzgc.datasyncer.esearch.CustomFieldType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = DocumentInfo.CAR_INDEX_NAME, type = DocumentInfo.CAR_TYPE)
public class EsCarObject {
    @Id
    private String id;  //唯一ID

    @CustomField(type = CustomFieldType.Keyword)
    private String ipcid;   //设备序列号

    @CustomField(type = CustomFieldType.Date, format = DateFormat.custom, pattern = "yyyy-MM-dd HH:mm:ss")
    private String timestamp;   //抓拍时间

    @CustomField(type = CustomFieldType.Keyword)
    private String hostname;    //FTP服务器主机名

    @CustomField(type = CustomFieldType.Keyword)
    private String sabsolutepath;   //小图路径

    @CustomField(type = CustomFieldType.Keyword)
    private String babsolutepath;   //大图路径

    @CustomField(type = CustomFieldType.Keyword)
    private String provinceid;  //省或直辖市ID

    @CustomField(type = CustomFieldType.Keyword)
    private String cityid;  //市或直辖市下属区ID

    @CustomField(type = CustomFieldType.Keyword)
    private String regionid;    //区域ID或者直辖市下属街道ID

    @CustomField(type = CustomFieldType.Keyword)
    private String communityid; //社区ID

    @CustomField(type = CustomFieldType.Keyword)
    private String feature;

    @CustomField(type = CustomFieldType.Keyword)
    private byte[] bitfeature;

    @CustomField(type = CustomFieldType.Keyword)
    private String vehicle_object_type;

    @CustomField(type = CustomFieldType.Keyword)
    private String belt_maindriver;

    @CustomField(type = CustomFieldType.Keyword)
    private String belt_codriver;

    @CustomField(type = CustomFieldType.Keyword)
    private String brand_name;

    @CustomField(type = CustomFieldType.Keyword)
    private String call_code;

    @CustomField(type = CustomFieldType.Keyword)
    private String vehicle_color;

    @CustomField(type = CustomFieldType.Keyword)
    private String crash_code;

    @CustomField(type = CustomFieldType.Keyword)
    private String danger_code;

    @CustomField(type = CustomFieldType.Keyword)
    private String marker_code;

    @CustomField(type = CustomFieldType.Keyword)
    private String plate_schelter_code;

    @CustomField(type = CustomFieldType.Keyword)
    private String plate_flag_code;

    @CustomField(type = CustomFieldType.Keyword)
    private String plate_licence;

    @CustomField(type = CustomFieldType.Keyword)
    private String plate_destain_code;

    @CustomField(type = CustomFieldType.Keyword)
    private String plate_color_code;

    @CustomField(type = CustomFieldType.Keyword)
    private String plate_type_code;

    @CustomField(type = CustomFieldType.Keyword)
    private String rack_code;

    @CustomField(type = CustomFieldType.Keyword)
    private String sunroof_code;

    @CustomField(type = CustomFieldType.Keyword)
    private String vehicle_type;

}

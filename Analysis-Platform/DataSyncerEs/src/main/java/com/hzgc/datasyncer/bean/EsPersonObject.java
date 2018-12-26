package com.hzgc.datasyncer.bean;

import com.hzgc.datasyncer.esearch.CustomField;
import com.hzgc.datasyncer.esearch.CustomFieldType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Document(indexName = DocumentInfo.PERSON_INDEX_NAME, type = DocumentInfo.PERSON_TYPE)
public class EsPersonObject {
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
    private String age;

    @CustomField(type = CustomFieldType.Keyword)
    private String baby;

    @CustomField(type = CustomFieldType.Keyword)
    private String bag;

    @CustomField(type = CustomFieldType.Keyword)
    private String bottomcolor;

    @CustomField(type = CustomFieldType.Keyword)
    private String bottomtype;

    @CustomField(type = CustomFieldType.Keyword)
    private String hat;

    @CustomField(type = CustomFieldType.Keyword)
    private String hair;

    @CustomField(type = CustomFieldType.Keyword)
    private String knapsack;

    @CustomField(type = CustomFieldType.Keyword)
    private String messengerbag;

    @CustomField(type = CustomFieldType.Keyword)
    private String orientation;

    @CustomField(type = CustomFieldType.Keyword)
    private String sex;

    @CustomField(type = CustomFieldType.Keyword)
    private String shoulderbag;

    @CustomField(type = CustomFieldType.Keyword)
    private String umbrella;

    @CustomField(type = CustomFieldType.Keyword)
    private String uppercolor;

    @CustomField(type = CustomFieldType.Keyword)
    private String uppertype;

    @CustomField(type = CustomFieldType.Keyword)
    private String cartype;

    @CustomField(type = CustomFieldType.Keyword)
    private String feature;

    @CustomField(type = CustomFieldType.Keyword)
    private String bitfeature;

}

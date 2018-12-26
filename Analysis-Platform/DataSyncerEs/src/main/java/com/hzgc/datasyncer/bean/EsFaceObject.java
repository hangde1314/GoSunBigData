package com.hzgc.datasyncer.bean;

import com.hzgc.datasyncer.esearch.CustomField;
import com.hzgc.datasyncer.esearch.CustomFieldType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;


@Data
@Document(indexName = DocumentInfo.FACE_INDEX_NAME, type = DocumentInfo.FACE_TYPE)
public class EsFaceObject {
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
    private int eyeglasses;

    @CustomField(type = CustomFieldType.Keyword)
    private int age;

    @CustomField(type = CustomFieldType.Keyword)
    private int mask;

    @CustomField(type = CustomFieldType.Keyword)
    private int gender;

    @CustomField(type = CustomFieldType.Keyword)
    private int huzi;

    @CustomField(type = CustomFieldType.Keyword)
    private int sharpness;

    @CustomField(type = CustomFieldType.Keyword)
    private String feature;

    @CustomField(type = CustomFieldType.BINARY)
    private byte[] bitfeature;
}

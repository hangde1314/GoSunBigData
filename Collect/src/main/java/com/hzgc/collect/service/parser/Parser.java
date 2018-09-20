package com.hzgc.collect.service.parser;

public interface Parser {
    // 判断FTP当前上传文件是否需要解析
    boolean canParse(String path);
    // FTP上传路径解析
    FtpPathMetaData parse(String path);
    // 小图ftpUrl转大图ftpUrl
    String surlToBurl(String surl);
    // 大图ftpUrl转小图ftpUrl(带ip,hostname的ftpUrl都可以转)
    String ftpUrl_b2s(String burl, String type, int index);
    // 大图路径转小图路径(绝对路径和ftp相对路径都可以转)
    String path_b2s(String burl, String type, int index);
    //通过上传文件路径解析到ftpUrl
    String getFtpUrl_ip(String filePath);
    //通过上传文件路径解析到ftpUrl（ftp发送至kafka的key）
    String getFtpUrl_hostname(String filePath);
}

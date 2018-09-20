package com.hzgc.collect.expand.parser;

public class BoxParser extends AbstractParser {
    /**
     * 判断是否是可处理图片
     *
     * @param path 文件上传至ftp的相对路径
     * @return 是否可处理
     */
    @Override
    public boolean canParse(String path) {
        if (path.contains("unknown") || !path.contains(".jpg")) {
            return false;
        }
        String tmpStr = path.substring(path.lastIndexOf("_") + 1, path.lastIndexOf("."));
        return Integer.parseInt(tmpStr) == 0;
    }

    /**
     * 根据文件上传至ftp的绝对路径获取到ipcID、TimeStamp、Date、TimeSlot
     *
     * @param path 文件上传至ftp的相对路径，例如：/3B0383FPAG51511/2017/05/23/16/00/2017_05_23_16_00_15_5704_0.jpg
     * @return 设备、时间等信息 例如：{date=2017-05-23, sj=1600, ipcID=3B0383FPAG51511, time=2017-05-23 16:00:15}
     */
    @Override
    public FtpPathMetaData parse(String path) {
        FtpPathMetaData message = new FtpPathMetaData();
        String ipcID = path.substring(1, path.indexOf("/", 1));
        String timeStr = path.substring(path.lastIndexOf("/") + 1, path.lastIndexOf("_")).replace("_", "");

        String year = timeStr.substring(0, 4);
        String month = timeStr.substring(4, 6);
        String day = timeStr.substring(6, 8);
        String hour = timeStr.substring(8, 10);
        String minute = timeStr.substring(10, 12);
        String second = timeStr.substring(12, 14);

        StringBuilder time = new StringBuilder();
        time = time.append(year).append("-").append(month).append("-").append(day).
                append(" ").append(hour).append(":").append(minute).append(":").append(second);

        message.setIpcid(ipcID);
        message.setTimeStamp(time.toString());
        return message;
    }

    @Override
    public String surlToBurl(String surl) {
        return surl.substring(0, surl.lastIndexOf("_") + 1)
                + 0
                + surl.substring(surl.lastIndexOf("."));
    }

    @Override
    public String ftpUrl_b2s(String burl, String type, int index) {
        return burl.substring(0, burl.lastIndexOf("_") + 1)
                + type
                + index
                + burl.substring(burl.lastIndexOf("."));
    }

    @Override
    public String path_b2s(String burl, String type, int index) {
        return burl.substring(0, burl.lastIndexOf("_") + 1)
                + type
                + index
                + burl.substring(burl.lastIndexOf("."));
    }
}

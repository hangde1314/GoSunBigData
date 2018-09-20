package com.hzgc.collect.expand.parser;

/**
 * 大华抓拍机路径解析
 * 相机配置根路径: IPC-HFW5238M-AS-I1
 */
public class DaHuaParser_HFW5238M extends AbstractParser {
    @Override
    public boolean canParse(String path) {
        if (path.contains("unknown") || !path.contains(".jpg")) {
            return false;
        }
        String tmpStr = path.substring(path.lastIndexOf("[") + 1, path.lastIndexOf("]"));
        return Integer.parseInt(tmpStr) == 0;
    }

    /**
     * eg: /IPC-HFW5238M-AS-I1/3J07C6FPAU00272/2018-07-25/001/jpg/09/50/04[M][0@0][1].jpg
     */
    @Override
    public FtpPathMetaData parse(String path) {
        FtpPathMetaData message = new FtpPathMetaData();
        String ipcID = path.substring(path.indexOf("/", 1))
                .substring(1, path.substring(path.indexOf("/", 1)).indexOf("/", 1));
        String dateStr = path.split("/")[3].replace("-", "");
        String year = dateStr.substring(0, 4);
        String month = dateStr.substring(4, 6);
        String day = dateStr.substring(6, 8);
        String hour = path.split("/")[6];
        String minute = path.split("/")[7];
        String second = path.split("/")[8].substring(0, 2);

        message.setIpcid(ipcID);
        String time = year + "-" + month + "-" + day +
                " " + hour + ":" + minute + ":" + second;
        message.setTimeStamp(time);
        return message;
    }

    /**
     * eg:
     * ftp://s100:2121/IPC-HFW5238M-AS-I1/3J07C6FPAU00272/2018-07-25/001/jpg/09/50/04[M][0@0][1].jpg
     * ------>
     * ftp://s100:2121/IPC-HFW5238M-AS-I1/3J07C6FPAU00272/2018-07-25/001/jpg/09/50/04[M][0@0][0].jpg
     */
    @Override
    public String surlToBurl(String surl) {
        return surl.substring(0, surl.lastIndexOf("[") + 1)
                + 0
                + surl.substring(surl.lastIndexOf("[") + 2, surl.length());
    }

    @Override
    public String ftpUrl_b2s(String burl, String type, int index) {
        return burl.substring(0, burl.lastIndexOf("[") + 1)
                + type
                + index
                + burl.substring(burl.lastIndexOf("[") + 2, burl.length());
    }

    @Override
    public String path_b2s(String burl, String type, int index) {
        return burl.substring(0, burl.lastIndexOf("[") + 1)
                + type
                + index
                + burl.substring(burl.lastIndexOf("[") + 2, burl.length());
    }
}

package com.hzgc.collect.service.parser;

public class FtpPathBootStrap {
    private static Parser boxParser = new BoxParser();
    private static Parser daHuaParser_hfw5238M = new DaHuaParser_HFW5238M();
    private static Parser daHuaParser_hdbw5238R = new DaHuaParser_HDBW5238R();

    public static Parser getParser(String fileName) {
        if (fileName.contains(DeviceModel.DAHUA_HDBW5238R)) {
            return daHuaParser_hfw5238M.canParse(fileName) ? daHuaParser_hfw5238M : null;
        } else if (fileName.contains(DeviceModel.DAHUA_HDBW5238R)) {
            return daHuaParser_hdbw5238R.canParse(fileName) ? daHuaParser_hdbw5238R : null;
        } else {
            return boxParser.canParse(fileName) ? boxParser : null;
        }
    }
}

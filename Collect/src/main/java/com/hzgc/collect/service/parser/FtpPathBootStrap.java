package com.hzgc.collect.service.parser;

import com.hzgc.collect.config.CollectContext;

public class FtpPathBootStrap {
    private Parser boxParser;
    private Parser daHuaParser_hfw5238M;
    private Parser daHuaParser_hdbw5238R;

    public FtpPathBootStrap(CollectContext collectContext) {
        boxParser = new BoxParser(collectContext);
        daHuaParser_hfw5238M = new DaHuaParser_HFW5238M(collectContext);
        daHuaParser_hdbw5238R = new DaHuaParser_HDBW5238R(collectContext);
    }

    public Parser getParser(String fileName) {
        if (fileName.contains(DeviceModel.DAHUA_HFW5238M)) {
            return daHuaParser_hfw5238M.canParse(fileName) ? daHuaParser_hfw5238M : null;
        } else if (fileName.contains(DeviceModel.DAHUA_HDBW5238R)) {
            return daHuaParser_hdbw5238R.canParse(fileName) ? daHuaParser_hdbw5238R : null;
        } else {
            return boxParser.canParse(fileName) ? boxParser : null;
        }
    }
}

package com.hzgc.collect.expand.util;

import java.util.Arrays;
import java.util.List;

public class FtpType {
    static {
        List<String> ftpTypes = Arrays.asList(CollectProperties.getFtpType().split(","));

    }
}

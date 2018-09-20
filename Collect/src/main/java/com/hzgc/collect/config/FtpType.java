package com.hzgc.collect.config;

import java.util.Arrays;
import java.util.List;

public class FtpType {
    static {
        List<String> ftpTypes = Arrays.asList(CollectConfiguration.getFtpType().split(","));

    }
}

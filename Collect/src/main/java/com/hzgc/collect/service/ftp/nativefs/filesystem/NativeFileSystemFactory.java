/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.hzgc.collect.service.ftp.nativefs.filesystem;

import com.hzgc.collect.config.CollectContext;
import com.hzgc.collect.service.ftp.ftplet.FileSystemFactory;
import com.hzgc.collect.service.ftp.ftplet.FileSystemView;
import com.hzgc.collect.service.ftp.ftplet.FtpException;
import com.hzgc.collect.service.ftp.ftplet.User;
import com.hzgc.collect.service.ftp.nativefs.filesystem.impl.NativeFileSystemView;
import com.hzgc.common.util.json.JacksonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Native file system factory. It uses the OS file system.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
@Slf4j
@Component
public class NativeFileSystemFactory implements FileSystemFactory {

    private boolean createHome;

    private boolean caseInsensitive;

    private ReentrantLock lock = new ReentrantLock();

    @Autowired
    private CollectContext collectContext;

    /**
     * Should the home directories be created automatically
     * @return true if the file system will create the home directory if not available
     */
    public boolean isCreateHome() {
        return createHome;
    }

    /**
     * Set if the home directories be created automatically
     * @param createHome true if the file system will create the home directory if not available
     */

    public void setCreateHome(boolean createHome) {
        this.createHome = createHome;
    }

    /**
     * Is this file system case insensitive. 
     * Enabling might cause problems when working against case-sensitive file systems, like on Linux
     * @return true if this file system is case insensitive
     */
    public boolean isCaseInsensitive() {
        return caseInsensitive;
    }

    /**
     * Should this file system be case insensitive. 
     * Enabling might cause problems when working against case-sensitive file systems, like on Linux
     * @param caseInsensitive true if this file system should be case insensitive
     */
    public void setCaseInsensitive(boolean caseInsensitive) {
        this.caseInsensitive = caseInsensitive;
    }

    /**
     * Create the appropriate user file system view.
     */
    public FileSystemView createFileSystemView(User user) throws FtpException {
        try {
            lock.lock();
            // create home if does not exist
            if (createHome) {
                String homeDirStr = user.getHomeDirectory();
                File homeDir = new File(homeDirStr);
                if (homeDir.isFile()) {
                    log.warn("Not a directory :: " + homeDirStr);
                    throw new FtpException("Not a directory :: " + homeDirStr);
                }
                if ((!homeDir.exists()) && (!homeDir.mkdirs())) {
                    log.warn("Cannot create user home :: " + homeDirStr);
                    throw new FtpException("Cannot create user home :: "
                            + homeDirStr);
                }
            }
            log.info("Create file system view, user info is: ?", JacksonUtil.toJson(user));
            return new NativeFileSystemView(user,
                    caseInsensitive, collectContext);
        } finally {
            lock.unlock();
        }
    }

}

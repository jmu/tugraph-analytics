/*
 * Copyright 2023 AntGroup CO., Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */

package com.antgroup.geaflow.console.core.service.file;

import com.antgroup.geaflow.console.common.util.FileUtil;
import com.antgroup.geaflow.console.common.util.Fmt;
import com.antgroup.geaflow.console.common.util.Md5Util;
import com.antgroup.geaflow.console.common.util.exception.GeaflowException;
import com.antgroup.geaflow.console.core.model.file.GeaflowRemoteFile;
import com.antgroup.geaflow.console.core.model.release.GeaflowRelease;
import java.io.File;
import java.io.InputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class VersionFileFactory {

    public static final String LOCAL_VERSION_FILE_DIRECTORY = "/tmp/geaflow/local/versions";

    public static final String LOCAL_TASK_FILE_DIRECTORY = "/tmp/geaflow/local/tasks";

    @Autowired
    private RemoteFileStorage remoteFileStorage;

    public File getVersionFile(String versionName, GeaflowRemoteFile remoteFile) {
        String filePath = getVersionFilePath(versionName, remoteFile.getName());
        return downloadFileWithMd5(remoteFile.getPath(), filePath, remoteFile.getMd5());
    }

    public void deleteVersionFile(String versionName, GeaflowRemoteFile remoteFile) {
        String filePath = getVersionFilePath(versionName, remoteFile.getName());
        String md5 = getMd5FilePath(filePath);
        FileUtil.delete(filePath);
        FileUtil.delete(md5);
    }

    public File getTaskUserFile(String runtimeTaskId, GeaflowRemoteFile remoteFile) {
        String filePath = getTaskFilePath(runtimeTaskId, remoteFile.getName());
        return downloadFileWithMd5(remoteFile.getPath(), filePath, remoteFile.getMd5());
    }

    public File getTaskReleaseFile(String runtimeTaskId, String jobId, GeaflowRelease release) {
        String path = RemoteFileStorage.getPackageFilePath(jobId, release.getReleaseVersion());
        String filePath = getTaskFilePath(runtimeTaskId, new File(path).getName());
        return downloadFileWithMd5(path, filePath, release.getMd5());
    }

    private String getVersionFilePath(String versionName, String fileName) {
        return Fmt.as("{}/{}/{}", LOCAL_VERSION_FILE_DIRECTORY, versionName, fileName);
    }

    private String getTaskFilePath(String runtimeTaskId, String fileName) {
        return Fmt.as("{}/{}/{}", LOCAL_TASK_FILE_DIRECTORY, runtimeTaskId, fileName);
    }

    private File downloadFileWithMd5(String remotePath, String localPath, String md5) {
        // check file md5
        if (!md5.equals(loadFileMd5(localPath))) {
            // download file
            downloadFile(remotePath, localPath);

            // save file md5
            saveFileMd5(localPath);
        }

        return new File(localPath);
    }

    private String getMd5FilePath(String filePath) {
        return filePath + ".md5";
    }

    private String loadFileMd5(String filePath) {
        if (!FileUtil.exist(filePath)) {
            return null;
        }

        String md5FilePath = getMd5FilePath(filePath);
        if (!FileUtil.exist(md5FilePath)) {
            return null;
        }

        try {
            return FileUtil.readFileContent(md5FilePath).trim();

        } catch (Exception e) {
            throw new GeaflowException("Load md5 for file {} failed", filePath, e);
        }
    }

    private void downloadFile(String path, String filePath) {
        try {
            InputStream stream = remoteFileStorage.download(path);
            FileUtil.writeFile(filePath, stream);
            log.info("Download success to {}", filePath);
        } catch (Exception e) {
            throw new GeaflowException("Download file {} from {} failed", filePath, path, e);
        }
    }

    private void saveFileMd5(String filePath) {
        try {
            FileUtil.writeFile(getMd5FilePath(filePath), Md5Util.encodeFile(filePath));

        } catch (Exception e) {
            throw new GeaflowException("Save md5 for file {} failed", filePath, e);
        }
    }
}

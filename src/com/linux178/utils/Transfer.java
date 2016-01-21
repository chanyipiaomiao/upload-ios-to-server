package com.linux178.utils;


import java.io.File;
import java.util.Collection;
import java.util.List;


public interface Transfer {
    void uploadFile(Collection<File> uploadFileList);
    List<String> getFileListOnServer();
    void backupFileOnServer(List<String> needBackupFileList);
    void downloadFile(List<String> fileList,String path);
    void closeConnection();
    boolean makeDirectory(String pathname);
    void changeWorkingDirectory(String pathname);
}

package com.linux178.models;


public class UploadInfo {

    private String serverInfo;
    private String comment;
    private String suffix;
    private String dstPath;
    private String backupPath;
    private String promptText;
    private String downloadUrlPrefix;
    private String plistPrefix;
    private String ipaRegex;
    private String plistRegex;
    private String version;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getIpaRegex() {
        return ipaRegex;
    }

    public void setIpaRegex(String ipaRegex) {
        this.ipaRegex = ipaRegex;
    }

    public String getPlistRegex() {
        return plistRegex;
    }

    public void setPlistRegex(String plistRegex) {
        this.plistRegex = plistRegex;
    }

    public String getPlistPrefix() {
        return plistPrefix;
    }

    public void setPlistPrefix(String plistPrefix) {
        this.plistPrefix = plistPrefix;
    }

    public String getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(String serverInfo) {
        this.serverInfo = serverInfo;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getDstPath() {
        return dstPath;
    }

    public void setDstPath(String dstPath) {
        this.dstPath = dstPath;
    }

    public String getPromptText() {
        return promptText;
    }

    public void setPromptText(String promptText) {
        this.promptText = promptText;
    }

    public String getDownloadUrlPrefix() {
        return downloadUrlPrefix;
    }

    public void setDownloadUrlPrefix(String downloadUrlPrefix) {
        this.downloadUrlPrefix = downloadUrlPrefix;
    }

    public String getBackupPath() {
        return backupPath;
    }

    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }
}

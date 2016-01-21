package com.linux178.utils;


import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSString;
import com.dd.plist.PropertyListParser;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class PlistUtil {

    private String srcPlistFile;
    private NSDictionary rootDict;
    private List<NSString> nsStringArrayList = new ArrayList<NSString>();

    public PlistUtil(String srcPlistFile) {
        this.srcPlistFile = srcPlistFile;
    }

    /**
     * 读取文件plist文件
     */
    public void readPlist(){

        File file = new File(srcPlistFile);
        try {
            rootDict = (NSDictionary) PropertyListParser.parse(file);
            NSArray nsArray = (NSArray)rootDict.objectForKey("items");
            NSDictionary nsDictionary = (NSDictionary)nsArray.lastObject();

            NSArray assetsNSArray = (NSArray)nsDictionary.objectForKey("assets");
            NSDictionary urlDictionary = (NSDictionary)assetsNSArray.objectAtIndex(0);
            nsStringArrayList.add((NSString) urlDictionary.objectForKey("url"));


            NSDictionary metadataDictionary = (NSDictionary)nsDictionary.objectForKey("metadata");
            nsStringArrayList.add((NSString) metadataDictionary.objectForKey("bundle-version"));

        } catch (Exception e) {
            System.out.println("*** 读取文件失败,原因: " + e.getMessage());
            System.exit(1);
        }
    }


    /**
     * 得到指定的值
     * @return 值列表
     */
    public List<String> getSpecifyValue(){

        List<String> resultOfSpecifyElement = new ArrayList<String>();

        if (nsStringArrayList.size() == 0){
            readPlist();
        }

        if (nsStringArrayList.size() == 2){
            String url = nsStringArrayList.get(0).toString();
            resultOfSpecifyElement.add(url);

            String version = nsStringArrayList.get(1).toString();
            resultOfSpecifyElement.add(version);
        } else {
            System.out.println("*** 读取文件出错了,请检查");
            System.exit(1);
        }
        return resultOfSpecifyElement;
    }


    /**
     * 写plist文件
     * @param url 下载地址
     * @param version 版本号
     * @param dstPath 本地的目标路径
     */
    public void writePlist(String url,String version,String dstPath){

        if (nsStringArrayList.size() == 0){
            readPlist();
        }

        if (nsStringArrayList.size() == 2){
            nsStringArrayList.get(0).setContent(url);
            nsStringArrayList.get(1).setContent(version);
        } else {
            System.out.println("*** 读取文件出错了,请检查");
            System.exit(1);
        }

        try {
            PropertyListParser.saveAsXML(rootDict,new File(dstPath));
        } catch (IOException e) {
            System.out.println("*** 写入文件失败,原因: " + e.getMessage());
            System.exit(1);
        }
    }
}

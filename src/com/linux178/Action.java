package com.linux178;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.linux178.models.ServerInfo;
import com.linux178.models.UploadInfo;
import com.linux178.utils.*;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Action {

    public static void exeAction(){

        JSONObject jsonObject = ConfigUtil.getConfigFromJson("conf/config.json");

        if (jsonObject != null){
            JSONArray items = jsonObject.getJSONArray("items");
            List<String> itemList = new ArrayList<String>();
            for (Object object : items){
                itemList.add(object.toString());
            }

            // 输出列表
            Choice.itemsList(itemList);

            // 做出选择
            String choice = Choice.choiceItem();

            // 根据选择的平台，然后查询平台对应的服务器信息
            String platform = jsonObject.getString(choice);
            UploadInfo uploadInfo = JSONObject.parseObject(platform, UploadInfo.class);

            // 得到平台所使用的服务器信息代表字符串
            String server = uploadInfo.getServerInfo();

            // 再根据服务器信息字符串查询连接服务器所需要的IP、用户名、密码、端口信息生成一个对象
            ServerInfo serverInfo = JSONObject.parseObject(jsonObject.getString(server), ServerInfo.class);

            // 获取要上传的文件列表
            String srcDirectory = Choice.inputSrcDirectory(); // 获取源文件目录
            String[] suffix = uploadInfo.getSuffix().split("\\s"); //上传指定后缀名的文件
            Collection<File> filesOnLocalDir = FileUtils.listFiles(new File(srcDirectory), suffix, false);


            // 获取版本号
            String version = Choice.inputVersion(uploadInfo.getVersion());


            // 检查文件名是否符合规范
            String fileNameRegex = uploadInfo.getIpaRegex();
            Map<String,String> localChannelIpaMapping = new HashMap<String, String>();
            Pattern pattern = Pattern.compile(fileNameRegex);
            for (File file : filesOnLocalDir){
                String filename = file.getName();
                Matcher matcher = pattern.matcher(filename);
                if (matcher.find()){
                    if (! matcher.group(1).equals(version)){
                        System.out.println("*** 输入的版本号与文件名里面的版本号不一致,请检查");
                        System.exit(1);
                    }
                    localChannelIpaMapping.put(matcher.group(2), filename);
                } else {
                    System.out.printf("*** 文件名不合规范,规范是: %s 文件名: %s\n",fileNameRegex,filename);
                    System.exit(1);
                }
            }


            // 初始化FTP或者SFTP
            Transfer transfer;
            if ("ftp".equals(serverInfo.getProtocol())){
                transfer = new FTPUtil(serverInfo,uploadInfo,version);
            } else {
                transfer = new SFTPUtil(serverInfo,uploadInfo);
            }

            // 获取服务器上的文件列表
            List<String> fileListOnServer = transfer.getFileListOnServer();

            // 服务器上创建以版本号为名的目录
            if (! fileListOnServer.contains(version)){
                transfer.makeDirectory(version);
            }

            // 创建本地目录
            String plistRoot = choice + "-plist";
            String localOriginalPlistDir = plistRoot + "/original-plist";
            File originalDir = new File(localOriginalPlistDir);
            if (! originalDir.exists()){
                originalDir.mkdirs();
            }

            // 得到所有的plist文件
            List<String> plistFileList = new ArrayList<String>();
            Map<String,String> plistMapChannel = new HashMap<String, String>(); // 渠道号与plist文件映射关系
            String plistNameRegex = uploadInfo.getPlistRegex();
            for (String filename : fileListOnServer){
                if (filename.endsWith("plist")){
                    plistFileList.add(filename);
                    if ("ivp4ios.plist".equals(filename)){
                        plistMapChannel.put("8000",filename);
                    }
                    if (filename.matches(plistNameRegex)){
                        int xia = filename.indexOf("_");
                        int dian = filename.indexOf(".");
                        String channel = filename.substring(xia+1,dian);
                        plistMapChannel.put(channel,filename);
                    }
                }
            }


            // 下载所有的plist文件到本地
            System.out.println("--> 下载plist文件到本地目录");
            transfer.downloadFile(plistFileList, localOriginalPlistDir);


            // 读取所有的plist文件,得到里面的ipa文件路径
            List<String> ipaFileOnServer = new ArrayList<String>();
            for (String filename : plistFileList){
                String path = localOriginalPlistDir + "/" +filename;
                PlistUtil plistUtil = new PlistUtil(path);
                List<String> result = plistUtil.getSpecifyValue();
                if (result.size() != 2){
                    System.out.println("*** 获取的值并是所期望的,文件名: " + path);
                    continue;
                }
                String url = result.get(0);
                String[] filenameOnServer = url.split(uploadInfo.getDownloadUrlPrefix());
                ipaFileOnServer.add(filenameOnServer[1]);
            }


            // 为ipa文件生成plist文件
            String url = String.format("%s/%s",uploadInfo.getDownloadUrlPrefix(),version);
            for (String channel : localChannelIpaMapping.keySet()){
                String ipaName = localChannelIpaMapping.get(channel);
                if (plistMapChannel.containsKey(channel)){
                    String name = plistMapChannel.get(channel);
                    String dstPlistName = srcDirectory + "/" + name;
                    PlistUtil plistUtil = new PlistUtil(localOriginalPlistDir + "/" + name);
                    plistUtil.writePlist(url + "/" + ipaName,version,dstPlistName);
                } else {
                    String dstPlistName = String.
                            format("%s/%s_%s.plist", srcDirectory, uploadInfo.getPlistPrefix(), channel);
                    PlistUtil plistUtil = new PlistUtil("conf/template.plist");
                    plistUtil.writePlist(url + "/" + ipaName,version,dstPlistName);
                }
            }


            // 生成要上传文件列表
            List<String> uploadFileStringList = new ArrayList<String>();
            for (File file : FileUtils.listFiles(new File(srcDirectory), suffix, false)){
                uploadFileStringList.add(file.getName());
            }
            Collection<File> uploadIpaFileList = new ArrayList<File>();
            Collection<File> uploadPlistFileList = new ArrayList<File>();
            for (String filename : uploadFileStringList){
                String path = srcDirectory + "/" + filename;
                if (filename.endsWith("ipa")){
                    uploadIpaFileList.add(new File(path));
                }
                if (filename.endsWith("plist")){
                    uploadPlistFileList.add(new File(path));
                }
            }


            // 生成要备份文件列表
            List<String> needBackupFileList = new ArrayList<String>();
            for (String filename : ipaFileOnServer){
                File file = new File(filename);
                if (uploadFileStringList.contains(file.getName())){
                    needBackupFileList.add(filename);
                }
            }

            // 备份文件
            if (needBackupFileList.size() != 0){
                transfer.backupFileOnServer(needBackupFileList);
            }

            // 上传文件
            if (uploadIpaFileList.size() == 0 || uploadPlistFileList.size() == 0){
                System.out.println("*** 上传文件列表为空,请检查后重新执行程序");
                System.exit(1);
            } else {
                transfer.uploadFile(uploadPlistFileList);
                transfer.changeWorkingDirectory(version);
                transfer.uploadFile(uploadIpaFileList);
            }

            // 关闭连接
            transfer.closeConnection();


        } else {
            System.out.println("*** 读取配置文件失败,请检查后重新执行");
        }
    }
}

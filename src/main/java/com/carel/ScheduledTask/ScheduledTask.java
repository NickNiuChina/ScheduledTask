package com.carel.ScheduledTask;

import com.aliyun.oss.model.OSSVersionSummary;
import com.carel.ScheduledTask.utils.BackupMiniDb;
import com.carel.ScheduledTask.utils.BackupRDSDb;
import com.carel.ScheduledTask.utils.OssFiles;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * Main entry
 *
 */
	 
public class ScheduledTask {
	
	private static Logger logger=Logger.getLogger(ScheduledTask.class);
	
    public static void main(String[] args){
    	
    	// Backup RDS
    	logger.info("Backup RDS by pg_dump, please wait...");
    	int result = BackupRDSDb.backupRDSDb();
    	if (result > 0) {
    		logger.info("Backup success");
    	}else {
    		logger.error("Backup Failed");
    	}
    	
    	// Upload backup files
        
        File file=new File(ScheduledTask.class.getProtectionDomain().getCodeSource().getLocation().getFile());
		//System.out.println("Jar file: " + file.toString());
		
		String jarPath = file.getParentFile().getPath();//jar所在文件夹路径
		//System.out.println("jarPath: " + jarPath);
		String nowtime = new SimpleDateFormat("yyyyMMdd").format(new Date());

		String bfName = String.format("remotevalue-prod-back-%s.backup", nowtime);
		String backupFilePath = jarPath + File.separator + bfName ;   
		File bf = new File(backupFilePath);
		
		int backupFlag = 0;
		
	    if (bf.exists() && bf.isFile() && bf.length() > 0) {
	    	logger.info("RDS Backup file: " + backupFilePath);
	    	logger.info("RDS backup file check done: normal.");
	    	int res = OssFiles.uploadOssFile("rds-backup/" + bfName, backupFilePath);
	    	if (res > 0) {
		    	logger.info("RDS Backup file uploaded successfully! ");
		    	backupFlag = 1;
	    	} else {
	    		logger.error("RDS Backup file uploaded Failed! ");
	    		backupFlag = 0;
	    	}
	    } else {
	    	logger.info("RDS backup file check failed: abnormal.");
	    	backupFlag = 0;
	    }
    	
	    // Clean local backup file
	    if(backupFlag>0 && bf.delete()) {
	    	logger.info("File deleted successfully: " + backupFilePath);
	    } 
	    
	    
	    // ################################################################################
	    // --- Backup WechatMini server PostgreSQL -----------------------------
	    // wechat-mini-prod-back-20240410.backup
	    // --- Backup WechatMini server PostgreSQL -----------------------------
        // --- Backup WechatMini server PostgreSQL -----------------------------
    	logger.info("Backup Mini DB by pg_dump, please wait...");
    	result = BackupMiniDb.backupMiniDb();
    	if (result > 0) {
    		logger.info("Backup success");
    	}else {
    		logger.error("Backup Failed");
    	}	
    	// Upload backup files
        
        file = new File(ScheduledTask.class.getProtectionDomain().getCodeSource().getLocation().getFile());
		//System.out.println("Jar file: " + file.toString());
		
		jarPath = file.getParentFile().getPath();//jar所在文件夹路径
		//System.out.println("jarPath: " + jarPath);
		nowtime = new SimpleDateFormat("yyyyMMdd").format(new Date());

		bfName = String.format(" wechat-mini-prod-back-%s.backup", nowtime);
		backupFilePath = jarPath + File.separator + bfName ;   
		bf = new File(backupFilePath);
		
		backupFlag = 0;
		
	    if (bf.exists() && bf.isFile() && bf.length() > 0) {
	    	logger.info("RDS Backup file: " + backupFilePath);
	    	logger.info("RDS backup file check done: normal.");
	    	int res = OssFiles.uploadOssFile("rds-backup/" + bfName, backupFilePath);
	    	if (res > 0) {
		    	logger.info("RDS Backup file uploaded successfully! ");
		    	backupFlag = 1;
	    	} else {
	    		logger.error("RDS Backup file uploaded Failed! ");
	    		backupFlag = 0;
	    	}
	    } else {
	    	logger.info("RDS backup file check failed: abnormal.");
	    	backupFlag = 0;
	    } 	
    	
	    // ################################################################################
	    
	    // List all the buckups on OSS
	    ArrayList<OSSVersionSummary> alOssVersions = OssFiles.listOssFiles();
        for (OSSVersionSummary ossVersion : alOssVersions) {
        	logger.info("------------------------------------");
            logger.info("key name: " + ossVersion.getKey());
            logger.info("versionid: " + ossVersion.getVersionId());
            logger.info("Is latest: " + ossVersion.isLatest());
            logger.info("Is delete marker: " + ossVersion.isDeleteMarker());
            logger.info("------------------------------------");
        }        
        
        // TODO
    	//Clean the OSS RDS backup files, delete the oldest one
        
        // TODO
        // 快照制作镜像
    }
}

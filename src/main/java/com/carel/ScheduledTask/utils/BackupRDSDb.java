package com.carel.ScheduledTask.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.carel.ScheduledTask.ScheduledTask;


public class BackupRDSDb {
	// pg_dump -h 172.16.85.209 -U postgres -p 1921 -Fc remotevalue > remotevalue-prod-back-20240320.backup
	
	private static Logger logger=Logger.getLogger(ScheduledTask.class);
	
	public static int backupRDSDb() {
		try {
			
			Properties props = GetProperties.getProperties();
			
	        // 数据库连接信息
			String dbHost = props.getProperty("dbHost");
            String dbName = props.getProperty("dbName");
            String userName = props.getProperty("userName");
            String password = props.getProperty("password");
            String dbPort = props.getProperty("dbPort");
            
            // 备份文件的路径
            File file=new File(ScheduledTask.class.getProtectionDomain().getCodeSource().getLocation().getFile());
    		//System.out.println("Jar file: " + file.toString());
    		
    		String jarPath = file.getParentFile().getPath();//jar所在文件夹路径
    		//System.out.println("jarPath: " + jarPath);
    		String nowtime = new SimpleDateFormat("yyyyMMdd").format(new Date());

    		String bfName = String.format("remotevalue-prod-back-%s.backup", nowtime);
    		String backupFilePath = jarPath + File.separator + bfName ;        
            
    		 String osName = System.getProperty("os.name").toLowerCase();
    		 String pg_dump="";
	        if (osName.contains("windows")) {
	        	pg_dump = "C:\\Program Files\\PostgreSQL\\15\\bin\\pg_dump.exe";
	        } else {
	        	pg_dump = "pg_dump";
	        }
    		
		    ProcessBuilder pb = new ProcessBuilder(
		    		pg_dump,
		            "--host", dbHost,
		            "--port", dbPort,
		            "--username", userName,
		            "--verbose", "--file", backupFilePath,
		            "-Fc", dbName);
		    pb.environment().put("PGPASSWORD", password);
		    Process process = pb.start();
		    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		    String line;
		    while ((line = reader.readLine()) != null) {
		        System.out.println(line);
		    }
		    process.waitFor();
		    
		    File bf = new File(backupFilePath);
		    
		    if (bf.exists() && bf.isFile() && bf.length() > 0) {
		    	logger.info("RDS Backup file: " + backupFilePath);
		    	logger.info("RDS backup file check done: normal.");
			    return 1;
		    } else {
		    	logger.info("RDS backup file check failed: abnormal.");
			    return 0;
		    }
		    
		} catch (IOException | InterruptedException e) {
		    e.printStackTrace();
		    return 0;
		}
	}
}

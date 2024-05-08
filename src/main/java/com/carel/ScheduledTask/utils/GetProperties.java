package com.carel.ScheduledTask.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

import com.carel.ScheduledTask.ScheduledTask;

public class GetProperties {
	
	public static Properties getProperties() {
	
		Properties props = new Properties();
		String name = new String("ScheduledTask.properties");
		File file=new File(ScheduledTask.class.getProtectionDomain().getCodeSource().getLocation().getFile());
		//System.out.println("Jar file: " + file.toString());
		
		String jarPath = file.getParentFile().getPath();//jar所在文件夹路径
		//System.out.println("jarPath: " + jarPath);
		
		String resource = jarPath + File.separator +name;
		//System.out.println(resource);
	    
	    //解决中文乱码问题
	    try (InputStreamReader isr = new InputStreamReader(new FileInputStream(resource), "UTF-8")) {
		    //加载输入流文件信息
		    props.load(isr);
		    return props;
        } catch (IOException e) {
        	e.printStackTrace();
        	return null;
        }
	}
}

package com.carel.ScheduledTask.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.ListVersionsRequest;
import com.aliyun.oss.model.OSSVersionSummary;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyun.oss.model.VersionListing;

public class OssFiles {
	/*
	 * Get OSS client
	 * */
	private static OSS getOssClient() {
		
		Properties props = GetProperties.getProperties();
		
        // Endpoint以华东1（杭州）为例，其它Region请按实际情况填写。
        String endpoint = props.getProperty("endpoint");
        
        // 从环境变量中获取访问凭证。运行本代码示例之前，请确保已设置环境变量OSS_ACCESS_KEY_ID和OSS_ACCESS_KEY_SECRET。
        //EnvironmentVariableCredentialsProvider credentialsProvider = CredentialsProviderFactory.newEnvironmentVariableCredentialsProvider();
        
        // 从配置文件取RAM用户的访问密钥（AccessKey ID和AccessKey Secret）。
        String accessKeyId = props.getProperty("accessKeyId");
        String accessKeySecret = props.getProperty("accessKeySecret");
        // 使用代码嵌入的RAM用户的访问密钥配置访问凭证。
        CredentialsProvider credentialsProvider = new DefaultCredentialProvider(accessKeyId, accessKeySecret);
        
        // 填写Bucket名称，例如examplebucket。
        String bucketName = props.getProperty("bucketName");

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, credentialsProvider);
        return ossClient;		
	}
	
	
	/*
	 * 列出所有的 RDS 备份文件
	 * 
	 * */
	public static ArrayList<OSSVersionSummary> listOssFiles() {
		
		OSS ossClient =  getOssClient();
		Properties props = GetProperties.getProperties();
		ArrayList<OSSVersionSummary> arrayList = new ArrayList<>();
		
        try {
            // 列举包括删除标记在内的所有Object的版本信息。
            String nextKeyMarker = null;
            String nextVersionMarker = null;
            VersionListing versionListing = null;
            
            do {
                ListVersionsRequest listVersionsRequest = new ListVersionsRequest()
                        .withBucketName(props.getProperty("bucketName"))
                        .withKeyMarker(nextKeyMarker)
                        .withVersionIdMarker(nextVersionMarker);

                versionListing = ossClient.listVersions(listVersionsRequest);
                for (OSSVersionSummary ossVersion : versionListing.getVersionSummaries()) {
                	String key = ossVersion.getKey();
                	if (key.startsWith("rds-backup") && key.length() == 48) {
                		arrayList.add(ossVersion);
                	}
                }
                nextKeyMarker = versionListing.getNextKeyMarker();
                nextVersionMarker = versionListing.getNextVersionIdMarker();
            } while (versionListing.isTruncated());
            
            return arrayList;
            
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
            return null;
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
            return null;
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
                return arrayList;
            }
        }
	}
	
	/*
	 * 删除 RDS 备份文件
	 * 
	 * */
	
	public static int delteOssFile(OSSVersionSummary ossVersion) {

		OSS ossClient =  getOssClient();
		Properties props = GetProperties.getProperties();

		try {
			// 删除指定版本的Object。
			ossClient.deleteVersion(props.getProperty("bucketName"), ossVersion.getKey(), ossVersion.getVersionId());
			return 1;
		} catch (OSSException oe) {
			System.out.println("Caught an OSSException, which means your request made it to OSS, "
					+ "but was rejected with an error response for some reason.");
			System.out.println("Error Message:" + oe.getErrorMessage());
			System.out.println("Error Code:" + oe.getErrorCode());
			System.out.println("Request ID:" + oe.getRequestId());
			System.out.println("Host ID:" + oe.getHostId());
			return 0;
		} catch (ClientException ce) {
			System.out.println("Caught an ClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with OSS, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message:" + ce.getMessage());
			return 0;
		} finally {
			if (ossClient != null) {
				ossClient.shutdown();
				return 1;
			}
		}
	}

	/*
	 * 上传 RDS 备份文件
	 * 
	 * */
	
	public static int uploadOssFile(String objectName, String filePath) {

		OSS ossClient =  getOssClient();
		Properties props = GetProperties.getProperties();

		try {
            // 创建PutObjectRequest对象。
            PutObjectRequest putObjectRequest = new PutObjectRequest(props.getProperty("bucketName"), objectName, new File(filePath));
            // 如果需要上传时设置存储类型和访问权限，请参考以下示例代码。
            // ObjectMetadata metadata = new ObjectMetadata();
            // metadata.setHeader(OSSHeaders.OSS_STORAGE_CLASS, StorageClass.Standard.toString());
            // metadata.setObjectAcl(CannedAccessControlList.Private);
            // putObjectRequest.setMetadata(metadata);
            
            // 上传文件。
            PutObjectResult result = ossClient.putObject(putObjectRequest);
			return 1;
		} catch (OSSException oe) {
			System.out.println("Caught an OSSException, which means your request made it to OSS, "
					+ "but was rejected with an error response for some reason.");
			System.out.println("Error Message:" + oe.getErrorMessage());
			System.out.println("Error Code:" + oe.getErrorCode());
			System.out.println("Request ID:" + oe.getRequestId());
			System.out.println("Host ID:" + oe.getHostId());
			return 0;
		} catch (ClientException ce) {
			System.out.println("Caught an ClientException, which means the client encountered "
					+ "a serious internal problem while trying to communicate with OSS, "
					+ "such as not being able to access the network.");
			System.out.println("Error Message:" + ce.getMessage());
			return 0;
		} finally {
			if (ossClient != null) {
				ossClient.shutdown();
				return 1;
			}
		}
	}
	
}

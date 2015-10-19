package com.ant.myagile.utils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Component;

@Component
public class MyAgileFileUtils {
	static Logger log = Logger.getLogger(MyAgileFileUtils.class.getName());
	
	public static final String KEY_ENCRYPTION = "ant.myagile.axonactive";
	
	public static String getStorageLocation(String key) {
		Resource resource = new ClassPathResource("/config.properties");
		try {
			Properties props = PropertiesLoaderUtils.loadProperties(resource);
			String storageLocationRoot = props
					.getProperty("myagile.upload.root.location");
			String storageLocationDetail = props.getProperty(key);
			if (key.equalsIgnoreCase("myagile.upload.root.location")) {
				return storageLocationRoot;
			}
			if (storageLocationRoot != null && storageLocationDetail != null) {
				return storageLocationRoot + storageLocationDetail;
			}
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Read "config.properties" file
	 * 
	 * @return the root location of file storage of myagile
	 */
	public static String getRootStorageLocation() {
		Resource resource = new ClassPathResource("/config.properties");
		try {
			Properties props = PropertiesLoaderUtils.loadProperties(resource);
			String storageLocationRoot = props
					.getProperty("myagile.upload.root.location");
			if (storageLocationRoot != null) {
				return storageLocationRoot;
			}
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	public static String fileNameProcess(String fileName) {
		String timelong = String.valueOf(new Date().getTime());
		return FilenameUtils.normalize(fileName + "_" + timelong, true)
				.replaceAll("[^\\w._]+", "_").toLowerCase();
	}

	public static boolean deleteFile(String file) {
		try {
			File f = new File(file);
			if (f.exists()) {
				f.delete();
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static String renameFile(String path, String oldFileName, String newFileName) {
			  String extension = FilenameUtils.getExtension(oldFileName);
			  String oldFileAbsPath = path + "/" + oldFileName;
			  String newFileAbsPath = path + "/" + newFileName + "." + extension;
			  File oldFile = new File(oldFileAbsPath);
			  File newFile = new File(newFileAbsPath);
			  if (oldFile.exists()) {
			   oldFile.renameTo(newFile);
			  }
			  return newFileName + "." + extension;
	}
	
	public static String getFileNameFinal(String fileName){
		String fileWithoutExtension =FilenameUtils.removeExtension(fileName);
	    String extension = FilenameUtils.getExtension(fileName);
	    String fileNamewithDate = fileNameProcess(fileWithoutExtension);
		return fileNamewithDate+"."+extension;
	}
}

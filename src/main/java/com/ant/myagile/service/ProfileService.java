package com.ant.myagile.service;

public interface ProfileService {
	 String teamImageNameProcess(String userName);
	 void deleteLogo(String filename);
	 String renameLogo(String oldFileName, String newFileName);
}

package com.ant.myagile.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ant.myagile.service.ProfileService;
import com.ant.myagile.utils.MyAgileFileUtils;

@Service
@Transactional
public class ProfileServiceImpl implements ProfileService {
	private String imageTeamFolder = MyAgileFileUtils.getStorageLocation("myagile.upload.image.member.folder"); 
	@Override
	public String teamImageNameProcess(String userName) {
		return MyAgileFileUtils.fileNameProcess(userName);
	}
	@Override
	public void deleteLogo(String filename) {
		MyAgileFileUtils.deleteFile(imageTeamFolder+"/"+filename);	
	}

	@Override
	public String renameLogo(String oldFileName, String newFileName) {
		return MyAgileFileUtils.renameFile(imageTeamFolder, oldFileName, newFileName);
	}

}

package com.ant.myagile.service.impl;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import com.ant.myagile.service.FileService;

@Repository("fileService")
public class FileServiceImpl implements FileService {
	
	@Value(value = "${myagile.upload.root.location}")
	private String uploadLocation; 
	
	@Override
	public File saveFile(String location, MultipartFile file) throws IllegalStateException, IOException {
		File movedFile = new File(location);
		file.transferTo(movedFile);		
		return movedFile;
	}

	@Override
	public File saveProjectImage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File saveAvatar() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public File saveTeamAvatar() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File saveVideo(MultipartFile file) throws IllegalStateException, IOException {
		String fileName = file.getOriginalFilename();
		String fileLocation = uploadLocation+"/meeting_video/";
		File f = new File(fileLocation);
		if(!f.exists())
		{
			f.mkdirs();
		}

		// Clean file name for security reason
		fileName = fileName.replaceAll("[^\\w._]+","_");
		
		// Move file to specified folder
		String location = f.getPath()+'/'+fileName;
		
		return this.saveFile(location, file);
	}

}

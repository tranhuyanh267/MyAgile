package com.ant.myagile.service;

import java.io.File;
import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
	File saveFile(String location, MultipartFile file) throws IllegalStateException, IOException;
	File saveProjectImage();
	File saveAvatar();
	File saveVideo(MultipartFile file) throws IllegalStateException, IOException;
	File saveTeamAvatar();
}

package com.ant.myagile.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ant.myagile.service.impl.ProjectServiceImpl;
import com.ant.myagile.utils.MyAgileFileUtils;
@RunWith(PowerMockRunner.class)
@PrepareForTest({ProjectServiceImpl.class, MyAgileFileUtils.class})
public class ProjectServiceImplTest {
	@InjectMocks
	ProjectServiceImpl projectServiceImpl;
	String directoryPath;
	String directoryPathNew;
	String path;
	
	@Before
	public void setUp() throws Exception {
		PowerMockito.mockStatic(MyAgileFileUtils.class);
		directoryPath = "nature.jpg";
		directoryPathNew = "nature_0093";
		path = "folder";
	}	
	
	@Test
	public void testchangeDirTemp() throws Exception{
		File directoryMockOld = Mockito.mock(File.class);
		File directoryMockNew = Mockito.mock(File.class);
		File pathMock = Mockito.mock(File.class);
		
		whenNew(File.class).withArguments(directoryPath).thenReturn(directoryMockOld);
		whenNew(File.class).withArguments(directoryPathNew).thenReturn(directoryMockNew);
		whenNew(File.class).withArguments(path).thenReturn(pathMock);
		
		Mockito.when(directoryMockOld.exists()).thenReturn(true);
		Mockito.when(pathMock.exists()).thenReturn(false);
		Mockito.when(pathMock.mkdirs()).thenReturn(true);
		Mockito.when(directoryMockOld.renameTo(directoryMockNew)).thenReturn(true);
		
		assertTrue(projectServiceImpl.moveFileFromTempDirectoryToRealPath(directoryPathNew, directoryPath, path));
		
		verifyNew(File.class).withArguments(directoryPath);
		verifyNew(File.class).withArguments(directoryPathNew);
		verifyNew(File.class).withArguments(path);
	}
	
	@Test
	public void testRenameLogo() throws Exception{
		 String expected = "nature_0093.jpg";
		 Mockito.when(MyAgileFileUtils.renameFile(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())).thenReturn("nature_0093.jpg");
		 String actual = projectServiceImpl.renameLogo(directoryPath, directoryPathNew);
		 assertEquals(expected, actual);
	}
	
	@Test
	public void testDeleteLogo() throws Exception{
		boolean expected = true;
		Mockito.when(MyAgileFileUtils.deleteFile(Mockito.anyString())).thenReturn(true);
		boolean actual = projectServiceImpl.deleteLogo(directoryPath);
		assertEquals(expected, actual);
	}

}

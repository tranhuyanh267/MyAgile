package com.ant.myagile.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.powermock.api.mockito.PowerMockito.verifyNew;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.io.File;

import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.ant.myagile.utils.MyAgileFileUtils;

@RunWith(PowerMockRunner.class)
@PrepareForTest({MyAgileFileUtils.class, FilenameUtils.class})
public class FileUtilsTest {

	private static final String directoryPath = new String("nature_0093.jpg");
	private static final String directoryPathNew = new String("autumn-nature-wallpaper1");
	private static final String path = new String("folder");
	
	@Before
	public void setUp() throws Exception {
		
	}

	@Test
	public void testDeleteFile() throws Exception {
		File directoryMock = Mockito.mock(File.class);
		PowerMockito.whenNew(File.class).withArguments(directoryPath).thenReturn(directoryMock);
		Mockito.when(directoryMock.exists()).thenReturn(true);
		Mockito.when(directoryMock.delete()).thenReturn(true);
		assertTrue(MyAgileFileUtils.deleteFile(directoryPath));
		verifyNew(File.class).withArguments(directoryPath);
	}
	@Test
	public void testRenameFile() throws Exception{
		String expected = directoryPathNew + ".jpg";
		
		PowerMockito.mockStatic(FilenameUtils.class);
				
		File directoryMockOld = Mockito.mock(File.class);
		File directoryMockNew = Mockito.mock(File.class);
		
		whenNew(File.class).withArguments(path + "/" + directoryPath).thenReturn(directoryMockOld);
		whenNew(File.class).withArguments(path + "/" + directoryPathNew).thenReturn(directoryMockNew);
			
		Mockito.when(FilenameUtils.getExtension(directoryPath)).thenReturn("jpg");
		Mockito.when(directoryMockOld.exists()).thenReturn(true);
		Mockito.when(directoryMockOld.renameTo(directoryMockNew)).thenReturn(true);
		
		String actual = MyAgileFileUtils.renameFile(path, directoryPath, directoryPathNew);
		
		assertEquals(actual, expected);
		verifyNew(File.class).withArguments(path + "/" + directoryPath);
		
	}

}

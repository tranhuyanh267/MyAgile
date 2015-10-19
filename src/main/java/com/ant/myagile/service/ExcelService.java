package com.ant.myagile.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import jxl.write.WritableSheet;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public interface ExcelService {
	
	void setContent(String sheetName, ArrayList<ArrayList<String>> content);

	ByteArrayOutputStream writeASheet() throws IOException, WriteException;

	void createLabel(WritableSheet sheet, ArrayList<ArrayList<String>> arr)throws WriteException;

	void createContent(WritableSheet sheet, ArrayList<ArrayList<String>> arr) throws WriteException, RowsExceededException, IllegalArgumentException;

	void addTitle(WritableSheet sheet, int column, int row, String s)throws RowsExceededException, WriteException;

	void addLabel(WritableSheet sheet, int column, int row, String s)throws WriteException, RowsExceededException;
		
	ArrayList<ArrayList<String>> read(String fileName);
	
	ArrayList<ArrayList<String>> readXLSX(String fileName);
}

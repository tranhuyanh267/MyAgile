package com.ant.myagile.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

import jxl.Cell;
import jxl.CellView;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.ant.myagile.service.ExcelService;
@Service("excelService")
public class ExcelServiceImpl implements ExcelService {
	private WritableCellFormat arialBoldUnderline;
	private WritableCellFormat arialItalicWithBackGroundColor;
	private WritableCellFormat arials;
	String sheetName;
	ArrayList<ArrayList<String>> data;

	@Override
	public ByteArrayOutputStream writeASheet() throws IOException, WriteException {
		WorkbookSettings wbSettings = new WorkbookSettings();
		wbSettings.setLocale(new Locale("vi", "VN"));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		WritableWorkbook workbook = Workbook.createWorkbook(baos, wbSettings);
		workbook.createSheet(sheetName, 0);
		WritableSheet excelSheet = workbook.getSheet(0);
		createLabel(excelSheet, this.data);
		createContent(excelSheet, this.data);
		workbook.write();
		workbook.close();
		return baos;
	}

	@Override
	public void createLabel(WritableSheet sheet,
			ArrayList<ArrayList<String>> arr) throws WriteException {
		WritableFont times10pt = new WritableFont(WritableFont.ARIAL, 10);
		arials = new WritableCellFormat(times10pt);
		arials.setWrap(true);
		arials.setBorder(jxl.format.Border.ALL, jxl.format.BorderLineStyle.THIN);
		/**/
		WritableFont arials10ptBoldUnderline = new WritableFont(
				WritableFont.ARIAL, 10, WritableFont.BOLD, false,
				UnderlineStyle.SINGLE);
		arialBoldUnderline = new WritableCellFormat(arials10ptBoldUnderline);
		arialBoldUnderline.setWrap(true);
		arialBoldUnderline.setBorder(jxl.format.Border.ALL,
				jxl.format.BorderLineStyle.THIN);
		/**/
		WritableFont arials10ptItalic = new WritableFont(
				WritableFont.ARIAL, 10, WritableFont.BOLD, false,
				UnderlineStyle.SINGLE);
		arialItalicWithBackGroundColor = new WritableCellFormat(arials10ptItalic);
		arialItalicWithBackGroundColor.setWrap(true);
		arialItalicWithBackGroundColor.setBorder(jxl.format.Border.ALL,
				jxl.format.BorderLineStyle.THIN);
		arialItalicWithBackGroundColor.setBackground(Colour.CORAL);

		CellView cv = new CellView();
		cv.setAutosize(true);
		for (int i = 0; i < arr.get(0).size(); i++) {
			sheet.setColumnView(i, cv);
			addTitle(sheet, i, 0, arr.get(0).get(i));
		}
	}
	
	@Override
	public void createContent(WritableSheet sheet,
			ArrayList<ArrayList<String>> arr) throws WriteException,
			RowsExceededException, IllegalArgumentException {
		for (int i = 1; i < arr.size(); i++) {
			for (int j = 0; j < arr.get(i).size(); j++) {
				String content = arr.get(i).get(j);
				if ((j == 0 || j%10 == 0) && content.contains("|")) {
					addLabelChildren(sheet, j, i, content);
				}
				else {
					addLabel(sheet, j, i, content);
				}
				
			}
		}
	}

	@Override
	public void addTitle(WritableSheet sheet, int column, int row, String s)
			throws RowsExceededException, WriteException {
		Label label;
		label = new Label(column, row, s, arialBoldUnderline);
		sheet.addCell(label);
	}

	@Override
	public void addLabel(WritableSheet sheet, int column, int row, String s)
			throws WriteException, RowsExceededException {
		Label label;
		label = new Label(column, row, s, arials);
		sheet.addCell(label);
	}
	
	public void addLabelChildren(WritableSheet sheet, int column, int row, String s)
			throws WriteException, RowsExceededException {
		Label label;
		label = new Label(column, row, s, arialItalicWithBackGroundColor);
		sheet.addCell(label);
	}

	@Override
	public ArrayList<ArrayList<String>> read(String fileName) {
		ArrayList<ArrayList<String>> arraylistResult = new ArrayList<ArrayList<String>>();
		File inputWorkbook = new File(fileName);
		Workbook workbook;
		try {
			workbook = Workbook.getWorkbook(inputWorkbook);
			Sheet sheet = workbook.getSheet(0);
			for (int i = 0; i < sheet.getRows(); i++) {
				Cell[] cells = sheet.getRow(i);
				ArrayList<String> arraylistTemp = new ArrayList<String>();
				for (int j = 0; j < cells.length; j++) {
					arraylistTemp.add(cells[j].getContents());
				}
				arraylistResult.add(arraylistTemp);
			}
		} catch (Exception e) {
		}
		return arraylistResult;
	}
	
	@Override
	public ArrayList<ArrayList<String>> readXLSX(String fileName) {
		ArrayList<ArrayList<String>> arraylistResult = new ArrayList<ArrayList<String>>();
		try {
		    InputStream inp = new FileInputStream(fileName);
		    XSSFWorkbook workbook = new XSSFWorkbook(inp);
		    XSSFSheet sheet = workbook.getSheetAt(0);
		    for(Row row : sheet) {
		    	ArrayList<String> arraylistTemp = new ArrayList<String>();
		    	   for(int cn=0; cn<row.getLastCellNum(); cn++) {
		    		   org.apache.poi.ss.usermodel.Cell cell = row.getCell(cn, Row.CREATE_NULL_AS_BLANK);
		    		   switch (cell.getCellType()) {
						case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_STRING:
							arraylistTemp.add(cell.getStringCellValue());
							break;
						case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_NUMERIC:
							arraylistTemp.add((int)cell.getNumericCellValue()+"");
							break;
						case org.apache.poi.ss.usermodel.Cell.CELL_TYPE_BLANK:
							arraylistTemp.add("");
							break;
						}
		    	   }
		    	   arraylistResult.add(arraylistTemp);
			}
		} catch (Exception e) {	}
		return arraylistResult;
	}

	@Override
	public void setContent(String sheetName,
			ArrayList<ArrayList<String>> content) {
		this.sheetName = sheetName;
		this.data = content;
	}
	
	public String getSheetName() {
		return sheetName;
	}

	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}


}

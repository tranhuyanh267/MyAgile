package com.ant.myagile.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jxl.write.WriteException;
import net.sf.jasperreports.engine.JRException;

import com.ant.myagile.model.Issue;
import com.ant.myagile.model.UserStory;
import com.hp.gagawa.java.elements.Table;
import com.itextpdf.text.DocumentException;

public interface ExportImportFileService {
	void exportIssuesPDF(List<Issue> issueList) throws DocumentException, IOException;
	Table prepareIssueTableContent(Issue issueData) throws DocumentException, IOException; 
	void generatePlainExcel(List<Issue> issueList) throws WriteException, IOException;
	void generateRichExcel(List<Issue> issueList) throws WriteException, IOException,JRException;
	void exportProductBacklogToExcel(List<UserStory> userStories) throws WriteException, IOException;
	ArrayList<ArrayList<String>> readExcel(String name,int type) throws IOException ;
	void exportUserStoryPDF(List<UserStory> selectedUserStories) throws IOException, DocumentException;
}

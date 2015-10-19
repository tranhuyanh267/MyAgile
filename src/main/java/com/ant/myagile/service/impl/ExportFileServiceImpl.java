package com.ant.myagile.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;

import jxl.write.WriteException;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapArrayDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Issue;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.ExcelService;
import com.ant.myagile.service.ExportImportFileService;
import com.ant.myagile.service.UserStoryService;
import com.hp.gagawa.java.DocumentType;
import com.hp.gagawa.java.elements.Body;
import com.hp.gagawa.java.elements.Head;
import com.hp.gagawa.java.elements.Meta;
import com.hp.gagawa.java.elements.Table;
import com.hp.gagawa.java.elements.Td;
import com.hp.gagawa.java.elements.Tr;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.ElementHandler;
import com.itextpdf.tool.xml.Writable;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import com.itextpdf.tool.xml.pipeline.WritableElement;

@Component
public class ExportFileServiceImpl implements ExportImportFileService 
{
	@Autowired
	private ExcelService excelServices;
	@Autowired
	private UserStoryService userStoryService;
	
	@Override
	public void exportIssuesPDF(List<Issue> issueList) throws DocumentException, IOException 
	{
		String relativeFontPath = "/resources/assets/font/ARIAL.TTF";
		FacesContext context = FacesContext.getCurrentInstance();
		ServletContext servletContext = (ServletContext) context.getExternalContext().getContext();
		String absolutePath = servletContext.getRealPath(relativeFontPath);
		Document document = new Document(PageSize.A4, 0, 0, 0, 0);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfWriter.getInstance(document, baos);

		if (!document.isOpen()) 
		{
			document.open();
		}
		
		BaseFont bf = BaseFont.createFont(absolutePath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED); 
		Font font = new Font(bf);
		
		com.hp.gagawa.java.Document xhtmlDocument = new com.hp.gagawa.java.Document(DocumentType.XHTMLTransitional);
		
		Head head = xhtmlDocument.head;
		
		Meta charSetMeta = new Meta("text/html; charset=UTF-8");
		charSetMeta.setHttpEquiv("Content-Type");
		head.appendChild(charSetMeta);
		
		head.appendText("<style type='text/css'> body { font-family: Arial, Helvetica, sans-serif;  } </style>");
		
		Body body = xhtmlDocument.body;
		Table wrapperTable = new Table();
		wrapperTable.setWidth("100%");
		wrapperTable.setBorder("1");
		wrapperTable.setStyle("border-collapse: collapse;");
		wrapperTable.setCellpadding("10");
		wrapperTable.setCellspacing("0");
		
		Tr row = null;
		final Paragraph page = new Paragraph();
		page.setFont(font);
		if (issueList.size() > 1) 
		{
			for (int i = 0; i < issueList.size(); i++) 
			{
				if(i % 2 == 0) 
				{
					row = new Tr();
					row.setStyle("height: 50%");
				}
				
				Td tableData = new Td();
				tableData.setValign("top");
				tableData.setWidth("50%");
				tableData.appendChild(prepareIssueTableContent(issueList.get(i)));
				row.appendChild(tableData);
				
				if((i % 2 != 0) || (i % 2 == 0 && i == (issueList.size() - 1))) 
				{
					wrapperTable.appendChild(row);
				}
			}
			
			body.appendChild(wrapperTable);
			XMLWorkerHelper.getInstance().parseXHtml(new ElementHandler() {
			@Override
			public void add(Writable w) {
					 if (w instanceof WritableElement) {
				           List<Element> elements = ((WritableElement)w).elements();
				           page.addAll(elements);
					 }
					
				}
			}, new StringReader(xhtmlDocument.write()));
			
		} else if(issueList.size() == 1) {
			wrapperTable.setWidth("50%");
			row = new Tr();
			row.setStyle("height: 50%");
			Td tableData = new Td();
			tableData.setValign("top");
			tableData.appendChild(prepareIssueTableContent(issueList.get(0)));
			row.appendChild(tableData);
			wrapperTable.appendChild(row);
			body.appendChild(wrapperTable);
			XMLWorkerHelper.getInstance().parseXHtml(new ElementHandler() {
				@Override
				public void add(Writable w) {
					 if (w instanceof WritableElement) {
				           List<Element> elements = ((WritableElement)w).elements();
				           page.addAll(elements);
					 }
					
				}
			}, new StringReader(xhtmlDocument.write()));
		}
		
		document.add(page);
		document.close();
		writeFileToResponse(baos, "issues_" + new Date().getTime()+".pdf","application/pdf; charset=UTF-8");
	}

	@Override
	public Table prepareIssueTableContent(Issue issueData) throws DocumentException, IOException {
	
		Tr lineBreakRow = new Tr();
		Td lineBreak = new Td();
		lineBreak.setColspan("2");
		lineBreak.appendText("<div style='width: 100%; height: 1px; background-color: #000;'></div>");
		lineBreakRow.appendChild(lineBreak);
		
		// Issue Id
		Table issueTable = new Table();
		issueTable.setWidth("100%");
		issueTable.setCellpadding("5");
	    Tr issueTypeAndAssignedMemberRow = new Tr();
	    
	    // Issue/US Type + Issue/US Id + Parent Issue/US Id (if any)
	    String issueType = (issueData.getType() == null) ? "" : issueData.getType();
	    Td issueTypeTd = new Td();
	    issueTypeTd.setStyle("font-size: 14px;");
	    issueTypeTd.setWidth("50%");
	    issueTypeTd.appendText(issueType+" #" + issueData.getDisplayIssueId() + ((issueData.getParent() == null) ? "": ", US #" + String.valueOf(issueData.getParent().getDisplayIssueId())));
	    
	    Td assignedMemberTd = new Td();
		assignedMemberTd.setWidth("50%");
		if (issueData.getAssigned() != null) {
			String assignedMember = issueData.getAssigned().getLastName() + " " + issueData.getAssigned().getFirstName();
			 assignedMemberTd.appendText(assignedMember);
			 assignedMemberTd.setStyle("font-size: 14px;; text-align: right;");
		}
		
		issueTypeAndAssignedMemberRow.appendChild(issueTypeTd, assignedMemberTd);

		int maxLengthName = 170;
		String subjectIssue = issueData.getSubject();
		if(subjectIssue.length() > maxLengthName){
			subjectIssue = subjectIssue.substring(0,maxLengthName) + "...";
		}
		
		// Issue Subject
		Tr issueSubjectRow = new Tr();
		Td issueSubjectData = new Td();
		String escSubject = Jsoup.clean(subjectIssue, Whitelist.none());
		issueSubjectData.setStyle("font-size: 14px;;");
		issueSubjectData.setColspan("2");
		issueSubjectData.appendText(escSubject);
		issueSubjectData.setHeight("35px");
		issueSubjectRow.appendChild(issueSubjectData);		
		
		// Project Name and remain point
		String projectName="";
		if(issueData.getParent()==null)
		{
			if(issueData.getUserStory().getProject() != null)
			{
				projectName=issueData.getUserStory().getProject().getProjectName();
			}
		}
		else
		{
			if(issueData.getParent().getUserStory().getProject() != null)
			{
				projectName=issueData.getParent().getUserStory().getProject().getProjectName();
			}
		}
		
		String remainingPoint = (issueData.getRemain() == null) ? "" : issueData.getRemain();
		Tr projectNameAndRemainPointRow = new Tr();
		
		Td projectNameData = new Td();
		projectNameData.setWidth("80%");
		projectNameData.setStyle("font-size: 14px;");
		projectNameData.appendText("Project: "+projectName);
		
		Td remainingPointData = new Td();
		remainingPointData.setWidth("20%");
		remainingPointData.setStyle("font-size: 14px;; text-align: right;");
		remainingPointData.appendText("Remaining: "+remainingPoint);
		projectNameAndRemainPointRow.appendChild(projectNameData,remainingPointData);

		// Priority
		String issuePriority = (issueData.getPriority() == null) ? "" : issueData.getPriority();
		Tr issuePriorityRow = new Tr();
		Td issuePriorityData = new Td();
		issuePriorityData.setColspan("2");
		issuePriorityData.setStyle("font-size: 14px;");
		issuePriorityData.appendText("Priority: "+issuePriority);
		issuePriorityRow.appendChild(issuePriorityData);
		
		// Sprint Name
		String issueSprint = (issueData.getSprint() == null) ? "" : issueData.getSprint().getSprintName();
		Tr issueSprintRow = new Tr();
		Td issueSprintData = new Td();
		issueSprintData.setColspan("2");
		issueSprintData.setStyle("font-size: 14px;");
		issueSprintData.appendText("Sprint: "+issueSprint);
		issueSprintRow.appendChild(issueSprintData);

		// Estimate
		String estimatePoint = (issueData.getEstimate() == null) ? "" : issueData.getEstimate();
		Tr estimatePointRow = new Tr();
		Td estimatePointData = new Td();
		estimatePointData.setColspan("2");
		estimatePointData.setStyle("font-size: 14px;");
		estimatePointData.appendText("Estimate: "+estimatePoint);
		estimatePointRow.appendChild(estimatePointData);

		// Printed Date
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		String printedDate = sdf.format(new Date());
		Tr printedDateRow = new Tr();
		Td printedDateData = new Td();
		printedDateData.setColspan("2");
		printedDateData.setStyle("font-size: 14px;");
		printedDateData.appendText("Printed Date: "+printedDate);
		printedDateRow.appendChild(printedDateData);
		
		// Issue Description
		String issueDesc = (issueData.getDescription() == null) ? "" : issueData.getDescription();
		issueDesc = issueDesc.replace("&nbsp;"," ");
		if(!issueDesc.trim().equals("")) {
			issueDesc = Jsoup.parse(issueDesc).outerHtml();
		}
		
		String plainTextDescription = Jsoup.parse(issueDesc).text();
		
		int maxLengthDescription = 700;
		if(plainTextDescription.length() > maxLengthDescription){
			issueDesc = plainTextDescription.substring(0, maxLengthDescription) + "...";
		}
		
		Tr issueDescRow = new Tr();
		Td issueDescData = new Td();
		issueDescData.setHeight("320px");
		issueDescData.setValign("top");
		issueDescData.setColspan("2");
		issueDescData.appendText(issueDesc);
		issueDescRow.appendChild(issueDescData);
		
		issueTable.appendChild(issueTypeAndAssignedMemberRow,
				issueSubjectRow,
				lineBreakRow, 
				issueDescRow,
				lineBreakRow, 
				projectNameAndRemainPointRow, 
				issuePriorityRow,
				issueSprintRow,
				estimatePointRow,
				printedDateRow
				);
		return issueTable;
	}

	public void writeFileToResponse(
			ByteArrayOutputStream baos, String fileName,String type) {
		try {
			FacesContext context = FacesContext.getCurrentInstance();
			ExternalContext externalContext = context.getExternalContext();
		
			externalContext.setResponseContentType(type);
			externalContext.setResponseHeader("Expires", "0");
			externalContext.setResponseHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
			externalContext.setResponseHeader("Pragma", "public");
			externalContext.setResponseHeader("Content-disposition", "attachment; filename=\"" + fileName + "\"");
			externalContext.setResponseContentLength(baos.size());
			OutputStream out = externalContext.getResponseOutputStream();
			baos.writeTo(out);
			externalContext.responseFlushBuffer();
			
			context.responseComplete();
		} catch (Exception e) {}
		
	}
	
	@Override
	public void generatePlainExcel(List<Issue> issueList) throws WriteException, IOException{
		ArrayList<ArrayList<String>> arrListIssues= new ArrayList<ArrayList<String>>();
		
		ArrayList<String> titleIssue= new ArrayList<String>();
		titleIssue.add("ID");
		titleIssue.add("Type");
		titleIssue.add("Subject");
		titleIssue.add("Assigned");
		titleIssue.add("Description");
		titleIssue.add("Project");
		titleIssue.add("Priority");
		titleIssue.add("Sprint");
		titleIssue.add("Estimate");
		titleIssue.add("Remaining");
		arrListIssues.add(titleIssue);
		
		long IssueParentId = 0;
		for(int i=0;i<issueList.size();i++)
		{
			if((issueList.get(i).getParent()!=null)&&(IssueParentId!=issueList.get(i).getParent().getDisplayIssueId())){
				IssueParentId = issueList.get(i).getParent().getDisplayIssueId();
				Issue IssueParent = issueList.get(i).getParent();
				ArrayList<String> issue= new ArrayList<String>();
				issue.add(IssueParent.getDisplayIssueId().toString());
				issue.add(IssueParent.getType());
				issue.add(IssueParent.getSubject());
				issue.add("");
				issue.add(IssueParent.getDescription());
				issue.add(IssueParent.getUserStory().getProject().getProjectName());
				issue.add((IssueParent.getPriority() == null) ? "" : IssueParent.getPriority());
				issue.add((IssueParent.getSprint() == null) ? "" : IssueParent.getSprint().getSprintName());
				issue.add((IssueParent.getEstimate() == null) ? "" : IssueParent.getEstimate());
				issue.add((IssueParent.getRemain() == null) ? "" : IssueParent.getRemain());
				arrListIssues.add(issue);
			}
			
			ArrayList<String> issue= new ArrayList<String>();
			if(issueList.get(i).getParent()!=null){
				issue.add(IssueParentId+" | "+issueList.get(i).getDisplayIssueId().toString());
				issue.add("Sub "+issueList.get(i).getType());
			}else{
				issue.add(issueList.get(i).getDisplayIssueId().toString());
				issue.add(issueList.get(i).getType());
			}
			issue.add(issueList.get(i).getSubject());
			issue.add((issueList.get(i).getAssigned()==null)? "" : 
				issueList.get(i).getAssigned().getFirstName()+" "+issueList.get(i).getAssigned().getLastName());
			issue.add((issueList.get(i).getDescription() == null) ? "" : Jsoup.parse(issueList.get(i).getDescription()).text());
			
			String projectName="";
			if(issueList.get(i).getParent()==null)
			{
				if(issueList.get(i).getUserStory().getProject() != null)
				{
					projectName=issueList.get(i).getUserStory().getProject().getProjectName();
				}
			}
			else
			{
				if(issueList.get(i).getParent().getUserStory().getProject() != null)
				{
					projectName=issueList.get(i).getParent().getUserStory().getProject().getProjectName();
				}
			}
			issue.add(projectName);
			
			issue.add((issueList.get(i).getPriority() == null) ? "" : issueList.get(i).getPriority());
			issue.add((issueList.get(i).getSprint() == null) ? "" : issueList.get(i).getSprint().getSprintName());
			issue.add((issueList.get(i).getEstimate() == null) ? "" : issueList.get(i).getEstimate());
			issue.add((issueList.get(i).getRemain() == null) ? "" : issueList.get(i).getRemain());
			
			arrListIssues.add(issue);
		}
		
		String name="issues_" + new Date().getTime();
		excelServices.setContent(name,arrListIssues);
	    this.writeFileToResponse(excelServices.writeASheet(), name+".xls", "application/vnd.ms-excel .xls; charset=UTF-8");
	}
	
	@Override
	public void generateRichExcel(List<Issue> issueList) throws WriteException, IOException,JRException{
		ArrayList<Hashtable<String, String>> arrListIssuesFormat= new ArrayList<Hashtable<String, String>>();
		ByteArrayOutputStream data=new ByteArrayOutputStream();

		long IssueParentId = 0;
		Hashtable<String, String> ht= new Hashtable<String, String>();
		for(int i=0;i<issueList.size();i++)
		{
			if((issueList.get(i).getParent()!=null)&&(IssueParentId!=issueList.get(i).getParent().getDisplayIssueId())){
				IssueParentId = issueList.get(i).getParent().getDisplayIssueId();
				Issue IssueParent = issueList.get(i).getParent();
				ht= new Hashtable<String, String>();
				ht.put("ID", IssueParent.getDisplayIssueId().toString());
				ht.put("Type", IssueParent.getType());
				ht.put("Subject", IssueParent.getSubject());
				ht.put("Assigned", "");
				String desc=(IssueParent.getDescription() == null) ? "" : IssueParent.getDescription();
				desc=Jsoup.parse(desc).outerHtml();
				desc=desc.replaceAll("<strong>", "<b>").replaceAll("</strong>", "</b>")
							.replaceAll("<em>", "<i>").replaceAll("</em>", "</i>");
				ht.put("Description", desc);
				ht.put("Project", IssueParent.getUserStory().getProject().getProjectName());
				ht.put("Priority", (IssueParent.getPriority() == null) ? "" : IssueParent.getPriority());
				ht.put("Sprint", (IssueParent.getSprint() == null) ? "" : IssueParent.getSprint().getSprintName());
				ht.put("Estimate", (IssueParent.getEstimate() == null) ? "" : IssueParent.getEstimate());
				ht.put("Remaining", (IssueParent.getRemain() == null) ? "" : IssueParent.getRemain());
				arrListIssuesFormat.add(ht);
			}
			
			ht= new Hashtable<String, String>();
			if(issueList.get(i).getParent()!=null){
				ht.put("ID", IssueParentId+" | "+issueList.get(i).getDisplayIssueId().toString());
				ht.put("Type", "Sub "+issueList.get(i).getType());
			}else{
				ht.put("ID", issueList.get(i).getDisplayIssueId().toString());
				ht.put("Type", issueList.get(i).getType());
			}
			ht.put("Subject", issueList.get(i).getSubject());
			ht.put("Assigned", (issueList.get(i).getAssigned()==null)? "" : 
				issueList.get(i).getAssigned().getFirstName()+" "+issueList.get(i).getAssigned().getLastName());
			String desc=(issueList.get(i).getDescription() == null) ? "" : issueList.get(i).getDescription();
			desc=Jsoup.parse(desc).outerHtml();
			desc=desc.replaceAll("<strong>", "<b>").replaceAll("</strong>", "</b>")
						.replaceAll("<em>", "<i>").replaceAll("</em>", "</i>");
			ht.put("Description", desc);
			
			String projectName="";
			if(issueList.get(i).getParent()==null)
			{
				if(issueList.get(i).getUserStory().getProject() != null)
				{
					projectName=issueList.get(i).getUserStory().getProject().getProjectName();
				}
			}
			else
			{
				if(issueList.get(i).getParent().getUserStory().getProject() != null)
				{
					projectName=issueList.get(i).getParent().getUserStory().getProject().getProjectName();
				}
			}
			ht.put("Project", projectName);
			ht.put("Priority", (issueList.get(i).getPriority() == null) ? "" : issueList.get(i).getPriority());
			ht.put("Sprint", (issueList.get(i).getSprint() == null) ? "" : issueList.get(i).getSprint().getSprintName());
			ht.put("Estimate", (issueList.get(i).getEstimate() == null) ? "" : issueList.get(i).getEstimate());
			ht.put("Remaining", (issueList.get(i).getRemain() == null) ? "" : issueList.get(i).getRemain());		
			arrListIssuesFormat.add(ht);
		}
		Resource resource = new ClassPathResource("/Reports/IssueReport.jrxml");
		// jrxml compiling process
		JasperReport jasperReport = JasperCompileManager.compileReport(resource.getInputStream());
		// filling report with data from data source
		JRMapArrayDataSource ds = new JRMapArrayDataSource(arrListIssuesFormat.toArray());
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport,new HashMap<String,Object>(),ds); 
		JRXlsExporter exporter = new JRXlsExporter();
		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
		exporter.setParameter(JRExporterParameter.OUTPUT_STREAM, data );
		exporter.exportReport();
		
		String name="issues_" + new Date().getTime();
		this.writeFileToResponse(data, name+".xls", "application/vnd.ms-excel; charset=UTF-8");
	}

	@Override
	public ArrayList<ArrayList<String>> readExcel(String name,int type) throws IOException {
		if(type==1)
			return excelServices.read(name);
		else
			return excelServices.readXLSX(name);
	}

	@Override
	public void exportProductBacklogToExcel(List<UserStory> userStories) throws WriteException, IOException 
	{
		ArrayList<ArrayList<String>> arrListProductBacklog= new ArrayList<ArrayList<String>>();
		String projectName = "";
		ArrayList<String> titleIssue= new ArrayList<String>();
		titleIssue.add("User Story ID");
		titleIssue.add("Name");
		titleIssue.add("Description");
		titleIssue.add("Value");
		titleIssue.add("Risk");
		titleIssue.add("Priority");
		titleIssue.add("Status");
		titleIssue.add("Note");
		titleIssue.add("ProjectName");
		
		arrListProductBacklog.add(titleIssue);
		
		for(UserStory us:userStories)
		{
			ArrayList<String> issue= new ArrayList<String>();
			issue.add(us.getUserStoryId().toString());
			issue.add(us.getName());
			issue.add(StringEscapeUtils.unescapeHtml(us.getDescription().replaceAll("\\<.*?>","")));
			issue.add(String.valueOf(us.getValue()));
			issue.add(String.valueOf(us.getRisk()));
			
			if(us.getPriority()!= null){
				issue.add(us.getPriority().toString());
			}else{
				issue.add("");
			}
			
			if(us.getStatus() != null){
				issue.add(us.getStatus().toString());
			}else{
				issue.add("");
			}
			
			issue.add(us.getNote());
			
			projectName = us.getProject().getProjectName();
			issue.add(projectName);
			arrListProductBacklog.add(issue);
		}
		
		String fileName = "ProductBacklog_" + projectName + "_" + new Date().getTime();
		String sheetName = "PBL_" + projectName;
		
		excelServices.setContent(sheetName, arrListProductBacklog);
		
	    this.writeFileToResponse(excelServices.writeASheet(), fileName+".xls", "application/vnd.ms-excel; charset=UTF-8");
	}

	@Override
	public void exportUserStoryPDF(List<UserStory> selectedUserStories) throws IOException, DocumentException 
	{
		List<UserStory> tempUserStoryList = new ArrayList<UserStory>();
		List<Long> userStoryIdList = new ArrayList<Long>();
		
		for(UserStory userstory:selectedUserStories)
		{
			userStoryIdList.add(userstory.getUserStoryId());
		}
		
		tempUserStoryList = this.userStoryService.findAllUserStoryByIdList(userStoryIdList);
		selectedUserStories = tempUserStoryList;
		String relativeFontPath = "/resources/assets/font/ARIAL.TTF";
		FacesContext context = FacesContext.getCurrentInstance();
		ServletContext servletContext = (ServletContext) context.getExternalContext().getContext();
		String absolutePath = servletContext.getRealPath(relativeFontPath);
		Document document = new Document(PageSize.A4, 0, 0, 0, 0);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		PdfWriter.getInstance(document, baos);

		if (!document.isOpen()) {
			document.open();
		}
		
		BaseFont bf = BaseFont.createFont(absolutePath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED); 
		Font font = new Font(bf);
		
		com.hp.gagawa.java.Document xhtmlDocument = new com.hp.gagawa.java.Document(DocumentType.XHTMLTransitional);
		
		Head head = xhtmlDocument.head;
		
		Meta charSetMeta = new Meta("text/html; charset=UTF-8");
		charSetMeta.setHttpEquiv("Content-Type");
		head.appendChild(charSetMeta);
		
		head.appendText("<style type='text/css'> body { font-family: Arial, Helvetica, sans-serif;  } </style>");
		
		Body body = xhtmlDocument.body;
		Table wrapperTable = new Table();
		wrapperTable.setWidth("100%");
		wrapperTable.setBorder("1");
		wrapperTable.setStyle("border-collapse: collapse;");
		wrapperTable.setCellpadding("10");
		wrapperTable.setCellspacing("0");
		
		Tr row = null;
		final Paragraph page = new Paragraph();
		page.setFont(font);
		if (selectedUserStories.size() > 1) {
			for (int i = 0; i < selectedUserStories.size(); i++) {
				if(i % 2 == 0) {
					row = new Tr();
					row.setStyle("height: 50%");
				}
				
				Td tableData = new Td();
				tableData.setValign("top");
				tableData.setWidth("50%");
				tableData.appendChild(prepareUserStoryTableContent(selectedUserStories.get(i)));
				row.appendChild(tableData);
				
				if((i % 2 != 0) || (i % 2 == 0 && i == (selectedUserStories.size() - 1))) {
					wrapperTable.appendChild(row);
				}
			}
			
			body.appendChild(wrapperTable);
			XMLWorkerHelper.getInstance().parseXHtml(new ElementHandler() {
			@Override
			public void add(Writable w) {
					 if (w instanceof WritableElement) {
				           List<Element> elements = ((WritableElement)w).elements();
				           page.addAll(elements);
					 }
					
				}
			}, new StringReader(xhtmlDocument.write()));
			
		} else if(selectedUserStories.size() == 1) {
			wrapperTable.setWidth("50%");
			row = new Tr();
			row.setStyle("height: 50%");
			Td tableData = new Td();
			tableData.setValign("top");
			tableData.appendChild(prepareUserStoryTableContent(selectedUserStories.get(0)));
			row.appendChild(tableData);
			wrapperTable.appendChild(row);
			body.appendChild(wrapperTable);
			XMLWorkerHelper.getInstance().parseXHtml(new ElementHandler() {
				@Override
				public void add(Writable w) {
					 if (w instanceof WritableElement) {
				           List<Element> elements = ((WritableElement)w).elements();
				           page.addAll(elements);
					 }
					
				}
			}, new StringReader(xhtmlDocument.write()));
		}
		document.add(page);
		document.close();
		writeFileToResponse(baos, "userstories_" + new Date().getTime()+".pdf","application/pdf; charset=UTF-8");
	}

	private Table prepareUserStoryTableContent(UserStory userStory) 
	{
		Tr lineBreakRow = new Tr();
		Td lineBreak = new Td();
		lineBreak.setColspan("2");
		lineBreak.appendText("<div style='width: 100%; height: 1px; background-color: #000;'></div>");
		lineBreakRow.appendChild(lineBreak);
		
		// table userstory
		Table userStoryTable = new Table();
		userStoryTable.setWidth("100%");
		userStoryTable.setCellpadding("5");

		//userstory id
		Tr userStoryIdRow = new Tr();
		Td userStoryTypeTd = new Td();
		userStoryTypeTd.setStyle("font-size: 14px;");
		userStoryTypeTd.setWidth("50%");
		userStoryTypeTd.appendText("UserStory #"+userStory.getUserStoryId());
		userStoryIdRow.appendChild(userStoryTypeTd);
		
		int maxLengthName = 170;
		String nameUserStory = userStory.getName();
		if(nameUserStory.length() > maxLengthName){
			nameUserStory = nameUserStory.substring(0,maxLengthName) + "...";
		}
		
		// userstory Subject
		Tr userStorySubjectRow = new Tr();
		Td userStorySubjectData = new Td();
		String escSubject = Jsoup.clean(nameUserStory, Whitelist.none());
		userStorySubjectData.setStyle("font-size: 14px;;");
		userStorySubjectData.setColspan("2");
		userStorySubjectData.appendText(escSubject);
		userStorySubjectData.setHeight("35px");
		userStorySubjectRow.appendChild(userStorySubjectData);		
		
		// Project Name and remain point
		String projectName = userStory.getProject().getProjectName();
		
		Tr projectNameRow = new Tr();
		Td projectNameData = new Td();
		projectNameData.setWidth("80%");
		projectNameData.setStyle("font-size: 14px;");
		projectNameData.appendText("Project: "+projectName);
		projectNameRow.appendChild(projectNameData);
		
		// Priority
		String userStoryPriority = (String.valueOf(userStory.getPriority()) == null) ? "" : String.valueOf(userStory.getPriority());
		Tr userStoryPriorityRow = new Tr();
		Td userStoryPriorityData = new Td();
		userStoryPriorityData.setColspan("2");
		userStoryPriorityData.setStyle("font-size: 14px;");
		userStoryPriorityData.appendText("Priority: "+userStoryPriority);
		userStoryPriorityRow.appendChild(userStoryPriorityData);
		
		//userstory value row
		Tr userStoryValueRow = new Tr();
		Td userStoryvalueData = new Td();
		userStoryvalueData.setWidth("100%");
		userStoryvalueData.setStyle("font-size: 14px;");
		userStoryvalueData.appendText("Value: "+userStory.getValue());
		userStoryValueRow.appendChild(userStoryvalueData);

		// Printed Date
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
		String printedDate = sdf.format(new Date());
		Tr printedDateRow = new Tr();
		Td printedDateData = new Td();
		printedDateData.setColspan("2");
		printedDateData.setStyle("font-size: 14px;");
		printedDateData.appendText("Printed Date: "+printedDate);
		printedDateRow.appendChild(printedDateData);
		
		// userstory Description
		String userStoryDesc = (userStory.getDescription() == null) ? "" : userStory.getDescription();
		userStoryDesc = userStoryDesc.replace("&nbsp;", " ");
		
		if(!userStoryDesc.trim().equals("")) {
			userStoryDesc = Jsoup.parse(userStoryDesc).outerHtml();
		}

		String plainTextDescription = Jsoup.parse(userStoryDesc).text();
		
		int maxLengthDescription = 700;
		if(plainTextDescription.length() > maxLengthDescription){
			userStoryDesc = plainTextDescription.substring(0, maxLengthDescription) + "...";
		}
		Tr userStoryDescRow = new Tr();
		Td userStoryDescData = new Td();
		userStoryDescData.setHeight("320px");
		userStoryDescData.setValign("top");
		userStoryDescData.setColspan("2");
		if(!userStoryDesc.isEmpty()){
			userStoryDescData.appendText( userStoryDesc);
		}
		userStoryDescRow.appendChild(userStoryDescData);
		
		
		userStoryTable.appendChild(
				userStoryIdRow,
				userStorySubjectRow,
				lineBreakRow, 
				userStoryDescRow,
				lineBreakRow, 
				projectNameRow,
				userStoryValueRow,
				userStoryPriorityRow,
				printedDateRow
				);
		return userStoryTable;
	}


}

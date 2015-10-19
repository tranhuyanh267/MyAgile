package com.ant.myagile.managedbean;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.utils.JSFUtils;
import com.ant.myagile.utils.MyAgileFileUtils;
import com.ant.myagile.utils.Utils;

@Component("downloadBean")
@Scope("session")
public class DownloadBean implements Serializable {
	private static final long serialVersionUID = 626953318628565053L;
	private static final String ROOT_FOLDER_PATH = MyAgileFileUtils
			.getRootStorageLocation();
	private static final String ATTACHMENT_FOLDER = "/attachment";
	private String fileName;
	private String projectId;
	private String wikiId;
	private String topicId;
	
	public void download() throws IOException {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();
		externalContext.responseReset();

		String originalFilename = this.fileName;
		String filename = ROOT_FOLDER_PATH + "/projects/P" + this.projectId
				+ ATTACHMENT_FOLDER + "/" + this.fileName;

		File f = new File(filename);
		if (!f.exists()) {
			JSFUtils.addWarningMessage("downloadMsgs",
					Utils.getMessage("myagile.FileNotFound", null));
		} else {
			int length = 0;
			String mimetype = "ExternalContext#getMimeType()";
			externalContext
					.setResponseContentType((mimetype != null) ? mimetype
							: "application/octet-stream");
			externalContext.setResponseContentLength((int) f.length());
			externalContext.setResponseHeader("Content-Disposition",
					"attachment; filename=\"" + originalFilename + "\"");
			OutputStream output = externalContext.getResponseOutputStream();
			byte[] bbuf = new byte[filename.length()];
			DataInputStream in = new DataInputStream(new FileInputStream(f));
			while ((in != null) && ((length = in.read(bbuf)) != -1)) {
				output.write(bbuf, 0, length);
			}
			in.close();
			output.flush();
			output.close();
			facesContext.responseComplete();
		}
	}

	public void downloadAttachment() throws IOException {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();
		externalContext.responseReset();

		String originalFilename = this.fileName;
		String filename = ROOT_FOLDER_PATH + "/wiki/W" + this.wikiId
				+ ATTACHMENT_FOLDER + "/" + this.fileName;

		File f = new File(filename);
		if (!f.exists()) {
			JSFUtils.addWarningMessage("downloadMsgs",
					Utils.getMessage("myagile.FileNotFound", null));
		} else {
			int length = 0;
			String mimetype = "ExternalContext#getMimeType()";
			externalContext
					.setResponseContentType((mimetype != null) ? mimetype
							: "application/octet-stream");
			externalContext.setResponseContentLength((int) f.length());
			externalContext.setResponseHeader("Content-Disposition",
					"attachment; filename=\"" + originalFilename + "\"");
			OutputStream output = externalContext.getResponseOutputStream();
			byte[] bbuf = new byte[filename.length()];
			DataInputStream in = new DataInputStream(new FileInputStream(f));
			while ((in != null) && ((length = in.read(bbuf)) != -1)) {
				output.write(bbuf, 0, length);
			}
			in.close();
			output.flush();
			output.close();
			facesContext.responseComplete();
		}
	}

	public void downloadTopicAttachment() throws IOException {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		ExternalContext externalContext = facesContext.getExternalContext();
		externalContext.responseReset();

		String originalFilename = this.fileName;
		
		String filename = ROOT_FOLDER_PATH + "/wiki/W" + wikiId + "/T" + topicId
				+ ATTACHMENT_FOLDER + "/"
				+ this.fileName;
		File f = new File(filename);
		if (!f.exists()) {
			JSFUtils.addWarningMessage("downloadMsgs",
					Utils.getMessage("myagile.FileNotFound", null));
		} else {
			int length = 0;
			String mimetype = "ExternalContext#getMimeType()";
			externalContext
					.setResponseContentType((mimetype != null) ? mimetype
							: "application/octet-stream");
			externalContext.setResponseContentLength((int) f.length());
			externalContext.setResponseHeader("Content-Disposition",
					"attachment; filename=\"" + originalFilename + "\"");
			OutputStream output = externalContext.getResponseOutputStream();
			byte[] bbuf = new byte[filename.length()];
			DataInputStream in = new DataInputStream(new FileInputStream(f));
			while ((in != null) && ((length = in.read(bbuf)) != -1)) {
				output.write(bbuf, 0, length);
			}
			in.close();
			output.flush();
			output.close();
			facesContext.responseComplete();
		}
	}

	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getProjectId() {
		return this.projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	public String getWikiId() {
		return wikiId;
	}

	public void setWikiId(String wikiId) {
		this.wikiId = wikiId;
	}

	public String getTopicId() {
		return topicId;
	}

	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}

}
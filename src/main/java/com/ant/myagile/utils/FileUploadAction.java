package com.ant.myagile.utils;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import javax.imageio.ImageIO;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.ant.myagile.service.impl.MeetingVideoServiceImpl;

@Component
@Scope("request")
public class FileUploadAction extends HttpServlet {

	private static final int SMALL_AVATAR_SIZE = 100;
	private static final long BYTE = 1000;
	private static final long serialVersionUID = 3447685998419256747L;
	private static final String RESP_SUCCESS = "{\"jsonrpc\" : \"2.0\", \"result\" : \"success\", \"id\" : \"id\"}";
	private static final String RESP_ERROR = "{\"jsonrpc\" : \"2.0\", \"error\" : {\"code\": 101, \"message\": \"Failed to open input stream.\"}, \"id\" : \"id\"}";
	public static final String JSON = "application/json";
	public static final int BUF_SIZE = 2 * 1024;
	// bytes = 10KB.
	private static final int DEFAULT_BUFFER_SIZE = 10240;
	// ms = 1
	private static final long DEFAULT_EXPIRE_TIME = 604800000L;
	// week.
	private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";

	private final String fileUploadRootDir = MyAgileFileUtils
			.getRootStorageLocation();
	private final String fileUploadTempDir = MyAgileFileUtils
			.getStorageLocation("myagile.upload.temp.location");
	private final String imageProjectFolder = "/projects/";
	private final String meetingVideoFolder = "/meeting_videos/";
	private final String videoGuide = "video";
	private final String imageTeamFolder = "/teams";
	private final String imageMemberFolder = MyAgileFileUtils
			.getStorageLocation("myagile.upload.image.member.folder");
	private final String attachmentFileFolder = "/attachment/";

	private String fileDir = "";

	private int chunk;
	private int chunks;
	private String name;
	private String user;
	private String time;
	private boolean isLoadingSmallMemberLogo = false;

	/* (non-Javadoc)
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String realPath = "";
		isLoadingSmallMemberLogo = false;
		if (req.getParameter("type") != null
				&& req.getParameter("filename") != null) {
			if (req.getParameter("type").equalsIgnoreCase("project-logo")) {
				String filename = req.getParameter("filename");
				String p = req.getParameter("pId");
				if (p.equals("")) {
					realPath = fileUploadTempDir + "/" + imageProjectFolder
							+ "/" + filename;
				} else {
					realPath = fileUploadRootDir + "/" + imageProjectFolder
							+ "/P" + p + "/" + filename;
				}
			} else if (req.getParameter("type").equalsIgnoreCase("team-logo")) {
				String filename = req.getParameter("filename");
				String teamId = req.getParameter("t");
				if (!teamId.equals("")) {
					realPath = fileUploadRootDir + "/teams/T" + teamId + "/"
							+ filename;
				} else {
					realPath = fileUploadTempDir + "/teams/" + filename;
				}
			} else if (req.getParameter("type").equalsIgnoreCase(
					"meeting-video")) {
				String filename = req.getParameter("filename");
				String teamId = req.getParameter("t");
				String sprintId = req.getParameter("sp");
				realPath = MeetingVideoServiceImpl.meetingVideoURL(teamId,
						sprintId, filename);
			} else if (req.getParameter("type").equalsIgnoreCase("member-logo")) {
				String filename = req.getParameter("filename");
				realPath = imageMemberFolder + "/" + filename;
			} else if (req.getParameter("type").equalsIgnoreCase("small-member-logo")) {
				String filename = req.getParameter("filename");
				realPath = imageMemberFolder + "/" + filename;
				isLoadingSmallMemberLogo = true;
			} else if (req.getParameter("type").equalsIgnoreCase("video")) {
				String filename = req.getParameter("filename");
				realPath = fileUploadRootDir + "/" + videoGuide + "/" + filename;
			} else if (req.getParameter("type").equalsIgnoreCase("attachment")) {
				String filename = req.getParameter("filename");
				String wikiId = req.getParameter("id");
				realPath = fileUploadRootDir + "/wiki/W" + wikiId
						+ attachmentFileFolder + filename;
			} else if (req.getParameter("type").equalsIgnoreCase("topic-attachment")) {
				String filename = req.getParameter("filename");
				String wikiId = req.getParameter("wikiId");
				String topicId = req.getParameter("topicId");
				realPath = fileUploadRootDir + "/wiki/W" + wikiId + "/T"
						+ topicId + attachmentFileFolder + filename;
			} else if (req.getParameter("type").equalsIgnoreCase("facebook-news")){
				String filename = req.getParameter("filename");
				realPath = fileUploadRootDir + "/facebooknewsphoto/" + filename;
			}
			processRequest(req, resp, realPath);
		}

	}

	/**
	 * Handles an HTTP POST request from Plupload.
	 * 
	 * @param req
	 *            The HTTP request
	 * @param resp
	 *            The HTTP response
	 */
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String responseString = RESP_SUCCESS;
		boolean isMultipart = ServletFileUpload.isMultipartContent(req);

		if (isMultipart) {
			ServletFileUpload upload = new ServletFileUpload();
			try {
				FileItemIterator iter = upload.getItemIterator(req);
				while (iter.hasNext()) {
					FileItemStream item = iter.next();
					InputStream input = item.openStream();

					// Handle a form field.
					if (item.isFormField()) {
						String fileName = item.getFieldName();
						String value = Streams.asString(input);

						if (fileName.equalsIgnoreCase("upload_type")
								&& value.equalsIgnoreCase("project-logo")) {
							fileDir = fileUploadTempDir + imageProjectFolder;
						} else if (fileName.equalsIgnoreCase("upload_type")
								&& value.equalsIgnoreCase("team-logo")) {
							fileDir = fileUploadTempDir + imageTeamFolder;
						} else if (fileName.equalsIgnoreCase("upload_type")
								&& value.equalsIgnoreCase("member-logo")) {
							fileDir = imageMemberFolder;
						} else if (fileName.equalsIgnoreCase("upload_type")
								&& value.equalsIgnoreCase("meeting-video")) {
							fileDir = fileUploadTempDir + meetingVideoFolder;
						} else if (fileName.equalsIgnoreCase("upload_type")
								&& value.equalsIgnoreCase("attachment-file")) {
							fileDir = fileUploadTempDir + attachmentFileFolder;
						}

						if ("name".equals(fileName)) {
							this.name = value;
						} else if ("chunks".equals(fileName)) {
							this.chunks = Integer.parseInt(value);
						} else if ("chunk".equals(fileName)) {
							this.chunk = Integer.parseInt(value);
						} else if ("user".equals(fileName)) {
							this.user = value;
						} else if ("time".equals(fileName)) {
							this.time = value;
						}
					}

					// Handle a multi-part MIME encoded file.
					else {
						File dstFile = new File(fileDir);
						if (!dstFile.exists()) {
							dstFile.mkdirs();
						}
						File dst = new File(dstFile.getPath() + "/" + this.name);
						saveUploadFile(input, dst);
					}
				}
			} catch (Exception e) {
				responseString = RESP_ERROR;
			}
		}

		// Not a multi-part MIME request.
		else {
			responseString = RESP_ERROR;
		}

		resp.setContentType(JSON);
		byte[] responseBytes = responseString.getBytes();
		resp.setContentLength(responseBytes.length);
		ServletOutputStream output = resp.getOutputStream();
		output.write(responseBytes);
		output.flush();
	}

	/**
	 * Saves the given file item (using the given input stream) to the web
	 * server's local directory.
	 * 
	 * @param input
	 *            The input stream to read the file from
	 * @param dst
	 *            The dir of upload
	 */
	private void saveUploadFile(InputStream input, File dst) throws IOException {
		OutputStream out = null;
		try {
			if (dst.exists()) {
				out = new BufferedOutputStream(new FileOutputStream(dst, true),
						BUF_SIZE);
			} else {
				out = new BufferedOutputStream(new FileOutputStream(dst),
						BUF_SIZE);
			}

			byte[] buffer = new byte[BUF_SIZE];
			int len = 0;
			while ((len = input.read(buffer)) > 0) {
				out.write(buffer, 0, len);
			}
		} catch (Exception e) {

		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (IOException e) {
				}
			}
			if (null != out) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/*
	 * Handle for streaming video files and images by using byte-stream method.
	 */
	private void processRequest(HttpServletRequest request,
			HttpServletResponse response, String absoluteFilePath)
			throws IOException {
		File file = new File(absoluteFilePath);
		if (!file.exists()) {
			ServletContext context = getServletContext();
			String fullPath = context
					.getRealPath("/resources/img/no_image_available.png");
			file = new File(fullPath);
		} 
			
		try {
			
			if (isLoadingSmallMemberLogo) {
				File thumbnailFolder = new File(imageMemberFolder
						+ "/thumbnail");
				if (!thumbnailFolder.exists()) {
					thumbnailFolder.mkdirs();
				}
				File thumbnail = new File(thumbnailFolder.getPath() + "/"
						+ file.getName());
				if (!thumbnail.exists()) {
					thumbnail.createNewFile();
					Image image = ImageIO.read(file);
					ImageIO.write(resize(image, SMALL_AVATAR_SIZE, SMALL_AVATAR_SIZE), "JPEG", thumbnail);
				}
				file = thumbnail;
			}
			
			String fileName = file.getName();
			boolean content = true;

			if (request.getMethod().equalsIgnoreCase("HEAD")) {
				content = false;
			}

			Long length = file.length();
			Long lastModified = file.lastModified();
			String eTag = fileName + "_" + length + "_" + lastModified;

			// Validate request headers for caching
			// ---------------------------------------------------

			// If-None-Match header should contain "*" or ETag. If so, then
			// return 304.
			String ifNoneMatch = request.getHeader("If-None-Match");
			if (ifNoneMatch != null && matches(ifNoneMatch, eTag)) {
				// Required in 304.
				response.setHeader("ETag", eTag);
				response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}

			// If-Modified-Since header should be greater than LastModified. If
			// so, then return 304.
			// This header is ignored if any If-None-Match header is specified.
			long ifModifiedSince = request.getDateHeader("If-Modified-Since");
			if (ifNoneMatch == null && ifModifiedSince != -1
					&& ifModifiedSince + BYTE > lastModified) {
				// Required in 304.
				response.setHeader("ETag", eTag);
				response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
				return;
			}

			// Validate request headers for resume
			// ----------------------------------------------------

			// If-Match header should contain "*" or ETag. If not, then return
			// 412.
			String ifMatch = request.getHeader("If-Match");
			if (ifMatch != null && !matches(ifMatch, eTag)) {
				response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
				return;
			}

			// If-Unmodified-Since header should be greater than LastModified.
			// If not, then return 412.
			long ifUnmodifiedSince = request
					.getDateHeader("If-Unmodified-Since");
			if (ifUnmodifiedSince != -1
					&& ifUnmodifiedSince + BYTE <= lastModified) {
				response.sendError(HttpServletResponse.SC_PRECONDITION_FAILED);
				return;
			}

			// Validate and process range
			// -------------------------------------------------------------

			// Prepare some variables. The full Range represents the complete
			// file.
			Range full = new Range(0, length - 1, length);
			List<Range> ranges = new ArrayList<Range>();

			// Validate and process Range and If-Range headers.
			String range = request.getHeader("Range");
			if (range != null) {

				// Range header should match format "bytes=n-n,n-n,n-n...". If
				// not, then return 416.
				if (!range.matches("^bytes=\\d*-\\d*(,\\d*-\\d*)*$")) {
					// Required in 416.
					response.setHeader("Content-Range", "bytes */" + length);
					response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
					return;
				}

				// If-Range header should either match ETag or be greater then
				// LastModified. If not,
				// then return full file.
				String ifRange = request.getHeader("If-Range");
				if (ifRange != null && !ifRange.equals(eTag)) {
					try {
						long ifRangeTime = request.getDateHeader("If-Range");
						if (ifRangeTime != -1
								&& ifRangeTime + BYTE < lastModified) {
							ranges.add(full);
						}
					} catch (IllegalArgumentException ignore) {
						ranges.add(full);
					}
				}

				// If any valid If-Range header, then process each part of byte
				// range.
				if (ranges.isEmpty()) {
					for (String part : range.substring(6).split(",")) {
						// Assuming a file with length of 100, the following
						// examples returns bytes at:
						// 50-80 (50 to 80), 40- (40 to length=100), -20
						// (length-20=80 to length=100).
						long start = sublong(part, 0, part.indexOf("-"));
						long end = sublong(part, part.indexOf("-") + 1,
								part.length());

						if (start == -1) {
							start = length - end;
							end = length - 1;
						} else if (end == -1 || end > length - 1) {
							end = length - 1;
						}

						// Check if Range is syntactically valid. If not, then
						// return 416.
						if (start > end) {
							response.setHeader("Content-Range", "bytes */"
									+ length);
							response.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
							return;
						}

						// Add range.
						ranges.add(new Range(start, end, length));
					}
				}
			}

			// Prepare and initialize response
			// --------------------------------------------------------

			// Get content type by file name and set default GZIP support and
			// content disposition.
			String contentType = getServletContext().getMimeType(fileName);
			boolean acceptsGzip = false;
			String disposition = "inline";

			// If content type is unknown, then set the default value.
			// For all content types, see:
			// http://www.w3schools.com/media/media_mimeref.asp
			// To add new content types, add new mime-mapping entry in web.xml.
			if (contentType == null) {
				contentType = "application/octet-stream";
			}

			// If content type is text, then determine whether GZIP content
			// encoding is supported by
			// the browser and expand content type with the one and right
			// character encoding.
			if (contentType.startsWith("text")) {
				String acceptEncoding = request.getHeader("Accept-Encoding");
				acceptsGzip = acceptEncoding != null
						&& accepts(acceptEncoding, "gzip");
				contentType += ";charset=UTF-8";
			}

			// Else, expect for images, determine content disposition. If
			// content type is supported by
			// the browser, then set to inline, else attachment which will pop a
			// 'save as' dialogue.
			else if (!contentType.startsWith("image")) {
				String accept = request.getHeader("Accept");
				disposition = accept != null && accepts(accept, contentType) ? "inline"
						: "attachment";
			}

			// Initialize response.
			response.reset();
			response.setBufferSize(DEFAULT_BUFFER_SIZE);
			response.setHeader("Content-Disposition", disposition
					+ ";filename=\"" + fileName + "\"");
			response.setHeader("Accept-Ranges", "bytes");
			response.setHeader("ETag", eTag);
			response.setDateHeader("Last-Modified", lastModified);
			response.setDateHeader("Expires", System.currentTimeMillis()
					+ DEFAULT_EXPIRE_TIME);

			// Send requested file (part(s)) to client
			// ------------------------------------------------

			// Prepare streams.
			RandomAccessFile input = null;
			OutputStream output = null;

			try {
				// Open streams.
				input = new RandomAccessFile(file, "r");
				output = response.getOutputStream();

				if (ranges.isEmpty() || ranges.get(0) == full) {

					// Return full file.
					Range r = full;
					response.setContentType(contentType);
					response.setHeader("Content-Range", "bytes " + r.start
							+ "-" + r.end + "/" + r.total);

					if (content) {
						if (acceptsGzip) {
							// The browser accepts GZIP, so GZIP the content.
							response.setHeader("Content-Encoding", "gzip");
							output = new GZIPOutputStream(output,
									DEFAULT_BUFFER_SIZE);
						} else {
							// Content length is not directly predictable in
							// case of GZIP.
							// So only add it if there is no means of GZIP, else
							// browser will hang.
							response.setHeader("Content-Length",
									String.valueOf(r.length));
						}

						// Copy full range.
						copy(input, output, r.start, r.length);
					}

				} else if (ranges.size() == 1) {

					// Return single part of file.
					Range r = ranges.get(0);
					response.setContentType(contentType);
					response.setHeader("Content-Range", "bytes " + r.start
							+ "-" + r.end + "/" + r.total);
					response.setHeader("Content-Length",
							String.valueOf(r.length));
					response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

					if (content) {
						// Copy single part range.
						copy(input, output, r.start, r.length);
					}

				} else {

					// Return multiple parts of file.
					response.setContentType("multipart/byteranges; boundary="
							+ MULTIPART_BOUNDARY);
					response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

					if (content) {
						// Cast back to ServletOutputStream to get the easy
						// println methods.
						ServletOutputStream sos = (ServletOutputStream) output;

						// Copy multi part range.
						for (Range r : ranges) {
							// Add multipart boundary and header fields for
							// every range.
							sos.println();
							sos.println("--" + MULTIPART_BOUNDARY);
							sos.println("Content-Type: " + contentType);
							sos.println("Content-Range: bytes " + r.start + "-"
									+ r.end + "/" + r.total);

							// Copy single part range of multi part range.
							copy(input, output, r.start, r.length);
						}

						// End with multipart boundary.
						sos.println();
						sos.println("--" + MULTIPART_BOUNDARY + "--");
					}
				}
			} finally {
				// Gently close streams.
				close(output);
				close(input);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * Converts a given BufferedImage into an Image
	 * 
	 * @param bimage
	 *            The BufferedImage to be converted
	 * @return The converted Image
	 */
	public static Image toImage(BufferedImage bimage) {
		// Casting is enough to convert from BufferedImage to Image
		Image img = (Image) bimage;
		return img;
	}

	/**
	 * Resizes a given image to given width and height
	 * 
	 * @param img
	 *            The image to be resized
	 * @param width
	 *            The new width
	 * @param height
	 *            The new height
	 * @return The resized image
	 */
	public static BufferedImage resize(Image img, int width, int height) {
		// Create a null image
		Image image = null;
		// Resize into a BufferedImage
		BufferedImage bimg = new BufferedImage(width, height,
				BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D bGr = bimg.createGraphics();
		bGr.drawImage(img, 0, 0, width, height, null);
		bGr.dispose();
		// Convert to Image and return it
		// image = toImage(bimg);
		return bimg;
	}

	// Helpers (can be refactored to public utility class)
	// ----------------------------------------

	/**
	 * Returns true if the given accept header accepts the given value.
	 * 
	 * @param acceptHeader
	 *            The accept header.
	 * @param toAccept
	 *            The value to be accepted.
	 * @return True if the given accept header accepts the given value.
	 */
	private boolean accepts(String acceptHeader, String toAccept) {
		String[] acceptValues = acceptHeader.split("\\s*(,|;)\\s*");
		Arrays.sort(acceptValues);
		return Arrays.binarySearch(acceptValues, toAccept) > -1
				|| Arrays.binarySearch(acceptValues,
						toAccept.replaceAll("/.*$", "/*")) > -1
				|| Arrays.binarySearch(acceptValues, "*/*") > -1;
	}

	/**
	 * Returns true if the given match header matches the given value.
	 * 
	 * @param matchHeader
	 *            The match header.
	 * @param toMatch
	 *            The value to be matched.
	 * @return True if the given match header matches the given value.
	 */
	private boolean matches(String matchHeader, String toMatch) {
		String[] matchValues = matchHeader.split("\\s*,\\s*");
		Arrays.sort(matchValues);
		return Arrays.binarySearch(matchValues, toMatch) > -1
				|| Arrays.binarySearch(matchValues, "*") > -1;
	}

	/**
	 * Returns a substring of the given string value from the given begin index
	 * to the given end index as a long. If the substring is empty, then -1 will
	 * be returned
	 * 
	 * @param value
	 *            The string value to return a substring as long for.
	 * @param beginIndex
	 *            The begin index of the substring to be returned as long.
	 * @param endIndex
	 *            The end index of the substring to be returned as long.
	 * @return A substring of the given string value as long or -1 if substring
	 *         is empty.
	 */
	private long sublong(String value, int beginIndex, int endIndex) {
		String substring = value.substring(beginIndex, endIndex);
		return (substring.length() > 0) ? Long.parseLong(substring) : -1;
	}

	/**
	 * Copy the given byte range of the given input to the given output.
	 * 
	 * @param input
	 *            The input to copy the given range to the given output for.
	 * @param output
	 *            The output to copy the given range from the given input for.
	 * @param start
	 *            Start of the byte range.
	 * @param length
	 *            Length of the byte range.
	 * @throws IOException
	 *             If something fails at I/O level.
	 */
	private void copy(RandomAccessFile input, OutputStream output, long start,
			long length) throws IOException {
		byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
		int read;

		if (input.length() == length) {
			// Write full range.
			while ((read = input.read(buffer)) > 0) {
				output.write(buffer, 0, read);
			}
		} else {
			// Write partial range.
			input.seek(start);
			long toRead = length;

			while ((read = input.read(buffer)) > 0) {
				toRead -= read;
				if (toRead > 0) {
					output.write(buffer, 0, read);
				} else {
					output.write(buffer, 0, (int) toRead + read);
					break;
				}
			}
		}
	}

	/**
	 * Close the given resource.
	 * 
	 * @param resource
	 *            The resource to be closed.
	 */
	private static void close(Closeable resource) {
		if (resource != null) {
			try {
				resource.close();
			} catch (IOException ignore) {
				// Ignore IOException. If you want to handle this anyway, it
				// might be useful to know
				// that this will generally only be thrown when the client
				// aborted the request.
			}
		}
	}

	// Inner classes
	// ------------------------------------------------------------------------------

	/**
	 * This class represents a byte range.
	 */
	protected class Range {
		long start;
		long end;
		long length;
		long total;

		/**
		 * Construct a byte range.
		 * 
		 * @param start
		 *            Start of the byte range.
		 * @param end
		 *            End of the byte range.
		 * @param total
		 *            Total length of the byte source.
		 */
		public Range(long start, long end, long total) {
			this.start = start;
			this.end = end;
			this.length = end - start + 1;
			this.total = total;
		}
	}

	public int getChunk() {
		return chunk;
	}

	public void setChunk(int chunk) {
		this.chunk = chunk;
	}

	public int getChunks() {
		return chunks;
	}

	public void setChunks(int chunks) {
		this.chunks = chunks;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public boolean isGetMemberLogo() {
		return isLoadingSmallMemberLogo;
	}

	public void setGetMemberLogo(boolean isGetMemberLogo) {
		this.isLoadingSmallMemberLogo = isGetMemberLogo;
	}

}

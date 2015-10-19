package com.ant.myagile.service.impl;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.api.PagedList;
import org.springframework.social.facebook.api.Photo;
import org.springframework.social.facebook.api.Photo.Image;
import org.springframework.social.facebook.api.PhotoPost;
import org.springframework.social.facebook.api.Post;
import org.springframework.social.facebook.api.Post.PostType;
import org.springframework.social.facebook.api.impl.FacebookTemplate;
import org.springframework.stereotype.Service;

import com.ant.myagile.dao.FacebookNewsDao;
import com.ant.myagile.model.FacebookNews;
import com.ant.myagile.service.FacebookNewsService;
import com.ant.myagile.utils.MyAgileFileUtils;
import com.ant.myagile.viewmodel.FacebookNewsDTO;

@Service
public class FacebookNewsServiceImpl implements FacebookNewsService {

	private static final Logger LOGGER = Logger.getLogger(FacebookNewsServiceImpl.class);
    private static final int BEGIN_INDEX_GET_TOKEN = 13;
    private static final int MAX_FACEBOOK_POST = 10;
    
    private final String filePhotoFromFacebookNews = MyAgileFileUtils.getStorageLocation("myagile.download.image.facebooknews");
    
    private String accesstoken = "";
    
    @Autowired
    FacebookNewsDao facebookNewsDao;

    public String getAccessToken() {
        if ("".equals(this.accesstoken)) {
            String url = this.getPropertiesFacebook("facebook.access.token.url")
                    + "?redirect_uri=&client_id=" + this.getPropertiesFacebook("facebook.app.id")
                    + "&client_secret=" + this.getPropertiesFacebook("facebook.app.secrect")
                    + "&grant_type=client_credentials";
            String responseString = null;
            try {
                responseString = getResponse(url);
            } catch (MalformedURLException e) {
            } catch (IOException e) {
            }
            if (responseString != null) {
                return responseString.substring(BEGIN_INDEX_GET_TOKEN);
            } else {
            }
        }
        return this.accesstoken;
    }
    public String getResponse(String serviceUrl) throws IOException {
        String result = getResponse(serviceUrl,"POST","text/html");
        return result;
    }
    
    private String getResponse(String serviceUrl, String method,
            String contentType) throws IOException {
        HttpURLConnection connection = null;
        String response = null;
        try {
            URL url = new URL(serviceUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-type", contentType);
            connection.setRequestProperty("Accept", contentType);
            connection.setRequestProperty("Accept-Charset", "UTF-8");

            response = parseInputStreamToString(connection.getInputStream());
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return response;
    }
    
    private String parseInputStreamToString(InputStream inputStream)
            throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                inputStream, "UTF-8"));
        String output = "";
        StringBuffer stringBuffer = new StringBuffer();
        while ((output = reader.readLine()) != null) {
            stringBuffer.append(output);
        }
        return stringBuffer.toString();
    }
    
    private String getPropertiesFacebook(String key){
		//get info facebook app from config properties
		Properties properties = new Properties();
		String propFileName = "config.properties";
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			
		}
		return properties.getProperty(key);
    }
    
    public String savePhoto(InputStream in, File destination) {
        try {
            String path = filePhotoFromFacebookNews;
            File theDir = new File(path);
            if (!theDir.exists()) {
                boolean result = theDir.mkdirs();
                if (result) {
                    LOGGER.info("photos folder created");
                }
            }
            BufferedImage buff = ImageIO.read(in);
            ImageIO.write(buff, "JPEG", destination);
            return destination.toString();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return null;
        }
    }
	@Override
	public void saveFacebookNewsIntoDB() {
		String acesstoken = getAccessToken();
        Facebook facebook = new FacebookTemplate(acesstoken);
        PagedList<Post> list = facebook.feedOperations().getPosts(this.getPropertiesFacebook("facebook.pagename"));
        int numberPost = 0;
        while (numberPost < MAX_FACEBOOK_POST) {
            for (Post post : list) {
                if (post.getType() == PostType.PHOTO) {
                    PhotoPost photoPost = (PhotoPost) post;
                    FacebookNews news = new FacebookNews();
                    news.setMessage(photoPost.getMessage());
                    news.setTitle(photoPost.getName());
                    news.setLink(photoPost.getLink());
                    news.setCreateDate(new Date());
                    try {
                    	Photo image = facebook.mediaOperations().getPhoto(photoPost.getPhotoId());
                    	
                    	byte[] fImg = facebook.mediaOperations().getPhotoImage(photoPost.getPhotoId());
                    	InputStream in = new ByteArrayInputStream(fImg);
                    	//check exist photo 
                    	if(!facebookNewsDao.checkExistFacebookNewsFromPhotoId(photoPost.getPhotoId())){
                    		String pathImg = savePhoto(in,new File(filePhotoFromFacebookNews+"/"+photoPost.getPhotoId()+".jpg"));
    	                    if(!pathImg.isEmpty()){
    	                    	news.setImageName(photoPost.getPhotoId()+".jpg");
    	                    	news.setPhotoId(photoPost.getPhotoId());
    	                    }
    	                    //save facebookNews
    	                    facebookNewsDao.saveFacebookNews(news);
                    	}
					} catch (Exception e) {
						//skip photo error or private
					}
                    numberPost++;
                    if (numberPost >= MAX_FACEBOOK_POST) {
                        break;
                    }
                }
            }
            list = facebook.feedOperations().getPosts(this.getPropertiesFacebook("facebook.pagename"),list.getNextPage());
            if (list.size() < 1) {
                break;
            }
        }
	}

	@Override
	public List<FacebookNews> getFacebookNews() {
		return facebookNewsDao.getTenLatestFacebookNews();
	}
}

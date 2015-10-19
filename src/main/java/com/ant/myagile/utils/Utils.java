package com.ant.myagile.utils;

import java.io.IOException;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.hibernate.SessionFactory;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.security.authentication.encoding.MessageDigestPasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import com.ant.myagile.model.Member;
import com.ant.myagile.model.Topic;
import com.ant.myagile.model.UserStory;
import com.ant.myagile.service.ITopicService;
import com.ant.myagile.service.MemberService;

@Component
public class Utils implements Serializable {
	private static ObjectMapper mapper = new ObjectMapper();
	private static final long serialVersionUID = 1L;
	static Logger log = Logger.getLogger(Utils.class);
	public static final String URL_MYSTATUS = "http://mystatus.skype.com/";
	public static final String DOMAIN_SERVER = Utils.getConfigParam("myagile.domainServer");
	private static final Pattern emailFormat = Pattern.compile("^[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*"
			+ "@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?$");
	
	private static Logger logger = Logger.getLogger(Utils.class);
	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private MemberService memberService;

	public String toJSON(Object object) {
		if(object == null){
			return null;
		}
		try {
			return mapper.writeValueAsString(object);
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * 
	 * Get the user is logging in system
	 */
	public Member getLoggedInMember() {
		try {
			User loggedInMember = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			String username = loggedInMember.getUsername();
			Member member = memberService.getMemberByUsername(username);
			if (member != null) {
				return memberService.getMemberByUsername(username);
			} else {
				return memberService.findMemberByLDapUsername(username);
			}
		} catch (ClassCastException e) {
			return null;
		}
	}

	public String encodePassword(String pass) {
		ShaPasswordEncoder encoder = new ShaPasswordEncoder();
		return encoder.encodePassword(pass, null);
	}

	public void evict(Object object) {
		sessionFactory.getCurrentSession().evict(object);
	}

	/**
	 * Increase a date by number of days
	 * 
	 * @param date
	 *            : date need to be converted
	 * @param days
	 *            : number of increment days
	 * @return
	 */
	public static Date addDays(Date date, int days) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}

	/**
	 * Convert a Date type to simple format "MM/dd/yyyy"
	 * 
	 * @param date
	 *            : date need to be converted
	 * @return
	 */
	public static String toShortDate(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		dateFormat.setTimeZone(TimeZone.getDefault());
		return dateFormat.format(date);
	}

	public static String toLongDate(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getDefault());
		return dateFormat.format(date);
	}

	public static Date stringToDate(String strDate) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
		dateFormat.setTimeZone(TimeZone.getDefault());
		Date date = new Date();
		try {
			date = dateFormat.parse(strDate);
		} catch (Exception e) {

		}
		return date;
	}

	public static Date stringToDate(String strDate, SimpleDateFormat dateFormat) {
		dateFormat.setTimeZone(TimeZone.getDefault());
		Date date = new Date();
		try {
			date = dateFormat.parse(strDate);
		} catch (Exception e) {
		}
		return date;
	}

	public static String dateToString(Date date) {
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		calendar.setTime(date);
		SimpleDateFormat formatdate = new SimpleDateFormat("yyyy-MM-dd");
		return formatdate.format(calendar.getTime()) + " 00:00:00.0";
	}

	/**
	 * Read "MyagileMessage.properties" file and get message by the key input
	 */
	public static String getMessage(String key, Object params[]) {
		Resource resource = new ClassPathResource("/MyagileMessage.properties");
		try {
			Properties props = PropertiesLoaderUtils.loadProperties(resource);
			String message = props.getProperty(key);
			if (message != null) {
				String text = message;
				if (params != null) {
					text = "";
					MessageFormat mf = new MessageFormat(message);
					text = mf.format(params, new StringBuffer(), null).toString();
				}
				return text;
			} else {
				return null;
			}
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Convert String to float (example: D8T3 -> 11)
	 */
	public static float convertPoint(String stringPoint) {
		String dPoint = stringPoint.substring(1, stringPoint.indexOf('T'));
		String tPoint = stringPoint.substring(stringPoint.indexOf('T') + 1, stringPoint.length());
		return Float.parseFloat(dPoint) + Float.parseFloat(tPoint);
	}

	/**
	 * Get Point Remain Develop (example: D8T3 -> 8)
	 */
	public static String getPointDev(String stringPoint) {
		String dPoint = stringPoint.substring(1, stringPoint.indexOf('T'));
		return dPoint;
	}

	/**
	 * Get Point Remain Test (example: D8T3 -> 3)
	 */
	public static String getPointTest(String stringPoint) {
		String tPoint = stringPoint.substring(stringPoint.indexOf('T') + 1, stringPoint.length());
		return tPoint;
	}

	public static int getStatusNumberSkype(String account) throws Exception {
		Document doc = Jsoup.connect(URL_MYSTATUS + account + ".num").timeout(2000).get();
		int numberStatus = Integer.parseInt(Jsoup.parse(doc.toString()).getElementsByTag("body").text());
		return numberStatus;
	}

	public static String checkName(Member member) {
		String fullName = "";
		if ((member.getLastName() == null) && (member.getFirstName() == null)) {
			fullName = member.getUsername();
		}
		if ((member.getLastName() == null) && (member.getFirstName() != null)) {
			fullName = member.getFirstName();
		}
		if ((member.getLastName() != null) && (member.getFirstName() == null)) {
			fullName = member.getLastName();
		}
		if ((member.getLastName() != null) && (member.getFirstName() != null)) {
			fullName = member.getLastName() + " " + member.getFirstName();
		}
		return fullName;
	}

	/**
	 * Read "config.properties" file
	 * 
	 * @return value based on the key input
	 */
	public static String getConfigParam(String key) {
		Resource resource = new ClassPathResource("/config.properties");
		try {
			Properties props = PropertiesLoaderUtils.loadProperties(resource);
			String value = props.getProperty(key);
			if (value != null) {
				return value;
			}
			return null;
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Generate chat secret key from key and salt
	 * 
	 * @return secret key
	 */
	public static String generateSecretString(String key, String salt) {
		String message = key + salt;
		MessageDigestPasswordEncoder encoder = new MessageDigestPasswordEncoder("SHA-1");
		String hash = encoder.encodePassword(message, salt);
		return hash;
	}

	/**
	 * @return True if email is valid, otherwise return false
	 */
	public static boolean checkEmailFormat(String email) {
		return emailFormat.matcher(email).matches();
	}

	/**
	 * Standardize a key without redundant spaces
	 * 
	 * @param value
	 *            source key
	 * @return destination key
	 */
	public static String standardizeString(String value) {
		String str = value;
		str = str.trim();
		str = str.replaceAll("\\s+", " ");
		return str;
	}

	/**
	 * check value is belong to user story priority values
	 * 
	 * @param value
	 * @return true if the value is belong to user story priority values
	 */
	public boolean isBelongToUserStoryPriorityValues(String value) {
		boolean flag = false;
		if ((value != null) && !value.equals("")) {
			UserStory.PriorityType[] prioList = UserStory.PriorityType.values();
			String[] strList = new String[prioList.length];
			for (int i = 0; i < prioList.length; i++) {
				strList[i] = prioList[i].toString();
				if (value.toUpperCase().equals(strList[i])) {
					flag = true;
					break;
				}
			}
		}
		return flag;
	}

	/**
	 * check a number with key format is a value of range
	 * 
	 * @param value
	 * @param minValue
	 * @param maxValue
	 * @return true if condition full-fill
	 */
	public boolean isValueOfRange(String value, int minValue, int maxValue) {
		boolean flag = false;
		if ((value != null) && !value.equals("") && value.matches("(\\d*)")) {
			if ((Integer.parseInt(value) >= minValue) && (Integer.parseInt(value) <= maxValue)) {
				flag = true;
			}
		}
		return flag;
	}

	public boolean isExistTooLongWord(String value) {
		int longestLen = "pneumonoultramicroscopicsilicovolcanoconiosis".length();
		if (value == null || value.equals("")) {
			return false;
		}
		String[] words = value.split(" ");
		for (String w : words) {
			if (w.length() > longestLen) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param point
	 *            to format
	 * @return
	 */
	public static String formatFloatNumber(float point, String format) {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		dfs.setGroupingSeparator(',');
		DecimalFormat df = new DecimalFormat(format, dfs);
		return df.format(point);
	}

	/**
	 * 
	 * @param key
	 * @param timestamp
	 * @return one time password
	 */
	public static String generateTokenKey(String key) {
		ShaPasswordEncoder encoder = new ShaPasswordEncoder(1);
		return encoder.encodePassword(Utils.getConfigParam("myagile.key2") + key + Utils.getTimeStamp(), null);
	}

	/**
	 * Get time follow format yyyyMMddHHmm. Called in generateTokenKey
	 * functionality
	 * 
	 * @return
	 */
	public static String getTimeStamp() {
		Calendar cal = Calendar.getInstance();
		cal.getTime();
		SimpleDateFormat simpleDate = new SimpleDateFormat("yyyyMMddHHmm");
		return simpleDate.format(cal.getTime());
	}

	public static String createTopicLinkFromContent(String topicContent, long wikiId, ITopicService topicService) {
		String OPEN_DOUBLE_SQUARE_BRACKET = "[[",
			   CLOSE_DOUBLE_SQUARE_BRACKET = "]]";
		String content = topicContent;
		Map<String, String> topics = new HashMap<String, String>();
		Pattern pattern = Pattern.compile("\\[\\[(.*?)\\]\\]");
		Matcher matcher = pattern.matcher(content);
		while (matcher.find()) {
			topics.put(matcher.group(), "");
		}
		try {
			for (Entry<String, String> entry : topics.entrySet()) {

				String name = entry.getKey()
						.replace(OPEN_DOUBLE_SQUARE_BRACKET, "")
						.replace(CLOSE_DOUBLE_SQUARE_BRACKET, "").trim();
				String a = "<a style='color:{color}' href='{link}' data-id-topic ='{topicid}' class='linkWiki'>{title}</a>";
				String link = new String("/topic/details");
				Topic topic = topicService.getTopicByTitle(name, wikiId);
				if (topic == null) {
					a = a.replace("{color}", "red").replace("{topicid}", "0");
				} else {
					a = a.replace("{color}", "blue").replace("{topicid}",
							String.valueOf(topic.getTopicId()));
				}
				a = a.replace("{link}", link).replace("{title}", name);
				entry.setValue(a);

				content = content.replace(entry.getKey(), entry.getValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return content;
	}
	
	public static Date addDateToParticularDate(Date particularDate, int numberOfDateAdd){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(particularDate);
		calendar.add(Calendar.DATE, numberOfDateAdd);
		return calendar.getTime();
	}
	
	public static String converDateToString(Date date){
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = "";
		if(date != null){
			dateString = dateFormat.format(date);
		}
		return dateString;
	}
	
	public static Date zeroTime(Date date){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}
	
}

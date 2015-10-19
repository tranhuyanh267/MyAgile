package com.ant.myagile.utils.ActiveDirectory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

import com.ant.myagile.utils.Utils;

@Component
public class UtilsLdap {
	static final String LDAP_SERVICE_URL = Utils.getConfigParam("myagile.urlService");

	public static boolean authenticate(String username, String password) {
		try {
			String request = LDAP_SERVICE_URL + "?username=" + Encrypt.encryptString(username) + "&password=" + Encrypt.encryptString(password);
			Document doc = Jsoup.connect(request).timeout(2000).get();
			String result = Jsoup.parse(doc.toString()).getElementsByTag("body").text();
			return Boolean.parseBoolean(result);
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isAccountExistedLDAP(String username) {
		try {
			Document doc = Jsoup.connect(LDAP_SERVICE_URL + "?username=" + Encrypt.encryptString(username)).timeout(2000).get();
			String result = Jsoup.parse(doc.toString()).getElementsByTag("body").text();
			return Boolean.parseBoolean(result);
		} catch (Exception e) {
			return false;
		}
	}
}

package com.ant.myagile.webservices;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;

import com.ant.myagile.model.MyTeamLinked;
import com.ant.myagile.service.MyTeamLinkedService;
import com.ant.myagile.utils.SpringContext;

@Path("/pushEmployeeAavn")
public class EmployeeResourceService {
	private static final Logger LOGGER = Logger.getLogger(EmployeeResourceService.class);

	/**
	 * Read a HashMap consists email and id, then compare data between HashMap
	 * and database. If email not exist, add new row. Otherwise, ignore.
	 * 
	 * @param map
	 */
	@POST
	@Path("/getAll")
	public void updateMyTeamLinkedTable(@FormParam("contactList") String contactListEncrypt) {
		try {
			EncryptAavn encryptAavn = new EncryptAavn();
			String contactListAsJson = encryptAavn.decrypt(contactListEncrypt);
			JSONArray contactList = new JSONArray(contactListAsJson);
			MyTeamLinkedService myTeamLinkedService = SpringContext.getApplicationContext().getBean(MyTeamLinkedService.class);
			List<MyTeamLinked> list = myTeamLinkedService.findAll();
			if (list.size() == 0) {
				for (int i = 0; i <= contactList.length(); i++) {
					JSONObject object = (JSONObject) contactList.get(i);
					MyTeamLinked temp = new MyTeamLinked();
					temp.setEmail(object.get("email").toString());
					temp.setMyTeamId(object.get("id").toString());
					myTeamLinkedService.save(temp);
				}
			} else {
				for (int i = 0; i <= contactList.length(); i++) {
					boolean existed = false;
					JSONObject object = (JSONObject) contactList.get(i);
					for (int j = 0; j < list.size(); j++) {
						if (object.get("email").toString().equals(list.get(j).getEmail())) {
							existed = true;
							break;
						}
					}
					if (!existed) {
						MyTeamLinked temp = new MyTeamLinked();
						temp.setEmail(object.get("email").toString());
						temp.setMyTeamId(object.get("id").toString());
						myTeamLinkedService.save(temp);
					}
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		}
	}
}

class EncryptAavn {
	private static final String PRIVATE_KEY = "#aavn%^hr*ws!@#9";
	private final SecretKeySpec keyspec;
	private Cipher cipher;
	private static Logger logger = Logger.getLogger(EncryptAavn.class);

	public EncryptAavn() {
		keyspec = new SecretKeySpec(PRIVATE_KEY.getBytes(), "AES");
		try {
			cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		} catch (NoSuchAlgorithmException e) {
			logger.warn(e.getMessage());
		} catch (NoSuchPaddingException e) {
			logger.warn(e.getMessage());
		}
	}

	public String encrypt(String text) throws Exception {
		if (text == null || text.length() == 0) {
			throw new Exception("Empty string");
		}
		String encrypted = null;
		try {
			cipher.init(Cipher.ENCRYPT_MODE, keyspec);
			byte[] h1 = cipher.doFinal(text.getBytes());
			encrypted = Base64.encodeBase64String(h1).toString();
		} catch (Exception e) {
			throw new Exception("[encrypt] " + e.getMessage());
		}
		return encrypted;
	}

	public String decrypt(String code) throws Exception {
		if (code == null || code.length() == 0) {
			throw new Exception("Empty string");
		}
		byte[] decrypted;
		try {
			byte[] text = Base64.decodeBase64(code);
			cipher.init(Cipher.DECRYPT_MODE, keyspec);
			decrypted = cipher.doFinal(text);
		} catch (Exception e) {
			throw new Exception("[decrypt] " + e.getMessage());
		}
		return new String(decrypted);
	}
}
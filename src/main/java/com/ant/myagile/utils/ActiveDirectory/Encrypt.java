package com.ant.myagile.utils.ActiveDirectory;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

import com.ant.myagile.utils.Utils;

public class Encrypt {
	private static final String KEY1 = Utils.getConfigParam("myagile.key1");
	private static final String KEY2 = Utils.getConfigParam("myagile.key2");
	private final IvParameterSpec ivspec;
	private final SecretKeySpec keyspec;
	private Cipher cipher;
	private static Logger logger = Logger.getLogger(Encrypt.class);

	public Encrypt() {
		ivspec = new IvParameterSpec(KEY1.getBytes());
		keyspec = new SecretKeySpec(KEY2.getBytes(), "AES");
		try {
			cipher = Cipher.getInstance("AES/CBC/NoPadding");
		} catch (NoSuchAlgorithmException e) {
			logger.warn(e);
		} catch (NoSuchPaddingException e) {
			logger.warn(e);
		}
	}

	public byte[] encrypt(String text) throws Exception {
		if (text == null || text.length() == 0) {
			throw new Exception("Empty string");
		}
		byte[] encrypted = null;
		try {
			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			encrypted = cipher.doFinal(padString(text).getBytes());
		} catch (Exception e) {
			throw new Exception("[encrypt] " + e.getMessage());
		}

		return encrypted;
	}

	public byte[] decrypt(String code) throws Exception {
		if (code == null || code.length() == 0) {
			throw new Exception("Empty string");
		}
		byte[] decrypted = null;
		try {
			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

			decrypted = cipher.doFinal(hexToBytes(code));
		} catch (Exception e) {
			throw new Exception("[decrypt] " + e.getMessage());
		}
		return decrypted;
	}

	public static String bytesToHex(byte[] data) {
		if (data == null) {
			return null;
		}
		int len = data.length;
		String str = "";
		for (int i = 0; i < len; i++) {
			if ((data[i] & 0xFF) < 16) {
				str = str + "0" + java.lang.Integer.toHexString(data[i] & 0xFF);
			} else {
				str = str + java.lang.Integer.toHexString(data[i] & 0xFF);
			}
		}
		return str;
	}

	public static byte[] hexToBytes(String str) {
		if (str == null) {
			return null;
		} else if (str.length() < 2) {
			return null;
		} else {
			int len = str.length() / 2;
			byte[] buffer = new byte[len];
			for (int i = 0; i < len; i++) {
				buffer[i] = (byte) Integer.parseInt(str.substring(i * 2, i * 2 + 2), 16);
			}
			return buffer;
		}
	}

	private static String padString(String source) {
		char paddingChar = ' ';
		int size = 16;
		int x = source.length() % size;
		int padLength = size - x;
		for (int i = 0; i < padLength; i++) {
			source += paddingChar;
		}
		return source;
	}

	public static String encryptString(String value) throws Exception {
		Encrypt mcrypt = new Encrypt();
		String encrypted = Encrypt.bytesToHex(mcrypt.encrypt(value));
		return encrypted;
	}

	public static String decryptString(String value) throws Exception {
		Encrypt mcrypt = new Encrypt();
		String descripted = new String(mcrypt.decrypt(value));
		return descripted;
	}
}

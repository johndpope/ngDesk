package com.ngdesk.integration.security.dao;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;

public class SecurityService {
	
	public void generateKeys() throws NoSuchAlgorithmException  {
		KeyPairGenerator generator = KeyPairGenerator.getInstance("AES");
	    generator.initialize(2048, new SecureRandom());
	    KeyPair pair = generator.generateKeyPair();

	}

	public String encrypt(String plainText, PrivateKey privateKey) throws Exception {
		Cipher encryptCipher = Cipher.getInstance("AES");
		encryptCipher.init(Cipher.ENCRYPT_MODE, privateKey);

		byte[] cipherText = encryptCipher.doFinal(plainText.getBytes("UTF-8"));

		return Base64.getEncoder().encodeToString(cipherText);
	}

	// TODO: IMPLEMENT SECURITY
	public String decrypt(String cipherText, PublicKey publicKey) throws Exception {
		byte[] bytes = Base64.getDecoder().decode(cipherText);

		Cipher decriptCipher = Cipher.getInstance("AES");
		decriptCipher.init(Cipher.DECRYPT_MODE, publicKey);
		return new String(decriptCipher.doFinal(bytes), "UTF-8");
	}

}

package ch.util.crc;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
public class ShaUtil {
	// compare ch-test-easyweb
	// https://stackoverflow.com/questions/7225313/how-does-git-compute-file-hashes
	// https://stackoverflow.com/questions/27527028/java-sha1-output-not-the-same-as-linuxs-sha1sum-command
	public static void main(String[] args) {
		ShaUtil shaUtil = new ShaUtil();
		try {
			System.out.println(shaUtil.hashObject(new FileInputStream(new File("C:/Users/jimmy.shu/git/ch-test-redis/pom.xml"))));
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		System.out.println(shaUtil.shasum("blob 14\0Hello, World!\n"));
		System.out.println(shaUtil.shasum("test"));
		System.out.println(shaUtil.shasum("test\n"));
	}
	public String hashObject(InputStream is){
		MessageDigest sha = null;
		BufferedInputStream bis = null;
		try {
			sha = MessageDigest.getInstance("SHA1");   
			bis = new BufferedInputStream(is);
			byte[] temp = new byte[8192];
			int len;
			sha.update("blob ".getBytes());
			sha.update(Integer.toString(bis.available()).getBytes());
			sha.update("\0".getBytes());
			while ((len = bis.read(temp)) != -1)
				sha.update(temp, 0, len);
			return byteArrayToHexString(sha.digest());
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (bis != null)
				try {
					bis.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		}
	}
	public java.io.InputStream hashObject(InputStream is, String objectId){
		MessageDigest sha = null;
		String digest;
		java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
		try {
			sha = MessageDigest.getInstance("SHA1");   
			byte[] temp = new byte[8192];
			int len;
			sha.update("blob ".getBytes());
			sha.update(Integer.toString(is.available()).getBytes());
			sha.update("\0".getBytes());
			while ((len = is.read(temp)) != -1){
				sha.update(temp, 0, len);
				baos.write(temp, 0, len);
			}
			digest = byteArrayToHexString(sha.digest());
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (is != null)
				try {
					is.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		}
		if (objectId.equals(digest))
			return new java.io.ByteArrayInputStream(baos.toByteArray());
		throw new RuntimeException("hashObject");
	}
	public String shasum(InputStream is){
		MessageDigest sha = null;
		BufferedInputStream bis = null;
		try {
			sha = MessageDigest.getInstance("SHA1");   
			bis = new BufferedInputStream(is);
			byte[] temp = new byte[8192];
			int len;
			while ((len = bis.read(temp)) != -1)
				sha.update(temp, 0, len);
			return byteArrayToHexString(sha.digest());
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (bis != null)
				try {
					bis.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
		}
	}
	public String shasum(byte[] bytes){
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA1");
			sha.update(bytes);
			return byteArrayToHexString(sha.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}  
	}
	public String shasum(String text){
		try {
			MessageDigest sha = MessageDigest.getInstance("SHA1");
			sha.update(text.getBytes());
			return byteArrayToHexString(sha.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}  
	}
	private String byteArrayToHexString(byte[] b){
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < b.length; i++)
			builder.append(Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1));
		return builder.toString();
	} 
}
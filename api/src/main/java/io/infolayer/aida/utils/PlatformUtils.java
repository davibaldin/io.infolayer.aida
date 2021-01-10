/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.infolayer.aida.utils;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Random;

public class PlatformUtils {
	
	public static final String PLATFORM_MACOSX 		= "macosx";
	public static final String PLATFORM_LINUX 		= "linux";
	public static final String PLATFORM_WINDOWS 	= "windows";
	public static final String PLATFORM_AIX 		= "aix";
	public static final String PLATFORM_SOLARIS 	= "solaris";
	public static final String PLATFORM_UNKNOW 		= "unknow";
	
	public static String getCurrentPlatform() {
		
		//By System os.name
		String osname = System.getProperty("os.name");
		
		if(osname != null) {
			if (osname.toLowerCase().equals("mac os x")) {
				return PLATFORM_MACOSX;
			}else if (osname.toLowerCase().equals("linux")) {
				return PLATFORM_LINUX;
			}else if (osname.toLowerCase().startsWith("windows")) {
				return PLATFORM_WINDOWS;
			}
		}
		
		return PLATFORM_UNKNOW;
	}
	
	public static boolean isUnknownPlatform() {
		return PlatformUtils.getCurrentPlatform().equals(PLATFORM_UNKNOW);
	}
	
	public static boolean isPlatformCompatible(String platformList) {
		
		if (platformList != null) {
			
			//Special case: *
			if (platformList.equals("*")) {
				return true;
			}
			
			//List of platforms: Contains ,
			if (platformList.contains(",")) {
				String[] list = platformList.split(",");
				for (String item : list) {
					if (isPlatformCompatible(item.trim())) {
						return true;
					}
				}
			}
			
			//Check now
			if (getCurrentPlatform().equals(platformList)) {
				return true;
			}
			
		}
		
		return false;
	}
	
	public static boolean isWindows() {
		if (PLATFORM_WINDOWS.contentEquals(getCurrentPlatform())) {
			return true;
		}
		return false;
	}
	
	public static String sanitizePath(String path) {
		
		if (path == null) {
			return null;
		}
		
		if (isWindows()) {
			return path.replace("/", "\\");
		}
		
		return path;
	}
	
	public static String getHash(String string) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
			byte[] encodedhash = digest.digest(string.getBytes(StandardCharsets.UTF_8));
			return bytesToHex(encodedhash);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
		
	}
	
	private static String bytesToHex(byte[] hash) {
	    StringBuffer hexString = new StringBuffer();
	    for (int i = 0; i < hash.length; i++) {
	    String hex = Integer.toHexString(0xff & hash[i]);
	    if(hex.length() == 1) hexString.append('0');
	        hexString.append(hex);
	    }
	    return hexString.toString();
	}
	
	public static String formatTimestamp(long timestamp, String format) {
		
		if (format == null) {
			format = "dd/MM/yyyy HH:mm:ss";
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(new Date(timestamp));
	}
	
	// Credits:
	// https://www.geeksforgeeks.org/generate-random-string-of-given-size-in-java/
	public static String getAlphaNumericString(int n) {

		// length is bounded by 256 Character
		byte[] array = new byte[256];
		new Random().nextBytes(array);

		String randomString = new String(array, Charset.forName("UTF-8"));

		// Create a StringBuffer to store the result
		StringBuffer r = new StringBuffer();

		// Append first 20 alphanumeric characters
		// from the generated random String into the result
		for (int k = 0; k < randomString.length(); k++) {

			char ch = randomString.charAt(k);

			if (((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9')) && (n > 0)) {

				r.append(ch);
				n--;
			}
		}

		// return the resultant string
		return r.toString();
	}
	
}
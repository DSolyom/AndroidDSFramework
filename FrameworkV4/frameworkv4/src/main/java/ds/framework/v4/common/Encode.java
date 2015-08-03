/**
 *	Created by Dániel Sólyom
 *	Copyright 2010 All rights reserved.
 *
 *	last update - 24/11/2010
 */

package ds.framework.v4.common;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.graphics.Bitmap;

public class Encode {
	public static String md5(String s) {
		return md5(s.getBytes());
	}
	
	public static String md5(byte[] bytes) {
	    try {  
	        // Create MD5 Hash  
	        MessageDigest digest = java.security.MessageDigest.getInstance("MD5");  
	        digest.update(bytes);  
	        byte messageDigest[] = digest.digest();  
	          
	        // Create Hex String  
	        StringBuffer hexString = new StringBuffer();  
	        for (int i=0; i < messageDigest.length; i++) {
	            String h = Integer.toHexString(0xFF & messageDigest[i]);
	            switch(h.length()) {
		            case 0:
		            	h = "00";
		            	break;
		            
		            case 1:
		            	h = "0" + h;
		            	break;
		            
		            default:
		            	break;
		        }
	            hexString.append(h);
	        }
	        return hexString.toString();  
	          
	    } catch (NoSuchAlgorithmException e) {  
	        e.printStackTrace();  
	    }  
	    return "";  
	}
	
	public static final byte[] bitmapToBase64(Bitmap bmp) {
		return bitmapToBase64(bmp, Bitmap.CompressFormat.JPEG, 100);
	}
	
	public static final byte[] bitmapToBase64(Bitmap bmp, Bitmap.CompressFormat format, int quality) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();  
		bmp.compress(format, quality, baos); 
		byte[] b = baos.toByteArray(); 
		return Base64.encode(b, Base64.DEFAULT);
	}

	public static String urlEncodeUrl(String url) {
		final String[] urlParts = url.split("/");
    	url = urlParts[0] + "/" + urlParts[1] + "/" + urlParts[2];
    	for(int i = 3; i < urlParts.length; ++i) {
    		final String[] urlPP = urlParts[i].split(" ");
    		String part = "";
    		for(int j = 0; j < urlPP.length; ++j) {
	    		try {
					part += "%20" + URLEncoder.encode(urlPP[j], "UTF-8");
				} catch (UnsupportedEncodingException e) {
					part += "%20" + urlPP[j];
				}
    		}
    		if (part.length() > 3) {
    			url += "/" + part.substring(3);
    		} else {
    			url += "/";
    		}
    	};
    	return url;
	}
	
	/**
	 * remove accents - SLOW
	 * 
	 * @param str
	 * @return
	 */
	public static String accentFreeString(String str) {
		final String[] accented = new String[] { "o",
                 "à", "á", "â", "ã", "ä", "å", "æ", "ç", "è", "é", "ê", "ë", "ì", "í", "î", "ï",
                 "ð", "ñ", "ò", "ó", "ô", "õ", "ö", "ø", "ù", "ú", "û", "ü", "ý", "а", "б", "в", "г", "д", "е", "ё", "ж",
                 "з", "и", "й", "к", "л", "м", "н", "о", "п", "р", "с", "т", "у", "ф", "х", "ц", "ч", "ш", "щ", "ъ", "ы",
                 "ь", "э", "ю", "я", "ő", "ű", 
                 "À", "Á", "Â", "Ã", "Ä", "Å", "Æ", "Ç", "È", "É", "Ê", "Ë", "Ì", "Í", "Î", "Ï",
                 "Ð", "Ñ", "Ò", "Ó", "Ô", "Õ", "Ö", "Ø", "Ù", "Ú", "Û", "Ü", "Ý", "А", "Б", "В", "Г", "Д", "Е", "Ё", "Ж",
                 "З", "И", "Й", "К", "Л", "М", "Н", "О", "П", "Р", "С", "Т", "У", "Ф", "Х", "Ц", "Ч", "Ш", "Щ", "Ъ", "Ъ",
                 "Ь", "Э", "Ю", "Я", "Ő", "Ű"
		};
		final String[] target = new String[] { "o",
                 "a", "a", "a", "a", "a", "a", "ae", "c", "e", "e", "e", "e", "i", "i", "i", "i",
                 "o", "n", "o", "o", "o", "o", "o", "0", "u", "u", "u", "u", "y", "a", "b", "b", "g", "d", "e", "e", "zs",
                 "e", "n", "N", "k", "p", "m", "h", "o", "N", "p", "c", "t", "y", "fi", "x", "u", "y", "w", "w", "l", "bl",
                 "b", "e", "io", "r", "o", "u",
                 "A", "A", "A", "A", "A", "A", "AE", "c", "E", "E", "E", "E", "I", "I", "I", "I",
                 "O", "N", "O", "O", "O", "O", "O", "0", "U", "U", "U", "U", "Y", "A", "B", "B", "g", "d", "E", "E", "ZS",
                 "E", "N", "N", "K", "P", "M", "H", "O", "N", "P", "C", "T", "Y", "FI", "X", "U", "Y", "W", "W", "L", "BL",
                 "E", "IO", "R", "O", "U", "U"
		};
		
		for(int i = 0; i < accented.length; ++i) {
			str = str.replace(accented[i], target[i]);
		}
		return str;
	}
}

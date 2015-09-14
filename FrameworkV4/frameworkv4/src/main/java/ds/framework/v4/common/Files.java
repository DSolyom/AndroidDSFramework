/*
	Copyright 2011 Dániel Sólyom

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package ds.framework.v4.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class Files {
	
	public static byte[] getFileAsByteArray(URL url) throws IOException, OutOfMemoryError {
		InputStream iStream = null;
		try {
			iStream = getHttpStream(url);
			return getFileAsByteArray(iStream);
		} catch(IOException e) {
			throw(e);
		} catch(OutOfMemoryError e) {
			throw(e);
		} finally {
			if (iStream != null) {
				iStream.close();
			}
		}
	}
	
	/**
	 * 
	 * @param iStream
	 * @return
	 * @throws IOException
	 * @throws OutOfMemoryError
	 */
	public static byte[] getFileAsByteArray(InputStream iStream) throws IOException, OutOfMemoryError {
		try {
      		iStream.reset();
      	} catch(Throwable e) {
      		;
      	}
		
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = iStream.read(buffer)) != -1) {
        	byteBuffer.write(buffer, 0, len);
        }
		
		return byteBuffer.toByteArray();
	}
	
	/**
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	private static InputStream getHttpStream(URL url) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		
		conn.setDoInput(true);
		conn.setUseCaches(true);
		conn.connect();
		
		return conn.getInputStream();
	}
	
    public static byte[] toByteArray(String file) throws IOException {
        return toByteArray(new File(file));
    }

    public static byte[] toByteArray(File file) throws IOException {
        // Open file
        RandomAccessFile f = new RandomAccessFile(file, "r");

        try {
            // Get and check length
            long longlength = f.length();
            int length = (int) longlength;
            if (length != longlength) throw new IOException("File size >= 2 GB");

            // Read file and return data
            byte[] data = new byte[length];
            f.readFully(data);
            return data;
        }
        finally {
            f.close();
        }
    }
    
    public static String toBase64(String file) throws IOException {
    	return toBase64(new File(file));
    }
    
    public static String toBase64(File file) throws IOException {
    	return new String(Base64.encode(toByteArray(file), 0)).trim();
    }
    
    /**
     * 
     * @param stream
     * @param outFile
     * @param closeAtEnd
     * @throws Throwable
     */
	public static void putInputSteramIntoFile(InputStream stream, File outFile, boolean closeAtEnd) throws Throwable {
		try {
			final OutputStream output = new FileOutputStream(outFile);
		    try {
		        try {
		            final byte[] buffer = new byte[1024];
		            int read;
	
		            while ((read = stream.read(buffer)) != -1)
		                output.write(buffer, 0, read);
	
		            output.flush();
		        } finally {
		            output.close();
		        }
		    } catch (Exception e) {
		        Debug.logException(e);
		        throw(e);
		    }
		} finally {
			if (closeAtEnd) {
				stream.close();
			}
		}
	}
}

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
package ds.framework.v4.io;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import ds.framework.v4.common.Debug;

public class HttpURLRequest {

	public static final int GET = 0;
	public static final int POST = 1;
    public static final int PUT = 2;
    public static final int DELETE = 3;

	public static final int PARAM_MODE_STRING = 0;
	public static final int PARAM_MODE_HTML = 1;
	public static final int PARAM_MODE_HTML_ENTITY = 2;

    private static CookieManager sCookieManager = new CookieManager();

	private String mUrl;
	private String mPostBody;
	final private HashMap<String, String> mHeaders = new HashMap<>();

	final private HashMap<String, Object> mGetParams = new HashMap<String, Object>();
	final private HashMap<String, Object> mPostParams = new HashMap<String, Object>();

	private int mMode = GET;
	private int mPostParamMode = PARAM_MODE_STRING;
	private HttpURLConnection mHttpURLConnection;
	private Integer mStatusCode;
	private int mConnectionTimeout = 30000;
	private int mReadTimeout = 60000;
    private boolean mUseCaches = false;

    public HttpURLRequest() {
	}

	/**
	 *
	 * @param url
	 */
	public HttpURLRequest(String url) {
		this(url, GET);
	}

	/**
	 *
	 * @param url
	 * @param mode
	 */
	public HttpURLRequest(String url, int mode) {
        mUrl = url;
		mMode = mode;
	}
	
	/**
	 * 
	 * @param url
	 */
	public void setUrl(String url) {
        mUrl = url;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getUrl() {
		return mUrl;
	}
	
	/**
	 * 
	 * @param mode
	 */
	public void setMode(int mode) {
		mMode = mode;
	}
	
	/**
	 * 
	 * @param postPostParamMode
	 */
	public void setPostParamMode(int postPostParamMode) {
		mPostParamMode = postPostParamMode;
	}
	
	/**
	 * 
	 * @param connection
	 * @param so
	 */
	public void setTimeout(int connection, int so) {
		mConnectionTimeout = connection;
		mReadTimeout = so;
	}
	
	/**
	 * clear all data except cookies
	 */
	public void reset() {
        if (mHttpURLConnection != null) {
            mHttpURLConnection.disconnect();
        }
		mHttpURLConnection = null;
		mStatusCode = null;
	}
	
	/**
	 * clear everything
	 */
	public void clear() {
		reset();
        mPostBody = null;
		if (sCookieManager != null) {
			sCookieManager.getCookieStore().removeAll();
		}
	}
	
	/**
	 * 
	 * @param dataString
	 */
	public void setPostDataString(String dataString) {
		mPostBody = dataString;
	}
	
	/**
	 * 
	 * @param id
	 * @param value
	 * @param type
	 */
	public void addParam(String id, Object value, int type) {
		if (type == GET) {
			mGetParams.put(id, value);
		} else {
			mPostParams.put(id, value);
		}
	}
	
	/**
	 * 
	 * @param id
	 * @param type
	 */
	public boolean hasParam(String id, int type) {
		if (type == GET) {
			return mGetParams.containsKey(id);
		} else {
			return mPostParams.containsKey(id);
		}
	}
	
	/**
	 * returns parameters for type (GET, POST, etc)<br/>
	 * !note: not working with deprecated addData
	 * 
	 * @param type
	 * @return
	 */
	public HashMap<String, Object> getParams(int type) {
		if (type == GET) {
			return mGetParams;
		} else {
			return mPostParams;
		}
	}
	
	/**
	 * 
	 * @param id
	 * @param type
	 * @return
	 */
	public Object getParam(String id, int type) {
		if (type == GET) {
			return mGetParams.get(id);
		} else {
			return mPostParams.get(id);
		}
	}
	
	/**
	 * 
	 * @param id
	 * @param type
	 * @return
	 */
	public Object removeParam(String id, int type) {
		if (type == GET) {
			return mGetParams.remove(id);
		} else {
			return mPostParams.remove(id);
		}
	}
	
	/**
	 * 
	 * @param id
	 * @param value
	 */
	public void addHeader(String id, Object value) {
		mHeaders.put(id, String.valueOf(value));
	}
	
	/**
	 * 
	 * @return
	 */
	public HashMap<String, String> getHeaders() {
		return mHeaders;
	}

    /**
     *
     * @throws IOException
     */
    protected void openRequest() throws IOException {
        final String modeString = getModeString();
        Debug.logD("HttpURLRequest", "opening request " + mUrl + " (" + modeString + ")");

        try {
            final String finalUrl = createFullUrl();
            mHttpURLConnection = (HttpURLConnection) new URL(finalUrl).openConnection();
            mHttpURLConnection.setRequestMethod(modeString);
            mHttpURLConnection.setConnectTimeout(mConnectionTimeout);
            mHttpURLConnection.setReadTimeout(mReadTimeout);
            mHttpURLConnection.setUseCaches(mUseCaches);

            // user agent
            mHttpURLConnection.setRequestProperty("User-Agent", System.getProperty("http.agent"));

            // headers
            for(String key : mHeaders.keySet()) {
                mHttpURLConnection.setRequestProperty(key, mHeaders.get(key));
            }

            // cookies
            mHttpURLConnection.setRequestProperty("Cookie",
                    TextUtils.join(";", sCookieManager.getCookieStore().getCookies()));

            if (mMode == POST) {
                createPostBody();

                mHttpURLConnection.setRequestProperty("Content-Length", "" +
                        Integer.toString(mPostBody.length()));

                DataOutputStream wr = new DataOutputStream(
                        mHttpURLConnection.getOutputStream());
                wr.writeBytes(mPostBody);
                wr.flush();
                wr.close();
            }
        } catch(IOException e) {
            if (mHttpURLConnection != null) {
                mHttpURLConnection.disconnect();
                mHttpURLConnection = null;
            }

            throw(e);
        }
    }

    /**
     *
     * @param nullIfNotOK
     * @return
     * @throws IOException
     */
    public String getResponse(boolean nullIfNotOK) throws IOException {
        try {
            if (nullIfNotOK && getStatusCode() != 200) {
                return null;
            }

            // read cookies
            Map<String, List<String>> headerFields = mHttpURLConnection.getHeaderFields();
            List<String> cookiesHeader = headerFields.get("Set-Cookie");

            if (cookiesHeader != null) {
                for (String cookie : cookiesHeader) {
                    sCookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                }
            }

            // read response
            InputStream is = null;
            try {
                is = mHttpURLConnection.getInputStream();
            } catch(IOException e) {
				is = mHttpURLConnection.getErrorStream();
			}
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuffer response = new StringBuffer();
            while ((line = rd.readLine()) != null) {
                response.append(line);
                response.append('\r');
            }
            rd.close();

            return response.toString();
        } catch(IOException e) {
            throw (e);
        } finally {
            if (mHttpURLConnection != null) {
                mHttpURLConnection.disconnect();
                mHttpURLConnection = null;
            }
        }
    }

    private String getModeString() {
        switch(mMode) {
            case GET:
                return "GET";
            
            case POST: 
                return "POST";
            
            case PUT:
                return "PUT";
            
            case DELETE:
                return "DELETE";
        }
        return "GET";
    }

	public void post() throws IOException {
		mMode = POST;
        openRequest();
	}
	
	/**
	 * 
	 * @return
	 */
	public void createPostBody() {
        if (mPostBody != null) {
            return;
        }

        if (mPostParamMode == PARAM_MODE_HTML_ENTITY) {
            mPostBody = createPostDataString();
        } else {
            mPostBody = createPostBodyKeyValuePairs();
        }
	}

    /**
     *
     */
	public void get() throws IOException {
        mMode = GET;
        openRequest();
    }

    /**
     *
     */
    public void put() throws IOException {
        mMode = PUT;
        openRequest();
    }

    /**
     *
     */
    public void delete() throws IOException {
        mMode = DELETE;
        openRequest();
    }
	
	/**
	 * create url - add GET params
     * 
	 * @return
	 */
	protected String createFullUrl() {
		String url = mUrl;
		if (mGetParams.size() > 0) {
			url += (mUrl.contains("?") ? "" : "?");
		}

		for(String key : mGetParams.keySet()) {
			final Object param = mGetParams.get(key);
			if (param == null) {
				url += key + "=&";
			} else {
				url += key + "=" + URLEncoder.encode(param.toString()) + "&";
			}
		} 
		return url;
	}
	
	protected String createPostBodyKeyValuePairs() {
		final HashMap<String, Object> pairs = new HashMap<>();
		if (mPostParamMode == PARAM_MODE_STRING) {
			for(String key : mPostParams.keySet()) {
				pairs.put(key, mPostParams.get(key).toString());
			}
		} else {
			for(String key : mPostParams.keySet()) {
				try {
					Object param = mPostParams.get(key);
					if (param instanceof JSONObject) {
						addPostParams(pairs,
                                createParamFromJSONObject(new JSONObject().put(key, (JSONObject) mPostParams.get(key)), "")
                        );
					} else if (param instanceof JSONArray) {
						addPostParams(pairs,
								createParamFromJSONObject(new JSONObject().put(key, (JSONArray) mPostParams.get(key)), "")
						);
					} else {
						pairs.put(key, mPostParams.get(key).toString());
					}
				} catch(JSONException e) {
					throw(new IllegalArgumentException(e.getMessage()));
				}
			}
		}

        String ret = "";
        for(String key : pairs.keySet()) {
            final Object param = pairs.get(key);
            if (param == null) {
                ret += key + "=&";
            } else {
                ret += key + "=" + URLEncoder.encode(param.toString()) + "&";
            }
        }
        return ret;
	}

	private void addPostParams(HashMap<String, Object> pairs, ArrayList<String> params) {
		for(String param : params) {
			String[] keyValue = param.split("=");
			pairs.put(keyValue[0], keyValue[1]);
		}
	}

	/**
	 * 
	 * @return
	 */
	protected String createPostDataString() {
		String dataString = "";
		
		for(String key : mPostParams.keySet()) {
			try {
				dataString += createPostDataStringParam(new JSONObject().put(key, mPostParams.get(key))) + "&";
			} catch (JSONException e) {
				Debug.logException(e);
			}
		}
		
		return dataString;
	}

	/**
	 *
	 * @param param
	 * @return
	 */
	protected String createPostDataStringParam(Object param) {
		if (param instanceof JSONObject) {
			final ArrayList<String> paramsArray = createParamFromJSONObject((JSONObject) param, "");
			String result = "";
			for(String pm : paramsArray) {
				result += pm + "&";
			}
			return result;
		} else {
			return param.toString();
		}
	}
	
	private ArrayList<String> createParamFromJSONArray(JSONArray param, String sofar) {
		ArrayList<String> result = new ArrayList<String>();
		final int s = param.length();
		for(int i = 0; i < s; ++i) {
			final Object value = param.opt(i);
			if (value instanceof JSONObject) {
				result.addAll(createParamFromJSONObject((JSONObject) value, sofar + "[" + i + "]"));
			} else if (value instanceof JSONArray) {
				result.addAll(createParamFromJSONArray((JSONArray) value, sofar + "[" + i + "]"));
			} else {
				result.add(sofar + "[" + i + "]=" + value.toString());
			}
		}
		return result;
	}
	
	private ArrayList<String> createParamFromJSONObject(JSONObject param, String sofar) {
		ArrayList<String> result = new ArrayList<String>();
		@SuppressWarnings("unchecked")
		final Iterator<String> keys = param.keys();
		while(keys.hasNext()) {
			final String key = keys.next();
			final Object value = param.opt(key);
			
			final String sofarExt = sofar + (sofar == "" ? key : "[" + key + "]"); 
			
			if (value instanceof JSONObject) {
				result.addAll(createParamFromJSONObject((JSONObject) value, sofarExt));
			} else if (value instanceof JSONArray) {
				result.addAll(createParamFromJSONArray((JSONArray) value, sofarExt));
			} else {
				result.add(sofarExt + "=" + value.toString());
			}
		}
		return result;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getResponse() throws IOException {
		return getResponse(true);
	}
	
	/**
	 * 
	 */
	public String toString() {
		return mUrl + "#" + getModeString();
	}
	
	/**
	 * 
	 * @return
	 * @throws JSONException
	 */
	public JSONObject getJSONResponse() throws JSONException, IOException {
		return getJSONResponse(true);
	}

    /**
     *
     * @param nullIfNotOk
     * @return
     * @throws JSONException
     */
	public JSONObject getJSONResponse(boolean nullIfNotOk) throws JSONException, IOException {
		String response = "";
		try {
			response = getResponse(nullIfNotOk);
			return new JSONObject(response);
		} catch(JSONException e) {
			Debug.logE("HttpURLRequest", "Not json response: " + response);
			throw(e);
		}
	}

    /**
     *
     * @return
     */
	public Integer getStatusCode() throws IOException {
        if (mStatusCode == null) {
            try {
                mStatusCode = mHttpURLConnection.getResponseCode();
            } catch (IOException e) {

                // when the response code is 401 but the server had not gave the WWW-Authenticate header
                // first getResponseCode would throw an IOException
                mStatusCode = mHttpURLConnection.getResponseCode();
            }
        }
		return mStatusCode;
	}

    /**
     *
     */
	public void abort() {
		try {
			mHttpURLConnection.disconnect();
		} catch(Throwable e) {
			;
		}
	}

	public static int getIP(String hostname) {
	    InetAddress inetAddress;
	    try {
	        inetAddress = InetAddress.getByName(hostname);
	    } catch (UnknownHostException e) {
	        return -1;
	    }
	    byte[] addrBytes;
	    int addr;
	    addrBytes = inetAddress.getAddress();
	    addr = ((addrBytes[3] & 0xff) << 24)
	            | ((addrBytes[2] & 0xff) << 16)
	            | ((addrBytes[1] & 0xff) << 8)
	            |  (addrBytes[0] & 0xff);
	    return addr;
	}

    /**
     *
     * @return
     */
    public static CookieManager getCookieManager() {
        return sCookieManager;
    }
	
	/**
     * Disables the SSL certificate checking for new instances of {@link HttpsURLConnection}
     */
    public static void disableSSLCertificateChecking() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
 
            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            }
 
            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
            }
        } };
 
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
 
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
 
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
	
	public static class DebugLogConfig {
		 
	    static DalvikLogHandler activeHandler;
	 
	    protected static class DalvikLogHandler extends Handler {
	 
	        private static final String LOG_TAG = "HttpClient";
	 
	        @Override
	        public void close() {
	            // do nothing
	        }
	 
	        @Override
	        public void flush() {
	            // do nothing
	        }
	 
	        @Override
	        public void publish(LogRecord record) {
	            if (record.getLoggerName().startsWith("org.apache")) {
	                Log.d(LOG_TAG, record.getMessage());
	            }
	        }
	    }
	 
	    public static void enable() {
	        try {
	            String config = "org.apache.http.impl.conn.level = FINEST\n"
	                    + "org.apache.http.impl.client.level = FINEST\n"
	                    + "org.apache.http.client.level = FINEST\n" + "org.apache.http.level = FINEST";
	            InputStream in = new ByteArrayInputStream(config.getBytes());
	            LogManager.getLogManager().readConfiguration(in);
	        } catch (IOException e) {
	            Log.w(DebugLogConfig.class.getSimpleName(),
	                            "Can't read configuration file for logging");
	        }
	        Logger rootLogger = LogManager.getLogManager().getLogger("");
	        activeHandler = new DalvikLogHandler();
	        activeHandler.setLevel(Level.ALL);
	        rootLogger.addHandler(activeHandler);
	    }
	 
	}
}

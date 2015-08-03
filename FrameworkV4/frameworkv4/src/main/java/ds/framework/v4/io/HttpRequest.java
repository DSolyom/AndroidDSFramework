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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ds.framework.v4.common.Debug;

import android.util.Log;

public class HttpRequest {
	
	public static final int GET = 0;
	public static final int POST = 1;
	
	public static final int PARAM_MODE_STRING = 0;
	public static final int PARAM_MODE_HTML = 1;
	public static final int PARAM_MODE_HTML_ENTITY = 2;
	
	private String mUrl;
	private String mDataString;
	final private List<NameValuePair> mHeaders = new ArrayList<NameValuePair>();
	
	@Deprecated
	final private HashMap<String, Object> mData = new HashMap<String, Object>();
	
	final private HashMap<String, Object> mGetParams = new HashMap<String, Object>();
	final private HashMap<String, Object> mPostParams = new HashMap<String, Object>();

	private int mMode = GET;
	private int mPostParamMode = PARAM_MODE_STRING;
	private HttpUriRequest mRequest;
	private HttpResponse mHttpResponse;
	private Integer mStatusCode;
	private int mConnectionTimeout = 30000;
	private int mSoTimeout = 60000;
	private CookieStore mCookieStore;

	public HttpRequest() {
	}
	
	/**
	 * 
	 * @param url
	 */
	public HttpRequest(String url) {
		mUrl = url;
	}
	
	/**
	 * 
	 * @param url
	 * @param mode
	 */
	public HttpRequest(String url, int mode) {
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
		mSoTimeout = so;
	}
	
	/**
	 * clear all data except cookies
	 */
	public void clear() {
		mDataString = null;
		mData.clear();
		mHeaders.clear();
		mHttpResponse = null;
		mStatusCode = null;
	}
	
	/**
	 * clear everything
	 */
	public void reset() {
		clear();
		if (mCookieStore != null) {
			mCookieStore.clear();
		}
	}
	
	/**
	 * 
	 * @param dataString
	 */
	public void setData(String dataString) {
		mDataString = dataString;
	}
	
	/**
	 * 
	 * @param id
	 * @param value
	 */
	@Deprecated
	public void addData(String id, Object value) {
		mData.put(id, value);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	@Deprecated
	public boolean hasData(String id) {
		return mData.containsKey(id);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	@Deprecated
	public Object getData(String id) {
		return mData.get(id);
	}
	
	/**
	 * 
	 * @param id
	 * @return
	 */
	@Deprecated
	public Object removeData(String id) {
		return mData.remove(id);
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
		mHeaders.add(new BasicNameValuePair(id, String.valueOf(value)));
	}
	
	/**
	 * 
	 * @return
	 */
	public List<NameValuePair> getHeaders() {
		return mHeaders;
	}
	
	/**
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws Exception
	 */
	public void send() throws ClientProtocolException, IOException, Exception {
		if (mMode == GET) {
			get();
		} else {
			post();
		}
	}

	/**
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws Exception
	 */
	public void post() throws ClientProtocolException, IOException, Exception {
		postExecute(postCreate());
	}
	
	/**
	 * 
	 * @return
	 */
	public HttpPost postCreate() {
		HttpPost httppost = new HttpPost(createFullUrl(POST));
		
		try {
			for(NameValuePair nvp : mHeaders) {
				httppost.addHeader(nvp.getName(), nvp.getValue());
			} 
			if (mDataString == null) {
				if (mPostParamMode == PARAM_MODE_HTML_ENTITY) {
					mDataString = createPostDataString();
				} else {
					httppost.setEntity(new UrlEncodedFormEntity(createPostDataNameValuePairs(), HTTP.UTF_8));
				}
			}
			if (mDataString != null) {
				httppost.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE,
					    Boolean.FALSE);
				httppost.setEntity(new StringEntity(mDataString, HTTP.UTF_8));
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}

		return httppost;
	}
	
	/**
	 * 
	 * @param httppost
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws Exception
	 */
	public void postExecute(HttpPost httppost) throws ClientProtocolException, IOException, Exception {
		Debug.logW("HttpRequest", "post to " + httppost.getURI().toASCIIString());
		
		mRequest = httppost;
		
		DefaultHttpClient httpclient = sslClient(new DefaultHttpClient());
		if (mCookieStore != null) {
			httpclient.setCookieStore(mCookieStore);
		}
		HttpParams params = httpclient.getParams();
		params.setParameter(CoreProtocolPNames.USER_AGENT,
                System.getProperty("http.agent"));
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
		HttpConnectionParams.setConnectionTimeout(params, mConnectionTimeout);
		HttpConnectionParams.setSoTimeout(params, mSoTimeout);
		mHttpResponse = httpclient.execute(httppost);
		mCookieStore = httpclient.getCookieStore();
		mStatusCode = mHttpResponse.getStatusLine().getStatusCode();
		mRequest = null;
	}
	
	/**
	 * 
	 * @throws ClientProtocolException
	 * @throws IOException
	 * @throws Exception
	 */
	public void get() throws ClientProtocolException, IOException, Exception { 		
		HttpClient httpclient = sslClient(new DefaultHttpClient());
		
		HttpParams params = httpclient.getParams();
		params.setParameter(CoreProtocolPNames.USER_AGENT,
                System.getProperty("http.agent"));
		HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
		HttpConnectionParams.setConnectionTimeout(params, mConnectionTimeout);
		HttpConnectionParams.setSoTimeout(params, mSoTimeout);
		
		mRequest = new HttpGet(createFullUrl(GET));
		
		for(NameValuePair nvp : mHeaders) {
			mRequest.addHeader(nvp.getName(), nvp.getValue());
		} 

		HttpProtocolParams.setContentCharset(mRequest.getParams(), HTTP.UTF_8);
		
		Debug.logW("HttpRequest", "get from " + mRequest.getURI().toASCIIString());
		
		mHttpResponse = httpclient.execute(mRequest);
		mStatusCode = mHttpResponse.getStatusLine().getStatusCode();
		mRequest = null;
	}
	
	/**
	 * 
	 * @param client
	 * @return
	 */
	private DefaultHttpClient sslClient(HttpClient client) {
	    try {
	        X509TrustManager tm = new X509TrustManager() { 
	            public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
	            	// TODO
	            }

	            public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
	            	// TODO
	            }

	            public X509Certificate[] getAcceptedIssuers() {
	            	// TODO
	                return null;
	            }
	        };
	        SSLContext ctx = SSLContext.getInstance("TLS");
	        ctx.init(null, new TrustManager[]{tm}, null);
	        SSLSocketFactory ssf = new MySSLSocketFactory(ctx);
	        ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	        ClientConnectionManager ccm = client.getConnectionManager();
	        SchemeRegistry sr = ccm.getSchemeRegistry();
	        sr.register(new Scheme("https", ssf, 443));
	        return new DefaultHttpClient(ccm, client.getParams());
	    } catch (Exception ex) {
	        return null;
	    }
	}
	
	public static class MySSLSocketFactory extends SSLSocketFactory {
	     SSLContext sslContext = SSLContext.getInstance("TLS");

	     public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
	         super(truststore);

	         TrustManager tm = new X509TrustManager() {
	             public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	             }

	             public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	             }

	             public X509Certificate[] getAcceptedIssuers() {
	                 return null;
	             }
	         };

	         sslContext.init(null, new TrustManager[] { tm }, null);
	     }

	     public MySSLSocketFactory(SSLContext context) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, UnrecoverableKeyException {
	        super(null);
	        sslContext = context;
	     }

	     @Override
	     public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
	         return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
	     }

	     @Override
	     public Socket createSocket() throws IOException {
	         return sslContext.getSocketFactory().createSocket();
	     }
	}
	
	/**
	 * create url - add GET params
	 * 
	 * @param type
	 * @return
	 */
	protected String createFullUrl(int type) {
		String url = mUrl;
		if (mGetParams.size() > 0 || type == GET && mData.size() > 0) {
			url += (mUrl.contains("?") ? "" : "?");
		}
		if (type == GET) {
			for(String key : mData.keySet()) {
				final Object param = mData.get(key);
				if (param == null) {
					url += key + "=&";
				} else {
					url += key + "=" + URLEncoder.encode(param.toString()) + "&";
				}
			}
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
	
	protected ArrayList<NameValuePair> createPostDataNameValuePairs() {
		final ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
		if (mPostParamMode == PARAM_MODE_STRING) {
			for(String key : mData.keySet()) {
				pairs.add(new BasicNameValuePair(key, mData.get(key).toString()));
			}
			for(String key : mPostParams.keySet()) {
				pairs.add(new BasicNameValuePair(key, mPostParams.get(key).toString()));
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
						pairs.add(new BasicNameValuePair(key, mPostParams.get(key).toString()));
					}
				} catch(JSONException e) {
					throw(new IllegalArgumentException(e.getMessage()));
				}
			}
		}
		return pairs;
	}
	
	private void addPostParams(ArrayList<NameValuePair> pairs, ArrayList<String> params) {
		for(String param : params) {
			String[] keyValue = param.split("=");
			pairs.add(new BasicNameValuePair(keyValue[0], keyValue[1]));
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
	public CookieStore getCookieStore() {
		return mCookieStore;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getResponse() {
		return getResponse(true);
	}
	
	/**
	 * 
	 */
	public String toString() {
		return getResponse(false) + " - " + mStatusCode;
	}
	
	/**
	 * 
	 * @return
	 * @throws JSONException
	 */
	public JSONObject getJSONResponse() throws JSONException {
		String response = "";
		try {
			response = getResponse(true);
			return new JSONObject(response);
		} catch(JSONException e) {
			Debug.logE("HttpRequest", "Not json response: " + response);
			throw(e);
		}
	}
	
	public String getResponse(boolean nullIfNotOk) {
		try {
			if (nullIfNotOk && mStatusCode != HttpStatus.SC_OK) {
				return null;
			}
			InputStream is = mHttpResponse.getEntity().getContent();
			StringBuilder total = new StringBuilder();
		    
		    // Wrap a BufferedReader around the InputStream
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is));

		    String ret = "";
		    
		    // Read response until the end
		    while ((ret = rd.readLine()) != null) { 
		        total.append(ret); 
		    }
			mHttpResponse = null;
	
			return total.toString();
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public byte[] getResponseBytes() {
		return getResponseBytes(true);
	}
	
	/**
	 * 
	 * @param nullIfNotOk
	 * @return
	 */
	public byte[] getResponseBytes(boolean nullIfNotOk) {
		try {
			if (nullIfNotOk && mStatusCode != HttpStatus.SC_OK) {
				return null;
			}
			return EntityUtils.toByteArray(mHttpResponse.getEntity());
		} catch (Exception e) {
			return null;
		}
	}
	
	public Integer getStatusCode() {
		return mStatusCode;
	}
	
	public void abort() {
		try {
			mRequest.abort();
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

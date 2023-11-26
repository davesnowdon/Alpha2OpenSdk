package com.ubtech.alpha2.core.network.http;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import com.ubtech.alpha2.core.network.async.BaseAsyncDownloadTask;
import com.ubtech.alpha2.core.network.async.DownLoadBen;
import com.ubtech.alpha2.core.utils.NLog;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpVersion;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;
import org.apache.http.util.EntityUtils;

public class HttpClientManager {
   private final String tag = HttpClientManager.class.getSimpleName();
   private static final String VERSION = "1.4.3";
   private static HttpClientManager instance;
   private static final int BUFFER_SIZE = 4096;
   private static final int DEFAULT_MAX_CONNECTIONS = 10;
   private static final int DEFAULT_SOCKET_TIMEOUT = 10000;
   private static final int DEFAULT_MAX_RETRIES = 5;
   private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;
   private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
   private static final String ENCODING_GZIP = "gzip";
   private static int maxConnections = 10;
   private static int socketTimeout = 10000;
   private static final String ASSETS_PATH = "assets";
   private static final String ENCODE_UTF8 = "UTF-8";
   private final DefaultHttpClient httpClient;
   private final HttpContext httpContext;
   private final Map<Context, List<WeakReference<Future<?>>>> requestMap;
   private final Map<String, String> clientHeaderMap;
   private static PersistentCookieStore cookieStore;

   public static HttpClientManager getInstance(Context context) {
      if (instance == null) {
         Class var1 = HttpClientManager.class;
         synchronized(HttpClientManager.class) {
            if (instance == null) {
               instance = new HttpClientManager();
            }
         }
      }

      cookieStore = new PersistentCookieStore(context);
      instance.setCookieStore(cookieStore);
      return instance;
   }

   private HttpClientManager() {
      BasicHttpParams httpParams = new BasicHttpParams();
      ConnManagerParams.setTimeout(httpParams, (long)socketTimeout);
      ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(maxConnections));
      ConnManagerParams.setMaxTotalConnections(httpParams, 10);
      HttpConnectionParams.setSoTimeout(httpParams, socketTimeout);
      HttpConnectionParams.setConnectionTimeout(httpParams, socketTimeout);
      HttpConnectionParams.setTcpNoDelay(httpParams, true);
      HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
      HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
      HttpProtocolParams.setUserAgent(httpParams, String.format("android-http/%s", "1.4.3"));
      SchemeRegistry schemeRegistry = new SchemeRegistry();
      schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
      schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
      ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
      this.httpContext = new SyncBasicHttpContext(new BasicHttpContext());
      this.httpClient = new DefaultHttpClient(cm, httpParams);
      this.httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
         public void process(HttpRequest request, HttpContext context) {
            if (!request.containsHeader("Accept-Encoding")) {
               request.addHeader("Accept-Encoding", "gzip");
            }

            Iterator var3 = HttpClientManager.this.clientHeaderMap.keySet().iterator();

            while(var3.hasNext()) {
               String header = (String)var3.next();
               request.addHeader(header, (String)HttpClientManager.this.clientHeaderMap.get(header));
            }

         }
      });
      this.httpClient.addResponseInterceptor(new HttpResponseInterceptor() {
         public void process(HttpResponse response, HttpContext context) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
               Header encoding = entity.getContentEncoding();
               if (encoding != null) {
                  HeaderElement[] var5 = encoding.getElements();
                  int var6 = var5.length;

                  for(int var7 = 0; var7 < var6; ++var7) {
                     HeaderElement element = var5[var7];
                     if (element.getName().equalsIgnoreCase("gzip")) {
                        response.setEntity(new HttpClientManager.InflatingEntity(response.getEntity()));
                        break;
                     }
                  }
               }

            }
         }
      });
      this.httpClient.setHttpRequestRetryHandler(new RetryHandler(5));
      this.requestMap = new WeakHashMap();
      this.clientHeaderMap = new HashMap();
   }

   public HttpClient getHttpClient() {
      return this.httpClient;
   }

   public HttpContext getHttpContext() {
      return this.httpContext;
   }

   public void setCookieStore(CookieStore cookieStore) {
      this.httpContext.setAttribute("http.cookie-store", cookieStore);
   }

   public void setUserAgent(String userAgent) {
      HttpProtocolParams.setUserAgent(this.httpClient.getParams(), userAgent);
   }

   public void setTimeout(int timeout) {
      HttpParams httpParams = this.httpClient.getParams();
      ConnManagerParams.setTimeout(httpParams, (long)timeout);
      HttpConnectionParams.setSoTimeout(httpParams, timeout);
      HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
   }

   public void setSSLSocketFactory(SSLSocketFactory sslSocketFactory) {
      this.httpClient.getConnectionManager().getSchemeRegistry().register(new Scheme("https", sslSocketFactory, 443));
   }

   public void addHeader(String header, String value) {
      this.clientHeaderMap.put(header, value);
   }

   public void setBasicAuth(String user, String pass) {
      AuthScope scope = AuthScope.ANY;
      this.setBasicAuth(user, pass, scope);
   }

   public void setBasicAuth(String user, String pass, AuthScope scope) {
      UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(user, pass);
      this.httpClient.getCredentialsProvider().setCredentials(scope, credentials);
   }

   public void cancelRequests(Context context, boolean mayInterruptIfRunning) {
      List<WeakReference<Future<?>>> requestList = (List)this.requestMap.get(context);
      if (requestList != null) {
         Iterator var4 = requestList.iterator();

         while(var4.hasNext()) {
            WeakReference<Future<?>> requestRef = (WeakReference)var4.next();
            Future<?> request = (Future)requestRef.get();
            if (request != null) {
               request.cancel(mayInterruptIfRunning);
            }
         }
      }

      this.requestMap.remove(context);
   }

   public String get(String url) throws HttpException {
      return this.get((Context)null, url, (RequestParams)null);
   }

   public String get(String url, RequestParams params) throws HttpException {
      return this.get((Context)null, url, params);
   }

   public String get(Context context, String url) throws HttpException {
      return this.get(context, url, (RequestParams)null);
   }

   public String get(Context context, String url, RequestParams params) throws HttpException {
      return this.sendRequest(this.httpClient, this.httpContext, new HttpGet(getUrlWithQueryString(url, params)), (String)null, context);
   }

   public String get(Context context, String url, Header[] headers, RequestParams params) throws HttpException {
      HttpUriRequest request = new HttpGet(getUrlWithQueryString(url, params));
      if (headers != null) {
         request.setHeaders(headers);
      }

      return this.sendRequest(this.httpClient, this.httpContext, request, (String)null, context);
   }

   public String post(String url) throws HttpException {
      return this.post((Context)null, url, (RequestParams)null);
   }

   public String post(String url, RequestParams params) throws HttpException {
      return this.post((Context)null, url, params);
   }

   public String post(Context context, String url, RequestParams params) throws HttpException {
      return this.post(context, url, this.paramsToEntity(params), (String)null);
   }

   public String post(Context context, String url, HttpEntity entity, String contentType) throws HttpException {
      return this.sendRequest(this.httpClient, this.httpContext, this.addEntityToRequestBase(new HttpPost(url), entity), contentType, context);
   }

   public String post(Context context, String url, Header[] headers, RequestParams params, String contentType) throws HttpException {
      HttpEntityEnclosingRequestBase request = new HttpPost(url);
      if (params != null) {
         request.setEntity(this.paramsToEntity(params));
      }

      if (headers != null) {
         request.setHeaders(headers);
      }

      return this.sendRequest(this.httpClient, this.httpContext, request, contentType, context);
   }

   public String post(Context context, String url, Header[] headers, HttpEntity entity, String contentType) throws HttpException {
      HttpEntityEnclosingRequestBase request = this.addEntityToRequestBase(new HttpPost(url), entity);
      if (headers != null) {
         request.setHeaders(headers);
      }

      return this.sendRequest(this.httpClient, this.httpContext, request, contentType, context);
   }

   public String put(String url) throws HttpException {
      return this.put((Context)null, url, (RequestParams)null);
   }

   public String put(String url, RequestParams params) throws HttpException {
      return this.put((Context)null, url, params);
   }

   public String put(Context context, String url, RequestParams params) throws HttpException {
      return this.put(context, url, this.paramsToEntity(params), (String)null);
   }

   public String put(Context context, String url, HttpEntity entity, String contentType) throws HttpException {
      return this.sendRequest(this.httpClient, this.httpContext, this.addEntityToRequestBase(new HttpPut(url), entity), contentType, context);
   }

   public String put(Context context, String url, Header[] headers, HttpEntity entity, String contentType) throws HttpException {
      HttpEntityEnclosingRequestBase request = this.addEntityToRequestBase(new HttpPut(url), entity);
      if (headers != null) {
         request.setHeaders(headers);
      }

      return this.sendRequest(this.httpClient, this.httpContext, request, contentType, context);
   }

   public String delete(String url) throws HttpException {
      return this.delete((Context)null, url);
   }

   public String delete(Context context, String url) throws HttpException {
      HttpDelete delete = new HttpDelete(url);
      return this.sendRequest(this.httpClient, this.httpContext, delete, (String)null, context);
   }

   public String delete(Context context, String url, Header[] headers) throws HttpException {
      HttpDelete delete = new HttpDelete(url);
      if (headers != null) {
         delete.setHeaders(headers);
      }

      return this.sendRequest(this.httpClient, this.httpContext, delete, (String)null, context);
   }

   protected String sendRequest(DefaultHttpClient client, HttpContext httpContext, HttpUriRequest uriRequest, String contentType, Context context) throws HttpException {
      String responseBody = "";
      if (contentType != null) {
         uriRequest.addHeader("Content-Type", contentType);
      }

      List<Cookie> list = cookieStore.getCookies();
      if (list != null && list.size() > 0) {
         Iterator var8 = list.iterator();

         while(var8.hasNext()) {
            Cookie cookie = (Cookie)var8.next();
            uriRequest.setHeader("Cookie", cookie.getValue());
         }
      }

      try {
         URI uri = uriRequest.getURI();
         NLog.e(this.tag, "url : " + uri.toString());
         String scheme = uri.getScheme();
         NLog.e(this.tag, "scheme : " + scheme);
         if (!TextUtils.isEmpty(scheme) && "assets".equals(scheme)) {
            String fileName = uri.getAuthority();
            InputStream intput = context.getAssets().open(fileName);
            responseBody = inputSteamToString(intput);
            NLog.e(this.tag, "responseBody : " + responseBody);
            return responseBody;
         } else {
            HttpEntity bufferEntity = null;
            HttpResponse response = client.execute(uriRequest, httpContext);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
               bufferEntity = new BufferedHttpEntity(entity);
               responseBody = EntityUtils.toString(bufferEntity, "UTF-8");
               NLog.e(this.tag, "responseBody : " + responseBody);
            }

            Header[] headers = response.getHeaders("Set-Cookie");
            if (headers != null && headers.length > 0) {
               for(int i = 0; i < headers.length; ++i) {
                  String cookie = headers[i].getValue();
                  BasicClientCookie newCookie = new BasicClientCookie("cookie" + i, cookie);
                  cookieStore.addCookie(newCookie);
               }
            }

            return responseBody;
         }
      } catch (Exception var17) {
         var17.printStackTrace();
         throw new HttpException(var17);
      }
   }

   public DownLoadBen download(DownLoadBen bean, BaseAsyncDownloadTask task) throws HttpException {
      String url = bean.getDownUrl();
      String preUrl = url.substring(0, url.lastIndexOf("/")) + "/";
      String name = url.substring(url.lastIndexOf("/") + 1);
      url = preUrl + Uri.encode(name, "US-ASCII");
      HttpGet get = new HttpGet(url);

      for(int i = 0; i < 3; ++i) {
         this.download(this.httpClient, this.httpContext, get, task, bean);
         if (!bean.isException()) {
            break;
         }
      }

      return bean;
   }

   protected DownLoadBen download(DefaultHttpClient client, HttpContext httpContext, HttpUriRequest uriRequest, BaseAsyncDownloadTask task, DownLoadBen bean) throws HttpException {
      try {
         URI uri = uriRequest.getURI();
         NLog.e(this.tag, "url : " + uri.toString());
         String uriStr = bean.getDownUrl();
         String name = uriStr.substring(uriStr.lastIndexOf("/") + 1);
         bean.setFileName(name);
         File file = new File(bean.getAppPath() + "/" + name);
         long isCurrent = 0L;
         if (file.exists()) {
            isCurrent = file.length();
            HttpResponse response = client.execute(uriRequest);
            HttpEntity entity = response.getEntity();
            if (isCurrent == entity.getContentLength()) {
               return bean;
            }
         }

         Header header_size = new BasicHeader("Range", "bytes=" + isCurrent + "-");
         uriRequest.addHeader(header_size);
         HttpResponse response = client.execute(uriRequest);
         HttpEntity entity = response.getEntity();
         if (entity != null) {
            InputStream instream = entity.getContent();
            long contentLength = entity.getContentLength() + isCurrent;
            Log.e("FileDownload", "getContentLength " + entity.getContentLength() + " skipsize= " + isCurrent);
            if (instream == null) {
               return bean;
            }

            try {
               RandomAccessFile randomFile = new RandomAccessFile(file, "rw");

               try {
                  randomFile.seek(isCurrent);
                  byte[] tmp = new byte[4096];
                  long count = isCurrent;
                  boolean var24 = false;

                  int length;
                  while((length = instream.read(tmp)) != -1 && !Thread.currentThread().isInterrupted()) {
                     count += (long)length;
                     randomFile.write(tmp, 0, length);
                     int proress = (int)((float)count / (float)contentLength * 100.0F);
                     task.onProgressUpdate(proress);
                  }

                  Log.e("FileDownload", "count " + (contentLength - count) + " skipsize= " + isCurrent);
               } finally {
                  if (instream != null) {
                     instream.close();
                  }

                  if (randomFile != null) {
                     randomFile.close();
                  }

               }
            } catch (OutOfMemoryError var30) {
               System.gc();
               throw new HttpException("File too large to fit into available memory");
            }
         }

         return bean;
      } catch (Exception var31) {
         var31.printStackTrace();
         Log.e("HttpDownload", "" + var31);
         bean.setException(true);
         throw new HttpException(var31);
      }
   }

   public static String inputSteamToString(InputStream in) throws IOException {
      ByteArrayOutputStream outStream = new ByteArrayOutputStream();
      byte[] data = new byte[1024];
      boolean var3 = true;

      int count;
      while((count = in.read(data, 0, 1024)) != -1) {
         outStream.write(data, 0, count);
      }

      data = null;
      return new String(outStream.toByteArray(), "UTF-8");
   }

   public static String getUrlWithQueryString(String url, RequestParams params) {
      if (params != null) {
         String paramString = params.getParamString();
         if (url.indexOf("?") == -1) {
            url = url + "?" + paramString;
         } else {
            url = url + "&" + paramString;
         }
      }

      return url;
   }

   private HttpEntity paramsToEntity(RequestParams params) {
      HttpEntity entity = null;
      if (params != null) {
         entity = params.getEntity();
         NLog.e(this.tag, "params : " + params.toString());
      }

      return entity;
   }

   private HttpEntityEnclosingRequestBase addEntityToRequestBase(HttpEntityEnclosingRequestBase requestBase, HttpEntity entity) {
      if (entity != null) {
         requestBase.setEntity(entity);
      }

      return requestBase;
   }

   private static class InflatingEntity extends HttpEntityWrapper {
      public InflatingEntity(HttpEntity wrapped) {
         super(wrapped);
      }

      public InputStream getContent() throws IOException {
         return new GZIPInputStream(this.wrappedEntity.getContent());
      }

      public long getContentLength() {
         return -1L;
      }
   }
}

package com.ubtechinc.alpha2ctrlapp.network.common;

import android.util.Log;
import com.ubtechinc.alpha2ctrlapp.network.JsonUtils;
import com.ubtechinc.alpha2ctrlapp.network.model.request.CommonRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;

public class HttpPost {
   public static final String NO_URL = "NO_URL";
   public static final int HTTP_TIMEOUT = 10000;
   public static final String WebServiceAdderss = "http://services.ubtrobot.com/ubx/";
   public static final String DeveloperServerAddress = "http://dev.ubtrobot.com/opencenter/app/accesscheckapp";
   private static final String BOUNDARY = "7db372eb000e2";
   private static final String PREFIX = "--";
   private static final String LINE_END = "\r\n";
   private static final String CONTENT_TYPE = "multipart/form-data";

   public HttpPost() {
   }

   public static String getJsonByPost(String action, CommonRequest request, boolean isGetList) {
      String result = "";
      String _url = "";
      if (!isGetList) {
         _url = "http://services.ubtrobot.com/ubx/" + action;
      } else {
         _url = "http://dev.ubtrobot.com/opencenter/app/accesscheckapp";
      }

      try {
         URL url = new URL(_url);
         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
         conn.setConnectTimeout(10000);
         conn.setReadTimeout(10000);
         conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
         conn.setRequestProperty("accept", "*/*");
         conn.setRequestProperty("connection", "Keep-Alive");
         conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
         conn.setDoOutput(true);
         conn.setDoInput(true);
         PrintWriter out = new PrintWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
         String _params = JsonUtils.getInstance().getJson(request);
         Log.i("nxy", "!!请求消息" + _params);
         out.print(_params);
         out.flush();

         BufferedReader in;
         String line;
         for(in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8")); (line = in.readLine()) != null; result = result + line) {
         }

         out.close();
         in.close();
      } catch (Exception var11) {
         var11.toString();
      }

      Log.i("nxy", "!!!响应消息" + result);
      return result;
   }

   private static byte[] getFileData(String filePath) {
      File file = new File(filePath);
      if (!file.exists()) {
         return null;
      } else {
         long length = file.length();
         byte[] buffer = new byte[(int)length];

         try {
            FileInputStream fis = new FileInputStream(file);
            fis.read(buffer);
         } catch (FileNotFoundException var7) {
            var7.printStackTrace();
         } catch (IOException var8) {
            var8.printStackTrace();
         }

         return buffer;
      }
   }

   public static String uploadFileByPost(String action, CommonRequest request, boolean isGetList, int type, String filePath, String serialNumber) {
      String _url = "";
      _url = "http://services.ubtrobot.com/ubx/" + action;
      HttpClient httpClient = new DefaultHttpClient();
      int index = filePath.lastIndexOf("/");
      filePath.substring(index + 1);
      org.apache.http.client.methods.HttpPost httpPost = new org.apache.http.client.methods.HttpPost(_url);
      MultipartEntity mpEntity = new MultipartEntity();
      String _params = "1233245345634";
      StringBody stringBody = null;

      try {
         stringBody = new StringBody(serialNumber);
      } catch (UnsupportedEncodingException var20) {
         var20.printStackTrace();
      }

      ContentBody filebody = new FileBody(new File(filePath));
      mpEntity.addPart("serialNumber", stringBody);
      mpEntity.addPart("image", filebody);
      httpPost.setEntity(mpEntity);
      HttpResponse response = null;
      BufferedReader reader = null;
      StringBuilder s = new StringBuilder();

      try {
         response = httpClient.execute(httpPost);

         String sResponse;
         for(reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8")); (sResponse = reader.readLine()) != null; s = s.append(sResponse)) {
         }
      } catch (ClientProtocolException var21) {
         var21.printStackTrace();
      } catch (IOException var22) {
         var22.printStackTrace();
      }

      String result = "";
      result = result + s.toString();
      Log.i("wzt", "!!!响应消息 " + result);
      return result;
   }
}

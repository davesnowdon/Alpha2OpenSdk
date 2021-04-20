package com.ubtech.alpha2.core.network.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

public class RequestParams {
   private static String ENCODING = "UTF-8";
   protected ConcurrentHashMap<String, String> urlParams;
   protected ConcurrentHashMap<String, RequestParams.FileWrapper> fileParams;
   protected ConcurrentHashMap<String, ArrayList<String>> urlParamsWithArray;

   public RequestParams() {
      this.init();
   }

   public RequestParams(Map<String, String> source) {
      this.init();
      Iterator var2 = source.entrySet().iterator();

      while(var2.hasNext()) {
         Entry<String, String> entry = (Entry)var2.next();
         this.put((String)entry.getKey(), (String)entry.getValue());
      }

   }

   public RequestParams(String key, String value) {
      this.init();
      this.put(key, value);
   }

   public RequestParams(Object... keysAndValues) {
      this.init();
      int len = keysAndValues.length;
      if (len % 2 != 0) {
         throw new IllegalArgumentException("Supplied arguments must be even");
      } else {
         for(int i = 0; i < len; i += 2) {
            String key = String.valueOf(keysAndValues[i]);
            String val = String.valueOf(keysAndValues[i + 1]);
            this.put(key, val);
         }

      }
   }

   public void put(String key, String value) {
      if (key != null && value != null) {
         this.urlParams.put(key, value);
      }

   }

   public void put(String key, File file) throws FileNotFoundException {
      this.put(key, new FileInputStream(file), file.getName());
   }

   public void put(String key, ArrayList<String> values) {
      if (key != null && values != null) {
         this.urlParamsWithArray.put(key, values);
      }

   }

   public void add(String key, String value) {
      if (key != null && value != null) {
         ArrayList<String> paramArray = (ArrayList)this.urlParamsWithArray.get(key);
         if (paramArray == null) {
            paramArray = new ArrayList();
            this.put(key, paramArray);
         }

         paramArray.add(value);
      }

   }

   public void put(String key, InputStream stream) {
      this.put(key, stream, (String)null);
   }

   public void put(String key, InputStream stream, String fileName) {
      this.put(key, stream, fileName, (String)null);
   }

   public void put(String key, InputStream stream, String fileName, String contentType) {
      if (key != null && stream != null) {
         this.fileParams.put(key, new RequestParams.FileWrapper(stream, fileName, contentType));
      }

   }

   public void remove(String key) {
      this.urlParams.remove(key);
      this.fileParams.remove(key);
      this.urlParamsWithArray.remove(key);
   }

   public String toString() {
      StringBuilder result = new StringBuilder();
      Iterator var2 = this.urlParams.entrySet().iterator();

      Entry entry;
      while(var2.hasNext()) {
         entry = (Entry)var2.next();
         if (result.length() > 0) {
            result.append("&");
         }

         result.append((String)entry.getKey());
         result.append("=");
         result.append((String)entry.getValue());
      }

      var2 = this.fileParams.entrySet().iterator();

      while(var2.hasNext()) {
         entry = (Entry)var2.next();
         if (result.length() > 0) {
            result.append("&");
         }

         result.append((String)entry.getKey());
         result.append("=");
         result.append("FILE");
      }

      var2 = this.urlParamsWithArray.entrySet().iterator();

      while(var2.hasNext()) {
         entry = (Entry)var2.next();
         if (result.length() > 0) {
            result.append("&");
         }

         ArrayList<String> values = (ArrayList)entry.getValue();

         for(int i = 0; i < values.size(); ++i) {
            if (i != 0) {
               result.append("&");
            }

            result.append((String)entry.getKey());
            result.append("=");
            result.append((String)values.get(i));
         }
      }

      return result.toString();
   }

   public HttpEntity getEntity() {
      HttpEntity entity = null;
      if (!this.fileParams.isEmpty()) {
         SimpleMultipartEntity multipartEntity = new SimpleMultipartEntity();
         Iterator var3 = this.urlParams.entrySet().iterator();

         Entry entry;
         while(var3.hasNext()) {
            entry = (Entry)var3.next();
            multipartEntity.addPart((String)entry.getKey(), (String)entry.getValue());
         }

         var3 = this.urlParamsWithArray.entrySet().iterator();

         while(var3.hasNext()) {
            entry = (Entry)var3.next();
            ArrayList<String> values = (ArrayList)entry.getValue();
            Iterator var6 = values.iterator();

            while(var6.hasNext()) {
               String value = (String)var6.next();
               multipartEntity.addPart((String)entry.getKey(), value);
            }
         }

         int currentIndex = 0;
         int lastIndex = this.fileParams.entrySet().size() - 1;

         for(Iterator var12 = this.fileParams.entrySet().iterator(); var12.hasNext(); ++currentIndex) {
            Entry<String, RequestParams.FileWrapper> entry = (Entry)var12.next();
            RequestParams.FileWrapper file = (RequestParams.FileWrapper)entry.getValue();
            if (file.inputStream != null) {
               boolean isLast = currentIndex == lastIndex;
               if (file.contentType != null) {
                  multipartEntity.addPart((String)entry.getKey(), file.getFileName(), file.inputStream, file.contentType, isLast);
               } else {
                  multipartEntity.addPart((String)entry.getKey(), file.getFileName(), file.inputStream, isLast);
               }
            }
         }

         entity = multipartEntity;
      } else {
         try {
            entity = new UrlEncodedFormEntity(this.getParamsList(), ENCODING);
         } catch (UnsupportedEncodingException var9) {
            var9.printStackTrace();
         }
      }

      return (HttpEntity)entity;
   }

   private void init() {
      this.urlParams = new ConcurrentHashMap();
      this.fileParams = new ConcurrentHashMap();
      this.urlParamsWithArray = new ConcurrentHashMap();
   }

   protected List<BasicNameValuePair> getParamsList() {
      List<BasicNameValuePair> lparams = new LinkedList();
      Iterator var2 = this.urlParams.entrySet().iterator();

      Entry entry;
      while(var2.hasNext()) {
         entry = (Entry)var2.next();
         lparams.add(new BasicNameValuePair((String)entry.getKey(), (String)entry.getValue()));
      }

      var2 = this.urlParamsWithArray.entrySet().iterator();

      while(var2.hasNext()) {
         entry = (Entry)var2.next();
         ArrayList<String> values = (ArrayList)entry.getValue();
         Iterator var5 = values.iterator();

         while(var5.hasNext()) {
            String value = (String)var5.next();
            lparams.add(new BasicNameValuePair((String)entry.getKey(), value));
         }
      }

      return lparams;
   }

   protected String getParamString() {
      return URLEncodedUtils.format(this.getParamsList(), ENCODING);
   }

   private static class FileWrapper {
      public InputStream inputStream;
      public String fileName;
      public String contentType;

      public FileWrapper(InputStream inputStream, String fileName, String contentType) {
         this.inputStream = inputStream;
         this.fileName = fileName;
         this.contentType = contentType;
      }

      public String getFileName() {
         return this.fileName != null ? this.fileName : "nofilename";
      }
   }
}

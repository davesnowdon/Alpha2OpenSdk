package com.ubtech.alpha2.core.utils.download.netdownload;

import android.content.Context;
import android.util.Log;
import com.ubtech.alpha2.core.utils.download.service.FileService;
import java.io.File;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileDownloader {
   private static final String TAG = "FileDownloader";
   private Context context;
   private FileService fileService;
   private boolean exit;
   private int downloadSize = 0;
   private int fileSize = 0;
   private DownloadThread[] threads;
   private File saveFile;
   private Map<Integer, Integer> data = new ConcurrentHashMap();
   private int block;
   private String downloadUrl;

   public int getThreadSize() {
      return this.threads.length;
   }

   public void exit() {
      this.exit = true;
   }

   public boolean getExit() {
      return this.exit;
   }

   public int getFileSize() {
      return this.fileSize;
   }

   protected synchronized void append(int size) {
      this.downloadSize += size;
   }

   protected synchronized void update(int threadId, int pos) {
      this.data.put(threadId, pos);
      this.fileService.update(this.downloadUrl, threadId, pos);
   }

   public FileDownloader(Context context, String downloadUrl, File fileSaveDir, int threadNum, String savaFileName) {
      try {
         this.context = context;
         this.downloadUrl = downloadUrl;
         this.fileService = new FileService(this.context);
         URL url = new URL(this.downloadUrl);
         if (!fileSaveDir.exists()) {
            fileSaveDir.mkdirs();
         }

         this.threads = new DownloadThread[threadNum];
         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
         conn.setRequestMethod("GET");
         conn.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
         conn.setRequestProperty("Accept-Language", "zh-CN");
         conn.setRequestProperty("Referer", downloadUrl);
         conn.setRequestProperty("Charset", "UTF-8");
         conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
         conn.setRequestProperty("Connection", "Keep-Alive");
         conn.setRequestProperty("Accept-Encoding", "identity");
         conn.connect();
         printResponseHeader(conn);
         if (conn.getResponseCode() != 200) {
            throw new RuntimeException("server no response ");
         } else {
            this.fileSize = conn.getContentLength();
            if (this.fileSize <= 0) {
               throw new RuntimeException("Unkown file size ");
            } else {
               this.saveFile = new File(fileSaveDir, savaFileName);
               if (!this.saveFile.getParentFile().exists()) {
                  this.saveFile.getParentFile().mkdirs();
               }

               Map<Integer, Integer> logdata = this.fileService.getData(downloadUrl);
               if (logdata.size() > 0) {
                  Iterator var10 = logdata.entrySet().iterator();

                  while(var10.hasNext()) {
                     Entry<Integer, Integer> entry = (Entry)var10.next();
                     this.data.put(entry.getKey(), entry.getValue());
                  }
               }

               if (this.data.size() == this.threads.length) {
                  for(int i = 0; i < this.threads.length; ++i) {
                     this.downloadSize += (Integer)this.data.get(i + 1);
                  }

                  print("已经下载的长度" + this.downloadSize);
               }

               this.block = this.fileSize % this.threads.length == 0 ? this.fileSize / this.threads.length : this.fileSize / this.threads.length + 1;
            }
         }
      } catch (Exception var12) {
         print(var12.toString());
         throw new RuntimeException("don't connection this url");
      }
   }

   private String getFileName(HttpURLConnection conn) {
      String filename = this.downloadUrl.substring(this.downloadUrl.lastIndexOf(47) + 1);
      if (filename == null || "".equals(filename.trim())) {
         int i = 0;

         while(true) {
            String mine = conn.getHeaderField(i);
            if (mine == null) {
               filename = UUID.randomUUID() + ".tmp";
               break;
            }

            if ("content-disposition".equals(conn.getHeaderFieldKey(i).toLowerCase())) {
               Matcher m = Pattern.compile(".*filename=(.*)").matcher(mine.toLowerCase());
               if (m.find()) {
                  return m.group(1);
               }
            }

            ++i;
         }
      }

      return filename;
   }

   public int download(DownloadProgressListener listener) throws Exception {
      try {
         RandomAccessFile randOut = new RandomAccessFile(this.saveFile, "rw");
         if (this.fileSize > 0) {
            randOut.setLength((long)this.fileSize);
         }

         randOut.close();
         URL url = new URL(this.downloadUrl);
         int i;
         if (this.data.size() != this.threads.length) {
            this.data.clear();
            i = 0;

            while(true) {
               if (i >= this.threads.length) {
                  this.downloadSize = 0;
                  break;
               }

               this.data.put(i + 1, 0);
               ++i;
            }
         }

         int i;
         for(i = 0; i < this.threads.length; ++i) {
            i = (Integer)this.data.get(i + 1);
            if (i < this.block && this.downloadSize < this.fileSize) {
               this.threads[i] = new DownloadThread(this, url, this.saveFile, this.block, (Integer)this.data.get(i + 1), i + 1);
               this.threads[i].setPriority(7);
               this.threads[i].start();
            } else {
               this.threads[i] = null;
            }
         }

         this.fileService.delete(this.downloadUrl);
         this.fileService.save(this.downloadUrl, this.data);
         boolean notFinish = true;

         while(true) {
            if (!notFinish) {
               if (this.downloadSize == this.fileSize) {
                  this.fileService.delete(this.downloadUrl);
               }
               break;
            }

            Thread.sleep(900L);
            notFinish = false;

            for(i = 0; i < this.threads.length; ++i) {
               if (this.threads[i] != null && !this.threads[i].isFinish()) {
                  notFinish = true;
                  if (this.threads[i].getDownLength() == -1L) {
                     notFinish = false;
                     listener.onDownloadSize(-1);
                     return -1;
                  }
               }
            }

            if (listener != null) {
               listener.onDownloadSize(this.downloadSize);
            }
         }
      } catch (Exception var6) {
         print(var6.toString());
         throw new Exception("file download error");
      }

      return this.downloadSize;
   }

   public static Map<String, String> getHttpResponseHeader(HttpURLConnection http) {
      Map<String, String> header = new LinkedHashMap();
      int i = 0;

      while(true) {
         String mine = http.getHeaderField(i);
         if (mine == null) {
            return header;
         }

         header.put(http.getHeaderFieldKey(i), mine);
         ++i;
      }
   }

   public static void printResponseHeader(HttpURLConnection http) {
      Map<String, String> header = getHttpResponseHeader(http);
      Iterator var2 = header.entrySet().iterator();

      while(var2.hasNext()) {
         Entry<String, String> entry = (Entry)var2.next();
         String key = entry.getKey() != null ? (String)entry.getKey() + ":" : "";
         print(key + (String)entry.getValue());
      }

   }

   private static void print(String msg) {
      Log.i("FileDownloader", msg);
   }
}

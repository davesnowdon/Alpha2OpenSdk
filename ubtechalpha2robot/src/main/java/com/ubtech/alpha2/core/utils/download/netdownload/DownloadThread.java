package com.ubtech.alpha2.core.utils.download.netdownload;

import android.util.Log;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadThread extends Thread {
   private static final String TAG = "DownloadThread";
   private File saveFile;
   private URL downUrl;
   private int block;
   private int threadId = -1;
   private int downLength;
   private boolean finish = false;
   private FileDownloader downloader;

   public DownloadThread(FileDownloader downloader, URL downUrl, File saveFile, int block, int downLength, int threadId) {
      this.downUrl = downUrl;
      this.saveFile = saveFile;
      this.block = block;
      this.downloader = downloader;
      this.threadId = threadId;
      this.downLength = downLength;
   }

   public void run() {
      if (this.downLength < this.block) {
         try {
            HttpURLConnection http = (HttpURLConnection)this.downUrl.openConnection();
            http.setRequestMethod("GET");
            http.setRequestProperty("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            http.setRequestProperty("Accept-Language", "zh-CN");
            http.setRequestProperty("Referer", this.downUrl.toString());
            http.setRequestProperty("Charset", "UTF-8");
            int startPos = this.block * (this.threadId - 1) + this.downLength;
            int endPos = this.block * this.threadId - 1;
            http.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
            http.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
            http.setRequestProperty("Connection", "Keep-Alive");
            http.setRequestProperty("Accept-Encoding", "identity");
            InputStream inStream = http.getInputStream();
            byte[] buffer = new byte[10240];

            print("Thread " + this.threadId + " start download from position " + startPos);
            RandomAccessFile threadfile = new RandomAccessFile(this.saveFile, "rwd");
            threadfile.seek((long)startPos);

            int offset;
            while(!this.downloader.getExit() && (offset = inStream.read(buffer, 0, 10240)) != -1) {
               threadfile.write(buffer, 0, offset);
               this.downLength += offset;
               this.downloader.update(this.threadId, this.downLength);
               this.downloader.append(offset);
            }

            threadfile.close();
            inStream.close();
            print("Thread " + this.threadId + " download finish");
            this.finish = true;
         } catch (Exception var8) {
            this.downLength = -1;
            print("Thread " + this.threadId + ":" + var8);
         }
      }

   }

   private static void print(String msg) {
      Log.i("DownloadThread", msg);
   }

   public boolean isFinish() {
      return this.finish;
   }

   public long getDownLength() {
      return (long)this.downLength;
   }
}

package com.ubtech.alpha2.core.utils.download2.netdownload;

import android.util.Log;
import com.ubtech.alpha2.core.utils.download2.manager.DownloadTaskManager2;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class DownloadThread2 extends Thread {
   private FileDownloader2 bean = null;
   private InputStream is;
   private int length;
   private FileOutputStream os;
   private HttpURLConnection conn;
   private boolean isCancel;
   private DownloadThread2.CancelTask mCancelTask;

   public synchronized boolean isCancel() {
      return this.isCancel;
   }

   public synchronized void setRunning(boolean isCancel) {
      this.isCancel = isCancel;
   }

   public synchronized void setCancel() {
      this.isCancel = true;
      this.mCancelTask.start();
   }

   public DownloadThread2(FileDownloader2 bean) {
      this.bean = bean;
      this.mCancelTask = new DownloadThread2.CancelTask();
   }

   public void run() {
      boolean var29 = false;

      label334: {
         label335: {
            try {
               var29 = true;
               URL imageUrl = new URL(this.bean.getDownUrl());
               SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss:SSS  ");
               Date curDate = new Date(System.currentTimeMillis());
               String Time = formatter.format(curDate);
               Log.i("zdy", "begin = " + Time);
               this.os = new FileOutputStream(this.bean.getFileSaveDir());
               this.conn = (HttpURLConnection)imageUrl.openConnection();
               this.conn.setConnectTimeout(30000);
               this.conn.setReadTimeout(30000);
               this.conn.setInstanceFollowRedirects(true);
               this.conn.setRequestProperty("Accept-Encoding", "identity");
               this.conn.setRequestMethod("GET");
               this.conn.connect();
               if (this.conn.getResponseCode() != 200) {
                  var29 = false;
               } else {
                  int fileSize = this.conn.getContentLength();
                  if (fileSize <= 0) {
                     this.bean.getListener().onDowanFailed(this.bean.getRequestCode(), -4);
                     Log.i("zdy", "--->文件大小错误");
                  }

                  this.is = this.conn.getInputStream();
                  int buffer_size = 102400;
                  this.length = 0;
                  int proress = 0;
                  byte[] bytes = new byte[102400];
                  boolean var9 = false;

                  int count;
                  while(!this.isCancel() && (count = this.is.read(bytes, 0, 102400)) != -1) {
                     this.length += count;
                     this.os.write(bytes, 0, count);
                     proress = (int)((float)this.length / (float)fileSize * 100.0F);
                     this.bean.getListener().onProgrerss(this.bean.getRequestCode(), "" + proress);
                  }

                  this.setRunning(true);
                  Date curDate2 = new Date(System.currentTimeMillis());
                  String Time2 = formatter.format(curDate2);
                  Log.i("zdy", "end = " + Time2);
                  if (this.length == fileSize) {
                     Log.i("zdy", "完成");
                     this.bean.getListener().onDownloadOver(this.bean.getRequestCode());
                  }

                  Log.i("zdy", "线程结束");
                  var29 = false;
               }
               break label334;
            } catch (MalformedURLException var38) {
               var38.printStackTrace();
               this.bean.getListener().onDowanFailed(this.bean.getRequestCode(), -1);
               Log.i("zdy", "url 格式错误");
               var29 = false;
               break label335;
            } catch (Exception var39) {
               var39.printStackTrace();
               if (this.isCancel) {
                  this.bean.getListener().onDowanFailed(this.bean.getRequestCode(), -3);
                  Log.i("zdy", "无网络连接+手动取消");
                  var29 = false;
               } else {
                  this.bean.getListener().onDowanFailed(this.bean.getRequestCode(), -2);
                  Log.i("zdy", "网络连接失败" + var39.toString());
                  var29 = false;
               }
            } finally {
               if (var29) {
                  DownloadTaskManager2.getInstance().removeDownload(this.bean.getRequestCode());

                  try {
                     if (this.is != null) {
                        this.is.close();
                        this.is = null;
                     }

                     if (this.os != null) {
                        this.os.close();
                        this.os = null;
                     }
                  } catch (IOException var31) {
                     var31.printStackTrace();
                  }

                  if (this.conn != null && !this.isCancel) {
                     synchronized(this.conn) {
                        this.conn.disconnect();
                        this.conn = null;
                     }
                  }

                  Log.i("zdy", "finally conn.disconnect()");
               }
            }

            DownloadTaskManager2.getInstance().removeDownload(this.bean.getRequestCode());

            try {
               if (this.is != null) {
                  this.is.close();
                  this.is = null;
               }

               if (this.os != null) {
                  this.os.close();
                  this.os = null;
               }
            } catch (IOException var33) {
               var33.printStackTrace();
            }

            if (this.conn != null && !this.isCancel) {
               synchronized(this.conn) {
                  this.conn.disconnect();
                  this.conn = null;
               }
            }

            Log.i("zdy", "finally conn.disconnect()");
            return;
         }

         DownloadTaskManager2.getInstance().removeDownload(this.bean.getRequestCode());

         try {
            if (this.is != null) {
               this.is.close();
               this.is = null;
            }

            if (this.os != null) {
               this.os.close();
               this.os = null;
            }
         } catch (IOException var35) {
            var35.printStackTrace();
         }

         if (this.conn != null && !this.isCancel) {
            synchronized(this.conn) {
               this.conn.disconnect();
               this.conn = null;
            }
         }

         Log.i("zdy", "finally conn.disconnect()");
         return;
      }

      DownloadTaskManager2.getInstance().removeDownload(this.bean.getRequestCode());

      try {
         if (this.is != null) {
            this.is.close();
            this.is = null;
         }

         if (this.os != null) {
            this.os.close();
            this.os = null;
         }
      } catch (IOException var37) {
         var37.printStackTrace();
      }

      if (this.conn != null && !this.isCancel) {
         synchronized(this.conn) {
            this.conn.disconnect();
            this.conn = null;
         }
      }

      Log.i("zdy", "finally conn.disconnect()");
   }

   public class CancelTask extends Thread {
      public CancelTask() {
      }

      public void run() {
         Log.i("zdy", "CancelTask conn.disconnect()");
         if (DownloadThread2.this.conn != null) {
            DownloadThread2.this.conn.disconnect();
            DownloadThread2.this.conn = null;
            Log.i("zdy", " conn != null CancelTask conn.disconnect()");
         }

      }
   }
}

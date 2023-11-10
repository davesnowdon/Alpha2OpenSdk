package com.ubtech.alpha2.core.utils.download.manager;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import com.ubtech.alpha2.core.model.response.ApkConfigResponse;
import com.ubtech.alpha2.core.utils.CommonUtils;
import com.ubtech.alpha2.core.utils.WebServerConstants;
import com.ubtech.alpha2.core.utils.download.netdownload.DownLoadOverListener;
import com.ubtech.alpha2.core.utils.download.netdownload.DownloadProgressListener;
import com.ubtech.alpha2.core.utils.download.netdownload.FileDownloader;
import java.io.File;

public class DownLoadManager {
   private Handler handler;
   private Context context;
   private DownLoadManager.DownLoadProgressBar progressBar;
   private DownLoadManager.DownloadTask task;
   private final String TAG = "zdy";
   DownLoadOverListener mDownLoadOverListener;

   public DownLoadManager(Context context) {
      this.context = context;
      this.createHandler();
   }

   public void createHandler() {
      Looper looper;
      if ((looper = Looper.myLooper()) != null) {
         this.handler = new DownLoadManager.UIHander(looper);
      } else if ((looper = Looper.getMainLooper()) != null) {
         this.handler = new DownLoadManager.UIHander(looper);
      } else {
         this.handler = null;
      }

   }

   public boolean compareVersion(ApkConfigResponse apkConfig) {
      String packageName = this.context.getPackageName();
      PackageInfo info = null;

      try {
         info = this.context.getPackageManager().getPackageInfo(packageName, 0);
         String verLocal = info.versionName;
         String verServer = apkConfig.getApkVersion();
         if (verLocal.compareTo(verServer) < 0) {
            Log.i("zdy", "verLocal=" + verLocal + " verServer=" + verServer);
            return true;
         }
      } catch (NameNotFoundException var6) {
         var6.printStackTrace();
      }

      return false;
   }

   public void setDownLoadOverListener(DownLoadOverListener mDownLoadOverListener) {
      this.mDownLoadOverListener = mDownLoadOverListener;
   }

   public void onStartDownload(ApkConfigResponse apkConfig, String fileName) {
      this.progressBar = new DownLoadManager.DownLoadProgressBar();
      String url = "http://192.168.213.94:8080/ssh/" + apkConfig.getUploadFilePath();
      Log.e("zdy", "url : " + url);
      this.startDownload(url, fileName);
   }

   public void startDownLoadFile(String url, String fileName, WebServerConstants.Product type, String mac) {
      this.progressBar = new DownLoadManager.DownLoadProgressBar();
      this.startDownload(url, fileName, type, mac);
   }

   public void startDownLoadFile(String url, String fileName, WebServerConstants.Product type) {
      this.progressBar = new DownLoadManager.DownLoadProgressBar();
      this.startDownload(url, fileName, type, "");
   }

   private void startDownload(String url, String fileName, WebServerConstants.Product type, String mac) {
      if (CommonUtils.getNetworkType(this.context) != 1) {
         this.mDownLoadOverListener.onDowanFailed(-200);
      } else {
         if (Environment.getExternalStorageState().equals("mounted")) {
            if (!mac.equals("")) {
               if (mac.contains(":")) {
                  mac = mac.replaceAll(":", "_");
               }

               mac = "/" + mac;
            }

            File saveDir = new File(WebServerConstants.getSavePath(type) + mac);
            this.dowload(url, saveDir, fileName);
         } else {
            Log.i("zdy", "SDCard 不存在");
         }

      }
   }

   private void startDownload(String path, String fileName) {
      if (Environment.getExternalStorageState().equals("mounted")) {
         File saveDir = new File(WebServerConstants.LOCAL_APK_PATH);
         this.dowload(path, saveDir, fileName);
      } else {
         Log.i("zdy", "SDCard 不存在");
      }

   }

   public void stopDownLoad() {
      this.exit();
   }

   public void exit() {
      if (this.task != null) {
         this.task.exit();
      }

   }

   private void dowload(String path, File saveDir, String fileName) {
      if (!saveDir.exists()) {
         saveDir.mkdirs();
      }

      this.task = new DownLoadManager.DownloadTask(path, saveDir, fileName);
      (new Thread(this.task)).start();
   }

   public class DownLoadProgressBar {
      int progress;
      int max;

      public DownLoadProgressBar() {
      }

      public int getProgress() {
         return this.progress;
      }

      public void setProgress(int progress) {
         this.progress = progress;
      }

      public int getMax() {
         return this.max;
      }

      public void setMax(int max) {
         this.max = max;
      }
   }

   private class UIHander extends Handler {
      public UIHander(Looper looper) {
         super(looper);
      }

      public void handleMessage(Message msg) {
         switch(msg.what) {
         case -1:
            Log.i("zdy", "下载失败");
            DownLoadManager.this.mDownLoadOverListener.onDowanFailed(-200);
            break;
         case 1:
            int size = msg.getData().getInt("size");
            DownLoadManager.this.progressBar.setProgress(size);
            float num = (float)DownLoadManager.this.progressBar.getProgress() / (float)DownLoadManager.this.progressBar.getMax();
            int result = (int)(num * 100.0F);
            Log.i("zdy", "下载进度=" + result + "%");
            if (DownLoadManager.this.progressBar.getProgress() == DownLoadManager.this.progressBar.getMax()) {
               DownLoadManager.this.mDownLoadOverListener.onDownloadOver();
            } else {
               DownLoadManager.this.mDownLoadOverListener.onProgrerss(result + "%");
            }
         }

      }
   }

   private class DownloadTask implements Runnable {
      private String path;
      private File saveDir;
      private FileDownloader loader;
      private String savaFileName;

      public DownloadTask(String path, File saveDir, String fileName) {
         this.path = path;
         this.saveDir = saveDir;
         this.savaFileName = fileName;
      }

      public void exit() {
         if (this.loader != null) {
            this.loader.exit();
         }

      }

      public void run() {
         try {
            this.loader = new FileDownloader(DownLoadManager.this.context, this.path, this.saveDir, 1, this.savaFileName);
            DownLoadManager.this.progressBar.setMax(this.loader.getFileSize());
            this.loader.download(new DownloadProgressListener() {
               public void onDownloadSize(int size) {
                  if (size == -1) {
                     Log.i("zdy", "下载失败");
                     DownLoadManager.this.mDownLoadOverListener.onDowanFailed(-200);
                  } else {
                     Message msg = new Message();
                     msg.what = 1;
                     msg.getData().putInt("size", size);
                     DownLoadManager.this.handler.sendMessage(msg);
                  }
               }
            });
         } catch (Exception var2) {
            var2.printStackTrace();
            DownLoadManager.this.handler.sendMessage(DownLoadManager.this.handler.obtainMessage(-1));
         }

      }
   }
}

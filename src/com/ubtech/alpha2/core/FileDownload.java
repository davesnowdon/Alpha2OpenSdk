package com.ubtech.alpha2.core;

import android.content.Context;
import android.util.Log;
import com.ubtech.alpha2.FilePath;
import com.ubtech.alpha2.core.network.async.AsyncDownloadTaskManager;
import com.ubtech.alpha2.core.network.async.DownLoadBen;
import com.ubtech.alpha2.core.network.async.OnDownLoadListener;

public class FileDownload implements OnDownLoadListener {
   private AsyncDownloadTaskManager mAsyncTaskManager;

   public FileDownload(Context mContext) {
      this.mAsyncTaskManager = AsyncDownloadTaskManager.getInstance(mContext);
   }

   public void download(String downUrl) {
      this.mAsyncTaskManager.download(downUrl, this, FilePath.appPath);
   }

   public void download(String requsetCode, String downUrl) {
      this.mAsyncTaskManager.download(requsetCode, this, FilePath.appPath);
   }

   public void cancelRequest(String requsetCode) {
      this.mAsyncTaskManager.cancelRequest(requsetCode);
   }

   public void cancelRequest() {
      this.mAsyncTaskManager.cancelRequest();
   }

   public void onProgress(int progress) {
      Log.i("zdy", "onProgress " + progress);
   }

   public void onSuccess(String requestCode, DownLoadBen result) {
      Log.i("zdy", "onSuccess = " + result.getAbsolutePath());
   }

   public void onFailure(String requestCode, int state, DownLoadBen result) {
      switch(state) {
      case -400:
         Log.i("zdy", "-400");
         break;
      case -200:
         Log.i("zdy", "-200");
      }

   }
}

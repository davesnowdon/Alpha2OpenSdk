package com.ubtech.alpha2.core.network.async;

import android.content.Context;
import android.os.Build.VERSION;
import com.ubtech.alpha2.core.utils.NLog;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncDownloadTaskManager {
   private final String tag = AsyncDownloadTaskManager.class.getSimpleName();
   public static final int REQUEST_SUCCESS_CODE = 200;
   public static final int REQUEST_ERROR_CODE = -999;
   public static final int HTTP_ERROR_CODE = -200;
   public static final int HTTP_NULL_CODE = -400;
   private Context mContext;
   private static AsyncDownloadTaskManager instance;
   private static ExecutorService mExecutorService;
   private static Map<String, WeakReference<BaseAsyncDownloadTask>> requestMap;

   private AsyncDownloadTaskManager(Context context) {
      this.mContext = context;
      mExecutorService = Executors.newFixedThreadPool(10);
      requestMap = new WeakHashMap();
   }

   public static AsyncDownloadTaskManager getInstance(Context context) {
      if (instance == null) {
         Class var1 = AsyncDownloadTaskManager.class;
         synchronized(AsyncDownloadTaskManager.class) {
            if (instance == null) {
               instance = new AsyncDownloadTaskManager(context);
            }
         }
      }

      return instance;
   }

   public void download(String downUrl, OnDownLoadListener listener, String appPath) {
      this.downloadRequest(downUrl, true, downUrl, listener, false, appPath);
   }

   public void download(String requestCode, String downUrl, OnDownLoadListener listener, String appPath) {
      this.downloadRequest(requestCode, true, downUrl, listener, false, appPath);
   }

   public synchronized void downloadRequest(String requestCode, boolean downflag, String downUrl, OnDownLoadListener listener, boolean isLocalNetWork, String appPath) {
      if (this.isExist(requestCode)) {
         NLog.e(this.tag, "the url is exist");
      } else {
         DownLoadBen bean = new DownLoadBen(requestCode, downflag, downUrl, listener, appPath);
         if (requestCode != null && !requestCode.equals("")) {
            BaseAsyncDownloadTask mAsynctask = new BaseAsyncDownloadTask(bean, this.mContext, isLocalNetWork);
            if (VERSION.SDK_INT >= 11) {
               mAsynctask.executeOnExecutor(mExecutorService, new Object[0]);
            } else {
               mAsynctask.execute(new Object[0]);
            }

            requestMap.put(downUrl, new WeakReference(mAsynctask));
         } else {
            NLog.e(this.tag, "the error is requestCode < 0");
         }

      }
   }

   public synchronized void cancelRequest(String requestCode) {
      WeakReference<BaseAsyncDownloadTask> requestTask = (WeakReference)requestMap.get(requestCode);
      if (requestTask != null) {
         BaseAsyncDownloadTask request = (BaseAsyncDownloadTask)requestTask.get();
         if (request != null) {
            request.cancel(true);
            request = null;
         }
      }

      requestMap.remove(requestCode);
   }

   public boolean isExist(String code) {
      boolean isExist = false;
      if (requestMap != null) {
         Iterator it = requestMap.entrySet().iterator();

         while(it.hasNext()) {
            Entry<String, WeakReference<BaseAsyncDownloadTask>> entry = (Entry)it.next();
            String requestCode = (String)entry.getKey();
            if (code.equals(requestCode)) {
               isExist = true;
            }
         }
      }

      return isExist;
   }

   public void cancelRequest() {
      if (requestMap != null) {
         Iterator it = requestMap.entrySet().iterator();

         while(it.hasNext()) {
            Entry<String, WeakReference<BaseAsyncDownloadTask>> entry = (Entry)it.next();
            String requestCode = (String)entry.getKey();
            this.cancelRequest(requestCode);
         }

         requestMap.clear();
      }

   }
}

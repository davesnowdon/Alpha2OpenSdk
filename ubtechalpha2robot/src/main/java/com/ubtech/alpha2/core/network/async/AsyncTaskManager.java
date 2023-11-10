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

public class AsyncTaskManager {
   private final String tag = AsyncTaskManager.class.getSimpleName();
   public static final int REQUEST_SUCCESS_CODE = 200;
   public static final int REQUEST_ERROR_CODE = -999;
   public static final int HTTP_ERROR_CODE = -200;
   public static final int HTTP_NULL_CODE = -400;
   private Context mContext;
   private static AsyncTaskManager instance;
   private static ExecutorService mExecutorService;
   private static Map<Integer, WeakReference<BaseAsyncTask>> requestMap;

   private AsyncTaskManager(Context context) {
      this.mContext = context;
      mExecutorService = Executors.newFixedThreadPool(10);
      requestMap = new WeakHashMap();
   }

   public static AsyncTaskManager getInstance(Context context) {
      if (instance == null) {
         Class var1 = AsyncTaskManager.class;
         synchronized(AsyncTaskManager.class) {
            if (instance == null) {
               instance = new AsyncTaskManager(context);
            }
         }
      }

      return instance;
   }

   public void download(String downUrl, OnDataListener listener) {
      this.request(1, true, downUrl, listener, false);
   }

   public void download(int requestCode, String downUrl, OnDataListener listener) {
      this.request(requestCode, true, downUrl, listener, false);
   }

   public void request(int requestCode, OnDataListener listener, boolean isLocalNetWork) {
      this.request(requestCode, false, (String)null, listener, isLocalNetWork);
   }

   public void request(int requestCode, boolean downflag, String downUrl, OnDataListener listener, boolean isLocalNetWork) {
      DownLoad bean = new DownLoad(requestCode, downflag, downUrl, listener);
      if (requestCode > 0) {
         BaseAsyncTask mAsynctask = new BaseAsyncTask(bean, this.mContext, isLocalNetWork);
         if (VERSION.SDK_INT >= 11) {
            mAsynctask.executeOnExecutor(mExecutorService, new Object[0]);
         } else {
            mAsynctask.execute(new Object[0]);
         }

         requestMap.put(requestCode, new WeakReference(mAsynctask));
      } else {
         NLog.e(this.tag, "the error is requestCode < 0");
      }

   }

   public void cancelRequest(int requestCode) {
      WeakReference<BaseAsyncTask> requestTask = (WeakReference)requestMap.get(requestCode);
      if (requestTask != null) {
         BaseAsyncTask request = (BaseAsyncTask)requestTask.get();
         if (request != null) {
            request.cancel(true);
            request = null;
         }
      }

      requestMap.remove(requestCode);
   }

   public void cancelRequest() {
      if (requestMap != null) {
         Iterator it = requestMap.entrySet().iterator();

         while(it.hasNext()) {
            Entry<Integer, WeakReference<BaseAsyncTask>> entry = (Entry)it.next();
            Integer requestCode = (Integer)entry.getKey();
            this.cancelRequest(requestCode);
         }

         requestMap.clear();
      }

   }
}

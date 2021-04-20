package com.ubtechinc.alpha2ctrlapp.network.async;

import android.content.Context;
import android.os.AsyncTask.Status;
import android.os.Build.VERSION;
import android.util.Log;
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
   public static final int DEFAULT_DOWNLOAD_CODE = 10000;
   public static final int JSONMAPPING_ERROR_CODE = -300;
   public final int MAX_CONNECTIONS_NUM = 10;
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

   public void request(int requestCode, OnDataListener listener) {
      this.request(requestCode, true, listener);
   }

   public void request(int requestCode, boolean isCheckNetwork, OnDataListener listener) {
      DownLoad bean = new DownLoad(requestCode, isCheckNetwork, listener);
      if (requestCode > 0) {
         BaseAsyncTask mAsynctask = new BaseAsyncTask(bean, this.mContext);
         if (VERSION.SDK_INT >= 11) {
            mAsynctask.executeOnExecutor(mExecutorService, new Object[0]);
         } else {
            mAsynctask.execute(new Object[0]);
         }

         requestMap.put(requestCode, new WeakReference(mAsynctask));
      } else {
         Log.e(this.tag, "the error is requestCode < 0");
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

   public Status getRequestState(int requestCode) {
      WeakReference<BaseAsyncTask> requestTask = (WeakReference)requestMap.get(requestCode);
      if (requestTask != null) {
         BaseAsyncTask request = (BaseAsyncTask)requestTask.get();
         if (request != null) {
            return request.getStatus();
         }
      }

      return null;
   }
}

package com.ubtech.alpha2.core.network.async;

import android.content.Context;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import com.ubtech.alpha2.core.network.http.HttpClientManager;
import com.ubtech.alpha2.core.utils.CommonUtils;
import com.ubtech.alpha2.core.utils.NLog;
import org.apache.http.HttpException;

public class BaseAsyncDownloadTask extends AsyncTask<Object, Integer, Object> {
   private final String tag = BaseAsyncDownloadTask.class.getSimpleName();
   private DownLoadBen bean = null;
   private Context mContext;
   private boolean isLocalNetWork;

   public BaseAsyncDownloadTask(DownLoadBen bean, Context context, boolean isLocalNetWork) {
      this.isLocalNetWork = isLocalNetWork;
      this.bean = bean;
      this.mContext = context;
   }

   protected Object doInBackground(Object... params) {
      NLog.e(this.tag, "doInBackground" + CommonUtils.isNetworkConnected(this.mContext));

      try {
         if (this.bean.getListener() == null) {
            throw new HttpException("============listener is not null============");
         }

         if (!this.isLocalNetWork && !CommonUtils.isNetworkConnected(this.mContext)) {
            this.bean.setState(-400);
         } else {
            NLog.e(this.tag, "doInBackground");
            if (this.bean.isDownflag()) {
               if (TextUtils.isEmpty(this.bean.getDownUrl())) {
                  throw new HttpException("============downUrl is not null============");
               }

               this.bean = HttpClientManager.getInstance(this.mContext).download(this.bean, this);
               this.bean.setState(200);
               this.bean.setResult(true);
            } else {
               this.bean.setState(-400);
            }
         }
      } catch (Exception var3) {
         var3.printStackTrace();
         if (var3 instanceof HttpException) {
            this.bean.setState(-200);
         } else {
            this.bean.setState(-999);
         }

         this.bean.setResult(false);
         return this.bean;
      }

      return this.bean;
   }

   public void onProgressUpdate(Integer... progress) {
      if (progress != null) {
         this.bean.getListener().onProgress(progress[0]);
      }

   }

   protected void onPostExecute(Object result) {
      DownLoadBen bean = (DownLoadBen)result;
      switch(bean.getState()) {
      case -999:
      case -400:
         Log.d("", "!!!!!!!! download success");
         bean.getListener().onFailure(bean.getRequestCode(), bean.getState(), bean);
         break;
      case 200:
         Log.d("", "!!!!!!!! download success");
         bean.getListener().onSuccess(bean.getRequestCode(), bean);
      }

   }
}

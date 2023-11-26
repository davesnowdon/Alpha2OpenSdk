package com.ubtech.alpha2.core.network.async;

import android.content.Context;
import android.os.AsyncTask;
import com.ubtech.alpha2.core.utils.CommonUtils;
import com.ubtech.alpha2.core.utils.NLog;
import org.apache.http.HttpException;

public class BaseAsyncTask extends AsyncTask<Object, Integer, Object> {
   private final String tag = BaseAsyncTask.class.getSimpleName();
   private DownLoad bean = null;
   private Context mContext;
   private boolean isLocalNetWork;

   public BaseAsyncTask(DownLoad bean, Context context, boolean isLocalNetWork) {
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
            Object result = this.bean.getListener().doInBackground(this.bean.getRequestCode());
            this.bean.setState(200);
            this.bean.setResult(result);
         }
      } catch (Exception var3) {
         var3.printStackTrace();
         if (var3 instanceof HttpException) {
            this.bean.setState(-200);
         } else {
            this.bean.setState(-999);
         }

         this.bean.setResult(var3);
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
      DownLoad bean = (DownLoad)result;
      switch(bean.getState()) {
      case -999:
      case -400:
         bean.getListener().onFailure(bean.getRequestCode(), bean.getState(), bean.getResult());
         break;
      case 200:
         bean.getListener().onSuccess(bean.getRequestCode(), bean.getResult());
      }

   }
}

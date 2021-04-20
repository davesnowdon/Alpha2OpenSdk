package com.ubtechinc.alpha2ctrlapp.network.async;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import org.apache.http.HttpException;

public class BaseAsyncTask extends AsyncTask<Object, Integer, Object> {
   private final String tag = BaseAsyncTask.class.getSimpleName();
   private DownLoad bean = null;
   private Context mContext;

   public BaseAsyncTask(DownLoad bean, Context context) {
      this.bean = bean;
      this.mContext = context;
   }

   public boolean isNetworkConnected(Context context, boolean isCheckNetwork) {
      if (!isCheckNetwork) {
         return true;
      } else {
         ConnectivityManager cm = (ConnectivityManager)context.getSystemService("connectivity");
         NetworkInfo ni = cm.getActiveNetworkInfo();
         return ni != null && ni.isConnectedOrConnecting();
      }
   }

   protected void onPreExecute() {
   }

   protected Object doInBackground(Object... params) {
      try {
         if (this.bean.getListener() == null) {
            throw new HttpException("BaseAsyncTask listener is not null.");
         }

         if (this.isNetworkConnected(this.mContext, this.bean.isCheckNetwork)) {
            Object result = this.bean.getListener().doInBackground(this.bean.getRequestCode());
            this.bean.setState(200);
            this.bean.setResult(result);
         } else {
            this.bean.setState(-400);
         }
      } catch (Exception var3) {
         var3.printStackTrace();
         if (var3 instanceof HttpException) {
            if (String.valueOf(-300).equals(var3.getMessage())) {
               this.bean.setState(-300);
            } else {
               this.bean.setState(-200);
            }
         } else if (String.valueOf(-300).equals(var3.getMessage())) {
            this.bean.setState(-300);
         } else {
            this.bean.setState(-999);
         }

         this.bean.setResult(var3);
         return this.bean;
      }

      return this.bean;
   }

   protected void onPostExecute(Object result) {
      DownLoad bean = (DownLoad)result;
      switch(bean.getState()) {
      case -999:
         bean.getListener().onFailure(bean.getRequestCode(), bean.getState(), bean.getResult());
      case -400:
         bean.getListener().onFailure(bean.getRequestCode(), bean.getState(), bean.getResult());
      case -200:
         bean.getListener().onFailure(bean.getRequestCode(), bean.getState(), bean.getResult());
         break;
      case 200:
         bean.getListener().onSuccess(bean.getRequestCode(), bean.getResult());
         break;
      default:
         bean.getListener().onFailure(bean.getRequestCode(), bean.getState(), bean.getResult());
      }

   }
}

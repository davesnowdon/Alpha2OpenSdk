package com.ubtech.alpha2.core.base;

import android.content.Context;
import com.ubtech.alpha2.core.network.http.RequestParams;

public abstract class BaseAction {
   protected Context mContext;
   protected int pageSize = 20;
   protected RequestParams params;

   public BaseAction(Context mContext) {
      this.mContext = mContext;
   }

   protected String getURL(String url) {
      return this.getURL(url);
   }

   protected String getURL(String url, String... params) {
      StringBuilder urlBilder = (new StringBuilder("http://www.jingdl.com")).append(url);
      if (params != null) {
         String[] var4 = params;
         int var5 = params.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            String param = var4[var6];
            if (!urlBilder.toString().endsWith("/")) {
               urlBilder.append("/");
            }

            urlBilder.append(param);
         }
      }

      return urlBilder.toString();
   }

   protected RequestParams getRequestParams() {
      this.params = new RequestParams();
      return this.params;
   }
}

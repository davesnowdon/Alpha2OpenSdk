package com.ubtech.alpha2.core.business;

import android.content.Context;
import android.text.TextUtils;
import com.ubtech.alpha2.core.base.BaseManager;
import com.ubtech.alpha2.core.model.response.ApkConfigResponse;
import com.ubtech.alpha2.core.model.response.OrderXMLResponse;
import com.ubtech.alpha2.core.network.http.HttpException;
import com.ubtech.alpha2.core.network.http.RequestParams;
import com.ubtech.alpha2.core.utils.CacheManager;

public class DemoManager extends BaseManager {
   public DemoManager(Context context, int parseType) {
      super(context, parseType);
   }

   public DemoManager(Context context) {
      super(context);
   }

   public OrderXMLResponse getOrderXmlDemo(String url) throws HttpException {
      OrderXMLResponse response = null;
      String key = String.valueOf(url.hashCode());
      if (CacheManager.isInvalidObject(key, 1800L)) {
         response = (OrderXMLResponse)CacheManager.readObject(key);
         if (response != null) {
            return response;
         }
      }

      String result = this.httpManager.get(this.mContext, url);
      if (!TextUtils.isEmpty(result)) {
         response = (OrderXMLResponse)this.xmlToBean(result, OrderXMLResponse.class);
         if (response != null) {
            CacheManager.writeObject(response, key);
         }
      }

      return response;
   }

   public ApkConfigResponse checkMaxVersion(String url, RequestParams params) throws HttpException {
      ApkConfigResponse response = null;
      String json = this.httpManager.post(url, params);
      if (!TextUtils.isEmpty(json)) {
         response = (ApkConfigResponse)this.jsonToBean(json, ApkConfigResponse.class);
      }

      return response;
   }
}

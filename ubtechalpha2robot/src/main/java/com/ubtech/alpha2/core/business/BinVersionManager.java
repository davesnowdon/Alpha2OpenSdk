package com.ubtech.alpha2.core.business;

import android.content.Context;
import android.text.TextUtils;
import com.ubtech.alpha2.core.base.BaseManager;
import com.ubtech.alpha2.core.model.response.BinVersionResponse;
import com.ubtech.alpha2.core.network.http.HttpClientManager;
import com.ubtech.alpha2.core.network.http.HttpException;
import com.ubtech.alpha2.core.utils.CacheManager;
import java.io.FileInputStream;

public class BinVersionManager extends BaseManager {
   public BinVersionManager(Context context) {
      super(context);
   }

   public BinVersionManager(Context context, int parseType) {
      super(context, parseType);
   }

   public BinVersionResponse getBinVersionXML(String url) throws HttpException {
      BinVersionResponse response = null;
      String key = String.valueOf(url.hashCode());
      if (CacheManager.isInvalidObject(key, 1800L)) {
         response = (BinVersionResponse)CacheManager.readObject(key);
         if (response != null) {
            return response;
         }
      }

      String result = this.readFile(url);
      if (!TextUtils.isEmpty(result)) {
         response = (BinVersionResponse)this.xmlToBean(result, BinVersionResponse.class);
         if (response != null) {
            CacheManager.writeObject(response, key);
         }
      }

      return response;
   }

   public String readFile(String url) {
      String xmlString = null;

      try {
         FileInputStream fin = new FileInputStream(url);
         HttpClientManager var10000 = this.httpManager;
         xmlString = HttpClientManager.inputSteamToString(fin);
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      return xmlString;
   }
}

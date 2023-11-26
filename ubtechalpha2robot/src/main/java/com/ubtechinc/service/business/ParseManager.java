package com.ubtechinc.service.business;

import android.text.TextUtils;
import android.util.Log;
import com.ubtechinc.service.base.BaseManager;
import com.ubtechinc.service.protocols.ActionProfileXMLResponse;
import com.ubtechinc.utils.FileUtils;
import java.io.IOException;
import org.apache.http.HttpException;

public class ParseManager extends BaseManager {
   public ParseManager(int parseType) {
   }

   public ParseManager() {
   }

   public ActionProfileXMLResponse getXmlDemo(String url) throws HttpException {
      ActionProfileXMLResponse response = null;
      String result = null;

      try {
         result = FileUtils.readFromProfile(url);
      } catch (IOException var5) {
         var5.printStackTrace();
      }

      if (!TextUtils.isEmpty(result)) {
         response = (ActionProfileXMLResponse)this.xmlToBean(result, ActionProfileXMLResponse.class);
         Log.e("zdy", response.toString());
      }

      return response;
   }

   public boolean setXmlDemo(String url, ActionProfileXMLResponse response) throws HttpException {
      boolean isSuccess = false;
      String xmls = this.beanToXml(response, ActionProfileXMLResponse.class);

      try {
         FileUtils.writeToProfile(url, xmls);
         isSuccess = true;
      } catch (IOException var6) {
         var6.printStackTrace();
      }

      Log.e("zdy", xmls);
      return isSuccess;
   }
}

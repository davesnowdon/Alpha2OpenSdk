package com.ubtechinc.service.action;

import com.ubtechinc.service.business.ParseManager;
import com.ubtechinc.service.protocols.ActionProfileXMLResponse;
import org.apache.http.HttpException;

public class ParseAction {
   public ParseAction() {
   }

   public ActionProfileXMLResponse getXmlDemo(String url) {
      ParseManager mgr = new ParseManager();
      ActionProfileXMLResponse bean = null;

      try {
         bean = mgr.getXmlDemo(url);
      } catch (HttpException var5) {
         var5.printStackTrace();
      }

      return bean;
   }

   public boolean setXmlDemo(String url, ActionProfileXMLResponse response) {
      ParseManager mgr = new ParseManager();
      boolean isSuccess = false;

      try {
         isSuccess = mgr.setXmlDemo(url, response);
      } catch (HttpException var6) {
         var6.printStackTrace();
      }

      return isSuccess;
   }
}

package com.ubtechinc.alpha2ctrlapp.network.model.request;

public class ActionNameRequest extends CommonRequest {
   private String actionOriginalIds;
   private String systemLanguage;

   public ActionNameRequest() {
   }

   public String getSystemLanguage() {
      return this.systemLanguage;
   }

   public void setSystemLanguage(String systemLanguage) {
      this.systemLanguage = systemLanguage;
   }

   public String getActionOriginalIds() {
      return this.actionOriginalIds;
   }

   public void setActionOriginalIds(String actionOriginalIds) {
      this.actionOriginalIds = actionOriginalIds;
   }
}

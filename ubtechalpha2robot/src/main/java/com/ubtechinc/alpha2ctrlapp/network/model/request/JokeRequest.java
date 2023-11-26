package com.ubtechinc.alpha2ctrlapp.network.model.request;

public class JokeRequest extends CommonRequest {
   private String systemLanguage;

   public JokeRequest() {
   }

   public String getSystemLanguage() {
      return this.systemLanguage;
   }

   public void setSystemLanguage(String systemLanguage) {
      this.systemLanguage = systemLanguage;
   }
}

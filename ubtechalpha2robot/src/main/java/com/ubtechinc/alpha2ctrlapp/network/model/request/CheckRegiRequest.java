package com.ubtechinc.alpha2ctrlapp.network.model.request;

public class CheckRegiRequest extends CommonRequest {
   private String userName;

   public CheckRegiRequest() {
   }

   public String getUserName() {
      return this.userName;
   }

   public void setUserName(String userName) {
      this.userName = userName;
   }
}

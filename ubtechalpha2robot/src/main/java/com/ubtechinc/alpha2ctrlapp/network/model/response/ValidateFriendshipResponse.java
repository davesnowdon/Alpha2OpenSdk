package com.ubtechinc.alpha2ctrlapp.network.model.response;

public class ValidateFriendshipResponse {
   private boolean status;
   private String info;
   private ValidateFriendshipModel[] models;

   public ValidateFriendshipResponse() {
   }

   public boolean isStatus() {
      return this.status;
   }

   public void setStatus(boolean status) {
      this.status = status;
   }

   public String getInfo() {
      return this.info;
   }

   public void setInfo(String info) {
      this.info = info;
   }

   public ValidateFriendshipModel[] getModels() {
      return this.models;
   }

   public void setModels(ValidateFriendshipModel[] models) {
      this.models = models;
   }
}

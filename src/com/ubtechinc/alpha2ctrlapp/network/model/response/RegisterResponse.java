package com.ubtechinc.alpha2ctrlapp.network.model.response;

import com.ubtechinc.alpha2ctrlapp.network.model.RegisterResultModel;

public class RegisterResponse {
   private boolean status;
   private String info;
   private RegisterResultModel[] models;

   public RegisterResponse() {
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

   public RegisterResultModel[] getModels() {
      return this.models;
   }

   public void setModels(RegisterResultModel[] models) {
      this.models = models;
   }
}

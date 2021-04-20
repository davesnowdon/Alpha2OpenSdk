package com.ubtechinc.alpha2ctrlapp.network.model.response;

import java.util.List;

public class ActionNameResponse {
   private boolean status;
   private String info;
   private List<ActionNameModel> models;

   public ActionNameResponse() {
   }

   public List<ActionNameModel> getModels() {
      return this.models;
   }

   public void setModels(List<ActionNameModel> models) {
      this.models = models;
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
}

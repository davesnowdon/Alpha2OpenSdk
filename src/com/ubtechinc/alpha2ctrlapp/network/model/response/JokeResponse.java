package com.ubtechinc.alpha2ctrlapp.network.model.response;

import com.ubtechinc.alpha2ctrlapp.network.model.JokeModel;
import java.util.List;

public class JokeResponse {
   private boolean status;
   private String info;
   private List<JokeModel> models;

   public JokeResponse() {
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

   public List<JokeModel> getModels() {
      return this.models;
   }

   public void setModels(List<JokeModel> models) {
      this.models = models;
   }
}

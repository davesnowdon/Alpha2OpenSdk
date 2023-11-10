package com.ubtechinc.alpha2ctrlapp.network.model.response;

import com.ubtechinc.alpha2ctrlapp.network.model.AuthenticationModel;
import java.util.List;

public class AuthenticationResponse {
   private boolean status;
   private String info;
   private List<AuthenticationModel> models;

   public AuthenticationResponse() {
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

   public List<AuthenticationModel> getModels() {
      return this.models;
   }

   public void setModels(List<AuthenticationModel> models) {
      this.models = models;
   }
}

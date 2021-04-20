package com.ubtechinc.alpha2ctrlapp.network.model.request;

public class GetCipherRequest extends CommonRequest {
   private String type;
   private String code;

   public GetCipherRequest() {
   }

   public String getType() {
      return this.type;
   }

   public void setType(String type) {
      this.type = type;
   }

   public String getCode() {
      return this.code;
   }

   public void setCode(String code) {
      this.code = code;
   }
}

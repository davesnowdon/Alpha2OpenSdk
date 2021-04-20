package com.ubtechinc.alpha2ctrlapp.network.model.request;

public class UploadFileRequest extends CommonRequest {
   private String serialNumber;

   public UploadFileRequest() {
   }

   public String getSerialNumber() {
      return this.serialNumber;
   }

   public void setSerialNumber(String serialNumber) {
      this.serialNumber = serialNumber;
   }
}

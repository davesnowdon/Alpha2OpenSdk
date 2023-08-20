package com.ubtechinc.alpha2ctrlapp.network.model.request;

public class UploadControllerInfoRequest extends CommonRequest {
   private String equipmentId;
   private String controlUserId;

   public UploadControllerInfoRequest() {
   }

   public String getEquipmentId() {
      return this.equipmentId;
   }

   public void setEquipmentId(String equipmentId) {
      this.equipmentId = equipmentId;
   }

   public String getControlUserId() {
      return this.controlUserId;
   }

   public void setControlUserId(String controlUserId) {
      this.controlUserId = controlUserId;
   }
}

package com.ubtechinc.alpha2ctrlapp.network.model.request;

public class VerificationRequest extends CommonRequest {
   private String code;
   private String equipmentId;

   public VerificationRequest() {
   }

   public String getEquipmentId() {
      return this.equipmentId;
   }

   public void setEquipmentId(String equipmentId) {
      this.equipmentId = equipmentId;
   }

   public String getCode() {
      return this.code;
   }

   public void setCode(String code) {
      this.code = code;
   }
}

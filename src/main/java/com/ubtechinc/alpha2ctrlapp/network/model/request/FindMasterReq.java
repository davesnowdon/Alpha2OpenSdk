package com.ubtechinc.alpha2ctrlapp.network.model.request;

public class FindMasterReq extends CommonRequest {
   private String equipmentId;

   public FindMasterReq() {
   }

   public String getEquipmentId() {
      return this.equipmentId;
   }

   public void setEquipmentId(String equipmentId) {
      this.equipmentId = equipmentId;
   }
}

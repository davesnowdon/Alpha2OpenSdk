package com.ubtechinc.alpha2ctrlapp.network.model.request;

public class ValidateFriendship extends CommonRequest {
   private String equipmentId;
   private String userId;
   private String token;
   private int relationStatus;

   public ValidateFriendship() {
   }

   public String getEquipmentId() {
      return this.equipmentId;
   }

   public void setEquipmentId(String equipmentId) {
      this.equipmentId = equipmentId;
   }

   public String getUserId() {
      return this.userId;
   }

   public void setUserId(String userId) {
      this.userId = userId;
   }

   public String getToken() {
      return this.token;
   }

   public void setToken(String token) {
      this.token = token;
   }

   public int getRelationStatus() {
      return this.relationStatus;
   }

   public void setRelationStatus(int relationStatus) {
      this.relationStatus = relationStatus;
   }
}

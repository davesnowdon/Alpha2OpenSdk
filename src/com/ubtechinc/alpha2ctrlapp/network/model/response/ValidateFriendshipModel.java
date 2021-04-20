package com.ubtechinc.alpha2ctrlapp.network.model.response;

public class ValidateFriendshipModel {
   private String userId;
   private String userName;
   private int relationStatus;

   public ValidateFriendshipModel() {
   }

   public String getUserId() {
      return this.userId;
   }

   public void setUserId(String userId) {
      this.userId = userId;
   }

   public String getUserName() {
      return this.userName;
   }

   public void setUserName(String userName) {
      this.userName = userName;
   }

   public int getRelationStatus() {
      return this.relationStatus;
   }

   public void setRelationStatus(int relationStatus) {
      this.relationStatus = relationStatus;
   }
}

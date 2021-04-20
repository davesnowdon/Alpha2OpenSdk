package com.ubtechinc.alpha2ctrlapp.network.model.response;

public class FindMasterModel {
   private String userName;
   private String userEmail;
   private String userPhone;
   private int upUserId;

   public FindMasterModel() {
   }

   public String getUserName() {
      return this.userName;
   }

   public void setUserName(String userName) {
      this.userName = userName;
   }

   public String getUserEmail() {
      return this.userEmail;
   }

   public void setUserEmail(String userEmail) {
      this.userEmail = userEmail;
   }

   public String getUserPhone() {
      return this.userPhone;
   }

   public void setUserPhone(String userPhone) {
      this.userPhone = userPhone;
   }

   public int getUpUserId() {
      return this.upUserId;
   }

   public void setUpUserId(int upUserId) {
      this.upUserId = upUserId;
   }
}

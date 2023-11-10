package com.ubtechinc.alpha2ctrlapp.network.model.request;

public class RegistRequest extends CommonRequest {
   private String userEmail;
   private String userPhone;
   private String userPassword;
   private String userName;
   private int type;
   private int userRoleType;

   public RegistRequest() {
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

   public String getUserPassword() {
      return this.userPassword;
   }

   public void setUserPassword(String userPassword) {
      this.userPassword = userPassword;
   }

   public int getType() {
      return this.type;
   }

   public void setType(int type) {
      this.type = type;
   }

   public int getUserRoleType() {
      return this.userRoleType;
   }

   public void setUserRoleType(int userRoleType) {
      this.userRoleType = userRoleType;
   }

   public String getUserName() {
      return this.userName;
   }

   public void setUserName(String userName) {
      this.userName = userName;
   }
}

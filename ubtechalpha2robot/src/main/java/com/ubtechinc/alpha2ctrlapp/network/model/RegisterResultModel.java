package com.ubtechinc.alpha2ctrlapp.network.model;

public class RegisterResultModel {
   private String token;
   private String appType;
   private String serviceVersion;
   private String userId;
   private String userName;
   private String userPassword;
   private String userEmail;
   private String userPhone;
   private String userStatus;
   private String userOnlyId;
   private String userRoleType;
   private String userGender;
   private String userImage;
   private int type;
   private String tokenExpires;
   private long activeDate;
   private long modifyDate;

   public RegisterResultModel() {
   }

   public String getToken() {
      return this.token;
   }

   public void setToken(String token) {
      this.token = token;
   }

   public String getAppType() {
      return this.appType;
   }

   public void setAppType(String appType) {
      this.appType = appType;
   }

   public String getServiceVersion() {
      return this.serviceVersion;
   }

   public void setServiceVersion(String serviceVersion) {
      this.serviceVersion = serviceVersion;
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

   public String getUserPassword() {
      return this.userPassword;
   }

   public void setUserPassword(String userPassword) {
      this.userPassword = userPassword;
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

   public String getUserStatus() {
      return this.userStatus;
   }

   public void setUserStatus(String userStatus) {
      this.userStatus = userStatus;
   }

   public String getUserOnlyId() {
      return this.userOnlyId;
   }

   public void setUserOnlyId(String userOnlyId) {
      this.userOnlyId = userOnlyId;
   }

   public String getUserRoleType() {
      return this.userRoleType;
   }

   public void setUserRoleType(String userRoleType) {
      this.userRoleType = userRoleType;
   }

   public String getUserGender() {
      return this.userGender;
   }

   public void setUserGender(String userGender) {
      this.userGender = userGender;
   }

   public String getUserImage() {
      return this.userImage;
   }

   public void setUserImage(String userImage) {
      this.userImage = userImage;
   }

   public int getType() {
      return this.type;
   }

   public void setType(int type) {
      this.type = type;
   }

   public String getTokenExpires() {
      return this.tokenExpires;
   }

   public void setTokenExpires(String tokenExpires) {
      this.tokenExpires = tokenExpires;
   }

   public long getActiveDate() {
      return this.activeDate;
   }

   public void setActiveDate(long activeDate) {
      this.activeDate = activeDate;
   }

   public long getModifyDate() {
      return this.modifyDate;
   }

   public void setModifyDate(long modifyDate) {
      this.modifyDate = modifyDate;
   }
}

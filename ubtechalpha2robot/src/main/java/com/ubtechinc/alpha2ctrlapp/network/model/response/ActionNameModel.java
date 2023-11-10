package com.ubtechinc.alpha2ctrlapp.network.model.response;

public class ActionNameModel {
   private int actionId;
   private String actionOriginalId;
   private String systemLanguage;
   private String actionName;
   private String actionLangName;
   private int actionSonType;
   private String actionDesciber;
   private String actionLangDesciber;
   private int actionTime;

   public ActionNameModel() {
   }

   public int getActionTime() {
      return this.actionTime;
   }

   public void setActionTime(int actionTime) {
      this.actionTime = actionTime;
   }

   public int getActionId() {
      return this.actionId;
   }

   public void setActionId(int actionId) {
      this.actionId = actionId;
   }

   public String getActionOriginalId() {
      return this.actionOriginalId;
   }

   public void setActionOriginalId(String actionOriginalId) {
      this.actionOriginalId = actionOriginalId;
   }

   public String getActionName() {
      return this.actionName;
   }

   public void setActionName(String actionName) {
      this.actionName = actionName;
   }

   public String getActionLangName() {
      return this.actionLangName;
   }

   public void setActionLangName(String actionLangName) {
      this.actionLangName = actionLangName;
   }

   public int getActionSonType() {
      return this.actionSonType;
   }

   public void setActionSonType(int actionSonType) {
      this.actionSonType = actionSonType;
   }

   public String getActionDesciber() {
      return this.actionDesciber;
   }

   public void setActionDesciber(String actionDesciber) {
      this.actionDesciber = actionDesciber;
   }

   public String getActionLangDesciber() {
      return this.actionLangDesciber;
   }

   public void setActionLangDesciber(String actionLangDesciber) {
      this.actionLangDesciber = actionLangDesciber;
   }

   public String getSystemLanguage() {
      return this.systemLanguage;
   }

   public void setSystemLanguage(String systemLanguage) {
      this.systemLanguage = systemLanguage;
   }
}

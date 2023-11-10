package com.ubtechinc.service.model;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class ActionFileEntrity {
   @Index(0)
   private int actionId;
   @Index(1)
   private int actionType;
   @Index(2)
   private String actionName;
   @Index(3)
   private String actionFilePath;
   @Index(4)
   private int downloadState;
   @Index(5)
   private String actionOriginalID;

   public ActionFileEntrity() {
   }

   public int getDownloadState() {
      return this.downloadState;
   }

   public void setDownloadState(int downloadState) {
      this.downloadState = downloadState;
   }

   public int getActionId() {
      return this.actionId;
   }

   public void setActionId(int actionId) {
      this.actionId = actionId;
   }

   public int getActionType() {
      return this.actionType;
   }

   public void setActionType(int actionType) {
      this.actionType = actionType;
   }

   public String getActionName() {
      return this.actionName;
   }

   public void setActionName(String actionName) {
      this.actionName = actionName;
   }

   public String getActionFilePath() {
      return this.actionFilePath;
   }

   public void setActionFilePath(String actionFilePath) {
      this.actionFilePath = actionFilePath;
   }

   public String getActionOriginalID() {
      return this.actionOriginalID;
   }

   public void setActionOriginalID(String actionOriginalID) {
      this.actionOriginalID = actionOriginalID;
   }
}

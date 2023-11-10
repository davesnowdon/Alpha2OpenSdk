package com.ubtechinc.alpha2ctrlapp.network.model.response;

import java.io.Serializable;

public class BaseResponse implements Serializable {
   private static final long serialVersionUID = -538305971460291809L;
   private boolean status;
   private String Message;
   private Object model;
   private int MessageCode = -1;

   public BaseResponse() {
   }

   public boolean isStatus() {
      return this.status;
   }

   public void setStatus(boolean status) {
      this.status = status;
   }

   public String getMessage() {
      return this.Message;
   }

   public void setMessage(String message) {
      this.Message = message;
   }

   public Object getModel() {
      return this.model;
   }

   public void setModel(Object model) {
      this.model = model;
   }

   public int getMessageCode() {
      return this.MessageCode;
   }

   public void setMessageCode(int messageCode) {
      this.MessageCode = messageCode;
   }
}

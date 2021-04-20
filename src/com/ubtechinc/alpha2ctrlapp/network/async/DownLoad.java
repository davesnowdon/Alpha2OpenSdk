package com.ubtechinc.alpha2ctrlapp.network.async;

public class DownLoad {
   private int requestCode;
   private boolean isRefresh;
   private int state;
   private Object result;
   boolean isCheckNetwork;
   private OnDataListener listener;

   public DownLoad() {
   }

   public DownLoad(int requestCode, boolean isCheckNetwork, OnDataListener listener) {
      this.requestCode = requestCode;
      this.isCheckNetwork = isCheckNetwork;
      this.listener = listener;
   }

   public int getRequestCode() {
      return this.requestCode;
   }

   public void setRequestCode(int requestCode) {
      this.requestCode = requestCode;
   }

   public boolean isRefresh() {
      return this.isRefresh;
   }

   public void setRefresh(boolean isRefresh) {
      this.isRefresh = isRefresh;
   }

   public int getState() {
      return this.state;
   }

   public void setState(int state) {
      this.state = state;
   }

   public Object getResult() {
      return this.result;
   }

   public void setResult(Object result) {
      this.result = result;
   }

   public boolean isCheckNetwork() {
      return this.isCheckNetwork;
   }

   public void setCheckNetwork(boolean isCheckNetwork) {
      this.isCheckNetwork = isCheckNetwork;
   }

   public OnDataListener getListener() {
      return this.listener;
   }

   public void setListener(OnDataListener listener) {
      this.listener = listener;
   }

   public String toString() {
      return "DownLoad [requestCode=" + this.requestCode + ", isRefresh=" + this.isRefresh + ", state=" + this.state + ", result=" + this.result + ", listener=" + this.listener + "]";
   }
}

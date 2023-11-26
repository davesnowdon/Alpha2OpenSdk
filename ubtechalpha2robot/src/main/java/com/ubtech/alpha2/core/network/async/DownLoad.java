package com.ubtech.alpha2.core.network.async;

public class DownLoad {
   private int requestCode;
   private boolean isRefresh;
   private boolean downflag;
   private String downUrl;
   private int state;
   private Object result;
   private OnDataListener listener;
   private String appPath;

   public String getAppPath() {
      return this.appPath;
   }

   public void setAppPath(String appPath) {
      this.appPath = appPath;
   }

   public DownLoad() {
   }

   public DownLoad(int requestCode, boolean downflag, String downUrl, OnDataListener listener) {
      this.requestCode = requestCode;
      this.downflag = downflag;
      this.downUrl = downUrl;
      this.listener = listener;
   }

   public DownLoad(int requestCode, boolean downflag, String downUrl, OnDataListener listener, String appPath) {
      this.requestCode = requestCode;
      this.downflag = downflag;
      this.downUrl = downUrl;
      this.listener = listener;
      this.appPath = appPath;
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

   public boolean isDownflag() {
      return this.downflag;
   }

   public void setDownflag(boolean downflag) {
      this.downflag = downflag;
   }

   public String getDownUrl() {
      return this.downUrl;
   }

   public void setDownUrl(String downUrl) {
      this.downUrl = downUrl;
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

   public OnDataListener getListener() {
      return this.listener;
   }

   public void setListener(OnDataListener listener) {
      this.listener = listener;
   }
}

package com.ubtech.alpha2.core.network.async;

public class DownLoadBen {
   private String requestCode;
   private boolean isRefresh;
   private boolean downflag;
   private String downUrl;
   private int state;
   private boolean result;
   private OnDownLoadListener listener;
   private String appPath;
   private String fileName;
   private boolean isException;

   public boolean isException() {
      return this.isException;
   }

   public void setException(boolean isException) {
      this.isException = isException;
   }

   public String getFileName() {
      return this.fileName;
   }

   public void setFileName(String fileName) {
      this.fileName = fileName;
   }

   public String getAppPath() {
      return this.appPath;
   }

   public void setAppPath(String appPath) {
      this.appPath = appPath;
   }

   public DownLoadBen() {
   }

   public DownLoadBen(String requestCode, boolean downflag, String downUrl, OnDownLoadListener listener) {
      this.requestCode = requestCode;
      this.downflag = downflag;
      this.downUrl = downUrl;
      this.listener = listener;
   }

   public DownLoadBen(String requestCode, boolean downflag, String downUrl, OnDownLoadListener listener, String appPath) {
      this.requestCode = requestCode;
      this.downflag = downflag;
      this.downUrl = downUrl;
      this.listener = listener;
      this.appPath = appPath;
   }

   public String getAbsolutePath() {
      return this.appPath + "/" + this.fileName;
   }

   public String getRequestCode() {
      return this.requestCode;
   }

   public void setRequestCode(String requestCode) {
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

   public boolean getResult() {
      return this.result;
   }

   public void setResult(boolean result) {
      this.result = result;
   }

   public OnDownLoadListener getListener() {
      return this.listener;
   }

   public void setListener(OnDownLoadListener listener) {
      this.listener = listener;
   }
}

package com.ubtech.alpha2.core.utils.download2.netdownload;

import java.io.File;

public class FileDownloader2 {
   public static final int URL_ERROR_CODE = -1;
   public static final int HTTP_ERROR_CODE = -2;
   public static final int CANCEL_CODE = -3;
   public static final int SIZE_ERROR_CODE = -4;
   public static final int CONNECT_ERROR_CODE = -5;
   private File fileSaveDir;
   private int requestCode;
   private boolean isRefresh;
   private boolean downflag;
   private String downUrl;
   private int state;
   private Object result;
   private DownLoadlistener2 listener;

   public File getFileSaveDir() {
      return this.fileSaveDir;
   }

   public void setFileSaveDir(File fileSaveDir) {
      this.fileSaveDir = fileSaveDir;
   }

   public FileDownloader2() {
   }

   public FileDownloader2(int requestCode, boolean downflag, String downUrl, DownLoadlistener2 listener, File file) {
      this.requestCode = requestCode;
      this.downflag = downflag;
      this.downUrl = downUrl;
      this.listener = listener;
      this.fileSaveDir = file;
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

   public DownLoadlistener2 getListener() {
      return this.listener;
   }

   public void setListener(DownLoadlistener2 listener) {
      this.listener = listener;
   }
}

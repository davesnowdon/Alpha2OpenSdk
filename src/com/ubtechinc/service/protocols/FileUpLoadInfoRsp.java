package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class FileUpLoadInfoRsp {
   @Index(0)
   private int port;
   @Index(1)
   private long fileLens;
   @Index(2)
   private String fileName;
   @Index(3)
   private String ipAddress;
   @Index(4)
   private boolean status;

   public FileUpLoadInfoRsp() {
   }

   public int getPort() {
      return this.port;
   }

   public void setPort(int port) {
      this.port = port;
   }

   public long getFileLens() {
      return this.fileLens;
   }

   public void setFileLens(long fileLens) {
      this.fileLens = fileLens;
   }

   public String getFileName() {
      return this.fileName;
   }

   public void setFileName(String fileName) {
      this.fileName = fileName;
   }

   public String getIpAddress() {
      return this.ipAddress;
   }

   public void setIpAddress(String ipAddress) {
      this.ipAddress = ipAddress;
   }

   public boolean isStatus() {
      return this.status;
   }

   public void setStatus(boolean status) {
      this.status = status;
   }
}

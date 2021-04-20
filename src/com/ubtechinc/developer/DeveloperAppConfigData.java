package com.ubtechinc.developer;

import java.io.Serializable;

public class DeveloperAppConfigData implements Serializable {
   private static final long serialVersionUID = 1L;
   private int cmd;
   private byte[] datas;
   private String packageName;
   private byte[] tags;

   public DeveloperAppConfigData() {
   }

   public byte[] getTags() {
      return this.tags;
   }

   public void setTags(byte[] tags) {
      this.tags = tags;
   }

   public int getCmd() {
      return this.cmd;
   }

   public void setCmd(int cmd) {
      this.cmd = cmd;
   }

   public byte[] getDatas() {
      return this.datas;
   }

   public void setDatas(byte[] datas) {
      this.datas = datas;
   }

   public String getPackageName() {
      return this.packageName;
   }

   public void setPackageName(String packageName) {
      this.packageName = packageName;
   }
}

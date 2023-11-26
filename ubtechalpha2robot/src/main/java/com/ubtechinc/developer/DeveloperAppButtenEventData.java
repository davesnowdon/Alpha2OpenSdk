package com.ubtechinc.developer;

import java.io.Serializable;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class DeveloperAppButtenEventData implements Serializable {
   private static final long serialVersionUID = 1L;
   @Index(0)
   private int cmd;
   @Index(1)
   private byte[] datas;
   @Index(2)
   private String packageName;

   public DeveloperAppButtenEventData() {
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

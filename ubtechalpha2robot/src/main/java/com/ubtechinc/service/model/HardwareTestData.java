package com.ubtechinc.service.model;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class HardwareTestData {
   @Index(0)
   private int cmd;
   @Index(1)
   private byte[] datas;

   public HardwareTestData() {
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
}

package com.ubtechinc.service.protocols;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class TransferPhotoInfo implements Serializable {
   private static final long serialVersionUID = 1L;
   @Index(0)
   private int type;
   @Index(1)
   private int amount;
   @Index(2)
   private String path;
   @Index(3)
   private List<String> delPics = new ArrayList();

   public TransferPhotoInfo() {
   }

   public String getPath() {
      return this.path;
   }

   public void setPath(String path) {
      this.path = path;
   }

   public int getType() {
      return this.type;
   }

   public void setType(int type) {
      this.type = type;
   }

   public int getAmount() {
      return this.amount;
   }

   public void setAmount(int amount) {
      this.amount = amount;
   }

   public List<String> getDelPics() {
      return this.delPics;
   }

   public void setDelPics(List<String> delPics) {
      this.delPics = delPics;
   }
}

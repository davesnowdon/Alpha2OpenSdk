package com.ubtechinc.service.protocols;

import java.io.Serializable;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class CtrlAllRobotInfo implements Serializable {
   private static final long serialVersionUID = -2453466582021638760L;
   @Index(0)
   private int mBroadcastIndex;
   @Index(1)
   private String actionName;
   @Index(2)
   private String reserve;

   public CtrlAllRobotInfo() {
   }

   public int getmBroadcastIndex() {
      return this.mBroadcastIndex;
   }

   public void setmBroadcastIndex(int mBroadcastIndex) {
      this.mBroadcastIndex = mBroadcastIndex;
   }

   public String getActionName() {
      return this.actionName;
   }

   public void setActionName(String actionName) {
      this.actionName = actionName;
   }

   public String getReserve() {
      return this.reserve;
   }

   public void setReserve(String reserve) {
      this.reserve = reserve;
   }
}

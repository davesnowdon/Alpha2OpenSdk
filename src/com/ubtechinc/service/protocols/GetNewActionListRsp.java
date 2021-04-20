package com.ubtechinc.service.protocols;

import java.util.ArrayList;
import java.util.List;
import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class GetNewActionListRsp {
   @Index(0)
   private boolean isOldVersion = false;
   @Index(1)
   private List<NewActionInfo> actionList = new ArrayList();

   public GetNewActionListRsp() {
   }

   public List<NewActionInfo> getActionList() {
      return this.actionList;
   }

   public void setActionList(List<NewActionInfo> actionList) {
      this.actionList = actionList;
   }

   public boolean isOldVersion() {
      return this.isOldVersion;
   }

   public void setOldVersion(boolean oldVersion) {
      this.isOldVersion = oldVersion;
   }
}

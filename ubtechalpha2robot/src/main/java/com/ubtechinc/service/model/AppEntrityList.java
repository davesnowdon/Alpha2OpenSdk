package com.ubtechinc.service.model;

import java.util.ArrayList;
import java.util.List;
import org.msgpack.annotation.Message;

@Message
public class AppEntrityList {
   private List<AppEntrityInfo> appList = new ArrayList();

   public AppEntrityList() {
   }

   public List<AppEntrityInfo> getAppList() {
      return this.appList;
   }

   public void setAppList(List<AppEntrityInfo> appList) {
      this.appList = appList;
   }
}

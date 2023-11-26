package com.ubtechinc.service.model;

import java.util.ArrayList;
import java.util.List;
import org.msgpack.annotation.Message;

@Message
public class AlarmEntrityList {
   private List<AlarmEntrity> wifilist = new ArrayList();

   public AlarmEntrityList() {
   }

   public void addToList(AlarmEntrity info) {
      this.wifilist.add(info);
   }

   public List<AlarmEntrity> getList() {
      return this.wifilist;
   }
}

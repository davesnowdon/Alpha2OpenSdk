package com.ubtechinc.service.model;

import java.util.ArrayList;
import java.util.List;
import org.msgpack.annotation.Message;

@Message
public class DeskClockList {
   private List<DeskClock> list = new ArrayList();

   public DeskClockList() {
   }

   public void addToList(DeskClock info) {
      this.list.add(info);
   }

   public List<DeskClock> getList() {
      return this.list;
   }
}

package org.msgpack.template;

import java.util.ArrayList;
import java.util.List;

public class FieldList {
   private ArrayList<FieldList.Entry> list = new ArrayList();

   public FieldList() {
   }

   public void add(String name) {
      this.add(name, FieldOption.DEFAULT);
   }

   public void add(String name, FieldOption option) {
      this.list.add(new FieldList.Entry(name, option));
   }

   public void put(int index, String name) {
      this.put(index, name, FieldOption.DEFAULT);
   }

   public void put(int index, String name, FieldOption option) {
      if (this.list.size() < index) {
         do {
            this.list.add(new FieldList.Entry());
         } while(this.list.size() < index);

         this.list.add(new FieldList.Entry(name, option));
      } else {
         this.list.set(index, new FieldList.Entry(name, option));
      }

   }

   public List<FieldList.Entry> getList() {
      return this.list;
   }

   public static class Entry {
      private String name;
      private FieldOption option;

      public Entry() {
         this((String)null, FieldOption.IGNORE);
      }

      public Entry(String name, FieldOption option) {
         this.name = name;
         this.option = option;
      }

      public String getName() {
         return this.name;
      }

      public FieldOption getOption() {
         return this.option;
      }

      public boolean isAvailable() {
         return this.option != FieldOption.IGNORE;
      }
   }
}

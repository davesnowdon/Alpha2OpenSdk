package com.ubtechinc.db.pojos;

public class Alpha2App {
   public String name;
   public String appid;
   public static final String SQL_TABLE = "CREATE TABLE app (_id INTEGER PRIMARY KEY,name TEXT,appid TEXT);";

   public Alpha2App() {
   }
}

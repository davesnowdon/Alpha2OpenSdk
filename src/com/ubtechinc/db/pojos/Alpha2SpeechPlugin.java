package com.ubtechinc.db.pojos;

public class Alpha2SpeechPlugin {
   public String name;
   public String action;
   public static String SQL = "CREATE TABLE speech (_id INTEGER PRIMARY KEY,name TEXT,action TEXT);";

   public Alpha2SpeechPlugin() {
   }
}

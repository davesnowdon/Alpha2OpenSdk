package com.ubtechinc.db.pojos;

public class Alpha2Photo {
   public String filePath;
   public String url;
   public static final String SQL_TABLE = "CREATE TABLE photoUrl (_id INTEGER PRIMARY KEY,filePath TEXT,url TEXT);";

   public Alpha2Photo() {
   }
}

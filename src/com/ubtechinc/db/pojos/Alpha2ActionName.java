package com.ubtechinc.db.pojos;

public class Alpha2ActionName {
   public String action_id;
   public String action_type;
   public String action_cn_name;
   public String action_en_name;
   public static final String SQL_TABLE = "CREATE TABLE actionName (_id INTEGER PRIMARY KEY,action_id TEXT,action_type TEXT,action_cn_name TEXT,action_en_name TEXT);";

   public Alpha2ActionName() {
   }
}

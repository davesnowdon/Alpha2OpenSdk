package com.ubtechinc.db.pojos;

public class Alpha2State {
   public int power;
   public int debug;
   public static String SQL = "CREATE TABLE state (_id INTEGER PRIMARY KEY,power INTEGER,debug INTEGER);";

   public Alpha2State() {
   }
}

package com.ubtechinc.utils;

import java.util.Comparator;

public class PinyinComparatorUtils implements Comparator<SortBaseModel> {
   public PinyinComparatorUtils() {
   }

   public int compare(SortBaseModel o1, SortBaseModel o2) {
      if (!o1.getSortLetters().equals("@") && !o2.getSortLetters().equals("#")) {
         return !o1.getSortLetters().equals("#") && !o2.getSortLetters().equals("@") ? o1.getSortLetters().compareTo(o2.getSortLetters()) : 1;
      } else {
         return -1;
      }
   }
}

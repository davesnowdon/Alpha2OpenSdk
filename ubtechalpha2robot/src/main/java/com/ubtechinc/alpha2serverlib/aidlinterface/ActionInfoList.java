package com.ubtechinc.alpha2serverlib.aidlinterface;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class ActionInfoList implements Parcelable {
   private List<String> acitonInfo = new ArrayList();
   public static final Creator<ActionInfoList> CREATOR = new Creator<ActionInfoList>() {
      public ActionInfoList createFromParcel(Parcel in) {
         return new ActionInfoList(in);
      }

      public ActionInfoList[] newArray(int size) {
         return new ActionInfoList[size];
      }
   };

   public List<String> getAcitonInfo() {
      return this.acitonInfo;
   }

   public void setAcitonInfo(ArrayList<String> acitonInfo) {
      this.acitonInfo = acitonInfo;
   }

   public ActionInfoList() {
   }

   public ActionInfoList(Parcel in) {
      this.acitonInfo = in.readArrayList(ArrayList.class.getClassLoader());
   }

   public int describeContents() {
      return 0;
   }

   public void writeToParcel(Parcel dest, int flags) {
      dest.writeStringList(this.acitonInfo);
   }
}

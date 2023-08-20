package com.ubtechinc.alpha2serverlib.aidlinterface;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import java.util.ArrayList;
import java.util.List;

public class AlphaActionList implements Parcelable {
   private List<ActionInfoList> acitonList = new ArrayList();
   public static final Creator<AlphaActionList> CREATOR = new Creator<AlphaActionList>() {
      public AlphaActionList createFromParcel(Parcel in) {
         return new AlphaActionList(in);
      }

      public AlphaActionList[] newArray(int size) {
         return new AlphaActionList[size];
      }
   };

   public List<ActionInfoList> getAcitonInfo() {
      return this.acitonList;
   }

   public void setAcitonInfo(ArrayList<ActionInfoList> acitonInfo) {
      this.acitonList = acitonInfo;
   }

   public AlphaActionList() {
   }

   public AlphaActionList(Parcel in) {
      in.readTypedList(this.acitonList, ActionInfoList.CREATOR);
   }

   public int describeContents() {
      return 0;
   }

   public void writeToParcel(Parcel dest, int flags) {
      dest.writeTypedList(this.acitonList);
   }

   public void readFromParcel(Parcel in) {
      in.readTypedList(this.acitonList, ActionInfoList.CREATOR);
   }
}

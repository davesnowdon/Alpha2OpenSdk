package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class SetChargeAndPlayData {
   @Index(0)
   private boolean mIsFromClient;
   @Index(1)
   private boolean mIsSetting;
   @Index(2)
   private boolean mIsOpen;
   @Index(3)
   private boolean mSetOpen;

   public SetChargeAndPlayData() {
   }

   public boolean ismIsFromClient() {
      return this.mIsFromClient;
   }

   public void setmIsFromClient(boolean mIsFromClient) {
      this.mIsFromClient = mIsFromClient;
   }

   public boolean ismIsSetting() {
      return this.mIsSetting;
   }

   public void setmIsSetting(boolean mIsSetting) {
      this.mIsSetting = mIsSetting;
   }

   public boolean ismIsOpen() {
      return this.mIsOpen;
   }

   public void setmIsOpen(boolean mIsOpen) {
      this.mIsOpen = mIsOpen;
   }

   public boolean ismSetOpen() {
      return this.mSetOpen;
   }

   public void setmSetOpen(boolean mSetOpen) {
      this.mSetOpen = mSetOpen;
   }
}

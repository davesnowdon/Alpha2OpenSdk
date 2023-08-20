package com.ubtechinc.alpha2robot.constant;

public class UbxErrorCode {
   public UbxErrorCode() {
   }

   public static enum API_EEROR_CODE {
      API_ERROR_NOT_INIT,
      API_ERROR_SUCCEED,
      API_ERROR_APPID_NOT_ACTIVE,
      API_ERROR_AUTHORIZE_ERROR;

      private API_EEROR_CODE() {
      }
   }
}

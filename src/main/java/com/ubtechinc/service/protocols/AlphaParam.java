package com.ubtechinc.service.protocols;

import org.msgpack.annotation.Index;
import org.msgpack.annotation.Message;

@Message
public class AlphaParam {
   public static final int GET_INIT_PARAM = 1;
   public static final int SET_MASTER_NAME = 2;
   public static final String iParamcmd = "iParamcmd";
   public static final String bIsFromClient = "bIsFromClient";
   public static final String sAndroidVersion = "sAndroidVersion";
   public static final String sServiceVersion = "sServiceVersion";
   public static final String sHeaderVersion = "sHeaderVersion";
   public static final String sChestVersion = "sChestVersion";
   public static final String lSdTotalVolume = "lSdTotalVolume";
   public static final String lSdSurplusVolume = "lSdSurplusVolume";
   public static final String sServiceLanguage = "sServiceLanguage";
   public static final String sMasterName = "sMasterName";
   @Index(0)
   String param;

   public AlphaParam() {
   }

   public String getParam() {
      return this.param;
   }

   public void setParam(String param) {
      this.param = param;
   }
}

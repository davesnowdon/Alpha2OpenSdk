package com.ubtechinc.service.model;

import org.msgpack.annotation.Message;

@Message
public class HaveLoginSpotify {
   private boolean isLogin;

   public HaveLoginSpotify() {
   }

   public boolean isLogin() {
      return this.isLogin;
   }

   public void setLogin(boolean isLogin) {
      this.isLogin = isLogin;
   }
}

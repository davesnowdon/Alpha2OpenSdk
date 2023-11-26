package com.ubtechinc.service.model;

import org.msgpack.annotation.Message;

@Message
public class SaveSpotifyToken {
   private String mToken;

   public SaveSpotifyToken() {
   }

   public String getmToken() {
      return this.mToken;
   }

   public void setmToken(String mToken) {
      this.mToken = mToken;
   }
}

package com.ubtechinc.service.protocols;

import java.io.Serializable;
import org.msgpack.annotation.Message;

@Message
public class LoginAlpha implements Serializable {
   private String mStrPassword;

   public LoginAlpha() {
   }

   public String getmStrPassword() {
      return this.mStrPassword;
   }

   public void setmStrPassword(String mStrPassword) {
      this.mStrPassword = mStrPassword;
   }
}

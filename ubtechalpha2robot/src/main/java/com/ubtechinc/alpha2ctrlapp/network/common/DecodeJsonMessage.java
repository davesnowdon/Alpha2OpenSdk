package com.ubtechinc.alpha2ctrlapp.network.common;

import com.ubtechinc.alpha2ctrlapp.network.JsonUtils;
import com.ubtechinc.alpha2ctrlapp.network.model.response.BaseResponse;
import com.ubtechinc.alpha2ctrlapp.network.model.response.CommonResPonse;

public class DecodeJsonMessage {
   public DecodeJsonMessage() {
   }

   public static BaseResponse decode(Object jsonStr) {
      BaseResponse response = new BaseResponse();
      CommonResPonse commonRespon = (CommonResPonse)JsonUtils.getInstance().jsonToBean(jsonStr.toString(), CommonResPonse.class);
      response.setStatus(commonRespon.isStatus());
      response.setMessage(commonRespon.getInfo());
      response.setModel(commonRespon.getModels());
      if (response.getMessage().length() >= 4 && isNumeric(response.getMessage().substring(0, 4))) {
         response.setMessageCode(Integer.valueOf(response.getMessage().substring(0, 4)));
      } else {
         if (response.isStatus()) {
            response.setMessageCode(1);
         } else {
            response.setMessageCode(-2);
         }

         if (response.getMessage().equals("-1")) {
            response.setMessageCode(-1);
         }
      }

      return response;
   }

   public static String[] getMessage(String info) {
      String[] msg = new String[2];
      if (info.length() >= 4 && isNumeric(info.substring(0, 4))) {
         msg[0] = info.substring(0, 4);
      } else {
         msg[0] = "-2";
      }

      msg[1] = info;
      return msg;
   }

   public static boolean isNumeric(String str) {
      try {
         Double.parseDouble(str);
         return true;
      } catch (Exception var2) {
         return false;
      }
   }
}

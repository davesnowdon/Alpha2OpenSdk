package com.ubtechinc.alpha2ctrlapp.network.action;

import android.content.Context;
import com.ubtechinc.alpha2ctrlapp.network.common.HttpPost;
import org.apache.http.HttpException;

public class LoggerAction extends BaseAction {
   private Context context;
   private IBaseDataListener listener;

   public LoggerAction(Context context, IBaseDataListener listener) {
      super(context);
      this.context = context;
      this.listener = listener;
   }

   public Object doInBackground(int requestCode) throws HttpException {
      switch(requestCode) {
      case 3002:
         return HttpPost.getJsonByPost("system/addDna", this.getParamerObj(), false);
      case 3003:
         return HttpPost.getJsonByPost("", this.getParamerObj(), false);
      default:
         return null;
      }
   }

   public void onSuccess(int requestCode, Object result) {
      super.onSuccess(requestCode, result);
      this.listener.onSuccess(requestCode, result);
   }

   public void onFailure(int requestCode, int state, Object result) {
      super.onFailure(requestCode, state, result);
      this.listener.onFailure(requestCode, state, result);
   }
}

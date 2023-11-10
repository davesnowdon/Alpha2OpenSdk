package com.ubtechinc.alpha2ctrlapp.network.action;

import android.content.Context;
import com.ubtechinc.alpha2ctrlapp.network.DesUtil;
import com.ubtechinc.alpha2ctrlapp.network.async.AsyncTaskManager;
import com.ubtechinc.alpha2ctrlapp.network.async.OnDataListener;
import com.ubtechinc.alpha2ctrlapp.network.model.request.CommonRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.http.HttpException;

public class BaseAction implements OnDataListener {
   private AsyncTaskManager mAsyncTaskManager;
   protected Context mContext;
   private CommonRequest paramerObj;

   public BaseAction(Context context) {
      this.mContext = context;
      this.mAsyncTaskManager = AsyncTaskManager.getInstance(this.mContext);
   }

   public void setParamerObj(CommonRequest paramerObj) {
      this.paramerObj = paramerObj;
   }

   public CommonRequest getParamerObj() {
      return this.paramerObj;
   }

   public void request(int requsetCode) {
      this.mAsyncTaskManager.request(requsetCode, this);
   }

   public void cancelRequest(int requsetCode) {
      this.mAsyncTaskManager.cancelRequest(requsetCode);
   }

   public void cancelRequest() {
      this.mAsyncTaskManager.cancelRequest();
   }

   public void doRequest(int requestType, String action) {
      this.getParamerObj().setRequestTime(this.getCurrentTime());
      this.getParamerObj().setRequestKey(DesUtil.getMD5(this.getParamerObj().getRequestTime() + "UBTech832%1293*6", 32));
      this.request(requestType);
   }

   public String getCurrentTime() {
      SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
      Date curDate = new Date(System.currentTimeMillis());
      String str = formatter.format(curDate);
      return str;
   }

   public Object doInBackground(int requestCode) throws HttpException {
      return null;
   }

   public void onSuccess(int requestCode, Object result) {
   }

   public void onFailure(int requestCode, int state, Object result) {
      switch(state) {
      case -999:
      case -400:
      case -200:
      default:
      }
   }
}

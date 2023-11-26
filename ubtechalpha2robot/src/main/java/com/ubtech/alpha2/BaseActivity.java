package com.ubtech.alpha2;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
// ISSUE-1 Layout classes not present in original SDK jar
//import com.ubtech.alpha2.R.layout;
import com.ubtech.alpha2.core.network.async.AsyncTaskManager;
import com.ubtech.alpha2.core.network.async.OnDataListener;
import com.ubtech.alpha2.core.utils.NLog;
import org.apache.http.HttpException;

public class BaseActivity extends Activity implements OnDataListener {
   private final String tag = BaseActivity.class.getSimpleName();
   private AsyncTaskManager mAsyncTaskManager;
   protected Context mContext;

   public BaseActivity() {
   }

   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      // ISSUE-1 Layout classes not present in original SDK jar
      //this.setContentView(layout.activity_main);
      this.mContext = this;
      this.mAsyncTaskManager = AsyncTaskManager.getInstance(this.mContext);
   }

   public void request(int requsetCode, boolean isLocalNetWork) {
      this.mAsyncTaskManager.request(requsetCode, this, isLocalNetWork);
   }

   public Object doInBackground(int requsetCode) throws HttpException {
      return null;
   }

   public void onProgress(int progress) {
   }

   public void onSuccess(int requestCode, Object result) {
   }

   public void onFailure(int requestCode, int state, Object result) {
      switch(state) {
      case -400:
         NLog.i(this.tag, "网络不可用");
         break;
      case -200:
         NLog.i(this.tag, "网络有问题");
      }

   }
}

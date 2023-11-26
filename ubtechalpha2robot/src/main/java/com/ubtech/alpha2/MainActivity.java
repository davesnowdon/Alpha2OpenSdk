package com.ubtech.alpha2;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
// ISSUE-1 Layout classes not present in original SDK jar
//import com.ubtech.alpha2.R.layout;
import com.ubtech.alpha2.core.action.DemoAction;
import com.ubtech.alpha2.core.apkupdate.InstallerUtils;
import com.ubtech.alpha2.core.apkupdate.UnInstallerUtils;
import com.ubtech.alpha2.core.db.dao.OrderItemDao;
import com.ubtech.alpha2.core.model.response.OrderItem;
import com.ubtech.alpha2.core.model.response.OrderXMLResponse;
import com.ubtech.alpha2.core.utils.NLog;
import java.io.File;
import org.apache.http.HttpException;

public class MainActivity extends BaseActivity {
   private final String tag = MainActivity.class.getSimpleName();
   private final int TEST_CODE_1 = 100;
   private OrderItemDao orderItemDao;

   public MainActivity() {
   }

   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      // ISSUE-1 Layout classes not present in original SDK jar
      //this.setContentView(layout.activity_main);
      NLog.setDebug(true);
   }

   public void onTest(View view) {
      this.onSlience();
   }

   public void onUpdate() {
      Intent intent = new Intent("android.intent.action.VIEW");
      Log.e("zdy", "onUpdate");
      String path = FilePath.appPath + "/zdyDebug.apk";
      intent.setDataAndType(Uri.fromFile(new File(path)), "application/vnd.android.package-archive");
      this.startActivity(intent);
   }

   public void onSlience() {
      Thread installThread = new Thread(new Runnable() {
         public void run() {
            String path = FilePath.appPath + "/zdyDebug.apk";
            InstallerUtils installerUtils = new InstallerUtils();
            installerUtils.install(path);
         }
      });
      installThread.start();
   }

   public void onUnInitall() {
      Thread unInstallThread = new Thread(new Runnable() {
         public void run() {
            String busybox = "mount -o remount rw /data";
            String chmod = "chmod 777 /data/app/com.example.zdydebug.apk";
            String uninstallapk = "pm uninstall com.example.zdydebug";
            UnInstallerUtils unInstallerUtils = new UnInstallerUtils();
            unInstallerUtils.chmodApk(busybox, chmod);
            unInstallerUtils.uninstallApk(uninstallapk);
         }
      });
      unInstallThread.start();
   }

   public Object doInBackground(int requsetCode) throws HttpException {
      DemoAction action = new DemoAction(this.mContext);

      try {
         return action.getOrderXmlDemo("assets://order.xml");
      } catch (com.ubtech.alpha2.core.network.http.HttpException var4) {
         var4.printStackTrace();
         return super.doInBackground(requsetCode);
      }
   }

   public void onProgress(int progress) {
      NLog.e(this.tag, "onProgress : " + progress + "");
   }

   public void onSuccess(int requestCode, Object result) {
      Log.e(this.tag, "***onSuccess******");
      if (null != result) {
         OrderXMLResponse bean = (OrderXMLResponse)result;
         Log.e("zdy", "**********" + ((OrderItem)bean.getItem().get(0)).toString());
         this.orderItemDao.saveOrderItemList(bean.getItem());
         OrderItem orderItem = this.orderItemDao.queryByName("ǰ��");
         Log.e("zdy", "**********" + orderItem.toString());
      }

   }
}

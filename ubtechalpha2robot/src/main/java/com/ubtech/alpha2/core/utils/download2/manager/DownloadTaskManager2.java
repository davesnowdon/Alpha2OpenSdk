package com.ubtech.alpha2.core.utils.download2.manager;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import com.ubtech.alpha2.core.utils.CommonUtils;
import com.ubtech.alpha2.core.utils.WebServerConstants;
import com.ubtech.alpha2.core.utils.download2.netdownload.DownLoadlistener2;
import com.ubtech.alpha2.core.utils.download2.netdownload.DownloadThread2;
import com.ubtech.alpha2.core.utils.download2.netdownload.FileDownloader2;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.Map.Entry;

public class DownloadTaskManager2 {
   public static DownloadTaskManager2 instance;
   private static Map<Integer, WeakReference<DownloadThread2>> requestMap;
   private Context context;

   public DownloadTaskManager2(Context context) {
      this.context = context;
      requestMap = new WeakHashMap();
   }

   public static void initInstance(Context context) {
      if (instance == null) {
         instance = new DownloadTaskManager2(context);
      }

   }

   public static synchronized DownloadTaskManager2 getInstance() {
      return instance;
   }

   public int startDownload(int requestCode, boolean downflag, String url, String fileName, WebServerConstants.Product type, String mac, DownLoadlistener2 listener) {
      if (this.hasDownLoad(requestCode) && !downflag) {
         Log.e("zdy", "已经在下载。。。。。直接返回");
         return 2;
      } else if (CommonUtils.getNetworkType(this.context) != 1) {
         listener.onDowanFailed(requestCode, -200);
         return -1;
      } else {
         if (Environment.getExternalStorageState().equals("mounted")) {
            if (!mac.equals("")) {
               if (mac.contains(":")) {
                  mac = mac.replaceAll(":", "_");
               }

               mac = "/" + mac;
            }

            File saveDir = new File(WebServerConstants.getSavePath(type) + mac);
            if (!saveDir.exists()) {
               saveDir.mkdirs();
            }

            File filePath = new File(saveDir, fileName);
            this.startDownload(requestCode, downflag, url, listener, filePath);
         } else {
            Log.i("zdy", "SDCard 不存在");
         }

         return 1;
      }
   }

   public boolean hasDownLoad(int requestCode) {
      return requestMap.containsKey(requestCode);
   }

   private synchronized void startDownload(int requestCode, boolean downflag, String downUrl, DownLoadlistener2 listener, File file) {
      FileDownloader2 bean = new FileDownloader2(requestCode, downflag, downUrl, listener, file);
      DownloadThread2 t = new DownloadThread2(bean);
      t.start();
      requestMap.put(requestCode, new WeakReference(t));
   }

   public synchronized void cancelDownload(int requestCode) {
      WeakReference<DownloadThread2> requestTask = (WeakReference)requestMap.get(requestCode);
      if (requestTask != null) {
         DownloadThread2 request = (DownloadThread2)requestTask.get();
         if (request != null) {
            request.setCancel();
            request = null;
         }
      }

      requestMap.remove(requestCode);
   }

   public synchronized void cancelRequest() {
      if (requestMap != null) {
         Iterator it = requestMap.entrySet().iterator();

         while(it.hasNext()) {
            Entry<Integer, WeakReference<DownloadThread2>> entry = (Entry)it.next();
            Integer requestCode = (Integer)entry.getKey();
            this.cancelDownload(requestCode);
         }

         requestMap.clear();
      }

   }

   public synchronized void removeDownload(int requestCode) {
      WeakReference var10000 = (WeakReference)requestMap.get(requestCode);
      requestMap.remove(requestCode);
   }
}

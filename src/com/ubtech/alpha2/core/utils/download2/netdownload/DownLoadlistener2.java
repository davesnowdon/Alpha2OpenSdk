package com.ubtech.alpha2.core.utils.download2.netdownload;

public interface DownLoadlistener2 {
   void onDownloadOver(int var1);

   void onProgrerss(int var1, String var2);

   void onDowanFailed(int var1, int var2);
}

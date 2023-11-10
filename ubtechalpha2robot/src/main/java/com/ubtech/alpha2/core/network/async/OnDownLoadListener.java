package com.ubtech.alpha2.core.network.async;

public interface OnDownLoadListener {
   void onProgress(int var1);

   void onSuccess(String var1, DownLoadBen var2);

   void onFailure(String var1, int var2, DownLoadBen var3);
}

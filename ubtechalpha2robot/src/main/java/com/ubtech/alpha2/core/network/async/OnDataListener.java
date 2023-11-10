package com.ubtech.alpha2.core.network.async;

import org.apache.http.HttpException;

public interface OnDataListener {
   Object doInBackground(int var1) throws HttpException;

   void onProgress(int var1);

   void onSuccess(int var1, Object var2);

   void onFailure(int var1, int var2, Object var3);
}

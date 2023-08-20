package com.ubtechinc.alpha2ctrlapp.network.async;

import org.apache.http.HttpException;

public interface OnDataListener {
   Object doInBackground(int var1) throws HttpException;

   void onSuccess(int var1, Object var2);

   void onFailure(int var1, int var2, Object var3);
}

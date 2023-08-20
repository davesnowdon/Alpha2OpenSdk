package com.ubtech.alpha2.core.action;

import android.content.Context;
import android.util.Log;
import com.ubtech.alpha2.core.base.BaseAction;
import com.ubtech.alpha2.core.business.DemoManager;
import com.ubtech.alpha2.core.model.response.ApkConfigResponse;
import com.ubtech.alpha2.core.model.response.OrderXMLResponse;
import com.ubtech.alpha2.core.network.http.HttpException;

public class DemoAction extends BaseAction {
   public DemoAction(Context mContext) {
      super(mContext);
   }

   public OrderXMLResponse getOrderXmlDemo(String url) throws HttpException {
      DemoManager mgr = new DemoManager(this.mContext);
      OrderXMLResponse bean = mgr.getOrderXmlDemo(url);
      return bean;
   }

   public ApkConfigResponse checkMaxVersion(String apkCategoryId, String apkTagId) throws HttpException {
      DemoManager mgr = new DemoManager(this.mContext);
      String url = "http://192.168.213.94:8080/ssh/apkConfigAction!checkMaxVersion.action";
      this.params = this.getRequestParams();
      this.params.put("apkCategoryId", apkCategoryId);
      this.params.put("apkTagId", apkTagId);
      Log.e("zdy", "ApkConfig URL-->" + url + "?apkTagId=" + apkCategoryId + "&apkTagId=" + apkTagId);
      return mgr.checkMaxVersion(url, this.params);
   }
}

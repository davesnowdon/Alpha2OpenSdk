package com.ubtech.alpha2.core.action;

import android.content.Context;
import com.ubtech.alpha2.core.base.BaseAction;
import com.ubtech.alpha2.core.business.BinVersionManager;
import com.ubtech.alpha2.core.model.response.BinVersionResponse;
import com.ubtech.alpha2.core.network.http.HttpException;

public class BinVersionAction extends BaseAction {
   public BinVersionAction(Context mContext) {
      super(mContext);
   }

   public BinVersionResponse getBinVerisonXml(String url) throws HttpException {
      BinVersionManager mgr = new BinVersionManager(this.mContext);
      BinVersionResponse bean = mgr.getBinVersionXML(url);
      return bean;
   }
}

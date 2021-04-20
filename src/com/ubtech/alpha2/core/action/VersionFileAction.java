package com.ubtech.alpha2.core.action;

import android.content.Context;
import com.ubtech.alpha2.core.base.BaseAction;
import com.ubtech.alpha2.core.business.VersionFileManager;
import com.ubtech.alpha2.core.model.response.VersionFileResponse;
import com.ubtech.alpha2.core.network.http.HttpException;

public class VersionFileAction extends BaseAction {
   public VersionFileAction(Context mContext) {
      super(mContext);
   }

   public VersionFileResponse getVersionFileXml(String url) throws HttpException {
      VersionFileManager mgr = new VersionFileManager(this.mContext);
      VersionFileResponse bean = mgr.getVersionFileXML(url);
      return bean;
   }
}

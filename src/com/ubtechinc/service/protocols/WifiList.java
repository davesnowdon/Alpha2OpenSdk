package com.ubtechinc.service.protocols;

import android.net.wifi.ScanResult;
import com.ubtechinc.utils.WifiControl;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.msgpack.annotation.Message;

@Message
public class WifiList {
   private List<WifiInfo> wifilist = new ArrayList();

   public WifiList() {
   }

   public void addToWifiList(WifiInfo info) {
      this.wifilist.add(info);
   }

   public List<WifiInfo> getList() {
      return this.wifilist;
   }

   public void setToList(List<ScanResult> result, WifiControl control) {
      if (result != null) {
         Iterator var3 = result.iterator();

         while(var3.hasNext()) {
            ScanResult r = (ScanResult)var3.next();
            WifiInfo info = new WifiInfo();
            info.setSsid(r.SSID);
            int nLevel = control.getWifiSignalLevel(r.level, 4);
            info.setLevel(nLevel);
            info.setCapabilities(r.capabilities);
            boolean isCurrent = control.isCurrentConnectWifi(r.SSID);
            info.setCurrentConnect(isCurrent);
            this.addToWifiList(info);
         }

      }
   }
}

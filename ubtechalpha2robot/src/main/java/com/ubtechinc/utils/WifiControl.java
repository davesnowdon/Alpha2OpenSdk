package com.ubtechinc.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WifiControl {
   private static final String TAG = "WifiControl";
   private StringBuffer mStringBuffer = new StringBuffer();
   private List<ScanResult> listResult;
   private ScanResult mScanResult;
   private WifiManager mWifiManager;
   private WifiInfo mWifiInfo;
   private List<WifiConfiguration> mWifiConfiguration;
   WifiLock mWifiLock;
   Context mContext;
   private WifiConfiguration mLastConnectConfigure;

   public WifiControl(Context context) {
      this.mContext = context;

      try {
         this.mWifiManager = (WifiManager)context.getSystemService("wifi");
      } catch (Exception var3) {
         Log.v("chenlin", var3.toString());
      }

      this.mWifiInfo = this.mWifiManager.getConnectionInfo();
   }

   public String getNetbroadcastAddr() {
      try {
         if (this.mWifiManager == null) {
            return null;
         } else {
            DhcpInfo d = this.mWifiManager.getDhcpInfo();
            return d == null ? null : this.intToIp(d.ipAddress & d.netmask | ~d.netmask);
         }
      } catch (Exception var2) {
         return null;
      }
   }

   private String intToIp(int paramInt) {
      return (paramInt & 255) + "." + (255 & paramInt >> 8) + "." + (255 & paramInt >> 16) + "." + (255 & paramInt >> 24);
   }

   public static final boolean ping() {
      String result = null;

      boolean var8;
      try {
         String ip = "www.baidu.com";
         Process p = Runtime.getRuntime().exec("ping -c 3 -w 100 " + ip);
         InputStream input = p.getInputStream();
         BufferedReader in = new BufferedReader(new InputStreamReader(input));
         StringBuffer stringBuffer = new StringBuffer();
         String content = "";

         while((content = in.readLine()) != null) {
            stringBuffer.append(content);
         }

         Log.d("------ping-----", "result content : " + stringBuffer.toString());
         int status = p.waitFor();
         if (status != 0) {
            result = "failed";
            return false;
         }

         result = "success";
         var8 = true;
      } catch (IOException var13) {
         result = "IOException";
         return false;
      } catch (InterruptedException var14) {
         result = "InterruptedException";
         return false;
      } finally {
         Log.d("----result---", "result = " + result);
      }

      return var8;
   }

   public void openNetCard() {
      if (!this.mWifiManager.isWifiEnabled()) {
         this.mWifiManager.setWifiEnabled(true);
      }

   }

   public boolean isEnable() {
      return this.mWifiManager.isWifiEnabled();
   }

   public void closeNetCard() {
      if (this.mWifiManager.isWifiEnabled()) {
         this.mWifiManager.setWifiEnabled(false);
      }

   }

   public int checkNetCardState() {
      if (this.mWifiManager.getWifiState() == 0) {
         Log.i("WifiControl", "网卡正在关闭");
      } else if (this.mWifiManager.getWifiState() == 1) {
         Log.i("WifiControl", "网卡已经关闭");
      } else if (this.mWifiManager.getWifiState() == 2) {
         Log.i("WifiControl", "网卡正在打开");
      } else if (this.mWifiManager.getWifiState() == 3) {
         Log.i("WifiControl", "网卡已经打开");
      } else {
         Log.i("WifiControl", "---_---晕......没有获取到状态---_---");
      }

      return this.mWifiManager.getWifiState();
   }

   public void scan() {
      this.mWifiManager.startScan();
      this.listResult = this.mWifiManager.getScanResults();
      if (this.listResult != null) {
         Log.i("WifiControl", "当前区域存在无线网络，请查看扫描结果");
      } else {
         Log.i("WifiControl", "当前区域没有无线网络");
      }

   }

   public String getScanResult() {
      if (this.mStringBuffer != null) {
         this.mStringBuffer = new StringBuffer();
      }

      this.scan();
      this.listResult = this.mWifiManager.getScanResults();
      if (this.listResult != null) {
         for(int i = 0; i < this.listResult.size(); ++i) {
            this.mScanResult = (ScanResult)this.listResult.get(i);
            this.mStringBuffer = this.mStringBuffer.append("NO.").append(i + 1).append(" :").append(this.mScanResult.SSID).append("->").append(this.mScanResult.BSSID).append("->").append(this.mScanResult.capabilities).append("->").append(this.mScanResult.frequency).append("->").append(this.mScanResult.level).append("->").append(this.mScanResult.describeContents()).append("\n\n");
         }
      }

      Log.i("WifiControl", this.mStringBuffer.toString());
      return this.mStringBuffer.toString();
   }

   public List<ScanResult> getScanResultForList() {
      this.scan();
      this.listResult = this.mWifiManager.getScanResults();
      return this.listResult;
   }

   public List<ScanResult> getKnowScanResult() {
      List<ScanResult> listScan = this.getScanResultForList();
      if (listScan == null) {
         return null;
      } else {
         List<ScanResult> list = new ArrayList();

         for(int i = 0; i < listScan.size(); ++i) {
            ScanResult mScanResult = (ScanResult)listScan.get(i);
            if (mScanResult != null && !mScanResult.SSID.equals("") && mScanResult.SSID != null && this.IsExsits(mScanResult.SSID) != null) {
               list.add(mScanResult);
            }
         }

         return list;
      }
   }

   public List<ScanResult> getUnKnowScanResult() {
      List<ScanResult> listScan = this.getScanResultForList();
      if (listScan == null) {
         return null;
      } else {
         List<ScanResult> list = new ArrayList();

         for(int i = 0; i < listScan.size(); ++i) {
            ScanResult mScanResult = (ScanResult)listScan.get(i);
            if (mScanResult.SSID != null && !mScanResult.SSID.equals("") && this.IsExsits(mScanResult.SSID) == null) {
               list.add(mScanResult);
            }
         }

         return list;
      }
   }

   public void connect() {
      this.mWifiInfo = this.mWifiManager.getConnectionInfo();
   }

   public WifiInfo getConnectInfo() {
      this.mWifiInfo = this.mWifiManager.getConnectionInfo();
      return this.mWifiInfo;
   }

   public boolean isCurrentConnectWifi(String strSSID) {
      WifiInfo info = this.getConnectInfo();
      return info != null && info.getSSID() != null && info.getSSID().equals(strSSID) || info != null && info.getSSID() != null && info.getSSID().equals("\"" + strSSID + "\"");
   }

   public void disconnectWifi() {
      int netId = this.getNetworkId();
      this.mWifiManager.disableNetwork(netId);
      this.mWifiManager.disconnect();
      this.mWifiInfo = null;
   }

   public boolean checkNetWorkState() {
      this.getConnectInfo();
      if (this.mWifiInfo != null) {
         Log.i("WifiControl", "网络正常工作");
         return true;
      } else {
         Log.i("WifiControl", "网络已断开");
         return false;
      }
   }

   public boolean isWifiConnect() {
      Context var10001 = this.mContext;
      ConnectivityManager connManager = (ConnectivityManager)this.mContext.getSystemService("connectivity");
      NetworkInfo mWifi = null;

      try {
         mWifi = connManager.getNetworkInfo(1);
      } catch (Exception var4) {
         return false;
      }

      return mWifi == null ? false : mWifi.isConnected();
   }

   public int getNetworkId() {
      return this.mWifiInfo == null ? 0 : this.mWifiInfo.getNetworkId();
   }

   public int getIPAddress() {
      return this.mWifiInfo == null ? 0 : this.mWifiInfo.getIpAddress();
   }

   public void acquireWifiLock() {
      this.mWifiLock.acquire();
   }

   public void releaseWifiLock() {
      if (this.mWifiLock.isHeld()) {
         this.mWifiLock.acquire();
      }

   }

   public void creatWifiLock() {
      this.mWifiLock = this.mWifiManager.createWifiLock("Test");
   }

   public List<WifiConfiguration> getConfiguration() {
      return this.mWifiConfiguration = this.mWifiManager.getConfiguredNetworks();
   }

   public void connectConfiguration(int index) {
      if (index < this.mWifiConfiguration.size()) {
         this.mWifiManager.enableNetwork(((WifiConfiguration)this.mWifiConfiguration.get(index)).networkId, true);
         this.mWifiManager.saveConfiguration();
      }
   }

   public String getMacAddress() {
      return this.mWifiInfo == null ? "NULL" : this.mWifiInfo.getMacAddress();
   }

   public String getSSID() {
      return this.mWifiInfo == null ? "NULL" : this.mWifiInfo.getSSID();
   }

   public String getBSSID() {
      return this.mWifiInfo == null ? "NULL" : this.mWifiInfo.getBSSID();
   }

   public String getWifiInfo() {
      return this.mWifiInfo == null ? "NULL" : this.mWifiInfo.toString();
   }

   public int getWifiSignalLevel(int rssi, int numLevels) {
      if (this.mWifiInfo != null) {
         WifiManager var10000 = this.mWifiManager;
         return WifiManager.calculateSignalLevel(rssi, numLevels);
      } else {
         return 0;
      }
   }

   public int addNetwork(WifiConfiguration wcg) {
      int wcgID = this.mWifiManager.addNetwork(wcg);
      if (wcgID != -1) {
         boolean bRet = this.mWifiManager.enableNetwork(wcgID, true);
         if (!bRet) {
            this.mWifiManager.removeNetwork(wcg.networkId);
            return -1;
         }

         this.mWifiManager.saveConfiguration();
         this.mLastConnectConfigure = wcg;
      }

      return wcgID;
   }

   public WifiConfiguration getLastConnectConfigure() {
      return this.mLastConnectConfigure;
   }

   public void connectKnowAp(String strSSID) {
      this.getConfiguration();
      if (this.mWifiConfiguration != null) {
         for(int i = 0; i < this.mWifiConfiguration.size(); ++i) {
            String ssid = "\"" + strSSID + "\"";
            WifiConfiguration wifi = (WifiConfiguration)this.mWifiConfiguration.get(i);
            if (wifi.SSID.equals(ssid)) {
               this.connectConfiguration(i);
               this.mLastConnectConfigure = wifi;
               break;
            }
         }

      }
   }

   public void forgetPassword(String SSID) {
      WifiConfiguration tempConfig = this.IsExsits(SSID);
      if (tempConfig != null) {
         this.mWifiManager.removeNetwork(tempConfig.networkId);
      }

   }

   public void removeNetwork(int netWorkId) {
      this.mWifiManager.removeNetwork(netWorkId);
   }

   public boolean isSecureWifi(String strSecure) {
      return strSecure.contains("WEP") || strSecure.contains("WPA");
   }

   private static boolean isHex(String key) {
      for(int i = key.length() - 1; i >= 0; --i) {
         char c = key.charAt(i);
         if ((c < '0' || c > '9') && (c < 'A' || c > 'F') && (c < 'a' || c > 'f')) {
            return false;
         }
      }

      return true;
   }

   private boolean isHexWepKey(String wepKey) {
      int len = wepKey.length();
      return len != 10 && len != 26 && len != 58 ? false : isHex(wepKey);
   }

   @SuppressLint({"NewApi"})
   public WifiConfiguration CreateWifiInfo(String SSID, String Password, String strSecure) {
      WifiConfiguration config = new WifiConfiguration();
      config.allowedAuthAlgorithms.clear();
      config.allowedGroupCiphers.clear();
      config.allowedKeyManagement.clear();
      config.allowedPairwiseCiphers.clear();
      config.allowedProtocols.clear();
      config.SSID = "\"" + SSID + "\"";
      byte Type;
      if (strSecure.contains("WEP")) {
         Type = 2;
      } else if (strSecure.contains("WPA2")) {
         Type = 4;
      } else if (strSecure.contains("WPA")) {
         Type = 3;
      } else {
         Type = 1;
      }

      WifiConfiguration tempConfig = this.IsExsits(SSID);
      if (tempConfig != null) {
         this.mWifiManager.removeNetwork(tempConfig.networkId);
      }

      if (Type == 1) {
         config.allowedKeyManagement.set(0);
      }

      if (Type == 2) {
         if (!TextUtils.isEmpty(Password)) {
            if (this.isHexWepKey(Password)) {
               config.wepKeys[0] = Password;
            } else {
               config.wepKeys[0] = "\"" + Password + "\"";
            }
         }

         config.allowedKeyManagement.set(0);
         config.allowedAuthAlgorithms.set(0);
         config.allowedAuthAlgorithms.set(1);
         config.wepTxKeyIndex = 0;
      }

      if (Type == 3) {
         config.preSharedKey = "\"" + Password + "\"";
         config.hiddenSSID = true;
         config.allowedAuthAlgorithms.set(0);
         config.allowedGroupCiphers.set(2);
         config.allowedKeyManagement.set(1);
         config.allowedPairwiseCiphers.set(1);
         config.allowedProtocols.set(0);
         config.allowedGroupCiphers.set(3);
         config.allowedPairwiseCiphers.set(2);
         config.status = 2;
      }

      if (Type == 4) {
         config.preSharedKey = "\"" + Password + "\"";
         config.allowedAuthAlgorithms.set(0);
         config.allowedGroupCiphers.set(2);
         config.allowedGroupCiphers.set(3);
         config.allowedKeyManagement.set(1);
         config.allowedPairwiseCiphers.set(1);
         config.allowedPairwiseCiphers.set(2);
         config.allowedProtocols.set(1);
         config.status = 2;
      }

      return config;
   }

   public WifiConfiguration IsExsits(String SSID) {
      List<WifiConfiguration> existingConfigs = this.mWifiManager.getConfiguredNetworks();
      if (existingConfigs == null) {
         return null;
      } else {
         Iterator var3 = existingConfigs.iterator();

         WifiConfiguration existingConfig;
         do {
            if (!var3.hasNext()) {
               return null;
            }

            existingConfig = (WifiConfiguration)var3.next();
         } while(existingConfig.SSID == null || !existingConfig.SSID.equals("\"" + SSID + "\""));

         return existingConfig;
      }
   }

   public void wifiStartScan() {
      this.mWifiManager.startScan();
   }

   public int wifiCheckState() {
      return this.mWifiManager.getWifiState();
   }

   public List<ScanResult> getScanResults() {
      this.listResult = this.mWifiManager.getScanResults();
      return this.listResult;
   }
}

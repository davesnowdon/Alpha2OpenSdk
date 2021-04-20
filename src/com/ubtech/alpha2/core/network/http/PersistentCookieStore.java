package com.ubtech.alpha2.core.network.http;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.http.client.CookieStore;
import org.apache.http.cookie.Cookie;

public class PersistentCookieStore implements CookieStore {
   private static final String COOKIE_PREFS = "CookiePrefsFile";
   private static final String COOKIE_NAME_STORE = "names";
   private static final String COOKIE_NAME_PREFIX = "cookie_";
   private final ConcurrentHashMap<String, Cookie> cookies;
   private final SharedPreferences cookiePrefs;

   public PersistentCookieStore(Context context) {
      this.cookiePrefs = context.getSharedPreferences("CookiePrefsFile", 0);
      this.cookies = new ConcurrentHashMap();
      String storedCookieNames = this.cookiePrefs.getString("names", (String)null);
      if (storedCookieNames != null) {
         String[] cookieNames = TextUtils.split(storedCookieNames, ",");
         String[] var4 = cookieNames;
         int var5 = cookieNames.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            String name = var4[var6];
            String encodedCookie = this.cookiePrefs.getString("cookie_" + name, (String)null);
            if (encodedCookie != null) {
               Cookie decodedCookie = this.decodeCookie(encodedCookie);
               if (decodedCookie != null) {
                  this.cookies.put(name, decodedCookie);
               }
            }
         }

         this.clearExpired(new Date());
      }

   }

   public void addCookie(Cookie cookie) {
      String name = cookie.getName() + cookie.getDomain();
      if (!cookie.isExpired(new Date())) {
         this.cookies.put(name, cookie);
      } else {
         this.cookies.remove(name);
      }

      Editor prefsWriter = this.cookiePrefs.edit();
      prefsWriter.putString("names", TextUtils.join(",", this.cookies.keySet()));
      prefsWriter.putString("cookie_" + name, this.encodeCookie(new SerializableCookie(cookie)));
      prefsWriter.commit();
   }

   public void clear() {
      Editor prefsWriter = this.cookiePrefs.edit();
      Iterator var2 = this.cookies.keySet().iterator();

      while(var2.hasNext()) {
         String name = (String)var2.next();
         prefsWriter.remove("cookie_" + name);
      }

      prefsWriter.remove("names");
      prefsWriter.commit();
      this.cookies.clear();
   }

   public boolean clearExpired(Date date) {
      boolean clearedAny = false;
      Editor prefsWriter = this.cookiePrefs.edit();
      Iterator var4 = this.cookies.entrySet().iterator();

      while(var4.hasNext()) {
         Entry<String, Cookie> entry = (Entry)var4.next();
         String name = (String)entry.getKey();
         Cookie cookie = (Cookie)entry.getValue();
         if (cookie.isExpired(date)) {
            this.cookies.remove(name);
            prefsWriter.remove("cookie_" + name);
            clearedAny = true;
         }
      }

      if (clearedAny) {
         prefsWriter.putString("names", TextUtils.join(",", this.cookies.keySet()));
      }

      prefsWriter.commit();
      return clearedAny;
   }

   public List<Cookie> getCookies() {
      return new ArrayList(this.cookies.values());
   }

   protected String encodeCookie(SerializableCookie cookie) {
      ByteArrayOutputStream os = new ByteArrayOutputStream();

      try {
         ObjectOutputStream outputStream = new ObjectOutputStream(os);
         outputStream.writeObject(cookie);
      } catch (Exception var4) {
         return null;
      }

      return this.byteArrayToHexString(os.toByteArray());
   }

   protected Cookie decodeCookie(String cookieStr) {
      byte[] bytes = this.hexStringToByteArray(cookieStr);
      ByteArrayInputStream is = new ByteArrayInputStream(bytes);
      Cookie cookie = null;

      try {
         ObjectInputStream ois = new ObjectInputStream(is);
         cookie = ((SerializableCookie)ois.readObject()).getCookie();
      } catch (Exception var6) {
         var6.printStackTrace();
      }

      return cookie;
   }

   protected String byteArrayToHexString(byte[] b) {
      StringBuffer sb = new StringBuffer(b.length * 2);
      byte[] var3 = b;
      int var4 = b.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         byte element = var3[var5];
         int v = element & 255;
         if (v < 16) {
            sb.append('0');
         }

         sb.append(Integer.toHexString(v));
      }

      return sb.toString().toUpperCase();
   }

   protected byte[] hexStringToByteArray(String s) {
      int len = s.length();
      byte[] data = new byte[len / 2];

      for(int i = 0; i < len; i += 2) {
         data[i / 2] = (byte)((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
      }

      return data;
   }
}

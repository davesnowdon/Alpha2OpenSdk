package com.ubtechinc.utils;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ClsUtils {
   public ClsUtils() {
   }

   public static boolean createBond(Class btClass, BluetoothDevice btDevice) throws Exception {
      Method createBondMethod = btClass.getMethod("createBond");
      Boolean returnValue = (Boolean)createBondMethod.invoke(btDevice);
      return returnValue;
   }

   public static boolean removeBond(Class btClass, BluetoothDevice btDevice) throws Exception {
      Method removeBondMethod = btClass.getMethod("removeBond");
      Boolean returnValue = (Boolean)removeBondMethod.invoke(btDevice);
      return returnValue;
   }

   public static void printAllInform(Class clsShow) {
      try {
         Method[] hideMethod = clsShow.getMethods();

         int i;
         for(i = 0; i < hideMethod.length; ++i) {
            Log.e("method name", hideMethod[i].getName());
         }

         Field[] allFields = clsShow.getFields();

         for(i = 0; i < allFields.length; ++i) {
            Log.e("Field name", allFields[i].getName());
         }
      } catch (SecurityException var4) {
         var4.printStackTrace();
      } catch (IllegalArgumentException var5) {
         var5.printStackTrace();
      } catch (Exception var6) {
         var6.printStackTrace();
      }

   }
}

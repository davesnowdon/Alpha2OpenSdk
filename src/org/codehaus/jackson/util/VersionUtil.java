package org.codehaus.jackson.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import org.codehaus.jackson.Version;

public class VersionUtil {
   public static final String VERSION_FILE = "VERSION.txt";
   private static final Pattern VERSION_SEPARATOR = Pattern.compile("[-_./;:]");

   public VersionUtil() {
   }

   public static Version versionFor(Class<?> cls) {
      Version version = null;

      try {
         InputStream in = cls.getResourceAsStream("VERSION.txt");
         if (in != null) {
            try {
               BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
               version = parseVersion(br.readLine());
            } finally {
               try {
                  in.close();
               } catch (IOException var10) {
                  throw new RuntimeException(var10);
               }
            }
         }
      } catch (IOException var12) {
      }

      return version == null ? Version.unknownVersion() : version;
   }

   public static Version parseVersion(String versionStr) {
      if (versionStr == null) {
         return null;
      } else {
         versionStr = versionStr.trim();
         if (versionStr.length() == 0) {
            return null;
         } else {
            String[] parts = VERSION_SEPARATOR.split(versionStr);
            if (parts.length < 2) {
               return null;
            } else {
               int major = parseVersionPart(parts[0]);
               int minor = parseVersionPart(parts[1]);
               int patch = parts.length > 2 ? parseVersionPart(parts[2]) : 0;
               String snapshot = parts.length > 3 ? parts[3] : null;
               return new Version(major, minor, patch, snapshot);
            }
         }
      }
   }

   protected static int parseVersionPart(String partStr) {
      partStr = partStr.toString();
      int len = partStr.length();
      int number = 0;

      for(int i = 0; i < len; ++i) {
         char c = partStr.charAt(i);
         if (c > '9' || c < '0') {
            break;
         }

         number = number * 10 + (c - 48);
      }

      return number;
   }
}

package org.codehaus.jackson.io;

import org.codehaus.jackson.SerializableString;
import org.codehaus.jackson.util.CharTypes;

public abstract class CharacterEscapes {
   public static final int ESCAPE_NONE = 0;
   public static final int ESCAPE_STANDARD = -1;
   public static final int ESCAPE_CUSTOM = -2;

   public CharacterEscapes() {
   }

   public abstract int[] getEscapeCodesForAscii();

   public abstract SerializableString getEscapeSequence(int var1);

   public static int[] standardAsciiEscapesForJSON() {
      int[] esc = CharTypes.get7BitOutputEscapes();
      int len = esc.length;
      int[] result = new int[len];
      System.arraycopy(esc, 0, result, 0, esc.length);
      return result;
   }
}

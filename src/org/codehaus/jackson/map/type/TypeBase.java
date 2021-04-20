package org.codehaus.jackson.map.type;

import org.codehaus.jackson.type.JavaType;

public abstract class TypeBase extends JavaType {
   volatile String _canonicalName;

   protected TypeBase(Class<?> raw, int hash) {
      super(raw, hash);
   }

   public String toCanonical() {
      String str = this._canonicalName;
      if (str == null) {
         str = this.buildCanonicalName();
      }

      return str;
   }

   protected abstract String buildCanonicalName();

   protected final JavaType copyHandlers(JavaType fromType) {
      this._valueHandler = fromType.getValueHandler();
      this._typeHandler = fromType.getTypeHandler();
      return this;
   }

   public abstract StringBuilder getGenericSignature(StringBuilder var1);

   public abstract StringBuilder getErasedSignature(StringBuilder var1);

   protected static StringBuilder _classSignature(Class<?> cls, StringBuilder sb, boolean trailingSemicolon) {
      if (cls.isPrimitive()) {
         if (cls == Boolean.TYPE) {
            sb.append('Z');
         } else if (cls == Byte.TYPE) {
            sb.append('B');
         } else if (cls == Short.TYPE) {
            sb.append('S');
         } else if (cls == Character.TYPE) {
            sb.append('C');
         } else if (cls == Integer.TYPE) {
            sb.append('I');
         } else if (cls == Long.TYPE) {
            sb.append('J');
         } else if (cls == Float.TYPE) {
            sb.append('F');
         } else if (cls == Double.TYPE) {
            sb.append('D');
         } else {
            if (cls != Void.TYPE) {
               throw new IllegalStateException("Unrecognized primitive type: " + cls.getName());
            }

            sb.append('V');
         }
      } else {
         sb.append('L');
         String name = cls.getName();
         int i = 0;

         for(int len = name.length(); i < len; ++i) {
            char c = name.charAt(i);
            if (c == '.') {
               c = '/';
            }

            sb.append(c);
         }

         if (trailingSemicolon) {
            sb.append(';');
         }
      }

      return sb;
   }
}

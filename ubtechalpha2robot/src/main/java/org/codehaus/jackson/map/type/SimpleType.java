package org.codehaus.jackson.map.type;

import java.util.Collection;
import java.util.Map;
import org.codehaus.jackson.type.JavaType;

public final class SimpleType extends TypeBase {
   protected final JavaType[] _typeParameters;
   protected final String[] _typeNames;

   protected SimpleType(Class<?> cls) {
      this(cls, (String[])null, (JavaType[])null);
   }

   protected SimpleType(Class<?> cls, String[] typeNames, JavaType[] typeParams) {
      super(cls, 0);
      if (typeNames != null && typeNames.length != 0) {
         this._typeNames = typeNames;
         this._typeParameters = typeParams;
      } else {
         this._typeNames = null;
         this._typeParameters = null;
      }

   }

   public static SimpleType constructUnsafe(Class<?> raw) {
      return new SimpleType(raw, (String[])null, (JavaType[])null);
   }

   protected JavaType _narrow(Class<?> subclass) {
      return new SimpleType(subclass, this._typeNames, this._typeParameters);
   }

   public JavaType narrowContentsBy(Class<?> subclass) {
      throw new IllegalArgumentException("Internal error: SimpleType.narrowContentsBy() should never be called");
   }

   public JavaType widenContentsBy(Class<?> subclass) {
      throw new IllegalArgumentException("Internal error: SimpleType.widenContentsBy() should never be called");
   }

   public static SimpleType construct(Class<?> cls) {
      if (Map.class.isAssignableFrom(cls)) {
         throw new IllegalArgumentException("Can not construct SimpleType for a Map (class: " + cls.getName() + ")");
      } else if (Collection.class.isAssignableFrom(cls)) {
         throw new IllegalArgumentException("Can not construct SimpleType for a Collection (class: " + cls.getName() + ")");
      } else if (cls.isArray()) {
         throw new IllegalArgumentException("Can not construct SimpleType for an array (class: " + cls.getName() + ")");
      } else {
         return new SimpleType(cls);
      }
   }

   public SimpleType withTypeHandler(Object h) {
      SimpleType newInstance = new SimpleType(this._class, this._typeNames, this._typeParameters);
      newInstance._typeHandler = h;
      return newInstance;
   }

   public JavaType withContentTypeHandler(Object h) {
      throw new IllegalArgumentException("Simple types have no content types; can not call withContenTypeHandler()");
   }

   protected String buildCanonicalName() {
      StringBuilder sb = new StringBuilder();
      sb.append(this._class.getName());
      if (this._typeParameters != null && this._typeParameters.length > 0) {
         sb.append('<');
         boolean first = true;
         JavaType[] arr$ = this._typeParameters;
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            JavaType t = arr$[i$];
            if (first) {
               first = false;
            } else {
               sb.append(',');
            }

            sb.append(t.toCanonical());
         }

         sb.append('>');
      }

      return sb.toString();
   }

   public boolean isContainerType() {
      return false;
   }

   public int containedTypeCount() {
      return this._typeParameters == null ? 0 : this._typeParameters.length;
   }

   public JavaType containedType(int index) {
      return index >= 0 && this._typeParameters != null && index < this._typeParameters.length ? this._typeParameters[index] : null;
   }

   public String containedTypeName(int index) {
      return index >= 0 && this._typeNames != null && index < this._typeNames.length ? this._typeNames[index] : null;
   }

   public StringBuilder getErasedSignature(StringBuilder sb) {
      return _classSignature(this._class, sb, true);
   }

   public StringBuilder getGenericSignature(StringBuilder sb) {
      _classSignature(this._class, sb, false);
      if (this._typeParameters != null) {
         sb.append('<');
         JavaType[] arr$ = this._typeParameters;
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            JavaType param = arr$[i$];
            sb = param.getGenericSignature(sb);
         }

         sb.append('>');
      }

      sb.append(';');
      return sb;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder(40);
      sb.append("[simple type, class ").append(this.buildCanonicalName()).append(']');
      return sb.toString();
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (o == null) {
         return false;
      } else if (o.getClass() != this.getClass()) {
         return false;
      } else {
         SimpleType other = (SimpleType)o;
         if (other._class != this._class) {
            return false;
         } else {
            JavaType[] p1 = this._typeParameters;
            JavaType[] p2 = other._typeParameters;
            if (p1 != null) {
               if (p2 == null) {
                  return false;
               } else if (p1.length != p2.length) {
                  return false;
               } else {
                  int i = 0;

                  for(int len = p1.length; i < len; ++i) {
                     if (!p1[i].equals(p2[i])) {
                        return false;
                     }
                  }

                  return true;
               }
            } else {
               return p2 == null || p2.length == 0;
            }
         }
      }
   }
}

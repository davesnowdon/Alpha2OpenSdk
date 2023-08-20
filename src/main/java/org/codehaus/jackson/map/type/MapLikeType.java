package org.codehaus.jackson.map.type;

import java.util.Map;
import org.codehaus.jackson.type.JavaType;

public class MapLikeType extends TypeBase {
   protected final JavaType _keyType;
   protected final JavaType _valueType;

   protected MapLikeType(Class<?> mapType, JavaType keyT, JavaType valueT) {
      super(mapType, keyT.hashCode() ^ valueT.hashCode());
      this._keyType = keyT;
      this._valueType = valueT;
   }

   public static MapLikeType construct(Class<?> rawType, JavaType keyT, JavaType valueT) {
      return new MapLikeType(rawType, keyT, valueT);
   }

   protected JavaType _narrow(Class<?> subclass) {
      return new MapLikeType(subclass, this._keyType, this._valueType);
   }

   public JavaType narrowContentsBy(Class<?> contentClass) {
      return (JavaType)(contentClass == this._valueType.getRawClass() ? this : (new MapLikeType(this._class, this._keyType, this._valueType.narrowBy(contentClass))).copyHandlers(this));
   }

   public JavaType widenContentsBy(Class<?> contentClass) {
      return (JavaType)(contentClass == this._valueType.getRawClass() ? this : (new MapLikeType(this._class, this._keyType, this._valueType.widenBy(contentClass))).copyHandlers(this));
   }

   public JavaType narrowKey(Class<?> keySubclass) {
      return (JavaType)(keySubclass == this._keyType.getRawClass() ? this : (new MapLikeType(this._class, this._keyType.narrowBy(keySubclass), this._valueType)).copyHandlers(this));
   }

   public JavaType widenKey(Class<?> keySubclass) {
      return (JavaType)(keySubclass == this._keyType.getRawClass() ? this : (new MapLikeType(this._class, this._keyType.widenBy(keySubclass), this._valueType)).copyHandlers(this));
   }

   public MapLikeType withTypeHandler(Object h) {
      MapLikeType newInstance = new MapLikeType(this._class, this._keyType, this._valueType);
      newInstance._typeHandler = h;
      return newInstance;
   }

   public MapLikeType withContentTypeHandler(Object h) {
      return new MapLikeType(this._class, this._keyType, this._valueType.withTypeHandler(h));
   }

   protected String buildCanonicalName() {
      StringBuilder sb = new StringBuilder();
      sb.append(this._class.getName());
      if (this._keyType != null) {
         sb.append('<');
         sb.append(this._keyType.toCanonical());
         sb.append(',');
         sb.append(this._valueType.toCanonical());
         sb.append('>');
      }

      return sb.toString();
   }

   public boolean isContainerType() {
      return true;
   }

   public boolean isMapLikeType() {
      return true;
   }

   public JavaType getKeyType() {
      return this._keyType;
   }

   public JavaType getContentType() {
      return this._valueType;
   }

   public int containedTypeCount() {
      return 2;
   }

   public JavaType containedType(int index) {
      if (index == 0) {
         return this._keyType;
      } else {
         return index == 1 ? this._valueType : null;
      }
   }

   public String containedTypeName(int index) {
      if (index == 0) {
         return "K";
      } else {
         return index == 1 ? "V" : null;
      }
   }

   public StringBuilder getErasedSignature(StringBuilder sb) {
      return _classSignature(this._class, sb, true);
   }

   public StringBuilder getGenericSignature(StringBuilder sb) {
      _classSignature(this._class, sb, false);
      sb.append('<');
      this._keyType.getGenericSignature(sb);
      this._valueType.getGenericSignature(sb);
      sb.append(">;");
      return sb;
   }

   public boolean isTrueMapType() {
      return Map.class.isAssignableFrom(this._class);
   }

   public String toString() {
      return "[map-like type; class " + this._class.getName() + ", " + this._keyType + " -> " + this._valueType + "]";
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (o == null) {
         return false;
      } else if (o.getClass() != this.getClass()) {
         return false;
      } else {
         MapLikeType other = (MapLikeType)o;
         return this._class == other._class && this._keyType.equals(other._keyType) && this._valueType.equals(other._valueType);
      }
   }
}

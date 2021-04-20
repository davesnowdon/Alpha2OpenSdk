package org.codehaus.jackson.map.type;

import org.codehaus.jackson.type.JavaType;

public final class MapType extends MapLikeType {
   private MapType(Class<?> mapType, JavaType keyT, JavaType valueT) {
      super(mapType, keyT, valueT);
   }

   public static MapType construct(Class<?> rawType, JavaType keyT, JavaType valueT) {
      return new MapType(rawType, keyT, valueT);
   }

   protected JavaType _narrow(Class<?> subclass) {
      return new MapType(subclass, this._keyType, this._valueType);
   }

   public JavaType narrowContentsBy(Class<?> contentClass) {
      return (JavaType)(contentClass == this._valueType.getRawClass() ? this : (new MapType(this._class, this._keyType, this._valueType.narrowBy(contentClass))).copyHandlers(this));
   }

   public JavaType widenContentsBy(Class<?> contentClass) {
      return (JavaType)(contentClass == this._valueType.getRawClass() ? this : (new MapType(this._class, this._keyType, this._valueType.widenBy(contentClass))).copyHandlers(this));
   }

   public JavaType narrowKey(Class<?> keySubclass) {
      return (JavaType)(keySubclass == this._keyType.getRawClass() ? this : (new MapType(this._class, this._keyType.narrowBy(keySubclass), this._valueType)).copyHandlers(this));
   }

   public JavaType widenKey(Class<?> keySubclass) {
      return (JavaType)(keySubclass == this._keyType.getRawClass() ? this : (new MapType(this._class, this._keyType.widenBy(keySubclass), this._valueType)).copyHandlers(this));
   }

   public MapType withTypeHandler(Object h) {
      MapType newInstance = new MapType(this._class, this._keyType, this._valueType);
      newInstance._typeHandler = h;
      return newInstance;
   }

   public MapType withContentTypeHandler(Object h) {
      return new MapType(this._class, this._keyType, this._valueType.withTypeHandler(h));
   }

   public String toString() {
      return "[map type; class " + this._class.getName() + ", " + this._keyType + " -> " + this._valueType + "]";
   }
}

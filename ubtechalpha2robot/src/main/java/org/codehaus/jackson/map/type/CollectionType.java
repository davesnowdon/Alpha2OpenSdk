package org.codehaus.jackson.map.type;

import org.codehaus.jackson.type.JavaType;

public final class CollectionType extends CollectionLikeType {
   private CollectionType(Class<?> collT, JavaType elemT) {
      super(collT, elemT);
   }

   protected JavaType _narrow(Class<?> subclass) {
      return new CollectionType(subclass, this._elementType);
   }

   public JavaType narrowContentsBy(Class<?> contentClass) {
      return (JavaType)(contentClass == this._elementType.getRawClass() ? this : (new CollectionType(this._class, this._elementType.narrowBy(contentClass))).copyHandlers(this));
   }

   public JavaType widenContentsBy(Class<?> contentClass) {
      return (JavaType)(contentClass == this._elementType.getRawClass() ? this : (new CollectionType(this._class, this._elementType.widenBy(contentClass))).copyHandlers(this));
   }

   public static CollectionType construct(Class<?> rawType, JavaType elemT) {
      return new CollectionType(rawType, elemT);
   }

   public CollectionType withTypeHandler(Object h) {
      CollectionType newInstance = new CollectionType(this._class, this._elementType);
      newInstance._typeHandler = h;
      return newInstance;
   }

   public CollectionType withContentTypeHandler(Object h) {
      return new CollectionType(this._class, this._elementType.withTypeHandler(h));
   }

   public String toString() {
      return "[collection type; class " + this._class.getName() + ", contains " + this._elementType + "]";
   }
}

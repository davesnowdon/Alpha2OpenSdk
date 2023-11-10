package org.codehaus.jackson.map.type;

import java.util.Collection;
import org.codehaus.jackson.type.JavaType;

public class CollectionLikeType extends TypeBase {
   protected final JavaType _elementType;

   protected CollectionLikeType(Class<?> collT, JavaType elemT) {
      super(collT, elemT.hashCode());
      this._elementType = elemT;
   }

   protected JavaType _narrow(Class<?> subclass) {
      return new CollectionLikeType(subclass, this._elementType);
   }

   public JavaType narrowContentsBy(Class<?> contentClass) {
      return (JavaType)(contentClass == this._elementType.getRawClass() ? this : (new CollectionLikeType(this._class, this._elementType.narrowBy(contentClass))).copyHandlers(this));
   }

   public JavaType widenContentsBy(Class<?> contentClass) {
      return (JavaType)(contentClass == this._elementType.getRawClass() ? this : (new CollectionLikeType(this._class, this._elementType.widenBy(contentClass))).copyHandlers(this));
   }

   public static CollectionLikeType construct(Class<?> rawType, JavaType elemT) {
      return new CollectionLikeType(rawType, elemT);
   }

   public CollectionLikeType withTypeHandler(Object h) {
      CollectionLikeType newInstance = new CollectionLikeType(this._class, this._elementType);
      newInstance._typeHandler = h;
      return newInstance;
   }

   public CollectionLikeType withContentTypeHandler(Object h) {
      return new CollectionLikeType(this._class, this._elementType.withTypeHandler(h));
   }

   public boolean isContainerType() {
      return true;
   }

   public boolean isCollectionLikeType() {
      return true;
   }

   public JavaType getContentType() {
      return this._elementType;
   }

   public int containedTypeCount() {
      return 1;
   }

   public JavaType containedType(int index) {
      return index == 0 ? this._elementType : null;
   }

   public String containedTypeName(int index) {
      return index == 0 ? "E" : null;
   }

   public StringBuilder getErasedSignature(StringBuilder sb) {
      return _classSignature(this._class, sb, true);
   }

   public StringBuilder getGenericSignature(StringBuilder sb) {
      _classSignature(this._class, sb, false);
      sb.append('<');
      this._elementType.getGenericSignature(sb);
      sb.append(">;");
      return sb;
   }

   protected String buildCanonicalName() {
      StringBuilder sb = new StringBuilder();
      sb.append(this._class.getName());
      if (this._elementType != null) {
         sb.append('<');
         sb.append(this._elementType.toCanonical());
         sb.append('>');
      }

      return sb.toString();
   }

   public boolean isTrueCollectionType() {
      return Collection.class.isAssignableFrom(this._class);
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (o == null) {
         return false;
      } else if (o.getClass() != this.getClass()) {
         return false;
      } else {
         CollectionLikeType other = (CollectionLikeType)o;
         return this._class == other._class && this._elementType.equals(other._elementType);
      }
   }

   public String toString() {
      return "[collection-like type; class " + this._class.getName() + ", contains " + this._elementType + "]";
   }
}

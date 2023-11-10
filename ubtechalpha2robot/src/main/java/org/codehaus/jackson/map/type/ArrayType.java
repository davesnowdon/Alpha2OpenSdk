package org.codehaus.jackson.map.type;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import org.codehaus.jackson.type.JavaType;

public final class ArrayType extends TypeBase {
   final JavaType _componentType;
   final Object _emptyArray;

   private ArrayType(JavaType componentType, Object emptyInstance) {
      super(emptyInstance.getClass(), componentType.hashCode());
      this._componentType = componentType;
      this._emptyArray = emptyInstance;
   }

   public static ArrayType construct(JavaType componentType) {
      Object emptyInstance = Array.newInstance(componentType.getRawClass(), 0);
      return new ArrayType(componentType, emptyInstance);
   }

   public ArrayType withTypeHandler(Object h) {
      ArrayType newInstance = new ArrayType(this._componentType, this._emptyArray);
      newInstance._typeHandler = h;
      return newInstance;
   }

   public ArrayType withContentTypeHandler(Object h) {
      return new ArrayType(this._componentType.withTypeHandler(h), this._emptyArray);
   }

   protected String buildCanonicalName() {
      return this._class.getName();
   }

   protected JavaType _narrow(Class<?> subclass) {
      if (!subclass.isArray()) {
         throw new IllegalArgumentException("Incompatible narrowing operation: trying to narrow " + this.toString() + " to class " + subclass.getName());
      } else {
         Class<?> newCompClass = subclass.getComponentType();
         JavaType newCompType = TypeFactory.defaultInstance().constructType((Type)newCompClass);
         return construct(newCompType);
      }
   }

   public JavaType narrowContentsBy(Class<?> contentClass) {
      return (JavaType)(contentClass == this._componentType.getRawClass() ? this : construct(this._componentType.narrowBy(contentClass)).copyHandlers(this));
   }

   public JavaType widenContentsBy(Class<?> contentClass) {
      return (JavaType)(contentClass == this._componentType.getRawClass() ? this : construct(this._componentType.widenBy(contentClass)).copyHandlers(this));
   }

   public boolean isArrayType() {
      return true;
   }

   public boolean isAbstract() {
      return false;
   }

   public boolean isConcrete() {
      return true;
   }

   public boolean hasGenericTypes() {
      return this._componentType.hasGenericTypes();
   }

   public String containedTypeName(int index) {
      return index == 0 ? "E" : null;
   }

   public boolean isContainerType() {
      return true;
   }

   public JavaType getContentType() {
      return this._componentType;
   }

   public int containedTypeCount() {
      return 1;
   }

   public JavaType containedType(int index) {
      return index == 0 ? this._componentType : null;
   }

   public StringBuilder getGenericSignature(StringBuilder sb) {
      sb.append('[');
      return this._componentType.getGenericSignature(sb);
   }

   public StringBuilder getErasedSignature(StringBuilder sb) {
      sb.append('[');
      return this._componentType.getErasedSignature(sb);
   }

   public String toString() {
      return "[array type, component type: " + this._componentType + "]";
   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (o == null) {
         return false;
      } else if (o.getClass() != this.getClass()) {
         return false;
      } else {
         ArrayType other = (ArrayType)o;
         return this._componentType.equals(other._componentType);
      }
   }
}

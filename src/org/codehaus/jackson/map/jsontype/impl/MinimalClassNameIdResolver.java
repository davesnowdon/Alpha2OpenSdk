package org.codehaus.jackson.map.jsontype.impl;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;

public class MinimalClassNameIdResolver extends ClassNameIdResolver {
   protected final String _basePackageName;
   protected final String _basePackagePrefix;

   protected MinimalClassNameIdResolver(JavaType baseType, TypeFactory typeFactory) {
      super(baseType, typeFactory);
      String base = baseType.getRawClass().getName();
      int ix = base.lastIndexOf(46);
      if (ix < 0) {
         this._basePackageName = "";
         this._basePackagePrefix = ".";
      } else {
         this._basePackagePrefix = base.substring(0, ix + 1);
         this._basePackageName = base.substring(0, ix);
      }

   }

   public JsonTypeInfo.Id getMechanism() {
      return JsonTypeInfo.Id.MINIMAL_CLASS;
   }

   public String idFromValue(Object value) {
      String n = value.getClass().getName();
      return n.startsWith(this._basePackagePrefix) ? n.substring(this._basePackagePrefix.length() - 1) : n;
   }

   public JavaType typeFromId(String id) {
      if (id.startsWith(".")) {
         StringBuilder sb = new StringBuilder(id.length() + this._basePackageName.length());
         if (this._basePackageName.length() == 0) {
            sb.append(id.substring(1));
         } else {
            sb.append(this._basePackageName).append(id);
         }

         id = sb.toString();
      }

      return super.typeFromId(id);
   }
}

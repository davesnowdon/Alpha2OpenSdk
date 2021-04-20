package org.codehaus.jackson.map.jsontype.impl;

import java.util.Collection;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.TypeDeserializer;
import org.codehaus.jackson.map.TypeSerializer;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.map.jsontype.TypeIdResolver;
import org.codehaus.jackson.map.jsontype.TypeResolverBuilder;
import org.codehaus.jackson.type.JavaType;

public class StdTypeResolverBuilder implements TypeResolverBuilder<StdTypeResolverBuilder> {
   protected JsonTypeInfo.Id _idType;
   protected JsonTypeInfo.As _includeAs;
   protected String _typeProperty;
   protected TypeIdResolver _customIdResolver;

   public StdTypeResolverBuilder() {
   }

   public StdTypeResolverBuilder init(JsonTypeInfo.Id idType, TypeIdResolver idRes) {
      if (idType == null) {
         throw new IllegalArgumentException("idType can not be null");
      } else {
         this._idType = idType;
         this._customIdResolver = idRes;
         this._typeProperty = idType.getDefaultPropertyName();
         return this;
      }
   }

   public TypeSerializer buildTypeSerializer(SerializationConfig config, JavaType baseType, Collection<NamedType> subtypes, BeanProperty property) {
      TypeIdResolver idRes = this.idResolver(config, baseType, subtypes, true, false);
      switch(this._includeAs) {
      case WRAPPER_ARRAY:
         return new AsArrayTypeSerializer(idRes, property);
      case PROPERTY:
         return new AsPropertyTypeSerializer(idRes, property, this._typeProperty);
      case WRAPPER_OBJECT:
         return new AsWrapperTypeSerializer(idRes, property);
      default:
         throw new IllegalStateException("Do not know how to construct standard type serializer for inclusion type: " + this._includeAs);
      }
   }

   public TypeDeserializer buildTypeDeserializer(DeserializationConfig config, JavaType baseType, Collection<NamedType> subtypes, BeanProperty property) {
      TypeIdResolver idRes = this.idResolver(config, baseType, subtypes, false, true);
      switch(this._includeAs) {
      case WRAPPER_ARRAY:
         return new AsArrayTypeDeserializer(baseType, idRes, property);
      case PROPERTY:
         return new AsPropertyTypeDeserializer(baseType, idRes, property, this._typeProperty);
      case WRAPPER_OBJECT:
         return new AsWrapperTypeDeserializer(baseType, idRes, property);
      default:
         throw new IllegalStateException("Do not know how to construct standard type serializer for inclusion type: " + this._includeAs);
      }
   }

   public StdTypeResolverBuilder inclusion(JsonTypeInfo.As includeAs) {
      if (includeAs == null) {
         throw new IllegalArgumentException("includeAs can not be null");
      } else {
         this._includeAs = includeAs;
         return this;
      }
   }

   public StdTypeResolverBuilder typeProperty(String typeIdPropName) {
      if (typeIdPropName == null || typeIdPropName.length() == 0) {
         typeIdPropName = this._idType.getDefaultPropertyName();
      }

      this._typeProperty = typeIdPropName;
      return this;
   }

   public String getTypeProperty() {
      return this._typeProperty;
   }

   protected TypeIdResolver idResolver(MapperConfig<?> config, JavaType baseType, Collection<NamedType> subtypes, boolean forSer, boolean forDeser) {
      if (this._customIdResolver != null) {
         return this._customIdResolver;
      } else if (this._idType == null) {
         throw new IllegalStateException("Can not build, 'init()' not yet called");
      } else {
         switch(this._idType) {
         case CLASS:
            return new ClassNameIdResolver(baseType, config.getTypeFactory());
         case MINIMAL_CLASS:
            return new MinimalClassNameIdResolver(baseType, config.getTypeFactory());
         case NAME:
            return TypeNameIdResolver.construct(config, baseType, subtypes, forSer, forDeser);
         case CUSTOM:
         case NONE:
         default:
            throw new IllegalStateException("Do not know how to construct standard type id resolver for idType: " + this._idType);
         }
      }
   }
}

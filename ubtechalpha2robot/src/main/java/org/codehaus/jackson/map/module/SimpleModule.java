package org.codehaus.jackson.map.module;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.KeyDeserializer;
import org.codehaus.jackson.map.Module;

public class SimpleModule extends Module {
   protected final String _name;
   protected final Version _version;
   protected SimpleSerializers _serializers = null;
   protected SimpleDeserializers _deserializers = null;
   protected SimpleSerializers _keySerializers = null;
   protected SimpleKeyDeserializers _keyDeserializers = null;
   protected SimpleAbstractTypeResolver _abstractTypes = null;

   public SimpleModule(String name, Version version) {
      this._name = name;
      this._version = version;
   }

   public SimpleModule addSerializer(JsonSerializer ser) {
      if (this._serializers == null) {
         this._serializers = new SimpleSerializers();
      }

      this._serializers.addSerializer(ser);
      return this;
   }

   public  SimpleModule addSerializer(Class type, JsonSerializer ser) {
      if (this._serializers == null) {
         this._serializers = new SimpleSerializers();
      }

      this._serializers.addSerializer(type, ser);
      return this;
   }

   public  SimpleModule addKeySerializer(Class type, JsonSerializer ser) {
      if (this._keySerializers == null) {
         this._keySerializers = new SimpleSerializers();
      }

      this._keySerializers.addSerializer(type, ser);
      return this;
   }

   public  SimpleModule addDeserializer(Class type, JsonDeserializer deser) {
      if (this._deserializers == null) {
         this._deserializers = new SimpleDeserializers();
      }

      this._deserializers.addDeserializer(type, deser);
      return this;
   }

   public SimpleModule addKeyDeserializer(Class type, KeyDeserializer deser) {
      if (this._keyDeserializers == null) {
         this._keyDeserializers = new SimpleKeyDeserializers();
      }

      this._keyDeserializers.addDeserializer(type, deser);
      return this;
   }

   public  SimpleModule addAbstractTypeMapping(Class superType, Class subType) {
      if (this._abstractTypes == null) {
         this._abstractTypes = new SimpleAbstractTypeResolver();
      }

      this._abstractTypes = this._abstractTypes.addMapping(superType, subType);
      return this;
   }

   public String getModuleName() {
      return this._name;
   }

   public void setupModule(Module.SetupContext context) {
      if (this._serializers != null) {
         context.addSerializers(this._serializers);
      }

      if (this._deserializers != null) {
         context.addDeserializers(this._deserializers);
      }

      if (this._keySerializers != null) {
         context.addKeySerializers(this._keySerializers);
      }

      if (this._keyDeserializers != null) {
         context.addKeyDeserializers(this._keyDeserializers);
      }

      if (this._abstractTypes != null) {
         context.addAbstractTypeResolver(this._abstractTypes);
      }

   }

   public Version version() {
      return this._version;
   }
}

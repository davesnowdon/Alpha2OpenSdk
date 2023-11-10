package org.codehaus.jackson.map.jsontype.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.map.MapperConfig;
import org.codehaus.jackson.map.introspect.BasicBeanDescription;
import org.codehaus.jackson.map.jsontype.NamedType;
import org.codehaus.jackson.type.JavaType;

public class TypeNameIdResolver extends TypeIdResolverBase {
   protected final MapperConfig<?> _config;
   protected final HashMap<String, String> _typeToId;
   protected final HashMap<String, JavaType> _idToType;

   protected TypeNameIdResolver(MapperConfig<?> config, JavaType baseType, HashMap<String, String> typeToId, HashMap<String, JavaType> idToType) {
      super(baseType, config.getTypeFactory());
      this._config = config;
      this._typeToId = typeToId;
      this._idToType = idToType;
   }

   public static TypeNameIdResolver construct(MapperConfig<?> config, JavaType baseType, Collection<NamedType> subtypes, boolean forSer, boolean forDeser) {
      if (forSer == forDeser) {
         throw new IllegalArgumentException();
      } else {
         HashMap<String, String> typeToId = null;
         HashMap<String, JavaType> idToType = null;
         if (forSer) {
            typeToId = new HashMap();
         }

         if (forDeser) {
            idToType = new HashMap();
         }

         if (subtypes != null) {
            Iterator i$ = subtypes.iterator();

            while(true) {
               Class cls;
               String id;
               JavaType prev;
               do {
                  do {
                     if (!i$.hasNext()) {
                        return new TypeNameIdResolver(config, baseType, typeToId, idToType);
                     }

                     NamedType t = (NamedType)i$.next();
                     cls = t.getType();
                     id = t.hasName() ? t.getName() : _defaultTypeId(cls);
                     if (forSer) {
                        typeToId.put(cls.getName(), id);
                     }
                  } while(!forDeser);

                  prev = (JavaType)idToType.get(id);
               } while(prev != null && cls.isAssignableFrom(prev.getRawClass()));

               idToType.put(id, config.constructType(cls));
            }
         } else {
            return new TypeNameIdResolver(config, baseType, typeToId, idToType);
         }
      }
   }

   public JsonTypeInfo.Id getMechanism() {
      return JsonTypeInfo.Id.NAME;
   }

   public String idFromValue(Object value) {
      Class<?> cls = value.getClass();
      String key = cls.getName();
      synchronized(this._typeToId) {
         String name = (String)this._typeToId.get(key);
         if (name == null) {
            if (this._config.isAnnotationProcessingEnabled()) {
               BasicBeanDescription beanDesc = (BasicBeanDescription)this._config.introspectClassAnnotations(cls);
               name = this._config.getAnnotationIntrospector().findTypeName(beanDesc.getClassInfo());
            }

            if (name == null) {
               name = _defaultTypeId(cls);
            }

            this._typeToId.put(key, name);
         }

         return name;
      }
   }

   public String idFromValueAndType(Object value, Class<?> type) {
      return this.idFromValue(value);
   }

   public JavaType typeFromId(String id) throws IllegalArgumentException {
      JavaType t = (JavaType)this._idToType.get(id);
      return t;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append('[').append(this.getClass().getName());
      sb.append("; id-to-type=").append(this._idToType);
      sb.append(']');
      return sb.toString();
   }

   protected static String _defaultTypeId(Class<?> cls) {
      String n = cls.getName();
      int ix = n.lastIndexOf(46);
      return ix < 0 ? n : n.substring(ix + 1);
   }
}

package org.codehaus.jackson.map.type;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.codehaus.jackson.map.util.ArrayBuilders;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

public final class TypeFactory {
   /** @deprecated */
   @Deprecated
   public static final TypeFactory instance = new TypeFactory();
   private static final JavaType[] NO_TYPES = new JavaType[0];
   protected final TypeModifier[] _modifiers;
   protected final TypeParser _parser;

   private TypeFactory() {
      this._parser = new TypeParser(this);
      this._modifiers = null;
   }

   protected TypeFactory(TypeParser p, TypeModifier[] mods) {
      this._parser = p;
      this._modifiers = mods;
   }

   public TypeFactory withModifier(TypeModifier mod) {
      return this._modifiers == null ? new TypeFactory(this._parser, new TypeModifier[]{mod}) : new TypeFactory(this._parser, (TypeModifier[])ArrayBuilders.insertInListNoDup(this._modifiers, mod));
   }

   public static TypeFactory defaultInstance() {
      return instance;
   }

   public static JavaType unknownType() {
      return defaultInstance()._unknownType();
   }

   public static Class<?> rawClass(Type t) {
      return t instanceof Class ? (Class)t : defaultInstance().constructType(t).getRawClass();
   }

   /** @deprecated */
   @Deprecated
   public static JavaType type(Type t) {
      return instance._constructType(t, (TypeBindings)null);
   }

   /** @deprecated */
   @Deprecated
   public static JavaType type(Type type, Class<?> context) {
      return instance.constructType(type, context);
   }

   /** @deprecated */
   @Deprecated
   public static JavaType type(Type type, JavaType context) {
      return instance.constructType(type, context);
   }

   /** @deprecated */
   @Deprecated
   public static JavaType type(Type type, TypeBindings bindings) {
      return instance._constructType(type, bindings);
   }

   /** @deprecated */
   @Deprecated
   public static JavaType type(TypeReference<?> ref) {
      return instance.constructType(ref.getType());
   }

   /** @deprecated */
   @Deprecated
   public static JavaType arrayType(Class<?> elementType) {
      return instance.constructArrayType(instance.constructType((Type)elementType));
   }

   /** @deprecated */
   @Deprecated
   public static JavaType arrayType(JavaType elementType) {
      return instance.constructArrayType(elementType);
   }

   /** @deprecated */
   @Deprecated
   public static JavaType collectionType(Class<? extends Collection> collectionType, Class<?> elementType) {
      return instance.constructCollectionType(collectionType, instance.constructType((Type)elementType));
   }

   /** @deprecated */
   @Deprecated
   public static JavaType collectionType(Class<? extends Collection> collectionType, JavaType elementType) {
      return instance.constructCollectionType(collectionType, elementType);
   }

   /** @deprecated */
   @Deprecated
   public static JavaType mapType(Class<? extends Map> mapClass, Class<?> keyType, Class<?> valueType) {
      return instance.constructMapType(mapClass, type((Type)keyType), instance.constructType((Type)valueType));
   }

   /** @deprecated */
   @Deprecated
   public static JavaType mapType(Class<? extends Map> mapType, JavaType keyType, JavaType valueType) {
      return instance.constructMapType(mapType, keyType, valueType);
   }

   /** @deprecated */
   @Deprecated
   public static JavaType parametricType(Class<?> parametrized, Class<?>... parameterClasses) {
      return instance.constructParametricType(parametrized, parameterClasses);
   }

   /** @deprecated */
   @Deprecated
   public static JavaType parametricType(Class<?> parametrized, JavaType... parameterTypes) {
      return instance.constructParametricType(parametrized, parameterTypes);
   }

   public static JavaType fromCanonical(String canonical) throws IllegalArgumentException {
      return instance.constructFromCanonical(canonical);
   }

   /** @deprecated */
   @Deprecated
   public static JavaType specialize(JavaType baseType, Class<?> subclass) {
      return instance.constructSpecializedType(baseType, subclass);
   }

   /** @deprecated */
   @Deprecated
   public static JavaType fastSimpleType(Class<?> cls) {
      return instance.uncheckedSimpleType(cls);
   }

   /** @deprecated */
   @Deprecated
   public static JavaType[] findParameterTypes(Class<?> clz, Class<?> expType) {
      return instance.findTypeParameters(clz, expType);
   }

   /** @deprecated */
   @Deprecated
   public static JavaType[] findParameterTypes(Class<?> clz, Class<?> expType, TypeBindings bindings) {
      return instance.findTypeParameters(clz, expType, bindings);
   }

   /** @deprecated */
   @Deprecated
   public static JavaType[] findParameterTypes(JavaType type, Class<?> expType) {
      return instance.findTypeParameters(type, expType);
   }

   /** @deprecated */
   @Deprecated
   public static JavaType fromClass(Class<?> clz) {
      return instance._fromClass(clz, (TypeBindings)null);
   }

   /** @deprecated */
   @Deprecated
   public static JavaType fromTypeReference(TypeReference<?> ref) {
      return type(ref.getType());
   }

   /** @deprecated */
   @Deprecated
   public static JavaType fromType(Type type) {
      return instance._constructType(type, (TypeBindings)null);
   }

   public JavaType constructSpecializedType(JavaType baseType, Class<?> subclass) {
      if (!(baseType instanceof SimpleType) || !subclass.isArray() && !Map.class.isAssignableFrom(subclass) && !Collection.class.isAssignableFrom(subclass)) {
         return baseType.narrowBy(subclass);
      } else if (!baseType.getRawClass().isAssignableFrom(subclass)) {
         throw new IllegalArgumentException("Class " + subclass.getClass().getName() + " not subtype of " + baseType);
      } else {
         JavaType subtype = instance._fromClass(subclass, new TypeBindings(this, baseType.getRawClass()));
         Object h = baseType.getValueHandler();
         if (h != null) {
            subtype.setValueHandler(h);
         }

         h = baseType.getTypeHandler();
         if (h != null) {
            subtype = subtype.withTypeHandler(h);
         }

         return subtype;
      }
   }

   public JavaType constructFromCanonical(String canonical) throws IllegalArgumentException {
      return this._parser.parse(canonical);
   }

   public JavaType[] findTypeParameters(JavaType type, Class<?> expType) {
      Class<?> raw = type.getRawClass();
      if (raw != expType) {
         return this.findTypeParameters(raw, expType, new TypeBindings(this, type));
      } else {
         int count = type.containedTypeCount();
         if (count == 0) {
            return null;
         } else {
            JavaType[] result = new JavaType[count];

            for(int i = 0; i < count; ++i) {
               result[i] = type.containedType(i);
            }

            return result;
         }
      }
   }

   public JavaType[] findTypeParameters(Class<?> clz, Class<?> expType) {
      return this.findTypeParameters(clz, expType, new TypeBindings(this, clz));
   }

   public JavaType[] findTypeParameters(Class<?> clz, Class<?> expType, TypeBindings bindings) {
      HierarchicType subType = _findSuperTypeChain(clz, expType);
      if (subType == null) {
         throw new IllegalArgumentException("Class " + clz.getName() + " is not a subtype of " + expType.getName());
      } else {
         HierarchicType superType;
         TypeBindings newBindings;
         for(superType = subType; superType.getSuperType() != null; bindings = newBindings) {
            superType = superType.getSuperType();
            Class<?> raw = superType.getRawClass();
            newBindings = new TypeBindings(this, raw);
            if (superType.isGeneric()) {
               ParameterizedType pt = superType.asGeneric();
               Type[] actualTypes = pt.getActualTypeArguments();
               TypeVariable<?>[] vars = raw.getTypeParameters();
               int len = actualTypes.length;

               for(int i = 0; i < len; ++i) {
                  String name = vars[i].getName();
                  JavaType type = instance._constructType(actualTypes[i], bindings);
                  newBindings.addBinding(name, type);
               }
            }
         }

         if (!superType.isGeneric()) {
            return null;
         } else {
            return bindings.typesAsArray();
         }
      }
   }

   public JavaType constructType(Type type) {
      return this._constructType(type, (TypeBindings)null);
   }

   public JavaType constructType(Type type, TypeBindings bindings) {
      return this._constructType(type, bindings);
   }

   public JavaType constructType(TypeReference<?> typeRef) {
      return this._constructType(typeRef.getType(), (TypeBindings)null);
   }

   public JavaType constructType(Type type, Class<?> context) {
      return this._constructType(type, new TypeBindings(this, context));
   }

   public JavaType constructType(Type type, JavaType context) {
      return this._constructType(type, new TypeBindings(this, context));
   }

   public JavaType _constructType(Type type, TypeBindings context) {
      JavaType resultType;
      if (type instanceof Class) {
         Class<?> cls = (Class)type;
         if (context == null) {
            context = new TypeBindings(this, cls);
         }

         resultType = this._fromClass(cls, context);
      } else if (type instanceof ParameterizedType) {
         resultType = this._fromParamType((ParameterizedType)type, context);
      } else if (type instanceof GenericArrayType) {
         resultType = this._fromArrayType((GenericArrayType)type, context);
      } else if (type instanceof TypeVariable) {
         resultType = this._fromVariable((TypeVariable)type, context);
      } else {
         if (!(type instanceof WildcardType)) {
            throw new IllegalArgumentException("Unrecognized Type: " + type.toString());
         }

         resultType = this._fromWildcard((WildcardType)type, context);
      }

      if (this._modifiers != null && !resultType.isContainerType()) {
         TypeModifier[] arr$ = this._modifiers;
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            TypeModifier mod = arr$[i$];
            resultType = mod.modifyType(resultType, type, context, this);
         }
      }

      return resultType;
   }

   protected ArrayType constructArrayType(Class<?> elementType) {
      return ArrayType.construct(this._constructType(elementType, (TypeBindings)null));
   }

   protected ArrayType constructArrayType(JavaType elementType) {
      return ArrayType.construct(elementType);
   }

   public CollectionType constructCollectionType(Class<? extends Collection> collectionClass, Class<?> elementClass) {
      return CollectionType.construct(collectionClass, this.constructType((Type)elementClass));
   }

   public CollectionType constructCollectionType(Class<? extends Collection> collectionClass, JavaType elementType) {
      return CollectionType.construct(collectionClass, elementType);
   }

   public CollectionLikeType constructCollectionLikeType(Class<?> collectionClass, Class<?> elementClass) {
      return CollectionLikeType.construct(collectionClass, this.constructType((Type)elementClass));
   }

   public CollectionLikeType constructCollectionLikeType(Class<?> collectionClass, JavaType elementType) {
      return CollectionLikeType.construct(collectionClass, elementType);
   }

   public MapType constructMapType(Class<? extends Map> mapClass, JavaType keyType, JavaType valueType) {
      return MapType.construct(mapClass, keyType, valueType);
   }

   public MapType constructMapType(Class<? extends Map> mapClass, Class<?> keyClass, Class<?> valueClass) {
      return MapType.construct(mapClass, this.constructType((Type)keyClass), this.constructType((Type)valueClass));
   }

   public MapLikeType constructMapLikeType(Class<?> mapClass, JavaType keyType, JavaType valueType) {
      return MapLikeType.construct(mapClass, keyType, valueType);
   }

   public MapLikeType constructMapLikeType(Class<?> mapClass, Class<?> keyClass, Class<?> valueClass) {
      return MapType.construct(mapClass, this.constructType((Type)keyClass), this.constructType((Type)valueClass));
   }

   public JavaType constructSimpleType(Class<?> rawType, JavaType[] parameterTypes) {
      TypeVariable<?>[] typeVars = rawType.getTypeParameters();
      if (typeVars.length != parameterTypes.length) {
         throw new IllegalArgumentException("Parameter type mismatch for " + rawType.getName() + ": expected " + typeVars.length + " parameters, was given " + parameterTypes.length);
      } else {
         String[] names = new String[typeVars.length];
         int i = 0;

         for(int len = typeVars.length; i < len; ++i) {
            names[i] = typeVars[i].getName();
         }

         JavaType resultType = new SimpleType(rawType, names, parameterTypes);
         return resultType;
      }
   }

   public JavaType uncheckedSimpleType(Class<?> cls) {
      return new SimpleType(cls, (String[])null, (JavaType[])null);
   }

   public JavaType constructParametricType(Class<?> parametrized, Class<?>... parameterClasses) {
      int len = parameterClasses.length;
      JavaType[] pt = new JavaType[len];

      for(int i = 0; i < len; ++i) {
         pt[i] = this._fromClass(parameterClasses[i], (TypeBindings)null);
      }

      return this.constructParametricType(parametrized, pt);
   }

   public JavaType constructParametricType(Class<?> parametrized, JavaType... parameterTypes) {
      Object resultType;
      if (parametrized.isArray()) {
         if (parameterTypes.length != 1) {
            throw new IllegalArgumentException("Need exactly 1 parameter type for arrays (" + parametrized.getName() + ")");
         }

         resultType = this.constructArrayType(parameterTypes[0]);
      } else if (Map.class.isAssignableFrom(parametrized)) {
         if (parameterTypes.length != 2) {
            throw new IllegalArgumentException("Need exactly 2 parameter types for Map types (" + parametrized.getName() + ")");
         }

         resultType = this.constructMapType(parametrized, parameterTypes[0], parameterTypes[1]);
      } else if (Collection.class.isAssignableFrom(parametrized)) {
         if (parameterTypes.length != 1) {
            throw new IllegalArgumentException("Need exactly 1 parameter type for Collection types (" + parametrized.getName() + ")");
         }

         resultType = this.constructCollectionType(parametrized, parameterTypes[0]);
      } else {
         resultType = this.constructSimpleType(parametrized, parameterTypes);
      }

      return (JavaType)resultType;
   }

   protected JavaType _fromClass(Class<?> clz, TypeBindings context) {
      if (clz.isArray()) {
         return ArrayType.construct(this._constructType(clz.getComponentType(), (TypeBindings)null));
      } else if (clz.isEnum()) {
         return new SimpleType(clz);
      } else if (Map.class.isAssignableFrom(clz)) {
         return this._mapType(clz);
      } else {
         return (JavaType)(Collection.class.isAssignableFrom(clz) ? this._collectionType(clz) : new SimpleType(clz));
      }
   }

   protected JavaType _fromParameterizedClass(Class<?> clz, List<JavaType> paramTypes) {
      if (clz.isArray()) {
         return ArrayType.construct(this._constructType(clz.getComponentType(), (TypeBindings)null));
      } else if (clz.isEnum()) {
         return new SimpleType(clz);
      } else if (Map.class.isAssignableFrom(clz)) {
         if (paramTypes.size() > 0) {
            JavaType keyType = (JavaType)paramTypes.get(0);
            JavaType contentType = paramTypes.size() >= 2 ? (JavaType)paramTypes.get(1) : this._unknownType();
            return MapType.construct(clz, keyType, contentType);
         } else {
            return this._mapType(clz);
         }
      } else if (Collection.class.isAssignableFrom(clz)) {
         return (JavaType)(paramTypes.size() >= 1 ? CollectionType.construct(clz, (JavaType)paramTypes.get(0)) : this._collectionType(clz));
      } else if (paramTypes.size() == 0) {
         return new SimpleType(clz);
      } else {
         JavaType[] pt = (JavaType[])paramTypes.toArray(new JavaType[paramTypes.size()]);
         return this.constructSimpleType(clz, pt);
      }
   }

   protected JavaType _fromParamType(ParameterizedType type, TypeBindings context) {
      Class<?> rawType = (Class)type.getRawType();
      Type[] args = type.getActualTypeArguments();
      int paramCount = args == null ? 0 : args.length;
      JavaType[] pt;
      if (paramCount == 0) {
         pt = NO_TYPES;
      } else {
         pt = new JavaType[paramCount];

         for(int i = 0; i < paramCount; ++i) {
            pt[i] = this._constructType(args[i], context);
         }
      }

      JavaType[] collectionParams;
      JavaType subtype;
      if (Map.class.isAssignableFrom(rawType)) {
         subtype = this.constructSimpleType(rawType, pt);
         collectionParams = this.findTypeParameters(subtype, Map.class);
         if (collectionParams.length != 2) {
            throw new IllegalArgumentException("Could not find 2 type parameters for Map class " + rawType.getName() + " (found " + collectionParams.length + ")");
         } else {
            return MapType.construct(rawType, collectionParams[0], collectionParams[1]);
         }
      } else if (Collection.class.isAssignableFrom(rawType)) {
         subtype = this.constructSimpleType(rawType, pt);
         collectionParams = this.findTypeParameters(subtype, Collection.class);
         if (collectionParams.length != 1) {
            throw new IllegalArgumentException("Could not find 1 type parameter for Collection class " + rawType.getName() + " (found " + collectionParams.length + ")");
         } else {
            return CollectionType.construct(rawType, collectionParams[0]);
         }
      } else {
         return (JavaType)(paramCount == 0 ? new SimpleType(rawType) : this.constructSimpleType(rawType, pt));
      }
   }

   protected JavaType _fromArrayType(GenericArrayType type, TypeBindings context) {
      JavaType compType = this._constructType(type.getGenericComponentType(), context);
      return ArrayType.construct(compType);
   }

   protected JavaType _fromVariable(TypeVariable<?> type, TypeBindings context) {
      if (context == null) {
         return this._unknownType();
      } else {
         String name = type.getName();
         JavaType actualType = context.findType(name);
         if (actualType != null) {
            return actualType;
         } else {
            Type[] bounds = type.getBounds();
            context._addPlaceholder(name);
            return this._constructType(bounds[0], context);
         }
      }
   }

   protected JavaType _fromWildcard(WildcardType type, TypeBindings context) {
      return this._constructType(type.getUpperBounds()[0], context);
   }

   private JavaType _mapType(Class<?> rawClass) {
      JavaType[] typeParams = this.findTypeParameters(rawClass, Map.class);
      if (typeParams == null) {
         return MapType.construct(rawClass, this._unknownType(), this._unknownType());
      } else if (typeParams.length != 2) {
         throw new IllegalArgumentException("Strange Map type " + rawClass.getName() + ": can not determine type parameters");
      } else {
         return MapType.construct(rawClass, typeParams[0], typeParams[1]);
      }
   }

   private JavaType _collectionType(Class<?> rawClass) {
      JavaType[] typeParams = this.findTypeParameters(rawClass, Collection.class);
      if (typeParams == null) {
         return CollectionType.construct(rawClass, this._unknownType());
      } else if (typeParams.length != 1) {
         throw new IllegalArgumentException("Strange Collection type " + rawClass.getName() + ": can not determine type parameters");
      } else {
         return CollectionType.construct(rawClass, typeParams[0]);
      }
   }

   protected JavaType _resolveVariableViaSubTypes(HierarchicType leafType, String variableName, TypeBindings bindings) {
      if (leafType != null && leafType.isGeneric()) {
         TypeVariable<?>[] typeVariables = leafType.getRawClass().getTypeParameters();
         int i = 0;

         for(int len = typeVariables.length; i < len; ++i) {
            TypeVariable<?> tv = typeVariables[i];
            if (variableName.equals(tv.getName())) {
               Type type = leafType.asGeneric().getActualTypeArguments()[i];
               if (type instanceof TypeVariable) {
                  return this._resolveVariableViaSubTypes(leafType.getSubType(), ((TypeVariable)type).getName(), bindings);
               }

               return this._constructType(type, bindings);
            }
         }
      }

      return this._unknownType();
   }

   protected JavaType _unknownType() {
      return new SimpleType(Object.class, (String[])null, (JavaType[])null);
   }

   protected static HierarchicType _findSuperTypeChain(Class<?> subtype, Class<?> supertype) {
      return supertype.isInterface() ? _findSuperInterfaceChain(subtype, supertype) : _findSuperClassChain(subtype, supertype);
   }

   protected static HierarchicType _findSuperClassChain(Type currentType, Class<?> target) {
      HierarchicType current = new HierarchicType(currentType);
      Class<?> raw = current.getRawClass();
      if (raw == target) {
         return current;
      } else {
         Type parent = raw.getGenericSuperclass();
         if (parent != null) {
            HierarchicType sup = _findSuperClassChain(parent, target);
            if (sup != null) {
               sup.setSubType(current);
               current.setSuperType(sup);
               return current;
            }
         }

         return null;
      }
   }

   protected static HierarchicType _findSuperInterfaceChain(Type currentType, Class<?> target) {
      HierarchicType current = new HierarchicType(currentType);
      Class<?> raw = current.getRawClass();
      if (raw == target) {
         return current;
      } else {
         Type[] parents = raw.getGenericInterfaces();
         if (parents != null) {
            Type[] arr$ = parents;
            int len$ = parents.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               Type parent = arr$[i$];
               HierarchicType sup = _findSuperInterfaceChain(parent, target);
               if (sup != null) {
                  sup.setSubType(current);
                  current.setSuperType(sup);
                  return current;
               }
            }
         }

         Type parent = raw.getGenericSuperclass();
         if (parent != null) {
            HierarchicType sup = _findSuperInterfaceChain(parent, target);
            if (sup != null) {
               sup.setSubType(current);
               current.setSuperType(sup);
               return current;
            }
         }

         return null;
      }
   }
}

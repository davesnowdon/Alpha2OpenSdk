package org.codehaus.jackson.map.introspect;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;

public interface VisibilityChecker {
   T with(JsonAutoDetect var1);

   T withGetterVisibility(JsonAutoDetect.Visibility var1);

   T withIsGetterVisibility(JsonAutoDetect.Visibility var1);

   T withSetterVisibility(JsonAutoDetect.Visibility var1);

   T withCreatorVisibility(JsonAutoDetect.Visibility var1);

   T withFieldVisibility(JsonAutoDetect.Visibility var1);

   boolean isGetterVisible(Method var1);

   boolean isGetterVisible(AnnotatedMethod var1);

   boolean isIsGetterVisible(Method var1);

   boolean isIsGetterVisible(AnnotatedMethod var1);

   boolean isSetterVisible(Method var1);

   boolean isSetterVisible(AnnotatedMethod var1);

   boolean isCreatorVisible(Member var1);

   boolean isCreatorVisible(AnnotatedMember var1);

   boolean isFieldVisible(Field var1);

   boolean isFieldVisible(AnnotatedField var1);

   @JsonAutoDetect(
      getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
      isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
      setterVisibility = JsonAutoDetect.Visibility.ANY,
      creatorVisibility = JsonAutoDetect.Visibility.ANY,
      fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY
   )
   public static class Std implements VisibilityChecker<VisibilityChecker.Std> {
      protected static final VisibilityChecker.Std DEFAULT = new VisibilityChecker.Std((JsonAutoDetect)VisibilityChecker.Std.class.getAnnotation(JsonAutoDetect.class));
      protected final JsonAutoDetect.Visibility _getterMinLevel;
      protected final JsonAutoDetect.Visibility _isGetterMinLevel;
      protected final JsonAutoDetect.Visibility _setterMinLevel;
      protected final JsonAutoDetect.Visibility _creatorMinLevel;
      protected final JsonAutoDetect.Visibility _fieldMinLevel;

      public static VisibilityChecker.Std defaultInstance() {
         return DEFAULT;
      }

      public Std(JsonAutoDetect ann) {
         JsonMethod[] incl = ann.value();
         this._getterMinLevel = hasMethod(incl, JsonMethod.GETTER) ? ann.getterVisibility() : JsonAutoDetect.Visibility.NONE;
         this._isGetterMinLevel = hasMethod(incl, JsonMethod.IS_GETTER) ? ann.isGetterVisibility() : JsonAutoDetect.Visibility.NONE;
         this._setterMinLevel = hasMethod(incl, JsonMethod.SETTER) ? ann.setterVisibility() : JsonAutoDetect.Visibility.NONE;
         this._creatorMinLevel = hasMethod(incl, JsonMethod.CREATOR) ? ann.creatorVisibility() : JsonAutoDetect.Visibility.NONE;
         this._fieldMinLevel = hasMethod(incl, JsonMethod.FIELD) ? ann.fieldVisibility() : JsonAutoDetect.Visibility.NONE;
      }

      public Std(JsonAutoDetect.Visibility getter, JsonAutoDetect.Visibility isGetter, JsonAutoDetect.Visibility setter, JsonAutoDetect.Visibility creator, JsonAutoDetect.Visibility field) {
         this._getterMinLevel = getter;
         this._isGetterMinLevel = isGetter;
         this._setterMinLevel = setter;
         this._creatorMinLevel = creator;
         this._fieldMinLevel = field;
      }

      public VisibilityChecker.Std with(JsonAutoDetect ann) {
         if (ann == null) {
            return this;
         } else {
            JsonMethod[] incl = ann.value();
            JsonAutoDetect.Visibility v = hasMethod(incl, JsonMethod.GETTER) ? ann.getterVisibility() : JsonAutoDetect.Visibility.NONE;
            VisibilityChecker.Std curr = this.withGetterVisibility(v);
            v = hasMethod(incl, JsonMethod.IS_GETTER) ? ann.isGetterVisibility() : JsonAutoDetect.Visibility.NONE;
            curr = curr.withIsGetterVisibility(v);
            v = hasMethod(incl, JsonMethod.SETTER) ? ann.setterVisibility() : JsonAutoDetect.Visibility.NONE;
            curr = curr.withSetterVisibility(v);
            v = hasMethod(incl, JsonMethod.CREATOR) ? ann.creatorVisibility() : JsonAutoDetect.Visibility.NONE;
            curr = curr.withCreatorVisibility(v);
            v = hasMethod(incl, JsonMethod.FIELD) ? ann.fieldVisibility() : JsonAutoDetect.Visibility.NONE;
            curr = curr.withFieldVisibility(v);
            return curr;
         }
      }

      public VisibilityChecker.Std withGetterVisibility(JsonAutoDetect.Visibility v) {
         if (v == JsonAutoDetect.Visibility.DEFAULT) {
            v = DEFAULT._getterMinLevel;
         }

         return this._getterMinLevel == v ? this : new VisibilityChecker.Std(v, this._isGetterMinLevel, this._setterMinLevel, this._creatorMinLevel, this._fieldMinLevel);
      }

      public VisibilityChecker.Std withIsGetterVisibility(JsonAutoDetect.Visibility v) {
         if (v == JsonAutoDetect.Visibility.DEFAULT) {
            v = DEFAULT._isGetterMinLevel;
         }

         return this._isGetterMinLevel == v ? this : new VisibilityChecker.Std(this._getterMinLevel, v, this._setterMinLevel, this._creatorMinLevel, this._fieldMinLevel);
      }

      public VisibilityChecker.Std withSetterVisibility(JsonAutoDetect.Visibility v) {
         if (v == JsonAutoDetect.Visibility.DEFAULT) {
            v = DEFAULT._setterMinLevel;
         }

         return this._setterMinLevel == v ? this : new VisibilityChecker.Std(this._getterMinLevel, this._isGetterMinLevel, v, this._creatorMinLevel, this._fieldMinLevel);
      }

      public VisibilityChecker.Std withCreatorVisibility(JsonAutoDetect.Visibility v) {
         if (v == JsonAutoDetect.Visibility.DEFAULT) {
            v = DEFAULT._creatorMinLevel;
         }

         return this._creatorMinLevel == v ? this : new VisibilityChecker.Std(this._getterMinLevel, this._isGetterMinLevel, this._setterMinLevel, v, this._fieldMinLevel);
      }

      public VisibilityChecker.Std withFieldVisibility(JsonAutoDetect.Visibility v) {
         if (v == JsonAutoDetect.Visibility.DEFAULT) {
            v = DEFAULT._fieldMinLevel;
         }

         return this._fieldMinLevel == v ? this : new VisibilityChecker.Std(this._getterMinLevel, this._isGetterMinLevel, this._setterMinLevel, this._creatorMinLevel, v);
      }

      public boolean isCreatorVisible(Member m) {
         return this._creatorMinLevel.isVisible(m);
      }

      public boolean isCreatorVisible(AnnotatedMember m) {
         return this.isCreatorVisible(m.getMember());
      }

      public boolean isFieldVisible(Field f) {
         return this._fieldMinLevel.isVisible(f);
      }

      public boolean isFieldVisible(AnnotatedField f) {
         return this.isFieldVisible(f.getAnnotated());
      }

      public boolean isGetterVisible(Method m) {
         return this._getterMinLevel.isVisible(m);
      }

      public boolean isGetterVisible(AnnotatedMethod m) {
         return this.isGetterVisible(m.getAnnotated());
      }

      public boolean isIsGetterVisible(Method m) {
         return this._isGetterMinLevel.isVisible(m);
      }

      public boolean isIsGetterVisible(AnnotatedMethod m) {
         return this.isIsGetterVisible(m.getAnnotated());
      }

      public boolean isSetterVisible(Method m) {
         return this._setterMinLevel.isVisible(m);
      }

      public boolean isSetterVisible(AnnotatedMethod m) {
         return this.isSetterVisible(m.getAnnotated());
      }

      private static boolean hasMethod(JsonMethod[] methods, JsonMethod method) {
         JsonMethod[] arr$ = methods;
         int len$ = methods.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            JsonMethod curr = arr$[i$];
            if (curr == method || curr == JsonMethod.ALL) {
               return true;
            }
         }

         return false;
      }

      public String toString() {
         return "[Visibility:" + " getter: " + this._getterMinLevel + ", isGetter: " + this._isGetterMinLevel + ", setter: " + this._setterMinLevel + ", creator: " + this._creatorMinLevel + ", field: " + this._fieldMinLevel + "]";
      }
   }
}

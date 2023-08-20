package org.msgpack.template;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class GenericCollectionTemplate implements GenericTemplate {
   Constructor<? extends Template> constructor;

   public GenericCollectionTemplate(TemplateRegistry registry, Class<? extends Template> tmpl) {
      try {
         this.constructor = tmpl.getConstructor(Template.class);
         this.constructor.newInstance(new AnyTemplate(registry));
      } catch (NoSuchMethodException var4) {
         throw new IllegalArgumentException(var4);
      } catch (InvocationTargetException var5) {
         throw new IllegalArgumentException(var5);
      } catch (IllegalAccessException var6) {
         throw new IllegalArgumentException(var6);
      } catch (InstantiationException var7) {
         throw new IllegalArgumentException(var7);
      }
   }

   public Template build(Template[] params) {
      try {
         return (Template)this.constructor.newInstance((Object[])params);
      } catch (InvocationTargetException var3) {
         throw new IllegalArgumentException(var3);
      } catch (IllegalAccessException var4) {
         throw new IllegalArgumentException(var4);
      } catch (InstantiationException var5) {
         throw new IllegalArgumentException(var5);
      }
   }
}

package org.msgpack.template;

import java.io.IOException;
import java.lang.reflect.Array;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class ObjectArrayTemplate extends AbstractTemplate {
   protected Class componentClass;
   protected Template componentTemplate;

   public ObjectArrayTemplate(Class componentClass, Template componentTemplate) {
      this.componentClass = componentClass;
      this.componentTemplate = componentTemplate;
   }

   public void write(Packer packer, Object v, boolean required) throws IOException {
      if (v == null) {
         if (required) {
            throw new MessageTypeException("Attempted to write null");
         } else {
            packer.writeNil();
         }
      } else if (v instanceof Object[] && this.componentClass.isAssignableFrom(v.getClass().getComponentType())) {
         Object[] array = (Object[])((Object[])v);
         int length = array.length;
         packer.writeArrayBegin(length);

         for(int i = 0; i < length; ++i) {
            this.componentTemplate.write(packer, array[i], required);
         }

         packer.writeArrayEnd();
      } else {
         throw new MessageTypeException();
      }
   }

   public Object read(Unpacker unpacker, Object to, boolean required) throws IOException {
      if (!required && unpacker.trySkipNil()) {
         return null;
      } else {
         int length = unpacker.readArrayBegin();
         Object[] array = (Object[])((Object[])Array.newInstance(this.componentClass, length));

         for(int i = 0; i < length; ++i) {
            array[i] = this.componentTemplate.read(unpacker, (Object)null, required);
         }

         unpacker.readArrayEnd();
         return array;
      }
   }
}

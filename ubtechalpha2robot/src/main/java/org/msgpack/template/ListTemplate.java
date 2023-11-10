package org.msgpack.template;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class ListTemplate<E> extends AbstractTemplate<List<E>> {
   private Template<E> elementTemplate;

   public ListTemplate(Template<E> elementTemplate) {
      this.elementTemplate = elementTemplate;
   }

   public void write(Packer pk, List<E> target, boolean required) throws IOException {
      if (!(target instanceof List)) {
         if (target == null) {
            if (required) {
               throw new MessageTypeException("Attempted to write null");
            } else {
               pk.writeNil();
            }
         } else {
            throw new MessageTypeException("Target is not a List but " + target.getClass());
         }
      } else {
         pk.writeArrayBegin(target.size());
         Iterator i$ = target.iterator();

         while(i$.hasNext()) {
            E e = i$.next();
            this.elementTemplate.write(pk, e);
         }

         pk.writeArrayEnd();
      }
   }

   public List<E> read(Unpacker u, List<E> to, boolean required) throws IOException {
      if (!required && u.trySkipNil()) {
         return null;
      } else {
         int n = u.readArrayBegin();
         if (to == null) {
            to = new ArrayList(n);
         } else {
            ((List)to).clear();
         }

         for(int i = 0; i < n; ++i) {
            E e = this.elementTemplate.read(u, (Object)null);
            ((List)to).add(e);
         }

         u.readArrayEnd();
         return (List)to;
      }
   }
}

package org.msgpack.template;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class SetTemplate<E> extends AbstractTemplate<Set<E>> {
   private Template<E> elementTemplate;

   public SetTemplate(Template<E> elementTemplate) {
      this.elementTemplate = elementTemplate;
   }

   public void write(Packer pk, Set<E> target, boolean required) throws IOException {
      if (!(target instanceof Set)) {
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

   public Set<E> read(Unpacker u, Set<E> to, boolean required) throws IOException {
      if (!required && u.trySkipNil()) {
         return null;
      } else {
         int n = u.readArrayBegin();
         if (to == null) {
            to = new HashSet(n);
         } else {
            ((Set)to).clear();
         }

         for(int i = 0; i < n; ++i) {
            E e = this.elementTemplate.read(u, (Object)null);
            ((Set)to).add(e);
         }

         u.readArrayEnd();
         return (Set)to;
      }
   }
}

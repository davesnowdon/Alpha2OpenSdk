package org.msgpack.template;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class CollectionTemplate<E> extends AbstractTemplate<Collection<E>> {
   private Template<E> elementTemplate;

   public CollectionTemplate(Template<E> elementTemplate) {
      this.elementTemplate = elementTemplate;
   }

   public void write(Packer pk, Collection<E> target, boolean required) throws IOException {
      if (target == null) {
         if (required) {
            throw new MessageTypeException("Attempted to write null");
         } else {
            pk.writeNil();
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

   public Collection<E> read(Unpacker u, Collection<E> to, boolean required) throws IOException {
      if (!required && u.trySkipNil()) {
         return null;
      } else {
         int n = u.readArrayBegin();
         if (to == null) {
            to = new LinkedList();
         } else {
            ((Collection)to).clear();
         }

         for(int i = 0; i < n; ++i) {
            E e = this.elementTemplate.read(u, (Object)null);
            ((Collection)to).add(e);
         }

         u.readArrayEnd();
         return (Collection)to;
      }
   }
}

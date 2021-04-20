package org.msgpack.template;

import java.io.IOException;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class CharacterTemplate extends AbstractTemplate<Character> {
   static final CharacterTemplate instance = new CharacterTemplate();

   private CharacterTemplate() {
   }

   public void write(Packer pk, Character target, boolean required) throws IOException {
      if (target == null) {
         if (required) {
            throw new MessageTypeException("Attempted to write null");
         } else {
            pk.writeNil();
         }
      } else {
         pk.write((int)target);
      }
   }

   public Character read(Unpacker u, Character to, boolean required) throws IOException {
      return !required && u.trySkipNil() ? null : (char)u.readInt();
   }

   public static CharacterTemplate getInstance() {
      return instance;
   }
}

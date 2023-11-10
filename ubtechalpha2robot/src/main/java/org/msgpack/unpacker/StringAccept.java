package org.msgpack.unpacker;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import org.msgpack.MessageTypeException;

final class StringAccept extends Accept {
   String value;
   private CharsetDecoder decoder;

   public StringAccept() {
      super("raw value");
      this.decoder = Charset.forName("UTF-8").newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
   }

   void acceptRaw(byte[] raw) {
      try {
         this.value = this.decoder.decode(ByteBuffer.wrap(raw)).toString();
      } catch (CharacterCodingException var3) {
         throw new MessageTypeException(var3);
      }
   }

   void acceptEmptyRaw() {
      this.value = "";
   }

   public void refer(ByteBuffer bb, boolean gift) throws IOException {
      try {
         this.value = this.decoder.decode(bb).toString();
      } catch (CharacterCodingException var4) {
         throw new MessageTypeException(var4);
      }
   }
}

package org.msgpack.type;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;

class ByteArrayRawValueImpl extends AbstractRawValue {
   private static ByteArrayRawValueImpl emptyInstance = new ByteArrayRawValueImpl(new byte[0], true);
   private static final ThreadLocal<CharsetDecoder> decoderStore = new ThreadLocal<CharsetDecoder>() {
      protected CharsetDecoder initialValue() {
         return Charset.forName("UTF-8").newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
      }
   };
   private byte[] bytes;

   public static RawValue getEmptyInstance() {
      return emptyInstance;
   }

   ByteArrayRawValueImpl(byte[] bytes, boolean gift) {
      if (gift) {
         this.bytes = bytes;
      } else {
         this.bytes = new byte[bytes.length];
         System.arraycopy(bytes, 0, this.bytes, 0, bytes.length);
      }

   }

   ByteArrayRawValueImpl(byte[] b, int off, int len) {
      this.bytes = new byte[len];
      System.arraycopy(b, off, this.bytes, 0, len);
   }

   public byte[] getByteArray() {
      return this.bytes;
   }

   public String getString() {
      CharsetDecoder decoder = (CharsetDecoder)decoderStore.get();

      try {
         return decoder.decode(ByteBuffer.wrap(this.bytes)).toString();
      } catch (CharacterCodingException var3) {
         throw new MessageTypeException(var3);
      }
   }

   public void writeTo(Packer pk) throws IOException {
      pk.write(this.bytes);
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof Value)) {
         return false;
      } else {
         Value v = (Value)o;
         return !v.isRawValue() ? false : Arrays.equals(this.bytes, v.asRawValue().getByteArray());
      }
   }

   public int hashCode() {
      return Arrays.hashCode(this.bytes);
   }
}

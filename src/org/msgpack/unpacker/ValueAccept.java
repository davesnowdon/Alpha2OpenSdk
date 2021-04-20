package org.msgpack.unpacker;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import org.msgpack.packer.Unconverter;
import org.msgpack.type.ValueFactory;

final class ValueAccept extends Accept {
   private Unconverter uc = null;

   ValueAccept() {
      super((String)null);
   }

   void setUnconverter(Unconverter uc) throws IOException {
      this.uc = uc;
   }

   void acceptBoolean(boolean v) throws IOException {
      this.uc.write(ValueFactory.createBooleanValue(v));
   }

   void acceptInteger(byte v) throws IOException {
      this.uc.write(ValueFactory.createIntegerValue(v));
   }

   void acceptInteger(short v) throws IOException {
      this.uc.write(ValueFactory.createIntegerValue(v));
   }

   void acceptInteger(int v) throws IOException {
      this.uc.write(ValueFactory.createIntegerValue(v));
   }

   void acceptInteger(long v) throws IOException {
      this.uc.write(ValueFactory.createIntegerValue(v));
   }

   void acceptUnsignedInteger(byte v) throws IOException {
      this.uc.write(ValueFactory.createIntegerValue(v & 255));
   }

   void acceptUnsignedInteger(short v) throws IOException {
      this.uc.write(ValueFactory.createIntegerValue(v & '\uffff'));
   }

   void acceptUnsignedInteger(int v) throws IOException {
      if (v < 0) {
         long value = (long)(v & 2147483647) + 2147483648L;
         this.uc.write(ValueFactory.createIntegerValue(value));
      } else {
         this.uc.write(ValueFactory.createIntegerValue(v));
      }

   }

   void acceptUnsignedInteger(long v) throws IOException {
      if (v < 0L) {
         BigInteger value = BigInteger.valueOf(v + 9223372036854775807L + 1L).setBit(63);
         this.uc.write(ValueFactory.createIntegerValue(value));
      } else {
         this.uc.write(ValueFactory.createIntegerValue(v));
      }

   }

   void acceptRaw(byte[] raw) throws IOException {
      this.uc.write(ValueFactory.createRawValue(raw));
   }

   void acceptEmptyRaw() throws IOException {
      this.uc.write(ValueFactory.createRawValue());
   }

   public void refer(ByteBuffer bb, boolean gift) throws IOException {
      byte[] raw = new byte[bb.remaining()];
      bb.get(raw);
      this.uc.write(ValueFactory.createRawValue(raw, true));
   }

   void acceptArray(int size) throws IOException {
      this.uc.writeArrayBegin(size);
   }

   void acceptMap(int size) throws IOException {
      this.uc.writeMapBegin(size);
   }

   void acceptNil() throws IOException {
      this.uc.write(ValueFactory.createNilValue());
   }

   void acceptFloat(float v) throws IOException {
      this.uc.write(ValueFactory.createFloatValue(v));
   }

   void acceptDouble(double v) throws IOException {
      this.uc.write(ValueFactory.createFloatValue(v));
   }
}

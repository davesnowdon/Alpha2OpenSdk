package org.msgpack.unpacker;

import java.math.BigInteger;

final class BigIntegerAccept extends Accept {
   BigInteger value;

   BigIntegerAccept() {
      super("integer");
   }

   void acceptInteger(byte v) {
      this.value = BigInteger.valueOf((long)v);
   }

   void acceptInteger(short v) {
      this.value = BigInteger.valueOf((long)v);
   }

   void acceptInteger(int v) {
      this.value = BigInteger.valueOf((long)v);
   }

   void acceptInteger(long v) {
      this.value = BigInteger.valueOf(v);
   }

   void acceptUnsignedInteger(byte v) {
      this.value = BigInteger.valueOf((long)(v & 255));
   }

   void acceptUnsignedInteger(short v) {
      this.value = BigInteger.valueOf((long)(v & '\uffff'));
   }

   void acceptUnsignedInteger(int v) {
      if (v < 0) {
         this.value = BigInteger.valueOf((long)(v & 2147483647) + 2147483648L);
      } else {
         this.value = BigInteger.valueOf((long)v);
      }

   }

   void acceptUnsignedInteger(long v) {
      if (v < 0L) {
         this.value = BigInteger.valueOf(v + 9223372036854775807L + 1L).setBit(63);
      } else {
         this.value = BigInteger.valueOf(v);
      }

   }
}

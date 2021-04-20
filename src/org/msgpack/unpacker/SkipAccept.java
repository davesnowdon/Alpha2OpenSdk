package org.msgpack.unpacker;

import java.io.IOException;
import java.nio.ByteBuffer;

final class SkipAccept extends Accept {
   SkipAccept() {
      super((String)null);
   }

   void acceptBoolean(boolean v) {
   }

   void acceptInteger(byte v) {
   }

   void acceptInteger(short v) {
   }

   void acceptInteger(int v) {
   }

   void acceptInteger(long v) {
   }

   void acceptUnsignedInteger(byte v) {
   }

   void acceptUnsignedInteger(short v) {
   }

   void acceptUnsignedInteger(int v) {
   }

   void acceptUnsignedInteger(long v) {
   }

   void acceptRaw(byte[] raw) {
   }

   void acceptEmptyRaw() {
   }

   public void refer(ByteBuffer bb, boolean gift) throws IOException {
   }

   void acceptArray(int size) {
   }

   void acceptMap(int size) {
   }

   void acceptNil() {
   }

   void acceptFloat(float v) {
   }

   void acceptDouble(double v) {
   }
}

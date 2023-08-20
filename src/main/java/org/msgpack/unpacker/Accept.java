package org.msgpack.unpacker;

import java.io.IOException;
import java.nio.ByteBuffer;
import org.msgpack.MessageTypeException;
import org.msgpack.io.BufferReferer;

abstract class Accept implements BufferReferer {
   private final String expected;

   Accept(String expected) {
      this.expected = expected;
   }

   void acceptBoolean(boolean v) throws IOException {
      throw new MessageTypeException(String.format("Expected %s, but got boolean", this.expected));
   }

   void acceptInteger(byte v) throws IOException {
      throw new MessageTypeException(String.format("Expected %s, but got integer value", this.expected));
   }

   void acceptInteger(short v) throws IOException {
      throw new MessageTypeException(String.format("Expected %s, but got integer value", this.expected));
   }

   void acceptInteger(int v) throws IOException {
      throw new MessageTypeException(String.format("Expected %s, but got integer value", this.expected));
   }

   void acceptInteger(long v) throws IOException {
      throw new MessageTypeException(String.format("Expected %s, but got integer value", this.expected));
   }

   void acceptUnsignedInteger(byte v) throws IOException {
      throw new MessageTypeException(String.format("Expected %s, but got integer value", this.expected));
   }

   void acceptUnsignedInteger(short v) throws IOException {
      throw new MessageTypeException(String.format("Expected %s, but got integer value", this.expected));
   }

   void acceptUnsignedInteger(int v) throws IOException {
      throw new MessageTypeException(String.format("Expected %s, but got integer value", this.expected));
   }

   void acceptUnsignedInteger(long v) throws IOException {
      throw new MessageTypeException(String.format("Expected %s, but got integer value", this.expected));
   }

   void acceptRaw(byte[] raw) throws IOException {
      throw new MessageTypeException(String.format("Expected %s, but got raw value", this.expected));
   }

   void acceptEmptyRaw() throws IOException {
      throw new MessageTypeException(String.format("Expected %s, but got raw value", this.expected));
   }

   void acceptArray(int size) throws IOException {
      throw new MessageTypeException(String.format("Expected %s, but got array value", this.expected));
   }

   void acceptMap(int size) throws IOException {
      throw new MessageTypeException(String.format("Expected %s, but got map value", this.expected));
   }

   void acceptNil() throws IOException {
      throw new MessageTypeException(String.format("Expected %s, but got nil value", this.expected));
   }

   void acceptFloat(float v) throws IOException {
      throw new MessageTypeException(String.format("Expected %s, but got float value", this.expected));
   }

   void acceptDouble(double v) throws IOException {
      throw new MessageTypeException(String.format("Expected %s, but got float value", this.expected));
   }

   public void refer(ByteBuffer bb, boolean gift) throws IOException {
      throw new MessageTypeException(String.format("Expected %s, but got raw value", this.expected));
   }
}

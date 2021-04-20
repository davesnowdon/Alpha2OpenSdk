package org.msgpack.packer;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;
import org.msgpack.type.Value;
import org.msgpack.type.ValueFactory;

public class Unconverter extends AbstractPacker {
   private PackerStack stack;
   private Object[] values;
   private Value result;

   public Unconverter() {
      this(new MessagePack());
   }

   public Unconverter(MessagePack msgpack) {
      super(msgpack);
      this.stack = new PackerStack();
      this.values = new Object[128];
   }

   public Value getResult() {
      return this.result;
   }

   public void resetResult() {
      this.result = null;
   }

   public void writeBoolean(boolean v) throws IOException {
      this.put(ValueFactory.createBooleanValue(v));
   }

   public void writeByte(byte v) throws IOException {
      this.put(ValueFactory.createIntegerValue(v));
   }

   public void writeShort(short v) throws IOException {
      this.put(ValueFactory.createIntegerValue(v));
   }

   public void writeInt(int v) throws IOException {
      this.put(ValueFactory.createIntegerValue(v));
   }

   public void writeBigInteger(BigInteger v) throws IOException {
      this.put(ValueFactory.createIntegerValue(v));
   }

   public void writeLong(long v) throws IOException {
      this.put(ValueFactory.createIntegerValue(v));
   }

   public void writeFloat(float v) throws IOException {
      this.put(ValueFactory.createFloatValue(v));
   }

   public void writeDouble(double v) throws IOException {
      this.put(ValueFactory.createFloatValue(v));
   }

   public void writeByteArray(byte[] b, int off, int len) throws IOException {
      this.put(ValueFactory.createRawValue(b, off, len));
   }

   public void writeByteBuffer(ByteBuffer bb) throws IOException {
      this.put(ValueFactory.createRawValue(bb));
   }

   public void writeString(String s) throws IOException {
      this.put(ValueFactory.createRawValue(s));
   }

   public Packer writeNil() throws IOException {
      this.put(ValueFactory.createNilValue());
      return this;
   }

   public Packer writeArrayBegin(int size) throws IOException {
      if (size == 0) {
         this.putContainer(ValueFactory.createArrayValue());
         this.stack.pushArray(0);
         this.values[this.stack.getDepth()] = null;
      } else {
         Value[] array = new Value[size];
         this.putContainer(ValueFactory.createArrayValue(array, true));
         this.stack.pushArray(size);
         this.values[this.stack.getDepth()] = array;
      }

      return this;
   }

   public Packer writeArrayEnd(boolean check) throws IOException {
      if (!this.stack.topIsArray()) {
         throw new MessageTypeException("writeArrayEnd() is called but writeArrayBegin() is not called");
      } else {
         int remain = this.stack.getTopCount();
         if (remain > 0) {
            if (check) {
               throw new MessageTypeException("writeArrayEnd(check=true) is called but the array is not end");
            }

            for(int i = 0; i < remain; ++i) {
               this.writeNil();
            }
         }

         this.stack.pop();
         if (this.stack.getDepth() <= 0) {
            this.result = (Value)this.values[0];
         }

         return this;
      }
   }

   public Packer writeMapBegin(int size) throws IOException {
      this.stack.checkCount();
      if (size == 0) {
         this.putContainer(ValueFactory.createMapValue());
         this.stack.pushMap(0);
         this.values[this.stack.getDepth()] = null;
      } else {
         Value[] array = new Value[size * 2];
         this.putContainer(ValueFactory.createMapValue(array, true));
         this.stack.pushMap(size);
         this.values[this.stack.getDepth()] = array;
      }

      return this;
   }

   public Packer writeMapEnd(boolean check) throws IOException {
      if (!this.stack.topIsMap()) {
         throw new MessageTypeException("writeMapEnd() is called but writeMapBegin() is not called");
      } else {
         int remain = this.stack.getTopCount();
         if (remain > 0) {
            if (check) {
               throw new MessageTypeException("writeMapEnd(check=true) is called but the map is not end");
            }

            for(int i = 0; i < remain; ++i) {
               this.writeNil();
            }
         }

         this.stack.pop();
         if (this.stack.getDepth() <= 0) {
            this.result = (Value)this.values[0];
         }

         return this;
      }
   }

   public Packer write(Value v) throws IOException {
      this.put(v);
      return this;
   }

   private void put(Value v) {
      if (this.stack.getDepth() <= 0) {
         this.result = v;
      } else {
         this.stack.checkCount();
         Value[] array = (Value[])((Value[])this.values[this.stack.getDepth()]);
         array[array.length - this.stack.getTopCount()] = v;
         this.stack.reduceCount();
      }

   }

   private void putContainer(Value v) {
      if (this.stack.getDepth() <= 0) {
         this.values[0] = v;
      } else {
         this.stack.checkCount();
         Value[] array = (Value[])((Value[])this.values[this.stack.getDepth()]);
         array[array.length - this.stack.getTopCount()] = v;
         this.stack.reduceCount();
      }

   }

   public void flush() throws IOException {
   }

   public void close() throws IOException {
   }
}

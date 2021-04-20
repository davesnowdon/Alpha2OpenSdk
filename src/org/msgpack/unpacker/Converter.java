package org.msgpack.unpacker;

import java.io.EOFException;
import java.io.IOException;
import java.math.BigInteger;
import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Unconverter;
import org.msgpack.type.ArrayValue;
import org.msgpack.type.MapValue;
import org.msgpack.type.Value;
import org.msgpack.type.ValueType;

public class Converter extends AbstractUnpacker {
   private final UnpackerStack stack;
   private Object[] values;
   protected Value value;

   public Converter(Value value) {
      this(new MessagePack(), value);
   }

   public Converter(MessagePack msgpack, Value value) {
      super(msgpack);
      this.stack = new UnpackerStack();
      this.values = new Object[128];
      this.value = value;
   }

   protected Value nextValue() throws IOException {
      throw new EOFException();
   }

   private void ensureValue() throws IOException {
      if (this.value == null) {
         this.value = this.nextValue();
      }

   }

   public boolean tryReadNil() throws IOException {
      this.stack.checkCount();
      if (this.getTop().isNilValue()) {
         this.stack.reduceCount();
         if (this.stack.getDepth() == 0) {
            this.value = null;
         }

         return true;
      } else {
         return false;
      }
   }

   public boolean trySkipNil() throws IOException {
      this.ensureValue();
      if (this.stack.getDepth() > 0 && this.stack.getTopCount() <= 0) {
         return true;
      } else if (this.getTop().isNilValue()) {
         this.stack.reduceCount();
         if (this.stack.getDepth() == 0) {
            this.value = null;
         }

         return true;
      } else {
         return false;
      }
   }

   public void readNil() throws IOException {
      if (!this.getTop().isNilValue()) {
         throw new MessageTypeException("Expected nil but got not nil value");
      } else {
         this.stack.reduceCount();
         if (this.stack.getDepth() == 0) {
            this.value = null;
         }

      }
   }

   public boolean readBoolean() throws IOException {
      boolean v = this.getTop().asBooleanValue().getBoolean();
      this.stack.reduceCount();
      return v;
   }

   public byte readByte() throws IOException {
      byte v = this.getTop().asIntegerValue().getByte();
      this.stack.reduceCount();
      if (this.stack.getDepth() == 0) {
         this.value = null;
      }

      return v;
   }

   public short readShort() throws IOException {
      short v = this.getTop().asIntegerValue().getShort();
      this.stack.reduceCount();
      if (this.stack.getDepth() == 0) {
         this.value = null;
      }

      return v;
   }

   public int readInt() throws IOException {
      int v = this.getTop().asIntegerValue().getInt();
      this.stack.reduceCount();
      if (this.stack.getDepth() == 0) {
         this.value = null;
      }

      return v;
   }

   public long readLong() throws IOException {
      long v = this.getTop().asIntegerValue().getLong();
      this.stack.reduceCount();
      if (this.stack.getDepth() == 0) {
         this.value = null;
      }

      return v;
   }

   public BigInteger readBigInteger() throws IOException {
      BigInteger v = this.getTop().asIntegerValue().getBigInteger();
      this.stack.reduceCount();
      if (this.stack.getDepth() == 0) {
         this.value = null;
      }

      return v;
   }

   public float readFloat() throws IOException {
      float v = this.getTop().asFloatValue().getFloat();
      this.stack.reduceCount();
      if (this.stack.getDepth() == 0) {
         this.value = null;
      }

      return v;
   }

   public double readDouble() throws IOException {
      double v = this.getTop().asFloatValue().getDouble();
      this.stack.reduceCount();
      if (this.stack.getDepth() == 0) {
         this.value = null;
      }

      return v;
   }

   public byte[] readByteArray() throws IOException {
      byte[] raw = this.getTop().asRawValue().getByteArray();
      this.stack.reduceCount();
      if (this.stack.getDepth() == 0) {
         this.value = null;
      }

      return raw;
   }

   public String readString() throws IOException {
      String str = this.getTop().asRawValue().getString();
      this.stack.reduceCount();
      if (this.stack.getDepth() == 0) {
         this.value = null;
      }

      return str;
   }

   public int readArrayBegin() throws IOException {
      Value v = this.getTop();
      if (!v.isArrayValue()) {
         throw new MessageTypeException("Expected array but got not array value");
      } else {
         ArrayValue a = v.asArrayValue();
         this.stack.reduceCount();
         this.stack.pushArray(a.size());
         this.values[this.stack.getDepth()] = a.getElementArray();
         return a.size();
      }
   }

   public void readArrayEnd(boolean check) throws IOException {
      if (!this.stack.topIsArray()) {
         throw new MessageTypeException("readArrayEnd() is called but readArrayBegin() is not called");
      } else {
         int remain = this.stack.getTopCount();
         if (remain > 0) {
            if (check) {
               throw new MessageTypeException("readArrayEnd(check=true) is called but the array is not end");
            }

            for(int i = 0; i < remain; ++i) {
               this.skip();
            }
         }

         this.stack.pop();
         if (this.stack.getDepth() == 0) {
            this.value = null;
         }

      }
   }

   public int readMapBegin() throws IOException {
      Value v = this.getTop();
      if (!v.isMapValue()) {
         throw new MessageTypeException("Expected map but got not map value");
      } else {
         MapValue m = v.asMapValue();
         this.stack.reduceCount();
         this.stack.pushMap(m.size());
         this.values[this.stack.getDepth()] = m.getKeyValueArray();
         return m.size();
      }
   }

   public void readMapEnd(boolean check) throws IOException {
      if (!this.stack.topIsMap()) {
         throw new MessageTypeException("readMapEnd() is called but readMapBegin() is not called");
      } else {
         int remain = this.stack.getTopCount();
         if (remain > 0) {
            if (check) {
               throw new MessageTypeException("readMapEnd(check=true) is called but the map is not end");
            }

            for(int i = 0; i < remain; ++i) {
               this.skip();
            }
         }

         this.stack.pop();
         if (this.stack.getDepth() == 0) {
            this.value = null;
         }

      }
   }

   private Value getTop() throws IOException {
      this.ensureValue();
      this.stack.checkCount();
      if (this.stack.getDepth() == 0) {
         return this.value;
      } else {
         Value[] array = (Value[])((Value[])this.values[this.stack.getDepth()]);
         return array[array.length - this.stack.getTopCount()];
      }
   }

   public Value readValue() throws IOException {
      if (this.stack.getDepth() == 0) {
         if (this.value == null) {
            return this.nextValue();
         } else {
            Value v = this.value;
            this.value = null;
            return v;
         }
      } else {
         return super.readValue();
      }
   }

   protected void readValue(Unconverter uc) throws IOException {
      if (uc.getResult() != null) {
         uc.resetResult();
      }

      this.stack.checkCount();
      Value v = this.getTop();
      if (!v.isArrayValue() && !v.isMapValue()) {
         uc.write(v);
         this.stack.reduceCount();
         if (this.stack.getDepth() == 0) {
            this.value = null;
         }

         if (uc.getResult() != null) {
            return;
         }
      }

      do {
         while(this.stack.getDepth() == 0 || this.stack.getTopCount() != 0) {
            this.stack.checkCount();
            v = this.getTop();
            if (v.isArrayValue()) {
               ArrayValue a = v.asArrayValue();
               uc.writeArrayBegin(a.size());
               this.stack.reduceCount();
               this.stack.pushArray(a.size());
               this.values[this.stack.getDepth()] = a.getElementArray();
            } else if (v.isMapValue()) {
               MapValue m = v.asMapValue();
               uc.writeMapBegin(m.size());
               this.stack.reduceCount();
               this.stack.pushMap(m.size());
               this.values[this.stack.getDepth()] = m.getKeyValueArray();
            } else {
               uc.write(v);
               this.stack.reduceCount();
            }
         }

         if (this.stack.topIsArray()) {
            uc.writeArrayEnd(true);
            this.stack.pop();
         } else {
            if (!this.stack.topIsMap()) {
               throw new RuntimeException("invalid stack");
            }

            uc.writeMapEnd(true);
            this.stack.pop();
         }

         if (this.stack.getDepth() == 0) {
            this.value = null;
         }
      } while(uc.getResult() == null);

   }

   public void skip() throws IOException {
      this.stack.checkCount();
      Value v = this.getTop();
      if (!v.isArrayValue() && !v.isMapValue()) {
         this.stack.reduceCount();
         if (this.stack.getDepth() == 0) {
            this.value = null;
         }

      } else {
         int targetDepth = this.stack.getDepth();

         do {
            while(this.stack.getTopCount() != 0) {
               this.stack.checkCount();
               v = this.getTop();
               if (v.isArrayValue()) {
                  ArrayValue a = v.asArrayValue();
                  this.stack.reduceCount();
                  this.stack.pushArray(a.size());
                  this.values[this.stack.getDepth()] = a.getElementArray();
               } else if (v.isMapValue()) {
                  MapValue m = v.asMapValue();
                  this.stack.reduceCount();
                  this.stack.pushMap(m.size());
                  this.values[this.stack.getDepth()] = m.getKeyValueArray();
               } else {
                  this.stack.reduceCount();
               }
            }

            this.stack.pop();
            if (this.stack.getDepth() == 0) {
               this.value = null;
            }
         } while(this.stack.getDepth() > targetDepth);

      }
   }

   public ValueType getNextType() throws IOException {
      return this.getTop().getType();
   }

   public void reset() {
      this.stack.clear();
      this.value = null;
   }

   public void close() throws IOException {
   }

   public int getReadByteCount() {
      throw new UnsupportedOperationException("Not implemented yet");
   }

   public void setRawSizeLimit(int size) {
      throw new UnsupportedOperationException("Not implemented yet");
   }

   public void setArraySizeLimit(int size) {
      throw new UnsupportedOperationException("Not implemented yet");
   }

   public void setMapSizeLimit(int size) {
      throw new UnsupportedOperationException("Not implemented yet");
   }
}

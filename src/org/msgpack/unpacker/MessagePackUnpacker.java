package org.msgpack.unpacker;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;
import org.msgpack.io.BufferReferer;
import org.msgpack.io.Input;
import org.msgpack.io.StreamInput;
import org.msgpack.packer.Unconverter;
import org.msgpack.type.ValueType;

public class MessagePackUnpacker extends AbstractUnpacker {
   private static final byte REQUIRE_TO_READ_HEAD = -63;
   protected final Input in;
   private final UnpackerStack stack;
   private byte headByte;
   private byte[] raw;
   private int rawFilled;
   private final IntAccept intAccept;
   private final LongAccept longAccept;
   private final BigIntegerAccept bigIntegerAccept;
   private final DoubleAccept doubleAccept;
   private final ByteArrayAccept byteArrayAccept;
   private final StringAccept stringAccept;
   private final ArrayAccept arrayAccept;
   private final MapAccept mapAccept;
   private final ValueAccept valueAccept;
   private final SkipAccept skipAccept;

   public MessagePackUnpacker(MessagePack msgpack, InputStream stream) {
      this(msgpack, (Input)(new StreamInput(stream)));
   }

   protected MessagePackUnpacker(MessagePack msgpack, Input in) {
      super(msgpack);
      this.stack = new UnpackerStack();
      this.headByte = -63;
      this.intAccept = new IntAccept();
      this.longAccept = new LongAccept();
      this.bigIntegerAccept = new BigIntegerAccept();
      this.doubleAccept = new DoubleAccept();
      this.byteArrayAccept = new ByteArrayAccept();
      this.stringAccept = new StringAccept();
      this.arrayAccept = new ArrayAccept();
      this.mapAccept = new MapAccept();
      this.valueAccept = new ValueAccept();
      this.skipAccept = new SkipAccept();
      this.in = in;
   }

   private byte getHeadByte() throws IOException {
      byte b = this.headByte;
      if (b == -63) {
         b = this.headByte = this.in.readByte();
      }

      return b;
   }

   final void readOne(Accept a) throws IOException {
      this.stack.checkCount();
      if (this.readOneWithoutStack(a)) {
         this.stack.reduceCount();
      }

   }

   final boolean readOneWithoutStack(Accept a) throws IOException {
      if (this.raw != null) {
         this.readRawBodyCont();
         a.acceptRaw(this.raw);
         this.raw = null;
         this.headByte = -63;
         return true;
      } else {
         int b = this.getHeadByte();
         if ((b & 128) == 0) {
            a.acceptInteger((int)b);
            this.headByte = -63;
            return true;
         } else if ((b & 224) == 224) {
            a.acceptInteger((int)b);
            this.headByte = -63;
            return true;
         } else {
            int count;
            if ((b & 224) == 160) {
               count = b & 31;
               if (count == 0) {
                  a.acceptEmptyRaw();
                  this.headByte = -63;
                  return true;
               } else {
                  if (!this.tryReferRawBody(a, count)) {
                     this.readRawBody(count);
                     a.acceptRaw(this.raw);
                     this.raw = null;
                  }

                  this.headByte = -63;
                  return true;
               }
            } else if ((b & 240) == 144) {
               count = b & 15;
               a.acceptArray(count);
               this.stack.reduceCount();
               this.stack.pushArray(count);
               this.headByte = -63;
               return false;
            } else if ((b & 240) == 128) {
               count = b & 15;
               a.acceptMap(count);
               this.stack.reduceCount();
               this.stack.pushMap(count);
               this.headByte = -63;
               return false;
            } else {
               return this.readOneWithoutStackLarge(a, b);
            }
         }
      }
   }

   private boolean readOneWithoutStackLarge(Accept a, int b) throws IOException {
      int count;
      String reason;
      switch(b & 255) {
      case 192:
         a.acceptNil();
         this.headByte = -63;
         return true;
      case 193:
      case 199:
      case 200:
      case 201:
      case 212:
      case 213:
      case 214:
      case 215:
      case 216:
      default:
         this.headByte = -63;
         throw new IOException("Invalid byte: " + b);
      case 194:
         a.acceptBoolean(false);
         this.headByte = -63;
         return true;
      case 195:
         a.acceptBoolean(true);
         this.headByte = -63;
         return true;
      case 196:
      case 217:
         int count = this.in.getByte();
         if (count == 0) {
            a.acceptEmptyRaw();
            this.in.advance();
            this.headByte = -63;
            return true;
         } else {
            if (count >= this.rawSizeLimit) {
               reason = String.format("Size of raw (%d) over limit at %d", Integer.valueOf(count), this.rawSizeLimit);
               throw new SizeLimitException(reason);
            }

            this.in.advance();
            if (!this.tryReferRawBody(a, count)) {
               this.readRawBody(count);
               a.acceptRaw(this.raw);
               this.raw = null;
            }

            this.headByte = -63;
            return true;
         }
      case 197:
      case 218:
         count = this.in.getShort() & '\uffff';
         if (count == 0) {
            a.acceptEmptyRaw();
            this.in.advance();
            this.headByte = -63;
            return true;
         } else {
            if (count >= this.rawSizeLimit) {
               reason = String.format("Size of raw (%d) over limit at %d", count, this.rawSizeLimit);
               throw new SizeLimitException(reason);
            }

            this.in.advance();
            if (!this.tryReferRawBody(a, count)) {
               this.readRawBody(count);
               a.acceptRaw(this.raw);
               this.raw = null;
            }

            this.headByte = -63;
            return true;
         }
      case 198:
      case 219:
         count = this.in.getInt();
         if (count == 0) {
            a.acceptEmptyRaw();
            this.in.advance();
            this.headByte = -63;
            return true;
         } else {
            if (count >= 0 && count < this.rawSizeLimit) {
               this.in.advance();
               if (!this.tryReferRawBody(a, count)) {
                  this.readRawBody(count);
                  a.acceptRaw(this.raw);
                  this.raw = null;
               }

               this.headByte = -63;
               return true;
            }

            reason = String.format("Size of raw (%d) over limit at %d", count, this.rawSizeLimit);
            throw new SizeLimitException(reason);
         }
      case 202:
         a.acceptFloat(this.in.getFloat());
         this.in.advance();
         this.headByte = -63;
         return true;
      case 203:
         a.acceptDouble(this.in.getDouble());
         this.in.advance();
         this.headByte = -63;
         return true;
      case 204:
         a.acceptUnsignedInteger(this.in.getByte());
         this.in.advance();
         this.headByte = -63;
         return true;
      case 205:
         a.acceptUnsignedInteger(this.in.getShort());
         this.in.advance();
         this.headByte = -63;
         return true;
      case 206:
         a.acceptUnsignedInteger(this.in.getInt());
         this.in.advance();
         this.headByte = -63;
         return true;
      case 207:
         a.acceptUnsignedInteger(this.in.getLong());
         this.in.advance();
         this.headByte = -63;
         return true;
      case 208:
         a.acceptInteger(this.in.getByte());
         this.in.advance();
         this.headByte = -63;
         return true;
      case 209:
         a.acceptInteger(this.in.getShort());
         this.in.advance();
         this.headByte = -63;
         return true;
      case 210:
         a.acceptInteger(this.in.getInt());
         this.in.advance();
         this.headByte = -63;
         return true;
      case 211:
         a.acceptInteger(this.in.getLong());
         this.in.advance();
         this.headByte = -63;
         return true;
      case 220:
         count = this.in.getShort() & '\uffff';
         if (count >= this.arraySizeLimit) {
            reason = String.format("Size of array (%d) over limit at %d", count, this.arraySizeLimit);
            throw new SizeLimitException(reason);
         }

         a.acceptArray(count);
         this.stack.reduceCount();
         this.stack.pushArray(count);
         this.in.advance();
         this.headByte = -63;
         return false;
      case 221:
         count = this.in.getInt();
         if (count >= 0 && count < this.arraySizeLimit) {
            a.acceptArray(count);
            this.stack.reduceCount();
            this.stack.pushArray(count);
            this.in.advance();
            this.headByte = -63;
            return false;
         }

         reason = String.format("Size of array (%d) over limit at %d", count, this.arraySizeLimit);
         throw new SizeLimitException(reason);
      case 222:
         count = this.in.getShort() & '\uffff';
         if (count >= this.mapSizeLimit) {
            reason = String.format("Size of map (%d) over limit at %d", count, this.mapSizeLimit);
            throw new SizeLimitException(reason);
         }

         a.acceptMap(count);
         this.stack.reduceCount();
         this.stack.pushMap(count);
         this.in.advance();
         this.headByte = -63;
         return false;
      case 223:
         count = this.in.getInt();
         if (count >= 0 && count < this.mapSizeLimit) {
            a.acceptMap(count);
            this.stack.reduceCount();
            this.stack.pushMap(count);
            this.in.advance();
            this.headByte = -63;
            return false;
         } else {
            reason = String.format("Size of map (%d) over limit at %d", count, this.mapSizeLimit);
            throw new SizeLimitException(reason);
         }
      }
   }

   private boolean tryReferRawBody(BufferReferer referer, int size) throws IOException {
      return this.in.tryRefer(referer, size);
   }

   private void readRawBody(int size) throws IOException {
      this.raw = new byte[size];
      this.rawFilled = 0;
      this.readRawBodyCont();
   }

   private void readRawBodyCont() throws IOException {
      int len = this.in.read(this.raw, this.rawFilled, this.raw.length - this.rawFilled);
      this.rawFilled += len;
      if (this.rawFilled < this.raw.length) {
         throw new EOFException();
      }
   }

   protected boolean tryReadNil() throws IOException {
      this.stack.checkCount();
      int b = this.getHeadByte() & 255;
      if (b == 192) {
         this.stack.reduceCount();
         this.headByte = -63;
         return true;
      } else {
         return false;
      }
   }

   public boolean trySkipNil() throws IOException {
      if (this.stack.getDepth() > 0 && this.stack.getTopCount() <= 0) {
         return true;
      } else {
         int b = this.getHeadByte() & 255;
         if (b == 192) {
            this.stack.reduceCount();
            this.headByte = -63;
            return true;
         } else {
            return false;
         }
      }
   }

   public void readNil() throws IOException {
      this.stack.checkCount();
      int b = this.getHeadByte() & 255;
      if (b == 192) {
         this.stack.reduceCount();
         this.headByte = -63;
      } else {
         throw new MessageTypeException("Expected nil but got not nil value");
      }
   }

   public boolean readBoolean() throws IOException {
      this.stack.checkCount();
      int b = this.getHeadByte() & 255;
      if (b == 194) {
         this.stack.reduceCount();
         this.headByte = -63;
         return false;
      } else if (b == 195) {
         this.stack.reduceCount();
         this.headByte = -63;
         return true;
      } else {
         throw new MessageTypeException("Expected Boolean but got not boolean value");
      }
   }

   public byte readByte() throws IOException {
      this.stack.checkCount();
      this.readOneWithoutStack(this.intAccept);
      int value = this.intAccept.value;
      if (value >= -128 && value <= 127) {
         this.stack.reduceCount();
         return (byte)value;
      } else {
         throw new MessageTypeException();
      }
   }

   public short readShort() throws IOException {
      this.stack.checkCount();
      this.readOneWithoutStack(this.intAccept);
      int value = this.intAccept.value;
      if (value >= -32768 && value <= 32767) {
         this.stack.reduceCount();
         return (short)value;
      } else {
         throw new MessageTypeException();
      }
   }

   public int readInt() throws IOException {
      this.readOne(this.intAccept);
      return this.intAccept.value;
   }

   public long readLong() throws IOException {
      this.readOne(this.longAccept);
      return this.longAccept.value;
   }

   public BigInteger readBigInteger() throws IOException {
      this.readOne(this.bigIntegerAccept);
      return this.bigIntegerAccept.value;
   }

   public float readFloat() throws IOException {
      this.readOne(this.doubleAccept);
      return (float)this.doubleAccept.value;
   }

   public double readDouble() throws IOException {
      this.readOne(this.doubleAccept);
      return this.doubleAccept.value;
   }

   public byte[] readByteArray() throws IOException {
      this.readOne(this.byteArrayAccept);
      return this.byteArrayAccept.value;
   }

   public String readString() throws IOException {
      this.readOne(this.stringAccept);
      return this.stringAccept.value;
   }

   public int readArrayBegin() throws IOException {
      this.readOne(this.arrayAccept);
      return this.arrayAccept.size;
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
      }
   }

   public int readMapBegin() throws IOException {
      this.readOne(this.mapAccept);
      return this.mapAccept.size;
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
      }
   }

   protected void readValue(Unconverter uc) throws IOException {
      if (uc.getResult() != null) {
         uc.resetResult();
      }

      this.valueAccept.setUnconverter(uc);
      this.stack.checkCount();
      if (this.readOneWithoutStack(this.valueAccept)) {
         this.stack.reduceCount();
         if (uc.getResult() != null) {
            return;
         }
      }

      do {
         while(this.stack.getTopCount() != 0) {
            this.readOne(this.valueAccept);
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
      } while(uc.getResult() == null);

   }

   public void skip() throws IOException {
      this.stack.checkCount();
      if (this.readOneWithoutStack(this.skipAccept)) {
         this.stack.reduceCount();
      } else {
         int targetDepth = this.stack.getDepth() - 1;

         do {
            while(this.stack.getTopCount() != 0) {
               this.readOne(this.skipAccept);
            }

            this.stack.pop();
         } while(this.stack.getDepth() > targetDepth);

      }
   }

   public ValueType getNextType() throws IOException {
      int b = this.getHeadByte();
      if ((b & 128) == 0) {
         return ValueType.INTEGER;
      } else if ((b & 224) == 224) {
         return ValueType.INTEGER;
      } else if ((b & 224) == 160) {
         return ValueType.RAW;
      } else if ((b & 240) == 144) {
         return ValueType.ARRAY;
      } else if ((b & 240) == 128) {
         return ValueType.MAP;
      } else {
         switch(b & 255) {
         case 192:
            return ValueType.NIL;
         case 193:
         case 199:
         case 200:
         case 201:
         case 212:
         case 213:
         case 214:
         case 215:
         case 216:
         default:
            throw new IOException("Invalid byte: " + b);
         case 194:
         case 195:
            return ValueType.BOOLEAN;
         case 196:
         case 197:
         case 198:
         case 217:
         case 218:
         case 219:
            return ValueType.RAW;
         case 202:
         case 203:
            return ValueType.FLOAT;
         case 204:
         case 205:
         case 206:
         case 207:
         case 208:
         case 209:
         case 210:
         case 211:
            return ValueType.INTEGER;
         case 220:
         case 221:
            return ValueType.ARRAY;
         case 222:
         case 223:
            return ValueType.MAP;
         }
      }
   }

   public void reset() {
      this.raw = null;
      this.stack.clear();
   }

   public void close() throws IOException {
      this.in.close();
   }

   public int getReadByteCount() {
      return this.in.getReadByteCount();
   }

   public void resetReadByteCount() {
      this.in.resetReadByteCount();
   }
}

package org.msgpack.util.json;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import org.msgpack.MessagePack;
import org.msgpack.MessageTypeException;
import org.msgpack.io.Output;
import org.msgpack.io.StreamOutput;
import org.msgpack.packer.AbstractPacker;
import org.msgpack.packer.Packer;
import org.msgpack.packer.PackerStack;

public class JSONPacker extends AbstractPacker {
   private static final byte[] NULL = new byte[]{110, 117, 108, 108};
   private static final byte[] TRUE = new byte[]{116, 114, 117, 101};
   private static final byte[] FALSE = new byte[]{102, 97, 108, 115, 101};
   private static final byte COMMA = 44;
   private static final byte COLON = 58;
   private static final byte QUOTE = 34;
   private static final byte LEFT_BR = 91;
   private static final byte RIGHT_BR = 93;
   private static final byte LEFT_WN = 123;
   private static final byte RIGHT_WN = 125;
   private static final byte BACKSLASH = 92;
   private static final byte ZERO = 48;
   private static final int FLAG_FIRST_ELEMENT = 1;
   private static final int FLAG_MAP_KEY = 2;
   private static final int FLAG_MAP_VALUE = 4;
   protected final Output out;
   private int[] flags;
   private PackerStack stack;
   private CharsetDecoder decoder;
   private static final int[] ESCAPE_TABLE = new int[128];
   private static final byte[] HEX_TABLE;

   public JSONPacker(OutputStream stream) {
      this(new MessagePack(), stream);
   }

   public JSONPacker(MessagePack msgpack, OutputStream stream) {
      this(msgpack, (Output)(new StreamOutput(stream)));
   }

   protected JSONPacker(MessagePack msgpack, Output out) {
      super(msgpack);
      this.stack = new PackerStack();
      this.out = out;
      this.stack = new PackerStack();
      this.flags = new int[128];
      this.decoder = Charset.forName("UTF-8").newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
   }

   protected void writeBoolean(boolean v) throws IOException {
      this.beginElement();
      if (v) {
         this.out.write(TRUE, 0, TRUE.length);
      } else {
         this.out.write(FALSE, 0, FALSE.length);
      }

      this.endElement();
   }

   protected void writeByte(byte v) throws IOException {
      this.beginElement();
      byte[] b = Byte.toString(v).getBytes();
      this.out.write(b, 0, b.length);
      this.endElement();
   }

   protected void writeShort(short v) throws IOException {
      this.beginElement();
      byte[] b = Short.toString(v).getBytes();
      this.out.write(b, 0, b.length);
      this.endElement();
   }

   protected void writeInt(int v) throws IOException {
      this.beginElement();
      byte[] b = Integer.toString(v).getBytes();
      this.out.write(b, 0, b.length);
      this.endElement();
   }

   protected void writeLong(long v) throws IOException {
      this.beginElement();
      byte[] b = Long.toString(v).getBytes();
      this.out.write(b, 0, b.length);
      this.endElement();
   }

   protected void writeBigInteger(BigInteger v) throws IOException {
      this.beginElement();
      byte[] b = v.toString().getBytes();
      this.out.write(b, 0, b.length);
      this.endElement();
   }

   protected void writeFloat(float v) throws IOException {
      this.beginElement();
      Float r = v;
      if (!r.isInfinite() && !r.isNaN()) {
         byte[] b = Float.toString(v).getBytes();
         this.out.write(b, 0, b.length);
         this.endElement();
      } else {
         throw new IOException("JSONPacker doesn't support NaN and infinite float value");
      }
   }

   protected void writeDouble(double v) throws IOException {
      this.beginElement();
      Double r = v;
      if (!r.isInfinite() && !r.isNaN()) {
         byte[] b = Double.toString(v).getBytes();
         this.out.write(b, 0, b.length);
         this.endElement();
      } else {
         throw new IOException("JSONPacker doesn't support NaN and infinite float value");
      }
   }

   protected void writeByteArray(byte[] b, int off, int len) throws IOException {
      this.beginStringElement();
      this.out.writeByte((byte)34);
      this.escape(this.out, b, off, len);
      this.out.writeByte((byte)34);
      this.endElement();
   }

   protected void writeByteBuffer(ByteBuffer bb) throws IOException {
      this.beginStringElement();
      this.out.writeByte((byte)34);
      int pos = bb.position();

      try {
         this.escape(this.out, bb);
      } finally {
         bb.position(pos);
      }

      this.out.writeByte((byte)34);
      this.endElement();
   }

   protected void writeString(String s) throws IOException {
      this.beginStringElement();
      this.out.writeByte((byte)34);
      escape(this.out, s);
      this.out.writeByte((byte)34);
      this.endElement();
   }

   public Packer writeNil() throws IOException {
      this.beginElement();
      this.out.write(NULL, 0, NULL.length);
      this.endElement();
      return this;
   }

   public Packer writeArrayBegin(int size) throws IOException {
      this.beginElement();
      this.out.writeByte((byte)91);
      this.endElement();
      this.stack.pushArray(size);
      this.flags[this.stack.getDepth()] = 1;
      return this;
   }

   public Packer writeArrayEnd(boolean check) throws IOException {
      if (!this.stack.topIsArray()) {
         throw new MessageTypeException("writeArrayEnd() is called but writeArrayBegin() is not called");
      } else {
         int remain = this.stack.getTopCount();
         if (remain > 0) {
            if (check) {
               throw new MessageTypeException("writeArrayEnd(check=true) is called but the array is not end: " + remain);
            }

            for(int i = 0; i < remain; ++i) {
               this.writeNil();
            }
         }

         this.stack.pop();
         this.out.writeByte((byte)93);
         return this;
      }
   }

   public Packer writeMapBegin(int size) throws IOException {
      this.beginElement();
      this.out.writeByte((byte)123);
      this.endElement();
      this.stack.pushMap(size);
      this.flags[this.stack.getDepth()] = 3;
      return this;
   }

   public Packer writeMapEnd(boolean check) throws IOException {
      if (!this.stack.topIsMap()) {
         throw new MessageTypeException("writeMapEnd() is called but writeMapBegin() is not called");
      } else {
         int remain = this.stack.getTopCount();
         if (remain > 0) {
            if (check) {
               throw new MessageTypeException("writeMapEnd(check=true) is called but the map is not end: " + remain);
            }

            for(int i = 0; i < remain; ++i) {
               this.writeNil();
            }
         }

         this.stack.pop();
         this.out.writeByte((byte)125);
         return this;
      }
   }

   public void flush() throws IOException {
      this.out.flush();
   }

   public void close() throws IOException {
      this.out.close();
   }

   public void reset() {
      this.stack.clear();
   }

   private void beginElement() throws IOException {
      int flag = this.flags[this.stack.getDepth()];
      if ((flag & 2) != 0) {
         throw new IOException("Key of a map must be a string in JSON");
      } else {
         this.beginStringElement();
      }
   }

   private void beginStringElement() throws IOException {
      int flag = this.flags[this.stack.getDepth()];
      if ((flag & 4) != 0) {
         this.out.writeByte((byte)58);
      } else if (this.stack.getDepth() > 0 && (flag & 1) == 0) {
         this.out.writeByte((byte)44);
      }

   }

   private void endElement() throws IOException {
      int flag = this.flags[this.stack.getDepth()];
      if ((flag & 2) != 0) {
         flag &= -3;
         flag |= 4;
      } else if ((flag & 4) != 0) {
         flag &= -5;
         flag |= 2;
      }

      flag &= -2;
      this.flags[this.stack.getDepth()] = flag;
      this.stack.reduceCount();
   }

   private void escape(Output out, byte[] b, int off, int len) throws IOException {
      this.escape(out, ByteBuffer.wrap(b, off, len));
   }

   private void escape(Output out, ByteBuffer bb) throws IOException {
      String str = this.decoder.decode(bb).toString();
      escape(out, str);
   }

   private static void escape(Output out, String s) throws IOException {
      byte[] tmp = new byte[]{92, 117, 0, 0, 0, 0};
      char[] chars = s.toCharArray();

      for(int i = 0; i < chars.length; ++i) {
         int ch = chars[i];
         if (ch <= 127) {
            int e = ESCAPE_TABLE[ch];
            if (e == 0) {
               tmp[2] = (byte)ch;
               out.write(tmp, 2, 1);
            } else if (e > 0) {
               tmp[2] = 92;
               tmp[3] = (byte)e;
               out.write(tmp, 2, 2);
            } else {
               tmp[2] = 48;
               tmp[3] = 48;
               tmp[4] = HEX_TABLE[ch >> 4];
               tmp[5] = HEX_TABLE[ch & 15];
               out.write(tmp, 0, 6);
            }
         } else if (ch <= 2047) {
            tmp[2] = (byte)(192 | ch >> 6);
            tmp[3] = (byte)(128 | ch & 63);
            out.write(tmp, 2, 2);
         } else if (ch >= '\ud800' && ch <= '\udfff') {
            tmp[2] = HEX_TABLE[ch >> 12 & 15];
            tmp[3] = HEX_TABLE[ch >> 8 & 15];
            tmp[4] = HEX_TABLE[ch >> 4 & 15];
            tmp[5] = HEX_TABLE[ch & 15];
            out.write(tmp, 0, 6);
         } else {
            tmp[2] = (byte)(224 | ch >> 12);
            tmp[3] = (byte)(128 | ch >> 6 & 63);
            tmp[4] = (byte)(128 | ch & 63);
            out.write(tmp, 2, 3);
         }
      }

   }

   static {
      for(int i = 0; i < 32; ++i) {
         ESCAPE_TABLE[i] = -1;
      }

      ESCAPE_TABLE[34] = 34;
      ESCAPE_TABLE[92] = 92;
      ESCAPE_TABLE[8] = 98;
      ESCAPE_TABLE[9] = 116;
      ESCAPE_TABLE[12] = 102;
      ESCAPE_TABLE[10] = 110;
      ESCAPE_TABLE[13] = 114;
      char[] hex = "0123456789ABCDEF".toCharArray();
      HEX_TABLE = new byte[hex.length];

      for(int i = 0; i < hex.length; ++i) {
         HEX_TABLE[i] = (byte)hex[i];
      }

   }
}

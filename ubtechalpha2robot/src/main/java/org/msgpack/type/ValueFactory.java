package org.msgpack.type;

import java.math.BigInteger;
import java.nio.ByteBuffer;

public final class ValueFactory {
   public static NilValue createNilValue() {
      return NilValue.getInstance();
   }

   public static BooleanValue createBooleanValue(boolean v) {
      return (BooleanValue)(v ? TrueValueImpl.getInstance() : FalseValueImpl.getInstance());
   }

   public static IntegerValue createIntegerValue(byte v) {
      return new IntValueImpl(v);
   }

   public static IntegerValue createIntegerValue(short v) {
      return new IntValueImpl(v);
   }

   public static IntegerValue createIntegerValue(int v) {
      return new IntValueImpl(v);
   }

   public static IntegerValue createIntegerValue(long v) {
      return new LongValueImpl(v);
   }

   public static IntegerValue createIntegerValue(BigInteger v) {
      return new BigIntegerValueImpl(v);
   }

   public static FloatValue createFloatValue(float v) {
      return new FloatValueImpl(v);
   }

   public static FloatValue createFloatValue(double v) {
      return new DoubleValueImpl(v);
   }

   public static RawValue createRawValue() {
      return ByteArrayRawValueImpl.getEmptyInstance();
   }

   public static RawValue createRawValue(byte[] b) {
      return createRawValue(b, false);
   }

   public static RawValue createRawValue(byte[] b, boolean gift) {
      return new ByteArrayRawValueImpl(b, gift);
   }

   public static RawValue createRawValue(byte[] b, int off, int len) {
      return new ByteArrayRawValueImpl(b, off, len);
   }

   public static RawValue createRawValue(String s) {
      return new StringRawValueImpl(s);
   }

   public static RawValue createRawValue(ByteBuffer bb) {
      int pos = bb.position();

      ByteArrayRawValueImpl var3;
      try {
         byte[] buf = new byte[bb.remaining()];
         bb.get(buf);
         var3 = new ByteArrayRawValueImpl(buf, true);
      } finally {
         bb.position(pos);
      }

      return var3;
   }

   public static ArrayValue createArrayValue() {
      return ArrayValueImpl.getEmptyInstance();
   }

   public static ArrayValue createArrayValue(Value[] array) {
      return array.length == 0 ? ArrayValueImpl.getEmptyInstance() : createArrayValue(array, false);
   }

   public static ArrayValue createArrayValue(Value[] array, boolean gift) {
      return (ArrayValue)(array.length == 0 ? ArrayValueImpl.getEmptyInstance() : new ArrayValueImpl(array, gift));
   }

   public static MapValue createMapValue() {
      return SequentialMapValueImpl.getEmptyInstance();
   }

   public static MapValue createMapValue(Value[] kvs) {
      return kvs.length == 0 ? SequentialMapValueImpl.getEmptyInstance() : createMapValue(kvs, false);
   }

   public static MapValue createMapValue(Value[] kvs, boolean gift) {
      return (MapValue)(kvs.length == 0 ? SequentialMapValueImpl.getEmptyInstance() : new SequentialMapValueImpl(kvs, gift));
   }

   private ValueFactory() {
   }
}

package org.msgpack.template;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.msgpack.type.Value;

public final class Templates {
   public static final Template<Value> TValue = ValueTemplate.getInstance();
   public static final Template<Byte> TByte = ByteTemplate.getInstance();
   public static final Template<Short> TShort = ShortTemplate.getInstance();
   public static final Template<Integer> TInteger = IntegerTemplate.getInstance();
   public static final Template<Long> TLong = LongTemplate.getInstance();
   public static final Template<Character> TCharacter = CharacterTemplate.getInstance();
   public static final Template<BigInteger> TBigInteger = BigIntegerTemplate.getInstance();
   public static final Template<BigDecimal> TBigDecimal = BigDecimalTemplate.getInstance();
   public static final Template<Float> TFloat = FloatTemplate.getInstance();
   public static final Template<Double> TDouble = DoubleTemplate.getInstance();
   public static final Template<Boolean> TBoolean = BooleanTemplate.getInstance();
   public static final Template<String> TString = StringTemplate.getInstance();
   public static final Template<byte[]> TByteArray = ByteArrayTemplate.getInstance();
   public static final Template<ByteBuffer> TByteBuffer = ByteBufferTemplate.getInstance();
   public static final Template<Date> TDate = DateTemplate.getInstance();

   public Templates() {
   }

   public static Objectemplate NotNullable(Template innerTemplate) {
      return new NotNullableTemplate(innerTemplate);
   }

   public static Template tList(Template elementTemplate) {
      return new ListTemplate(elementTemplate);
   }

   public static Template tMap(Template keyTemplate, Template valueTemplate) {
      return new MapTemplate(keyTemplate, valueTemplate);
   }

   public static Template tCollection(Template elementTemplate) {
      return new CollectionTemplate(elementTemplate);
   }

   public static Template tOrdinalEnum(Class enumClass) {
      return new OrdinalEnumTemplate(enumClass);
   }

   /** @deprecated */
   @Deprecated
   public static Template tByte() {
      return TByte;
   }

   /** @deprecated */
   @Deprecated
   public static Template tShort() {
      return TShort;
   }

   /** @deprecated */
   @Deprecated
   public static Template tInteger() {
      return TInteger;
   }

   /** @deprecated */
   @Deprecated
   public static Template tLong() {
      return TLong;
   }

   /** @deprecated */
   @Deprecated
   public static Template tCharacter() {
      return TCharacter;
   }

   /** @deprecated */
   @Deprecated
   public static Template tBigInteger() {
      return TBigInteger;
   }

   /** @deprecated */
   @Deprecated
   public static Template tBigDecimal() {
      return TBigDecimal;
   }

   /** @deprecated */
   @Deprecated
   public static Template tFloat() {
      return TFloat;
   }

   /** @deprecated */
   @Deprecated
   public static Template tDouble() {
      return TDouble;
   }

   /** @deprecated */
   @Deprecated
   public static Template tBoolean() {
      return TBoolean;
   }

   /** @deprecated */
   @Deprecated
   public static Template tString() {
      return TString;
   }

   /** @deprecated */
   @Deprecated
   public static Template tByteArray() {
      return TByteArray;
   }

   /** @deprecated */
   @Deprecated
   public static Template tByteBuffer() {
      return TByteBuffer;
   }

   /** @deprecated */
   @Deprecated
   public static Template tDate() {
      return TDate;
   }
}

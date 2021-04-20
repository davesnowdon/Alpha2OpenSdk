package org.codehaus.jackson;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public abstract class JsonNode implements Iterable<JsonNode> {
   protected static final List<JsonNode> NO_NODES = Collections.emptyList();
   protected static final List<String> NO_STRINGS = Collections.emptyList();

   protected JsonNode() {
   }

   public boolean isValueNode() {
      return false;
   }

   public boolean isContainerNode() {
      return false;
   }

   public boolean isMissingNode() {
      return false;
   }

   public boolean isArray() {
      return false;
   }

   public boolean isObject() {
      return false;
   }

   public boolean isPojo() {
      return false;
   }

   public boolean isNumber() {
      return false;
   }

   public boolean isIntegralNumber() {
      return false;
   }

   public boolean isFloatingPointNumber() {
      return false;
   }

   public boolean isInt() {
      return false;
   }

   public boolean isLong() {
      return false;
   }

   public boolean isDouble() {
      return false;
   }

   public boolean isBigDecimal() {
      return false;
   }

   public boolean isBigInteger() {
      return false;
   }

   public boolean isTextual() {
      return false;
   }

   public boolean isBoolean() {
      return false;
   }

   public boolean isNull() {
      return false;
   }

   public boolean isBinary() {
      return false;
   }

   public abstract JsonToken asToken();

   public abstract JsonParser.NumberType getNumberType();

   public String getTextValue() {
      return null;
   }

   public byte[] getBinaryValue() throws IOException {
      return null;
   }

   public boolean getBooleanValue() {
      return false;
   }

   public Number getNumberValue() {
      return null;
   }

   public int getIntValue() {
      return 0;
   }

   public long getLongValue() {
      return 0L;
   }

   public double getDoubleValue() {
      return 0.0D;
   }

   public BigDecimal getDecimalValue() {
      return BigDecimal.ZERO;
   }

   public BigInteger getBigIntegerValue() {
      return BigInteger.ZERO;
   }

   public JsonNode get(int index) {
      return null;
   }

   public JsonNode get(String fieldName) {
      return null;
   }

   public abstract String getValueAsText();

   public int getValueAsInt() {
      return this.getValueAsInt(0);
   }

   public int getValueAsInt(int defaultValue) {
      return defaultValue;
   }

   public long getValueAsLong() {
      return this.getValueAsLong(0L);
   }

   public long getValueAsLong(long defaultValue) {
      return defaultValue;
   }

   public double getValueAsDouble() {
      return this.getValueAsDouble(0.0D);
   }

   public double getValueAsDouble(double defaultValue) {
      return defaultValue;
   }

   public boolean getValueAsBoolean() {
      return this.getValueAsBoolean(false);
   }

   public boolean getValueAsBoolean(boolean defaultValue) {
      return defaultValue;
   }

   public boolean has(String fieldName) {
      return this.get(fieldName) != null;
   }

   public boolean has(int index) {
      return this.get(index) != null;
   }

   public abstract JsonNode findValue(String var1);

   public final List<JsonNode> findValues(String fieldName) {
      List<JsonNode> result = this.findValues(fieldName, (List)null);
      return result == null ? Collections.emptyList() : result;
   }

   public final List<String> findValuesAsText(String fieldName) {
      List<String> result = this.findValuesAsText(fieldName, (List)null);
      return result == null ? Collections.emptyList() : result;
   }

   public abstract JsonNode findPath(String var1);

   public abstract JsonNode findParent(String var1);

   public final List<JsonNode> findParents(String fieldName) {
      List<JsonNode> result = this.findParents(fieldName, (List)null);
      return result == null ? Collections.emptyList() : result;
   }

   public abstract List<JsonNode> findValues(String var1, List<JsonNode> var2);

   public abstract List<String> findValuesAsText(String var1, List<String> var2);

   public abstract List<JsonNode> findParents(String var1, List<JsonNode> var2);

   /** @deprecated */
   @Deprecated
   public final JsonNode getFieldValue(String fieldName) {
      return this.get(fieldName);
   }

   /** @deprecated */
   @Deprecated
   public final JsonNode getElementValue(int index) {
      return this.get(index);
   }

   public int size() {
      return 0;
   }

   public final Iterator<JsonNode> iterator() {
      return this.getElements();
   }

   public Iterator<JsonNode> getElements() {
      return NO_NODES.iterator();
   }

   public Iterator<String> getFieldNames() {
      return NO_STRINGS.iterator();
   }

   public Iterator<Entry<String, JsonNode>> getFields() {
      Collection<Entry<String, JsonNode>> coll = Collections.emptyList();
      return coll.iterator();
   }

   public abstract JsonNode path(String var1);

   /** @deprecated */
   @Deprecated
   public final JsonNode getPath(String fieldName) {
      return this.path(fieldName);
   }

   public abstract JsonNode path(int var1);

   /** @deprecated */
   @Deprecated
   public final JsonNode getPath(int index) {
      return this.path(index);
   }

   public JsonNode with(String propertyName) {
      throw new UnsupportedOperationException("JsonNode not of type ObjectNode (but " + this.getClass().getName() + "), can not call with() on it");
   }

   /** @deprecated */
   @Deprecated
   public abstract void writeTo(JsonGenerator var1) throws IOException, JsonGenerationException;

   public abstract JsonParser traverse();

   public abstract String toString();

   public abstract boolean equals(Object var1);
}

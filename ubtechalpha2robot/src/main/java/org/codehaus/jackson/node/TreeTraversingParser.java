package org.codehaus.jackson.node;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import org.codehaus.jackson.Base64Variant;
import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonStreamContext;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.ObjectCodec;
import org.codehaus.jackson.impl.JsonParserMinimalBase;

public class TreeTraversingParser extends JsonParserMinimalBase {
   protected ObjectCodec _objectCodec;
   protected NodeCursor _nodeCursor;
   protected JsonToken _nextToken;
   protected boolean _startContainer;
   protected boolean _closed;

   public TreeTraversingParser(JsonNode n) {
      this(n, (ObjectCodec)null);
   }

   public TreeTraversingParser(JsonNode n, ObjectCodec codec) {
      super(0);
      this._objectCodec = codec;
      if (n.isArray()) {
         this._nextToken = JsonToken.START_ARRAY;
         this._nodeCursor = new NodeCursor.Array(n, (NodeCursor)null);
      } else if (n.isObject()) {
         this._nextToken = JsonToken.START_OBJECT;
         this._nodeCursor = new NodeCursor.Object(n, (NodeCursor)null);
      } else {
         this._nodeCursor = new NodeCursor.RootValue(n, (NodeCursor)null);
      }

   }

   public void setCodec(ObjectCodec c) {
      this._objectCodec = c;
   }

   public ObjectCodec getCodec() {
      return this._objectCodec;
   }

   public void close() throws IOException {
      if (!this._closed) {
         this._closed = true;
         this._nodeCursor = null;
         this._currToken = null;
      }

   }

   public JsonToken nextToken() throws IOException, JsonParseException {
      if (this._nextToken != null) {
         this._currToken = this._nextToken;
         this._nextToken = null;
         return this._currToken;
      } else if (this._startContainer) {
         this._startContainer = false;
         if (!this._nodeCursor.currentHasChildren()) {
            this._currToken = this._currToken == JsonToken.START_OBJECT ? JsonToken.END_OBJECT : JsonToken.END_ARRAY;
            return this._currToken;
         } else {
            this._nodeCursor = this._nodeCursor.iterateChildren();
            this._currToken = this._nodeCursor.nextToken();
            if (this._currToken == JsonToken.START_OBJECT || this._currToken == JsonToken.START_ARRAY) {
               this._startContainer = true;
            }

            return this._currToken;
         }
      } else if (this._nodeCursor == null) {
         this._closed = true;
         return null;
      } else {
         this._currToken = this._nodeCursor.nextToken();
         if (this._currToken == null) {
            this._currToken = this._nodeCursor.endToken();
            this._nodeCursor = this._nodeCursor.getParent();
            return this._currToken;
         } else {
            if (this._currToken == JsonToken.START_OBJECT || this._currToken == JsonToken.START_ARRAY) {
               this._startContainer = true;
            }

            return this._currToken;
         }
      }
   }

   public JsonParser skipChildren() throws IOException, JsonParseException {
      if (this._currToken == JsonToken.START_OBJECT) {
         this._startContainer = false;
         this._currToken = JsonToken.END_OBJECT;
      } else if (this._currToken == JsonToken.START_ARRAY) {
         this._startContainer = false;
         this._currToken = JsonToken.END_ARRAY;
      }

      return this;
   }

   public boolean isClosed() {
      return this._closed;
   }

   public String getCurrentName() {
      return this._nodeCursor == null ? null : this._nodeCursor.getCurrentName();
   }

   public JsonStreamContext getParsingContext() {
      return this._nodeCursor;
   }

   public JsonLocation getTokenLocation() {
      return JsonLocation.NA;
   }

   public JsonLocation getCurrentLocation() {
      return JsonLocation.NA;
   }

   public String getText() {
      if (this._closed) {
         return null;
      } else {
         switch(this._currToken) {
         case FIELD_NAME:
            return this._nodeCursor.getCurrentName();
         case VALUE_STRING:
            return this.currentNode().getTextValue();
         case VALUE_NUMBER_INT:
         case VALUE_NUMBER_FLOAT:
            return String.valueOf(this.currentNode().getNumberValue());
         case VALUE_EMBEDDED_OBJECT:
            JsonNode n = this.currentNode();
            if (n != null && n.isBinary()) {
               return n.getValueAsText();
            }
         default:
            return this._currToken == null ? null : this._currToken.asString();
         }
      }
   }

   public char[] getTextCharacters() throws IOException, JsonParseException {
      return this.getText().toCharArray();
   }

   public int getTextLength() throws IOException, JsonParseException {
      return this.getText().length();
   }

   public int getTextOffset() throws IOException, JsonParseException {
      return 0;
   }

   public boolean hasTextCharacters() {
      return false;
   }

   public JsonParser.NumberType getNumberType() throws IOException, JsonParseException {
      JsonNode n = this.currentNumericNode();
      return n == null ? null : n.getNumberType();
   }

   public BigInteger getBigIntegerValue() throws IOException, JsonParseException {
      return this.currentNumericNode().getBigIntegerValue();
   }

   public BigDecimal getDecimalValue() throws IOException, JsonParseException {
      return this.currentNumericNode().getDecimalValue();
   }

   public double getDoubleValue() throws IOException, JsonParseException {
      return this.currentNumericNode().getDoubleValue();
   }

   public float getFloatValue() throws IOException, JsonParseException {
      return (float)this.currentNumericNode().getDoubleValue();
   }

   public long getLongValue() throws IOException, JsonParseException {
      return this.currentNumericNode().getLongValue();
   }

   public int getIntValue() throws IOException, JsonParseException {
      return this.currentNumericNode().getIntValue();
   }

   public Number getNumberValue() throws IOException, JsonParseException {
      return this.currentNumericNode().getNumberValue();
   }

   public Object getEmbeddedObject() {
      if (!this._closed) {
         JsonNode n = this.currentNode();
         if (n != null && n.isPojo()) {
            return ((POJONode)n).getPojo();
         }
      }

      return null;
   }

   public byte[] getBinaryValue(Base64Variant b64variant) throws IOException, JsonParseException {
      JsonNode n = this.currentNode();
      if (n != null) {
         byte[] data = n.getBinaryValue();
         if (data != null) {
            return data;
         }

         if (n.isPojo()) {
            Object ob = ((POJONode)n).getPojo();
            if (ob instanceof byte[]) {
               return (byte[])((byte[])ob);
            }
         }
      }

      return null;
   }

   protected JsonNode currentNode() {
      return !this._closed && this._nodeCursor != null ? this._nodeCursor.currentNode() : null;
   }

   protected JsonNode currentNumericNode() throws JsonParseException {
      JsonNode n = this.currentNode();
      if (n != null && n.isNumber()) {
         return n;
      } else {
         JsonToken t = n == null ? null : n.asToken();
         throw this._constructError("Current token (" + t + ") not numeric, can not use numeric value accessors");
      }
   }

   protected void _handleEOF() throws JsonParseException {
      this._throwInternal();
   }
}

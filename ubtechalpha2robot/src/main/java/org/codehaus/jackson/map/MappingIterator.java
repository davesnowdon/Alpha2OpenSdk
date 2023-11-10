package org.codehaus.jackson.map;

import java.io.IOException;
import java.util.Iterator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonStreamContext;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.type.JavaType;

public class MappingIterator<T> implements Iterator<T> {
   protected static final MappingIterator<?> EMPTY_ITERATOR = new MappingIterator((JavaType)null, (JsonParser)null, (DeserializationContext)null, (JsonDeserializer)null);
   protected final JavaType _type;
   protected final DeserializationContext _context;
   protected final JsonDeserializer<T> _deserializer;
   protected final JsonParser _parser;

   protected MappingIterator(JavaType type, JsonParser jp, DeserializationContext ctxt, JsonDeserializer<?> deser) {
      this._type = type;
      this._parser = jp;
      this._context = ctxt;
      this._deserializer = deser;
      if (jp != null && jp.getCurrentToken() == JsonToken.START_ARRAY) {
         JsonStreamContext sc = jp.getParsingContext();
         if (!sc.inRoot()) {
            jp.clearCurrentToken();
         }
      }

   }

   protected static <T> MappingIterator<T> emptyIterator() {
      return EMPTY_ITERATOR;
   }

   public boolean hasNext() {
      try {
         return this.hasNextValue();
      } catch (JsonMappingException var2) {
         throw new RuntimeJsonMappingException(var2.getMessage(), var2);
      } catch (IOException var3) {
         throw new RuntimeException(var3.getMessage(), var3);
      }
   }

   public T next() {
      try {
         return this.nextValue();
      } catch (JsonMappingException var2) {
         throw new RuntimeJsonMappingException(var2.getMessage(), var2);
      } catch (IOException var3) {
         throw new RuntimeException(var3.getMessage(), var3);
      }
   }

   public void remove() {
      throw new UnsupportedOperationException();
   }

   public boolean hasNextValue() throws IOException {
      if (this._parser == null) {
         return false;
      } else {
         JsonToken t = this._parser.getCurrentToken();
         if (t == null) {
            t = this._parser.nextToken();
            if (t == null) {
               this._parser.close();
               return false;
            }

            if (t == JsonToken.END_ARRAY) {
               return false;
            }
         }

         return true;
      }
   }

   public T nextValue() throws IOException {
      T result = this._deserializer.deserialize(this._parser, this._context);
      this._parser.clearCurrentToken();
      return result;
   }
}

package org.codehaus.jackson.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

public class JsonParserSequence extends JsonParserDelegate {
   protected final JsonParser[] _parsers;
   protected int _nextParser;

   protected JsonParserSequence(JsonParser[] parsers) {
      super(parsers[0]);
      this._parsers = parsers;
      this._nextParser = 1;
   }

   public static JsonParserSequence createFlattened(JsonParser first, JsonParser second) {
      if (!(first instanceof JsonParserSequence) && !(second instanceof JsonParserSequence)) {
         return new JsonParserSequence(new JsonParser[]{first, second});
      } else {
         ArrayList<JsonParser> p = new ArrayList();
         if (first instanceof JsonParserSequence) {
            ((JsonParserSequence)first).addFlattenedActiveParsers(p);
         } else {
            p.add(first);
         }

         if (second instanceof JsonParserSequence) {
            ((JsonParserSequence)second).addFlattenedActiveParsers(p);
         } else {
            p.add(second);
         }

         return new JsonParserSequence((JsonParser[])p.toArray(new JsonParser[p.size()]));
      }
   }

   protected void addFlattenedActiveParsers(List<JsonParser> result) {
      int i = this._nextParser - 1;

      for(int len = this._parsers.length; i < len; ++i) {
         JsonParser p = this._parsers[i];
         if (p instanceof JsonParserSequence) {
            ((JsonParserSequence)p).addFlattenedActiveParsers(result);
         } else {
            result.add(p);
         }
      }

   }

   public void close() throws IOException {
      do {
         this.delegate.close();
      } while(this.switchToNext());

   }

   public JsonToken nextToken() throws IOException, JsonParseException {
      JsonToken t = this.delegate.nextToken();
      if (t != null) {
         return t;
      } else {
         do {
            if (!this.switchToNext()) {
               return null;
            }

            t = this.delegate.nextToken();
         } while(t == null);

         return t;
      }
   }

   public int containedParsersCount() {
      return this._parsers.length;
   }

   protected boolean switchToNext() {
      if (this._nextParser >= this._parsers.length) {
         return false;
      } else {
         this.delegate = this._parsers[this._nextParser++];
         return true;
      }
   }
}

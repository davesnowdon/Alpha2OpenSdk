package org.msgpack.util.json;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.msgpack.MessagePack;
import org.msgpack.type.Value;
import org.msgpack.type.ValueFactory;
import org.msgpack.unpacker.Converter;

public class JSONUnpacker extends Converter {
   protected Reader in;
   private JSONParser parser;

   public JSONUnpacker(InputStream in) {
      this(new MessagePack(), in);
   }

   public JSONUnpacker(MessagePack msgpack, InputStream in) {
      this(msgpack, (Reader)(new InputStreamReader(in)));
   }

   JSONUnpacker(MessagePack msgpack, Reader in) {
      super(msgpack, (Value)null);
      this.in = in;
      this.parser = new JSONParser();
   }

   protected Value nextValue() throws IOException {
      try {
         Object obj = this.parser.parse(this.in);
         return this.objectToValue(obj);
      } catch (ParseException var2) {
         throw new IOException(var2);
      } catch (IOException var3) {
         throw new IOException(var3);
      }
   }

   private Value objectToValue(Object obj) {
      if (obj instanceof String) {
         return ValueFactory.createRawValue((String)obj);
      } else if (obj instanceof Integer) {
         return ValueFactory.createIntegerValue((Integer)obj);
      } else if (obj instanceof Long) {
         return ValueFactory.createIntegerValue((Long)obj);
      } else if (obj instanceof Map) {
         return this.mapToValue((Map)obj);
      } else if (obj instanceof List) {
         return this.listToValue((List)obj);
      } else if (obj instanceof Boolean) {
         return ValueFactory.createBooleanValue((Boolean)obj);
      } else {
         return (Value)(obj instanceof Double ? ValueFactory.createFloatValue((Double)obj) : ValueFactory.createNilValue());
      }
   }

   private Value listToValue(List list) {
      Value[] array = new Value[list.size()];

      for(int i = 0; i < array.length; ++i) {
         array[i] = this.objectToValue(list.get(i));
      }

      return ValueFactory.createArrayValue(array, true);
   }

   private Value mapToValue(Map map) {
      Value[] kvs = new Value[map.size() * 2];
      Iterator<Entry> it = map.entrySet().iterator();

      for(int i = 0; i < kvs.length; i += 2) {
         Entry pair = (Entry)it.next();
         kvs[i] = this.objectToValue(pair.getKey());
         kvs[i + 1] = this.objectToValue(pair.getValue());
      }

      return ValueFactory.createMapValue(kvs, true);
   }

   public int getReadByteCount() {
      throw new UnsupportedOperationException("Not implemented yet");
   }

   public void resetReadByteCount() {
      throw new UnsupportedOperationException("Not implemented yet");
   }

   public void close() throws IOException {
      this.in.close();
      super.close();
   }
}

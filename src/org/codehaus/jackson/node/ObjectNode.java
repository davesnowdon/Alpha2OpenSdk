package org.codehaus.jackson.node;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Map.Entry;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;

public class ObjectNode extends ContainerNode {
   protected LinkedHashMap<String, JsonNode> _children = null;

   public ObjectNode(JsonNodeFactory nc) {
      super(nc);
   }

   public JsonToken asToken() {
      return JsonToken.START_OBJECT;
   }

   public boolean isObject() {
      return true;
   }

   public int size() {
      return this._children == null ? 0 : this._children.size();
   }

   public Iterator<JsonNode> getElements() {
      return (Iterator)(this._children == null ? ContainerNode.NoNodesIterator.instance() : this._children.values().iterator());
   }

   public JsonNode get(int index) {
      return null;
   }

   public JsonNode get(String fieldName) {
      return this._children != null ? (JsonNode)this._children.get(fieldName) : null;
   }

   public Iterator<String> getFieldNames() {
      return (Iterator)(this._children == null ? ContainerNode.NoStringsIterator.instance() : this._children.keySet().iterator());
   }

   public JsonNode path(int index) {
      return MissingNode.getInstance();
   }

   public JsonNode path(String fieldName) {
      if (this._children != null) {
         JsonNode n = (JsonNode)this._children.get(fieldName);
         if (n != null) {
            return n;
         }
      }

      return MissingNode.getInstance();
   }

   public Iterator<Entry<String, JsonNode>> getFields() {
      return (Iterator)(this._children == null ? ObjectNode.NoFieldsIterator.instance : this._children.entrySet().iterator());
   }

   public ObjectNode with(String propertyName) {
      if (this._children == null) {
         this._children = new LinkedHashMap();
      } else {
         JsonNode n = (JsonNode)this._children.get(propertyName);
         if (n != null) {
            if (n instanceof ObjectNode) {
               return (ObjectNode)n;
            }

            throw new UnsupportedOperationException("Property '" + propertyName + "' has value that is not of type ObjectNode (but " + n.getClass().getName() + ")");
         }
      }

      ObjectNode result = this.objectNode();
      this._children.put(propertyName, result);
      return result;
   }

   public JsonNode findValue(String fieldName) {
      if (this._children != null) {
         Iterator i$ = this._children.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, JsonNode> entry = (Entry)i$.next();
            if (fieldName.equals(entry.getKey())) {
               return (JsonNode)entry.getValue();
            }

            JsonNode value = ((JsonNode)entry.getValue()).findValue(fieldName);
            if (value != null) {
               return value;
            }
         }
      }

      return null;
   }

   public List<JsonNode> findValues(String fieldName, List<JsonNode> foundSoFar) {
      if (this._children != null) {
         Iterator i$ = this._children.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, JsonNode> entry = (Entry)i$.next();
            if (fieldName.equals(entry.getKey())) {
               if (foundSoFar == null) {
                  foundSoFar = new ArrayList();
               }

               ((List)foundSoFar).add(entry.getValue());
            } else {
               foundSoFar = ((JsonNode)entry.getValue()).findValues(fieldName, (List)foundSoFar);
            }
         }
      }

      return (List)foundSoFar;
   }

   public List<String> findValuesAsText(String fieldName, List<String> foundSoFar) {
      if (this._children != null) {
         Iterator i$ = this._children.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, JsonNode> entry = (Entry)i$.next();
            if (fieldName.equals(entry.getKey())) {
               if (foundSoFar == null) {
                  foundSoFar = new ArrayList();
               }

               ((List)foundSoFar).add(((JsonNode)entry.getValue()).getValueAsText());
            } else {
               foundSoFar = ((JsonNode)entry.getValue()).findValuesAsText(fieldName, (List)foundSoFar);
            }
         }
      }

      return (List)foundSoFar;
   }

   public ObjectNode findParent(String fieldName) {
      if (this._children != null) {
         Iterator i$ = this._children.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, JsonNode> entry = (Entry)i$.next();
            if (fieldName.equals(entry.getKey())) {
               return this;
            }

            JsonNode value = ((JsonNode)entry.getValue()).findParent(fieldName);
            if (value != null) {
               return (ObjectNode)value;
            }
         }
      }

      return null;
   }

   public List<JsonNode> findParents(String fieldName, List<JsonNode> foundSoFar) {
      if (this._children != null) {
         Iterator i$ = this._children.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, JsonNode> entry = (Entry)i$.next();
            if (fieldName.equals(entry.getKey())) {
               if (foundSoFar == null) {
                  foundSoFar = new ArrayList();
               }

               ((List)foundSoFar).add(this);
            } else {
               foundSoFar = ((JsonNode)entry.getValue()).findParents(fieldName, (List)foundSoFar);
            }
         }
      }

      return (List)foundSoFar;
   }

   public final void serialize(JsonGenerator jg, SerializerProvider provider) throws IOException, JsonProcessingException {
      jg.writeStartObject();
      if (this._children != null) {
         Iterator i$ = this._children.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, JsonNode> en = (Entry)i$.next();
            jg.writeFieldName((String)en.getKey());
            ((BaseJsonNode)en.getValue()).serialize(jg, provider);
         }
      }

      jg.writeEndObject();
   }

   public void serializeWithType(JsonGenerator jg, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonProcessingException {
      typeSer.writeTypePrefixForObject(this, jg);
      if (this._children != null) {
         Iterator i$ = this._children.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, JsonNode> en = (Entry)i$.next();
            jg.writeFieldName((String)en.getKey());
            ((BaseJsonNode)en.getValue()).serialize(jg, provider);
         }
      }

      typeSer.writeTypeSuffixForObject(this, jg);
   }

   public JsonNode put(String fieldName, JsonNode value) {
      if (value == null) {
         value = this.nullNode();
      }

      return this._put(fieldName, (JsonNode)value);
   }

   public JsonNode remove(String fieldName) {
      return this._children != null ? (JsonNode)this._children.remove(fieldName) : null;
   }

   public ObjectNode remove(Collection<String> fieldNames) {
      if (this._children != null) {
         Iterator i$ = fieldNames.iterator();

         while(i$.hasNext()) {
            String fieldName = (String)i$.next();
            this._children.remove(fieldName);
         }
      }

      return this;
   }

   public ObjectNode removeAll() {
      this._children = null;
      return this;
   }

   public JsonNode putAll(Map<String, JsonNode> properties) {
      Entry en;
      Object n;
      if (this._children == null) {
         this._children = new LinkedHashMap(properties);
      } else {
         for(Iterator i$ = properties.entrySet().iterator(); i$.hasNext(); this._children.put(en.getKey(), n)) {
            en = (Entry)i$.next();
            n = (JsonNode)en.getValue();
            if (n == null) {
               n = this.nullNode();
            }
         }
      }

      return this;
   }

   public JsonNode putAll(ObjectNode other) {
      int len = other.size();
      if (len > 0) {
         if (this._children == null) {
            this._children = new LinkedHashMap(len);
         }

         other.putContentsTo(this._children);
      }

      return this;
   }

   public ObjectNode retain(Collection<String> fieldNames) {
      if (this._children != null) {
         Iterator entries = this._children.entrySet().iterator();

         while(entries.hasNext()) {
            Entry<String, JsonNode> entry = (Entry)entries.next();
            if (!fieldNames.contains(entry.getKey())) {
               entries.remove();
            }
         }
      }

      return this;
   }

   public ObjectNode retain(String... fieldNames) {
      return this.retain((Collection)Arrays.asList(fieldNames));
   }

   public ArrayNode putArray(String fieldName) {
      ArrayNode n = this.arrayNode();
      this._put(fieldName, n);
      return n;
   }

   public ObjectNode putObject(String fieldName) {
      ObjectNode n = this.objectNode();
      this._put(fieldName, n);
      return n;
   }

   public void putPOJO(String fieldName, Object pojo) {
      this._put(fieldName, this.POJONode(pojo));
   }

   public void putNull(String fieldName) {
      this._put(fieldName, this.nullNode());
   }

   public void put(String fieldName, int v) {
      this._put(fieldName, this.numberNode(v));
   }

   public void put(String fieldName, long v) {
      this._put(fieldName, this.numberNode(v));
   }

   public void put(String fieldName, float v) {
      this._put(fieldName, this.numberNode(v));
   }

   public void put(String fieldName, double v) {
      this._put(fieldName, this.numberNode(v));
   }

   public void put(String fieldName, BigDecimal v) {
      if (v == null) {
         this.putNull(fieldName);
      } else {
         this._put(fieldName, this.numberNode(v));
      }

   }

   public void put(String fieldName, String v) {
      if (v == null) {
         this.putNull(fieldName);
      } else {
         this._put(fieldName, this.textNode(v));
      }

   }

   public void put(String fieldName, boolean v) {
      this._put(fieldName, this.booleanNode(v));
   }

   public void put(String fieldName, byte[] v) {
      if (v == null) {
         this.putNull(fieldName);
      } else {
         this._put(fieldName, this.binaryNode(v));
      }

   }

   protected void putContentsTo(Map<String, JsonNode> dst) {
      if (this._children != null) {
         Iterator i$ = this._children.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, JsonNode> en = (Entry)i$.next();
            dst.put(en.getKey(), en.getValue());
         }
      }

   }

   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (o == null) {
         return false;
      } else if (o.getClass() != this.getClass()) {
         return false;
      } else {
         ObjectNode other = (ObjectNode)o;
         if (other.size() != this.size()) {
            return false;
         } else if (this._children != null) {
            Iterator i$ = this._children.entrySet().iterator();

            JsonNode value;
            JsonNode otherValue;
            do {
               if (!i$.hasNext()) {
                  return true;
               }

               Entry<String, JsonNode> en = (Entry)i$.next();
               String key = (String)en.getKey();
               value = (JsonNode)en.getValue();
               otherValue = other.get(key);
            } while(otherValue != null && otherValue.equals(value));

            return false;
         } else {
            return true;
         }
      }
   }

   public int hashCode() {
      return this._children == null ? -1 : this._children.hashCode();
   }

   public String toString() {
      StringBuilder sb = new StringBuilder(32 + (this.size() << 4));
      sb.append("{");
      if (this._children != null) {
         int count = 0;
         Iterator i$ = this._children.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<String, JsonNode> en = (Entry)i$.next();
            if (count > 0) {
               sb.append(",");
            }

            ++count;
            TextNode.appendQuoted(sb, (String)en.getKey());
            sb.append(':');
            sb.append(((JsonNode)en.getValue()).toString());
         }
      }

      sb.append("}");
      return sb.toString();
   }

   private final JsonNode _put(String fieldName, JsonNode value) {
      if (this._children == null) {
         this._children = new LinkedHashMap();
      }

      return (JsonNode)this._children.put(fieldName, value);
   }

   protected static class NoFieldsIterator implements Iterator<Entry<String, JsonNode>> {
      static final ObjectNode.NoFieldsIterator instance = new ObjectNode.NoFieldsIterator();

      private NoFieldsIterator() {
      }

      public boolean hasNext() {
         return false;
      }

      public Entry<String, JsonNode> next() {
         throw new NoSuchElementException();
      }

      public void remove() {
         throw new IllegalStateException();
      }
   }
}

package org.codehaus.jackson.node;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.TypeSerializer;

public final class ArrayNode extends ContainerNode {
   protected ArrayList<JsonNode> _children;

   public ArrayNode(JsonNodeFactory nc) {
      super(nc);
   }

   public JsonToken asToken() {
      return JsonToken.START_ARRAY;
   }

   public boolean isArray() {
      return true;
   }

   public int size() {
      return this._children == null ? 0 : this._children.size();
   }

   public Iterator<JsonNode> getElements() {
      return (Iterator)(this._children == null ? ContainerNode.NoNodesIterator.instance() : this._children.iterator());
   }

   public JsonNode get(int index) {
      return index >= 0 && this._children != null && index < this._children.size() ? (JsonNode)this._children.get(index) : null;
   }

   public JsonNode get(String fieldName) {
      return null;
   }

   public JsonNode path(String fieldName) {
      return MissingNode.getInstance();
   }

   public JsonNode path(int index) {
      return (JsonNode)(index >= 0 && this._children != null && index < this._children.size() ? (JsonNode)this._children.get(index) : MissingNode.getInstance());
   }

   public final void serialize(JsonGenerator jg, SerializerProvider provider) throws IOException, JsonProcessingException {
      jg.writeStartArray();
      if (this._children != null) {
         Iterator i$ = this._children.iterator();

         while(i$.hasNext()) {
            JsonNode n = (JsonNode)i$.next();
            ((BaseJsonNode)n).writeTo(jg);
         }
      }

      jg.writeEndArray();
   }

   public void serializeWithType(JsonGenerator jg, SerializerProvider provider, TypeSerializer typeSer) throws IOException, JsonProcessingException {
      typeSer.writeTypePrefixForArray(this, jg);
      if (this._children != null) {
         Iterator i$ = this._children.iterator();

         while(i$.hasNext()) {
            JsonNode n = (JsonNode)i$.next();
            ((BaseJsonNode)n).writeTo(jg);
         }
      }

      typeSer.writeTypeSuffixForArray(this, jg);
   }

   public JsonNode findValue(String fieldName) {
      if (this._children != null) {
         Iterator i$ = this._children.iterator();

         while(i$.hasNext()) {
            JsonNode node = (JsonNode)i$.next();
            JsonNode value = node.findValue(fieldName);
            if (value != null) {
               return value;
            }
         }
      }

      return null;
   }

   public List<JsonNode> findValues(String fieldName, List<JsonNode> foundSoFar) {
      JsonNode node;
      if (this._children != null) {
         for(Iterator i$ = this._children.iterator(); i$.hasNext(); foundSoFar = node.findValues(fieldName, foundSoFar)) {
            node = (JsonNode)i$.next();
         }
      }

      return foundSoFar;
   }

   public List<String> findValuesAsText(String fieldName, List<String> foundSoFar) {
      JsonNode node;
      if (this._children != null) {
         for(Iterator i$ = this._children.iterator(); i$.hasNext(); foundSoFar = node.findValuesAsText(fieldName, foundSoFar)) {
            node = (JsonNode)i$.next();
         }
      }

      return foundSoFar;
   }

   public ObjectNode findParent(String fieldName) {
      if (this._children != null) {
         Iterator i$ = this._children.iterator();

         while(i$.hasNext()) {
            JsonNode node = (JsonNode)i$.next();
            JsonNode parent = node.findParent(fieldName);
            if (parent != null) {
               return (ObjectNode)parent;
            }
         }
      }

      return null;
   }

   public List<JsonNode> findParents(String fieldName, List<JsonNode> foundSoFar) {
      JsonNode node;
      if (this._children != null) {
         for(Iterator i$ = this._children.iterator(); i$.hasNext(); foundSoFar = node.findParents(fieldName, foundSoFar)) {
            node = (JsonNode)i$.next();
         }
      }

      return foundSoFar;
   }

   public JsonNode set(int index, JsonNode value) {
      if (value == null) {
         value = this.nullNode();
      }

      return this._set(index, (JsonNode)value);
   }

   public void add(JsonNode value) {
      if (value == null) {
         value = this.nullNode();
      }

      this._add((JsonNode)value);
   }

   public JsonNode addAll(ArrayNode other) {
      int len = other.size();
      if (len > 0) {
         if (this._children == null) {
            this._children = new ArrayList(len + 2);
         }

         other.addContentsTo(this._children);
      }

      return this;
   }

   public JsonNode addAll(Collection<JsonNode> nodes) {
      int len = nodes.size();
      if (len > 0) {
         if (this._children == null) {
            this._children = new ArrayList(nodes);
         } else {
            this._children.addAll(nodes);
         }
      }

      return this;
   }

   public void insert(int index, JsonNode value) {
      if (value == null) {
         value = this.nullNode();
      }

      this._insert(index, (JsonNode)value);
   }

   public JsonNode remove(int index) {
      return index >= 0 && this._children != null && index < this._children.size() ? (JsonNode)this._children.remove(index) : null;
   }

   public ArrayNode removeAll() {
      this._children = null;
      return this;
   }

   public ArrayNode addArray() {
      ArrayNode n = this.arrayNode();
      this._add(n);
      return n;
   }

   public ObjectNode addObject() {
      ObjectNode n = this.objectNode();
      this._add(n);
      return n;
   }

   public void addPOJO(Object value) {
      if (value == null) {
         this.addNull();
      } else {
         this._add(this.POJONode(value));
      }

   }

   public void addNull() {
      this._add(this.nullNode());
   }

   public void add(int v) {
      this._add(this.numberNode(v));
   }

   public void add(long v) {
      this._add(this.numberNode(v));
   }

   public void add(float v) {
      this._add(this.numberNode(v));
   }

   public void add(double v) {
      this._add(this.numberNode(v));
   }

   public void add(BigDecimal v) {
      if (v == null) {
         this.addNull();
      } else {
         this._add(this.numberNode(v));
      }

   }

   public void add(String v) {
      if (v == null) {
         this.addNull();
      } else {
         this._add(this.textNode(v));
      }

   }

   public void add(boolean v) {
      this._add(this.booleanNode(v));
   }

   public void add(byte[] v) {
      if (v == null) {
         this.addNull();
      } else {
         this._add(this.binaryNode(v));
      }

   }

   public ArrayNode insertArray(int index) {
      ArrayNode n = this.arrayNode();
      this._insert(index, n);
      return n;
   }

   public ObjectNode insertObject(int index) {
      ObjectNode n = this.objectNode();
      this._insert(index, n);
      return n;
   }

   public void insertPOJO(int index, Object value) {
      if (value == null) {
         this.insertNull(index);
      } else {
         this._insert(index, this.POJONode(value));
      }

   }

   public void insertNull(int index) {
      this._insert(index, this.nullNode());
   }

   public void insert(int index, int v) {
      this._insert(index, this.numberNode(v));
   }

   public void insert(int index, long v) {
      this._insert(index, this.numberNode(v));
   }

   public void insert(int index, float v) {
      this._insert(index, this.numberNode(v));
   }

   public void insert(int index, double v) {
      this._insert(index, this.numberNode(v));
   }

   public void insert(int index, BigDecimal v) {
      if (v == null) {
         this.insertNull(index);
      } else {
         this._insert(index, this.numberNode(v));
      }

   }

   public void insert(int index, String v) {
      if (v == null) {
         this.insertNull(index);
      } else {
         this._insert(index, this.textNode(v));
      }

   }

   public void insert(int index, boolean v) {
      this._insert(index, this.booleanNode(v));
   }

   public void insert(int index, byte[] v) {
      if (v == null) {
         this.insertNull(index);
      } else {
         this._insert(index, this.binaryNode(v));
      }

   }

   protected void addContentsTo(List<JsonNode> dst) {
      if (this._children != null) {
         Iterator i$ = this._children.iterator();

         while(i$.hasNext()) {
            JsonNode n = (JsonNode)i$.next();
            dst.add(n);
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
         ArrayNode other = (ArrayNode)o;
         if (this._children != null && this._children.size() != 0) {
            return other._sameChildren(this._children);
         } else {
            return other.size() == 0;
         }
      }
   }

   public int hashCode() {
      int hash;
      if (this._children == null) {
         hash = 1;
      } else {
         hash = this._children.size();
         Iterator i$ = this._children.iterator();

         while(i$.hasNext()) {
            JsonNode n = (JsonNode)i$.next();
            if (n != null) {
               hash ^= n.hashCode();
            }
         }
      }

      return hash;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder(16 + (this.size() << 4));
      sb.append('[');
      if (this._children != null) {
         int i = 0;

         for(int len = this._children.size(); i < len; ++i) {
            if (i > 0) {
               sb.append(',');
            }

            sb.append(((JsonNode)this._children.get(i)).toString());
         }
      }

      sb.append(']');
      return sb.toString();
   }

   public JsonNode _set(int index, JsonNode value) {
      if (this._children != null && index >= 0 && index < this._children.size()) {
         return (JsonNode)this._children.set(index, value);
      } else {
         throw new IndexOutOfBoundsException("Illegal index " + index + ", array size " + this.size());
      }
   }

   private void _add(JsonNode node) {
      if (this._children == null) {
         this._children = new ArrayList();
      }

      this._children.add(node);
   }

   private void _insert(int index, JsonNode node) {
      if (this._children == null) {
         this._children = new ArrayList();
         this._children.add(node);
      } else {
         if (index < 0) {
            this._children.add(0, node);
         } else if (index >= this._children.size()) {
            this._children.add(node);
         } else {
            this._children.add(index, node);
         }

      }
   }

   private boolean _sameChildren(ArrayList<JsonNode> otherChildren) {
      int len = otherChildren.size();
      if (this.size() != len) {
         return false;
      } else {
         for(int i = 0; i < len; ++i) {
            if (!((JsonNode)this._children.get(i)).equals(otherChildren.get(i))) {
               return false;
            }
         }

         return true;
      }
   }
}

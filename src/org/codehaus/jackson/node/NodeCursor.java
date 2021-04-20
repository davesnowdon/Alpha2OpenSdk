package org.codehaus.jackson.node;

import java.util.Iterator;
import java.util.Map.Entry;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonStreamContext;
import org.codehaus.jackson.JsonToken;

abstract class NodeCursor extends JsonStreamContext {
   final NodeCursor _parent;

   public NodeCursor(int contextType, NodeCursor p) {
      this._type = contextType;
      this._index = -1;
      this._parent = p;
   }

   public final NodeCursor getParent() {
      return this._parent;
   }

   public abstract String getCurrentName();

   public abstract JsonToken nextToken();

   public abstract JsonToken nextValue();

   public abstract JsonToken endToken();

   public abstract JsonNode currentNode();

   public abstract boolean currentHasChildren();

   public final NodeCursor iterateChildren() {
      JsonNode n = this.currentNode();
      if (n == null) {
         throw new IllegalStateException("No current node");
      } else if (n.isArray()) {
         return new NodeCursor.Array(n, this);
      } else if (n.isObject()) {
         return new NodeCursor.Object(n, this);
      } else {
         throw new IllegalStateException("Current node of type " + n.getClass().getName());
      }
   }

   protected static final class Object extends NodeCursor {
      Iterator<Entry<String, JsonNode>> _contents;
      Entry<String, JsonNode> _current;
      boolean _needEntry;

      public Object(JsonNode n, NodeCursor p) {
         super(2, p);
         this._contents = ((ObjectNode)n).getFields();
         this._needEntry = true;
      }

      public String getCurrentName() {
         return this._current == null ? null : (String)this._current.getKey();
      }

      public JsonToken nextToken() {
         if (this._needEntry) {
            if (!this._contents.hasNext()) {
               this._current = null;
               return null;
            } else {
               this._needEntry = false;
               this._current = (Entry)this._contents.next();
               return JsonToken.FIELD_NAME;
            }
         } else {
            this._needEntry = true;
            return ((JsonNode)this._current.getValue()).asToken();
         }
      }

      public JsonToken nextValue() {
         JsonToken t = this.nextToken();
         if (t == JsonToken.FIELD_NAME) {
            t = this.nextToken();
         }

         return t;
      }

      public JsonToken endToken() {
         return JsonToken.END_OBJECT;
      }

      public JsonNode currentNode() {
         return this._current == null ? null : (JsonNode)this._current.getValue();
      }

      public boolean currentHasChildren() {
         return ((ContainerNode)this.currentNode()).size() > 0;
      }
   }

   protected static final class Array extends NodeCursor {
      Iterator<JsonNode> _contents;
      JsonNode _currentNode;

      public Array(JsonNode n, NodeCursor p) {
         super(1, p);
         this._contents = n.getElements();
      }

      public String getCurrentName() {
         return null;
      }

      public JsonToken nextToken() {
         if (!this._contents.hasNext()) {
            this._currentNode = null;
            return null;
         } else {
            this._currentNode = (JsonNode)this._contents.next();
            return this._currentNode.asToken();
         }
      }

      public JsonToken nextValue() {
         return this.nextToken();
      }

      public JsonToken endToken() {
         return JsonToken.END_ARRAY;
      }

      public JsonNode currentNode() {
         return this._currentNode;
      }

      public boolean currentHasChildren() {
         return ((ContainerNode)this.currentNode()).size() > 0;
      }
   }

   protected static final class RootValue extends NodeCursor {
      JsonNode _node;
      protected boolean _done = false;

      public RootValue(JsonNode n, NodeCursor p) {
         super(0, p);
         this._node = n;
      }

      public String getCurrentName() {
         return null;
      }

      public JsonToken nextToken() {
         if (!this._done) {
            this._done = true;
            return this._node.asToken();
         } else {
            this._node = null;
            return null;
         }
      }

      public JsonToken nextValue() {
         return this.nextToken();
      }

      public JsonToken endToken() {
         return null;
      }

      public JsonNode currentNode() {
         return this._node;
      }

      public boolean currentHasChildren() {
         return false;
      }
   }
}

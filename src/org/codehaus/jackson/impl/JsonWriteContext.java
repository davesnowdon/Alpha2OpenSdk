package org.codehaus.jackson.impl;

import org.codehaus.jackson.JsonStreamContext;

public class JsonWriteContext extends JsonStreamContext {
   public static final int STATUS_OK_AS_IS = 0;
   public static final int STATUS_OK_AFTER_COMMA = 1;
   public static final int STATUS_OK_AFTER_COLON = 2;
   public static final int STATUS_OK_AFTER_SPACE = 3;
   public static final int STATUS_EXPECT_VALUE = 4;
   public static final int STATUS_EXPECT_NAME = 5;
   protected final JsonWriteContext _parent;
   protected String _currentName;
   protected JsonWriteContext _child = null;

   protected JsonWriteContext(int type, JsonWriteContext parent) {
      this._type = type;
      this._parent = parent;
      this._index = -1;
   }

   public static JsonWriteContext createRootContext() {
      return new JsonWriteContext(0, (JsonWriteContext)null);
   }

   private final JsonWriteContext reset(int type) {
      this._type = type;
      this._index = -1;
      this._currentName = null;
      return this;
   }

   public final JsonWriteContext createChildArrayContext() {
      JsonWriteContext ctxt = this._child;
      if (ctxt == null) {
         this._child = ctxt = new JsonWriteContext(1, this);
         return ctxt;
      } else {
         return ctxt.reset(1);
      }
   }

   public final JsonWriteContext createChildObjectContext() {
      JsonWriteContext ctxt = this._child;
      if (ctxt == null) {
         this._child = ctxt = new JsonWriteContext(2, this);
         return ctxt;
      } else {
         return ctxt.reset(2);
      }
   }

   public final JsonWriteContext getParent() {
      return this._parent;
   }

   public final String getCurrentName() {
      return this._currentName;
   }

   public final int writeFieldName(String name) {
      if (this._type == 2) {
         if (this._currentName != null) {
            return 4;
         } else {
            this._currentName = name;
            return this._index < 0 ? 0 : 1;
         }
      } else {
         return 4;
      }
   }

   public final int writeValue() {
      if (this._type == 2) {
         if (this._currentName == null) {
            return 5;
         } else {
            this._currentName = null;
            ++this._index;
            return 2;
         }
      } else if (this._type == 1) {
         int ix = this._index++;
         return ix < 0 ? 0 : 1;
      } else {
         ++this._index;
         return this._index == 0 ? 0 : 3;
      }
   }

   protected final void appendDesc(StringBuilder sb) {
      if (this._type == 2) {
         sb.append('{');
         if (this._currentName != null) {
            sb.append('"');
            sb.append(this._currentName);
            sb.append('"');
         } else {
            sb.append('?');
         }

         sb.append(']');
      } else if (this._type == 1) {
         sb.append('[');
         sb.append(this.getCurrentIndex());
         sb.append(']');
      } else {
         sb.append("/");
      }

   }

   public final String toString() {
      StringBuilder sb = new StringBuilder(64);
      this.appendDesc(sb);
      return sb.toString();
   }
}

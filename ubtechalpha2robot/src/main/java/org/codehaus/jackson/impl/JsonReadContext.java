package org.codehaus.jackson.impl;

import org.codehaus.jackson.JsonLocation;
import org.codehaus.jackson.JsonStreamContext;
import org.codehaus.jackson.util.CharTypes;

public final class JsonReadContext extends JsonStreamContext {
   protected final JsonReadContext _parent;
   protected int _lineNr;
   protected int _columnNr;
   protected String _currentName;
   protected JsonReadContext _child = null;

   public JsonReadContext(JsonReadContext parent, int type, int lineNr, int colNr) {
      this._type = type;
      this._parent = parent;
      this._lineNr = lineNr;
      this._columnNr = colNr;
      this._index = -1;
   }

   protected final void reset(int type, int lineNr, int colNr) {
      this._type = type;
      this._index = -1;
      this._lineNr = lineNr;
      this._columnNr = colNr;
      this._currentName = null;
   }

   public static JsonReadContext createRootContext(int lineNr, int colNr) {
      return new JsonReadContext((JsonReadContext)null, 0, lineNr, colNr);
   }

   public final JsonReadContext createChildArrayContext(int lineNr, int colNr) {
      JsonReadContext ctxt = this._child;
      if (ctxt == null) {
         this._child = ctxt = new JsonReadContext(this, 1, lineNr, colNr);
         return ctxt;
      } else {
         ctxt.reset(1, lineNr, colNr);
         return ctxt;
      }
   }

   public final JsonReadContext createChildObjectContext(int lineNr, int colNr) {
      JsonReadContext ctxt = this._child;
      if (ctxt == null) {
         this._child = ctxt = new JsonReadContext(this, 2, lineNr, colNr);
         return ctxt;
      } else {
         ctxt.reset(2, lineNr, colNr);
         return ctxt;
      }
   }

   public final String getCurrentName() {
      return this._currentName;
   }

   public final JsonReadContext getParent() {
      return this._parent;
   }

   public final JsonLocation getStartLocation(Object srcRef) {
      long totalChars = -1L;
      return new JsonLocation(srcRef, totalChars, this._lineNr, this._columnNr);
   }

   public final boolean expectComma() {
      int ix = ++this._index;
      return this._type != 0 && ix > 0;
   }

   public void setCurrentName(String name) {
      this._currentName = name;
   }

   public final String toString() {
      StringBuilder sb = new StringBuilder(64);
      switch(this._type) {
      case 0:
         sb.append("/");
         break;
      case 1:
         sb.append('[');
         sb.append(this.getCurrentIndex());
         sb.append(']');
         break;
      case 2:
         sb.append('{');
         if (this._currentName != null) {
            sb.append('"');
            CharTypes.appendQuoted(sb, this._currentName);
            sb.append('"');
         } else {
            sb.append('?');
         }

         sb.append(']');
      }

      return sb.toString();
   }
}

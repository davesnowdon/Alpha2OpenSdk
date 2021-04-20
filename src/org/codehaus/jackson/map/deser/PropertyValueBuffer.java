package org.codehaus.jackson.map.deser;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;

public final class PropertyValueBuffer {
   final JsonParser _parser;
   final DeserializationContext _context;
   final Object[] _creatorParameters;
   private int _paramsNeeded;
   private PropertyValue _buffered;

   public PropertyValueBuffer(JsonParser jp, DeserializationContext ctxt, int paramCount) {
      this._parser = jp;
      this._context = ctxt;
      this._paramsNeeded = paramCount;
      this._creatorParameters = new Object[paramCount];
   }

   protected final Object[] getParameters(Object[] defaults) {
      if (defaults != null) {
         int i = 0;

         for(int len = this._creatorParameters.length; i < len; ++i) {
            if (this._creatorParameters[i] == null) {
               Object value = defaults[i];
               if (value != null) {
                  this._creatorParameters[i] = value;
               }
            }
         }
      }

      return this._creatorParameters;
   }

   protected PropertyValue buffered() {
      return this._buffered;
   }

   public boolean assignParameter(int index, Object value) {
      this._creatorParameters[index] = value;
      return --this._paramsNeeded <= 0;
   }

   public void bufferProperty(SettableBeanProperty prop, Object value) {
      this._buffered = new PropertyValue.Regular(this._buffered, value, prop);
   }

   public void bufferAnyProperty(SettableAnyProperty prop, String propName, Object value) {
      this._buffered = new PropertyValue.Any(this._buffered, value, prop, propName);
   }

   public void bufferMapProperty(Object key, Object value) {
      this._buffered = new PropertyValue.Map(this._buffered, value, key);
   }
}

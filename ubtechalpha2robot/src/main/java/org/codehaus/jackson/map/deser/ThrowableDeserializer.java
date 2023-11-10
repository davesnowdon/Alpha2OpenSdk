package org.codehaus.jackson.map.deser;

import java.io.IOException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonMappingException;

public class ThrowableDeserializer extends BeanDeserializer {
   protected static final String PROP_NAME_MESSAGE = "message";

   public ThrowableDeserializer(BeanDeserializer baseDeserializer) {
      super(baseDeserializer);
   }

   public Object deserializeFromObject(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
      if (this._propertyBasedCreator != null) {
         return this._deserializeUsingPropertyBased(jp, ctxt);
      } else if (this._delegatingCreator != null) {
         return this._delegatingCreator.deserialize(jp, ctxt);
      } else if (this._beanType.isAbstract()) {
         throw JsonMappingException.from(jp, "Can not instantiate abstract type " + this._beanType + " (need to add/enable type information?)");
      } else if (this._stringCreator == null) {
         throw new JsonMappingException("Can not deserialize Throwable of type " + this._beanType + " without having either single-String-arg constructor; or explicit @JsonCreator");
      } else {
         Object throwable = null;
         Object[] pending = null;

         int pendingIx;
         for(pendingIx = 0; jp.getCurrentToken() != JsonToken.END_OBJECT; jp.nextToken()) {
            String propName = jp.getCurrentName();
            SettableBeanProperty prop = this._beanProperties.find(propName);
            jp.nextToken();
            int i;
            if (prop != null) {
               if (throwable != null) {
                  prop.deserializeAndSet(jp, ctxt, throwable);
               } else {
                  if (pending == null) {
                     i = this._beanProperties.size();
                     pending = new Object[i + i];
                  }

                  pending[pendingIx++] = prop;
                  pending[pendingIx++] = prop.deserialize(jp, ctxt);
               }
            } else if (!"message".equals(propName)) {
               if (this._ignorableProps != null && this._ignorableProps.contains(propName)) {
                  jp.skipChildren();
               } else if (this._anySetter != null) {
                  this._anySetter.deserializeAndSet(jp, ctxt, throwable, propName);
               } else {
                  this.handleUnknownProperty(jp, ctxt, throwable, propName);
               }
            } else {
               throwable = this._stringCreator.construct(jp.getText());
               if (pending != null) {
                  i = 0;

                  for(int len = pendingIx; i < len; i += 2) {
                     prop = (SettableBeanProperty)pending[i];
                     prop.set(throwable, pending[i + 1]);
                  }

                  pending = null;
               }
            }
         }

         if (throwable == null) {
            throwable = this._stringCreator.construct((String)null);
            if (pending != null) {
               int i = 0;

               for(int len = pendingIx; i < len; i += 2) {
                  SettableBeanProperty prop = (SettableBeanProperty)pending[i];
                  prop.set(throwable, pending[i + 1]);
               }
            }
         }

         return throwable;
      }
   }
}

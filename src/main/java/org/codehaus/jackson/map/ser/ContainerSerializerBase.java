package org.codehaus.jackson.map.ser;

import org.codehaus.jackson.map.TypeSerializer;

public abstract class ContainerSerializerBase<T> extends SerializerBase<T> {
   protected ContainerSerializerBase(Class<T> t) {
      super(t);
   }

   protected ContainerSerializerBase(Class<?> t, boolean dummy) {
      super(t, dummy);
   }

   public ContainerSerializerBase<?> withValueTypeSerializer(TypeSerializer vts) {
      return vts == null ? this : this._withValueTypeSerializer(vts);
   }

   public abstract ContainerSerializerBase<?> _withValueTypeSerializer(TypeSerializer var1);
}

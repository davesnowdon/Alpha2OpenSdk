package org.msgpack.type;

import java.util.Map;

public interface MapValue extends Value, Map<Value, Value> {
   Value[] getKeyValueArray();
}

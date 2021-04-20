package org.msgpack.type;

import java.util.List;

public interface ArrayValue extends Value, List<Value> {
   Value[] getElementArray();
}

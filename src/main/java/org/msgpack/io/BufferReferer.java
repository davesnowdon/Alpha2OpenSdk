package org.msgpack.io;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface BufferReferer {
   void refer(ByteBuffer var1, boolean var2) throws IOException;
}

package org.msgpack;

import java.io.IOException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public interface MessagePackable {
   void writeTo(Packer var1) throws IOException;

   void readFrom(Unpacker var1) throws IOException;
}

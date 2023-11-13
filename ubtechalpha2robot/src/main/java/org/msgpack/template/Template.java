package org.msgpack.template;

import java.io.IOException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public interface Template {
   void write(Packer var1, T var2) throws IOException;

   void write(Packer var1, T var2, boolean var3) throws IOException;

   T read(Unpacker var1, T var2) throws IOException;

   T read(Unpacker var1, T var2, boolean var3) throws IOException;
}

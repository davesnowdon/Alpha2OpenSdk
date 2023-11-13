package org.msgpack.template;

import java.io.IOException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public abstract class AbstractTemplate implements Template {
   public AbstractTemplate() {
   }

   public void write(Packer pk, T v) throws IOException {
      this.write(pk, v, false);
   }

   public T read(Unpacker u, T to) throws IOException {
      return this.read(u, to, false);
   }
}

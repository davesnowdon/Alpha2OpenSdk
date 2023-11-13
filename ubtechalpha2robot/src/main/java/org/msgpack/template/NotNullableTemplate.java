package org.msgpack.template;

import java.io.IOException;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

public class NotNullableTemplate extends AbstractTemplate {
   private Template tmpl;

   public NotNullableTemplate(Template elementTemplate) {
      this.tmpl = elementTemplate;
   }

   public void write(Packer pk, T v, boolean required) throws IOException {
      this.tmpl.write(pk, v, required);
   }

   public void write(Packer pk, T v) throws IOException {
      this.write(pk, v, true);
   }

   public T read(Unpacker u, T to, boolean required) throws IOException {
      return this.tmpl.read(u, to, required);
   }

   public T read(Unpacker u, T to) throws IOException {
      return this.read(u, to, true);
   }
}

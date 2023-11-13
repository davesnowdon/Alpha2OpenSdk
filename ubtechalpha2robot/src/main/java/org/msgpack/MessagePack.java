package org.msgpack;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import org.msgpack.packer.BufferPacker;
import org.msgpack.packer.MessagePackBufferPacker;
import org.msgpack.packer.MessagePackPacker;
import org.msgpack.packer.Packer;
import org.msgpack.packer.Unconverter;
import org.msgpack.template.Template;
import org.msgpack.template.TemplateRegistry;
import org.msgpack.type.Value;
import org.msgpack.unpacker.BufferUnpacker;
import org.msgpack.unpacker.Converter;
import org.msgpack.unpacker.MessagePackBufferUnpacker;
import org.msgpack.unpacker.MessagePackUnpacker;
import org.msgpack.unpacker.Unpacker;

public class MessagePack {
   private TemplateRegistry registry;
   private static final MessagePack globalMessagePack = new MessagePack();

   public MessagePack() {
      this.registry = new TemplateRegistry((TemplateRegistry)null);
   }

   public MessagePack(MessagePack msgpack) {
      this.registry = new TemplateRegistry(msgpack.registry);
   }

   protected MessagePack(TemplateRegistry registry) {
      this.registry = registry;
   }

   public void setClassLoader(ClassLoader cl) {
      this.registry.setClassLoader(cl);
   }

   public Packer createPacker(OutputStream out) {
      return new MessagePackPacker(this, out);
   }

   public BufferPacker createBufferPacker() {
      return new MessagePackBufferPacker(this);
   }

   public BufferPacker createBufferPacker(int bufferSize) {
      return new MessagePackBufferPacker(this, bufferSize);
   }

   public Unpacker createUnpacker(InputStream in) {
      return new MessagePackUnpacker(this, in);
   }

   public BufferUnpacker createBufferUnpacker() {
      return new MessagePackBufferUnpacker(this);
   }

   public BufferUnpacker createBufferUnpacker(byte[] bytes) {
      return this.createBufferUnpacker().wrap(bytes);
   }

   public BufferUnpacker createBufferUnpacker(byte[] bytes, int off, int len) {
      return this.createBufferUnpacker().wrap(bytes, off, len);
   }

   public BufferUnpacker createBufferUnpacker(ByteBuffer buffer) {
      return this.createBufferUnpacker().wrap(buffer);
   }

   public  byte[] write(Object v) throws IOException {
      BufferPacker pk = this.createBufferPacker();
      if (v == null) {
         pk.writeNil();
      } else {
         Template tmpl = this.registry.lookup(v.getClass());
         tmpl.write(pk, v);
      }

      return pk.toByteArray();
   }

   public  byte[] write(Object v, Template template) throws IOException {
      BufferPacker pk = this.createBufferPacker();
      template.write(pk, v);
      return pk.toByteArray();
   }

   public  void write(OutputStream out, Object v) throws IOException {
      Packer pk = this.createPacker(out);
      if (v == null) {
         pk.writeNil();
      } else {
         Template tmpl = this.registry.lookup(v.getClass());
         tmpl.write(pk, v);
      }

   }

   public  void write(OutputStream out, Object v, Template template) throws IOException {
      Packer pk = this.createPacker(out);
      template.write(pk, v);
   }

   public byte[] write(Value v) throws IOException {
      BufferPacker pk = this.createBufferPacker();
      pk.write(v);
      return pk.toByteArray();
   }

   public Value read(byte[] bytes) throws IOException {
      return this.read(bytes, 0, bytes.length);
   }

   public Value read(byte[] bytes, int off, int len) throws IOException {
      return this.createBufferUnpacker(bytes, off, len).readValue();
   }

   public Value read(ByteBuffer buffer) throws IOException {
      return this.createBufferUnpacker(buffer).readValue();
   }

   public Value read(InputStream in) throws IOException {
      return this.createUnpacker(in).readValue();
   }

   public Object read(byte[] bytes, Object v) throws IOException {
      Template tmpl = this.registry.lookup(v.getClass());
      return this.read(bytes, v, tmpl);
   }

   public Object read(byte[] bytes, Template tmpl) throws IOException {
      return this.read((byte[])bytes, (Object)null, tmpl);
   }

   public Object read(byte[] bytes, Class c) throws IOException {
      Template tmpl = this.registry.lookup(c);
      return this.read((byte[])bytes, (Object)null, tmpl);
   }

   public Object read(byte[] bytes, Object v, Template tmpl) throws IOException {
      BufferUnpacker u = this.createBufferUnpacker(bytes);
      return tmpl.read(u, v);
   }

   public Object read(byte[] bytes, int off, int len, Class c) throws IOException {
      Template tmpl = this.registry.lookup(c);
      BufferUnpacker u = this.createBufferUnpacker(bytes, off, len);
      return tmpl.read(u, (Object)null);
   }

   public Object read(ByteBuffer b, Object v) throws IOException {
      Template tmpl = this.registry.lookup(v.getClass());
      return this.read(b, v, tmpl);
   }

   public Object read(ByteBuffer b, Template tmpl) throws IOException {
      return this.read((ByteBuffer)b, (Object)null, tmpl);
   }

   public Object read(ByteBuffer b, Class c) throws IOException {
      Template tmpl = this.registry.lookup(c);
      return this.read((ByteBuffer)b, (Object)null, tmpl);
   }

   public Object read(ByteBuffer b, Object v, Template tmpl) throws IOException {
      BufferUnpacker u = this.createBufferUnpacker(b);
      return tmpl.read(u, v);
   }

   public Object read(InputStream in, Object v) throws IOException {
      Template tmpl = this.registry.lookup(v.getClass());
      return this.read(in, v, tmpl);
   }

   public Object read(InputStream in, Template tmpl) throws IOException {
      return this.read((InputStream)in, (Object)null, tmpl);
   }

   public Object read(InputStream in, Class c) throws IOException {
      Template tmpl = this.registry.lookup(c);
      return this.read((InputStream)in, (Object)null, tmpl);
   }

   public Object read(InputStream in, Object v, Template tmpl) throws IOException {
      Unpacker u = this.createUnpacker(in);
      return tmpl.read(u, v);
   }

   public Object convert(Value v, T to) throws IOException {
      Template tmpl = this.registry.lookup(to.getClass());
      return tmpl.read(new Converter(this, v), to);
   }

   public Object convert(Value v, Class c) throws IOException {
      Template tmpl = this.registry.lookup(c);
      return tmpl.read(new Converter(this, v), (Object)null);
   }

   public Object convert(Value v, Template tmpl) throws IOException {
      return tmpl.read(new Converter(this, v), (Object)null);
   }

   public  Value unconvert(Object v) throws IOException {
      Unconverter pk = new Unconverter(this);
      if (v == null) {
         pk.writeNil();
      } else {
         Template tmpl = this.registry.lookup(v.getClass());
         tmpl.write(pk, v);
      }

      return pk.getResult();
   }

   public void register(Class<?> type) {
      this.registry.register(type);
   }

   public  void register(Class type, Template template) {
      this.registry.register((Type)type, (Template)template);
   }

   public boolean unregister(Class<?> type) {
      return this.registry.unregister(type);
   }

   public void unregister() {
      this.registry.unregister();
   }

   public Objectemplate lookup(Class type) {
      return this.registry.lookup(type);
   }

   public Template<?> lookup(Type type) {
      return this.registry.lookup(type);
   }

   /** @deprecated */
   @Deprecated
   public static byte[] pack(ObjecObject v) throws IOException {
      return globalMessagePack.write(v);
   }

   /** @deprecated */
   @Deprecated
   public static void pack(OutputStream out, ObjecObject v) throws IOException {
      globalMessagePack.write(out, v);
   }

   /** @deprecated */
   @Deprecated
   public static  byte[] pack(Object v, Template template) throws IOException {
      return globalMessagePack.write(v, template);
   }

   /** @deprecated */
   @Deprecated
   public static  void pack(OutputStream out, Object v, Template template) throws IOException {
      globalMessagePack.write(out, v, template);
   }

   /** @deprecated */
   @Deprecated
   public static Value unpack(byte[] bytes) throws IOException {
      return globalMessagePack.read(bytes);
   }

   /** @deprecated */
   @Deprecated
   public static Object unpack(byte[] bytes, Template template) throws IOException {
      BufferUnpacker u = (new MessagePackBufferUnpacker(globalMessagePack)).wrap(bytes);
      return template.read(u, (Object)null);
   }

   /** @deprecated */
   @Deprecated
   public static Object unpack(byte[] bytes, Template template, T to) throws IOException {
      BufferUnpacker u = (new MessagePackBufferUnpacker(globalMessagePack)).wrap(bytes);
      return template.read(u, to);
   }

   /** @deprecated */
   @Deprecated
   public static Object unpack(byte[] bytes, Class klass) throws IOException {
      return globalMessagePack.read(bytes, klass);
   }

   /** @deprecated */
   @Deprecated
   public static Object unpack(byte[] bytes, T to) throws IOException {
      return globalMessagePack.read(bytes, to);
   }

   /** @deprecated */
   @Deprecated
   public static Value unpack(InputStream in) throws IOException {
      return globalMessagePack.read(in);
   }

   /** @deprecated */
   @Deprecated
   public static Object unpack(InputStream in, Template tmpl) throws IOException, MessageTypeException {
      return tmpl.read(new MessagePackUnpacker(globalMessagePack, in), (Object)null);
   }

   /** @deprecated */
   @Deprecated
   public static Object unpack(InputStream in, Template tmpl, T to) throws IOException, MessageTypeException {
      return tmpl.read(new MessagePackUnpacker(globalMessagePack, in), to);
   }

   /** @deprecated */
   @Deprecated
   public static Object unpack(InputStream in, Class klass) throws IOException {
      return globalMessagePack.read(in, klass);
   }

   /** @deprecated */
   @Deprecated
   public static Object unpack(InputStream in, T to) throws IOException {
      return globalMessagePack.read(in, to);
   }
}

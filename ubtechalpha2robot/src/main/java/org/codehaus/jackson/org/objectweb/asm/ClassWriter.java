package org.codehaus.jackson.org.objectweb.asm;

public class ClassWriter implements ClassVisitor {
   public static final int COMPUTE_MAXS = 1;
   public static final int COMPUTE_FRAMES = 2;
   static final byte[] a;
   ClassReader J;
   int b;
   int c;
   final ByteVector d;
   Item[] e;
   int f;
   final Item g;
   final Item h;
   final Item i;
   Item[] E;
   private short D;
   private int j;
   private int k;
   String F;
   private int l;
   private int m;
   private int n;
   private int[] o;
   private int p;
   private ByteVector q;
   private int r;
   private int s;
   private AnnotationWriter t;
   private AnnotationWriter u;
   private Attribute v;
   private int w;
   private ByteVector x;
   FieldWriter y;
   FieldWriter z;
   MethodWriter A;
   MethodWriter B;
   private final boolean H;
   private final boolean G;
   boolean I;

   public ClassWriter(int var1) {
      this.c = 1;
      this.d = new ByteVector();
      this.e = new Item[256];
      this.f = (int)(0.75D * (double)this.e.length);
      this.g = new Item();
      this.h = new Item();
      this.i = new Item();
      this.H = (var1 & 1) != 0;
      this.G = (var1 & 2) != 0;
   }

   public ClassWriter(ClassReader var1, int var2) {
      this(var2);
      var1.a(this);
      this.J = var1;
   }

   public void visit(int var1, int var2, String var3, String var4, String var5, String[] var6) {
      this.b = var1;
      this.j = var2;
      this.k = this.newClass(var3);
      this.F = var3;
      if (var4 != null) {
         this.l = this.newUTF8(var4);
      }

      this.m = var5 == null ? 0 : this.newClass(var5);
      if (var6 != null && var6.length > 0) {
         this.n = var6.length;
         this.o = new int[this.n];

         for(int var7 = 0; var7 < this.n; ++var7) {
            this.o[var7] = this.newClass(var6[var7]);
         }
      }

   }

   public void visitSource(String var1, String var2) {
      if (var1 != null) {
         this.p = this.newUTF8(var1);
      }

      if (var2 != null) {
         this.q = (new ByteVector()).putUTF8(var2);
      }

   }

   public void visitOuterClass(String var1, String var2, String var3) {
      this.r = this.newClass(var1);
      if (var2 != null && var3 != null) {
         this.s = this.newNameType(var2, var3);
      }

   }

   public AnnotationVisitor visitAnnotation(String var1, boolean var2) {
      ByteVector var3 = new ByteVector();
      var3.putShort(this.newUTF8(var1)).putShort(0);
      AnnotationWriter var4 = new AnnotationWriter(this, true, var3, var3, 2);
      if (var2) {
         var4.g = this.t;
         this.t = var4;
      } else {
         var4.g = this.u;
         this.u = var4;
      }

      return var4;
   }

   public void visitAttribute(Attribute var1) {
      var1.a = this.v;
      this.v = var1;
   }

   public void visitInnerClass(String var1, String var2, String var3, int var4) {
      if (this.x == null) {
         this.x = new ByteVector();
      }

      ++this.w;
      this.x.putShort(var1 == null ? 0 : this.newClass(var1));
      this.x.putShort(var2 == null ? 0 : this.newClass(var2));
      this.x.putShort(var3 == null ? 0 : this.newUTF8(var3));
      this.x.putShort(var4);
   }

   public FieldVisitor visitField(int var1, String var2, String var3, String var4, Object var5) {
      return new FieldWriter(this, var1, var2, var3, var4, var5);
   }

   public MethodVisitor visitMethod(int var1, String var2, String var3, String var4, String[] var5) {
      return new MethodWriter(this, var1, var2, var3, var4, var5, this.H, this.G);
   }

   public void visitEnd() {
   }

   public byte[] toByteArray() {
      int var1 = 24 + 2 * this.n;
      int var2 = 0;

      FieldWriter var3;
      for(var3 = this.y; var3 != null; var3 = var3.a) {
         ++var2;
         var1 += var3.a();
      }

      int var4 = 0;

      MethodWriter var5;
      for(var5 = this.A; var5 != null; var5 = var5.a) {
         ++var4;
         var1 += var5.a();
      }

      int var6 = 0;
      if (this.l != 0) {
         ++var6;
         var1 += 8;
         this.newUTF8("Signature");
      }

      if (this.p != 0) {
         ++var6;
         var1 += 8;
         this.newUTF8("SourceFile");
      }

      if (this.q != null) {
         ++var6;
         var1 += this.q.b + 4;
         this.newUTF8("SourceDebugExtension");
      }

      if (this.r != 0) {
         ++var6;
         var1 += 10;
         this.newUTF8("EnclosingMethod");
      }

      if ((this.j & 131072) != 0) {
         ++var6;
         var1 += 6;
         this.newUTF8("Deprecated");
      }

      if ((this.j & 4096) != 0 && ((this.b & '\uffff') < 49 || (this.j & 262144) != 0)) {
         ++var6;
         var1 += 6;
         this.newUTF8("Synthetic");
      }

      if (this.x != null) {
         ++var6;
         var1 += 8 + this.x.b;
         this.newUTF8("InnerClasses");
      }

      if (this.t != null) {
         ++var6;
         var1 += 8 + this.t.a();
         this.newUTF8("RuntimeVisibleAnnotations");
      }

      if (this.u != null) {
         ++var6;
         var1 += 8 + this.u.a();
         this.newUTF8("RuntimeInvisibleAnnotations");
      }

      if (this.v != null) {
         var6 += this.v.a();
         var1 += this.v.a(this, (byte[])null, 0, -1, -1);
      }

      var1 += this.d.b;
      ByteVector var7 = new ByteVector(var1);
      var7.putInt(-889275714).putInt(this.b);
      var7.putShort(this.c).putByteArray(this.d.a, 0, this.d.b);
      int var8 = 393216 | (this.j & 262144) / 64;
      var7.putShort(this.j & ~var8).putShort(this.k).putShort(this.m);
      var7.putShort(this.n);

      int var9;
      for(var9 = 0; var9 < this.n; ++var9) {
         var7.putShort(this.o[var9]);
      }

      var7.putShort(var2);

      for(var3 = this.y; var3 != null; var3 = var3.a) {
         var3.a(var7);
      }

      var7.putShort(var4);

      for(var5 = this.A; var5 != null; var5 = var5.a) {
         var5.a(var7);
      }

      var7.putShort(var6);
      if (this.l != 0) {
         var7.putShort(this.newUTF8("Signature")).putInt(2).putShort(this.l);
      }

      if (this.p != 0) {
         var7.putShort(this.newUTF8("SourceFile")).putInt(2).putShort(this.p);
      }

      if (this.q != null) {
         var9 = this.q.b - 2;
         var7.putShort(this.newUTF8("SourceDebugExtension")).putInt(var9);
         var7.putByteArray(this.q.a, 2, var9);
      }

      if (this.r != 0) {
         var7.putShort(this.newUTF8("EnclosingMethod")).putInt(4);
         var7.putShort(this.r).putShort(this.s);
      }

      if ((this.j & 131072) != 0) {
         var7.putShort(this.newUTF8("Deprecated")).putInt(0);
      }

      if ((this.j & 4096) != 0 && ((this.b & '\uffff') < 49 || (this.j & 262144) != 0)) {
         var7.putShort(this.newUTF8("Synthetic")).putInt(0);
      }

      if (this.x != null) {
         var7.putShort(this.newUTF8("InnerClasses"));
         var7.putInt(this.x.b + 2).putShort(this.w);
         var7.putByteArray(this.x.a, 0, this.x.b);
      }

      if (this.t != null) {
         var7.putShort(this.newUTF8("RuntimeVisibleAnnotations"));
         this.t.a(var7);
      }

      if (this.u != null) {
         var7.putShort(this.newUTF8("RuntimeInvisibleAnnotations"));
         this.u.a(var7);
      }

      if (this.v != null) {
         this.v.a(this, (byte[])null, 0, -1, -1, var7);
      }

      if (this.I) {
         ClassWriter var10 = new ClassWriter(2);
         (new ClassReader(var7.a)).accept(var10, 4);
         return var10.toByteArray();
      } else {
         return var7.a;
      }
   }

   Item a(Object var1) {
      int var6;
      if (var1 instanceof Integer) {
         var6 = (Integer)var1;
         return this.a(var6);
      } else if (var1 instanceof Byte) {
         var6 = ((Byte)var1).intValue();
         return this.a(var6);
      } else if (var1 instanceof Character) {
         char var7 = (Character)var1;
         return this.a(var7);
      } else if (var1 instanceof Short) {
         var6 = ((Short)var1).intValue();
         return this.a(var6);
      } else if (var1 instanceof Boolean) {
         var6 = (Boolean)var1 ? 1 : 0;
         return this.a(var6);
      } else if (var1 instanceof Float) {
         float var5 = (Float)var1;
         return this.a(var5);
      } else if (var1 instanceof Long) {
         long var8 = (Long)var1;
         return this.a(var8);
      } else if (var1 instanceof Double) {
         double var3 = (Double)var1;
         return this.a(var3);
      } else if (var1 instanceof String) {
         return this.b((String)var1);
      } else if (var1 instanceof Type) {
         Type var2 = (Type)var1;
         return this.a(var2.getSort() == 10 ? var2.getInternalName() : var2.getDescriptor());
      } else {
         throw new IllegalArgumentException("value " + var1);
      }
   }

   public int newConst(Object var1) {
      return this.a(var1).a;
   }

   public int newUTF8(String var1) {
      this.g.a(1, var1, (String)null, (String)null);
      Item var2 = this.a(this.g);
      if (var2 == null) {
         this.d.putByte(1).putUTF8(var1);
         var2 = new Item(this.c++, this.g);
         this.b(var2);
      }

      return var2.a;
   }

   Item a(String var1) {
      this.h.a(7, var1, (String)null, (String)null);
      Item var2 = this.a(this.h);
      if (var2 == null) {
         this.d.b(7, this.newUTF8(var1));
         var2 = new Item(this.c++, this.h);
         this.b(var2);
      }

      return var2;
   }

   public int newClass(String var1) {
      return this.a(var1).a;
   }

   Item a(String var1, String var2, String var3) {
      this.i.a(9, var1, var2, var3);
      Item var4 = this.a(this.i);
      if (var4 == null) {
         this.a(9, this.newClass(var1), this.newNameType(var2, var3));
         var4 = new Item(this.c++, this.i);
         this.b(var4);
      }

      return var4;
   }

   public int newField(String var1, String var2, String var3) {
      return this.a(var1, var2, var3).a;
   }

   Item a(String var1, String var2, String var3, boolean var4) {
      int var5 = var4 ? 11 : 10;
      this.i.a(var5, var1, var2, var3);
      Item var6 = this.a(this.i);
      if (var6 == null) {
         this.a(var5, this.newClass(var1), this.newNameType(var2, var3));
         var6 = new Item(this.c++, this.i);
         this.b(var6);
      }

      return var6;
   }

   public int newMethod(String var1, String var2, String var3, boolean var4) {
      return this.a(var1, var2, var3, var4).a;
   }

   Item a(int var1) {
      this.g.a(var1);
      Item var2 = this.a(this.g);
      if (var2 == null) {
         this.d.putByte(3).putInt(var1);
         var2 = new Item(this.c++, this.g);
         this.b(var2);
      }

      return var2;
   }

   Item a(float var1) {
      this.g.a(var1);
      Item var2 = this.a(this.g);
      if (var2 == null) {
         this.d.putByte(4).putInt(this.g.c);
         var2 = new Item(this.c++, this.g);
         this.b(var2);
      }

      return var2;
   }

   Item a(long var1) {
      this.g.a(var1);
      Item var3 = this.a(this.g);
      if (var3 == null) {
         this.d.putByte(5).putLong(var1);
         var3 = new Item(this.c, this.g);
         this.b(var3);
         this.c += 2;
      }

      return var3;
   }

   Item a(double var1) {
      this.g.a(var1);
      Item var3 = this.a(this.g);
      if (var3 == null) {
         this.d.putByte(6).putLong(this.g.d);
         var3 = new Item(this.c, this.g);
         this.b(var3);
         this.c += 2;
      }

      return var3;
   }

   private Item b(String var1) {
      this.h.a(8, var1, (String)null, (String)null);
      Item var2 = this.a(this.h);
      if (var2 == null) {
         this.d.b(8, this.newUTF8(var1));
         var2 = new Item(this.c++, this.h);
         this.b(var2);
      }

      return var2;
   }

   public int newNameType(String var1, String var2) {
      return this.a(var1, var2).a;
   }

   Item a(String var1, String var2) {
      this.h.a(12, var1, var2, (String)null);
      Item var3 = this.a(this.h);
      if (var3 == null) {
         this.a(12, this.newUTF8(var1), this.newUTF8(var2));
         var3 = new Item(this.c++, this.h);
         this.b(var3);
      }

      return var3;
   }

   int c(String var1) {
      this.g.a(13, var1, (String)null, (String)null);
      Item var2 = this.a(this.g);
      if (var2 == null) {
         var2 = this.c(this.g);
      }

      return var2.a;
   }

   int a(String var1, int var2) {
      this.g.b = 14;
      this.g.c = var2;
      this.g.g = var1;
      this.g.j = 2147483647 & 14 + var1.hashCode() + var2;
      Item var3 = this.a(this.g);
      if (var3 == null) {
         var3 = this.c(this.g);
      }

      return var3.a;
   }

   private Item c(Item var1) {
      ++this.D;
      Item var2 = new Item(this.D, this.g);
      this.b(var2);
      if (this.E == null) {
         this.E = new Item[16];
      }

      if (this.D == this.E.length) {
         Item[] var3 = new Item[2 * this.E.length];
         System.arraycopy(this.E, 0, var3, 0, this.E.length);
         this.E = var3;
      }

      this.E[this.D] = var2;
      return var2;
   }

   int a(int var1, int var2) {
      this.h.b = 15;
      this.h.d = (long)var1 | (long)var2 << 32;
      this.h.j = 2147483647 & 15 + var1 + var2;
      Item var3 = this.a(this.h);
      if (var3 == null) {
         String var4 = this.E[var1].g;
         String var5 = this.E[var2].g;
         this.h.c = this.c(this.getCommonSuperClass(var4, var5));
         var3 = new Item(0, this.h);
         this.b(var3);
      }

      return var3.c;
   }

   protected String getCommonSuperClass(String var1, String var2) {
      Class var3;
      Class var4;
      try {
         var3 = Class.forName(var1.replace('/', '.'));
         var4 = Class.forName(var2.replace('/', '.'));
      } catch (Exception var6) {
         throw new RuntimeException(var6.toString());
      }

      if (var3.isAssignableFrom(var4)) {
         return var1;
      } else if (var4.isAssignableFrom(var3)) {
         return var2;
      } else if (!var3.isInterface() && !var4.isInterface()) {
         do {
            var3 = var3.getSuperclass();
         } while(!var3.isAssignableFrom(var4));

         return var3.getName().replace('.', '/');
      } else {
         return "java/lang/Object";
      }
   }

   private Item a(Item var1) {
      Item var2;
      for(var2 = this.e[var1.j % this.e.length]; var2 != null && (var2.b != var1.b || !var1.a(var2)); var2 = var2.k) {
      }

      return var2;
   }

   private void b(Item var1) {
      int var2;
      if (this.c > this.f) {
         var2 = this.e.length;
         int var3 = var2 * 2 + 1;
         Item[] var4 = new Item[var3];

         Item var8;
         for(int var5 = var2 - 1; var5 >= 0; --var5) {
            for(Item var6 = this.e[var5]; var6 != null; var6 = var8) {
               int var7 = var6.j % var4.length;
               var8 = var6.k;
               var6.k = var4[var7];
               var4[var7] = var6;
            }
         }

         this.e = var4;
         this.f = (int)((double)var3 * 0.75D);
      }

      var2 = var1.j % this.e.length;
      var1.k = this.e[var2];
      this.e[var2] = var1;
   }

   private void a(int var1, int var2, int var3) {
      this.d.b(var1, var2).putShort(var3);
   }

   static {
      byte[] var0 = new byte[220];
      String var1 = "AAAAAAAAAAAAAAAABCKLLDDDDDEEEEEEEEEEEEEEEEEEEEAAAAAAAADDDDDEEEEEEEEEEEEEEEEEEEEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAAAAAAAAAAAAAAAAAAIIIIIIIIIIIIIIIIDNOAAAAAAGGGGGGGHHFBFAAFFAAQPIIJJIIIIIIIIIIIIIIIIII";

      for(int var2 = 0; var2 < var0.length; ++var2) {
         var0[var2] = (byte)(var1.charAt(var2) - 65);
      }

      a = var0;
   }
}

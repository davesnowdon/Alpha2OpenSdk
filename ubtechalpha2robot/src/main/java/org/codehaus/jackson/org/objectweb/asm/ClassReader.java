package org.codehaus.jackson.org.objectweb.asm;

import java.io.IOException;
import java.io.InputStream;

public class ClassReader {
   public static final int SKIP_CODE = 1;
   public static final int SKIP_DEBUG = 2;
   public static final int SKIP_FRAMES = 4;
   public static final int EXPAND_FRAMES = 8;
   public final byte[] b;
   private final int[] a;
   private final String[] c;
   private final int d;
   public final int header;

   public ClassReader(byte[] var1) {
      this(var1, 0, var1.length);
   }

   public ClassReader(byte[] var1, int var2, int var3) {
      this.b = var1;
      this.a = new int[this.readUnsignedShort(var2 + 8)];
      int var4 = this.a.length;
      this.c = new String[var4];
      int var5 = 0;
      int var6 = var2 + 10;

      for(int var7 = 1; var7 < var4; ++var7) {
         this.a[var7] = var6 + 1;
         int var8;
         switch(var1[var6]) {
         case 1:
            var8 = 3 + this.readUnsignedShort(var6 + 1);
            if (var8 > var5) {
               var5 = var8;
            }
            break;
         case 2:
         case 7:
         case 8:
         default:
            var8 = 3;
            break;
         case 3:
         case 4:
         case 9:
         case 10:
         case 11:
         case 12:
            var8 = 5;
            break;
         case 5:
         case 6:
            var8 = 9;
            ++var7;
         }

         var6 += var8;
      }

      this.d = var5;
      this.header = var6;
   }

   public int getAccess() {
      return this.readUnsignedShort(this.header);
   }

   public String getClassName() {
      return this.readClass(this.header + 2, new char[this.d]);
   }

   public String getSuperName() {
      int var1 = this.a[this.readUnsignedShort(this.header + 4)];
      return var1 == 0 ? null : this.readUTF8(var1, new char[this.d]);
   }

   public String[] getInterfaces() {
      int var1 = this.header + 6;
      int var2 = this.readUnsignedShort(var1);
      String[] var3 = new String[var2];
      if (var2 > 0) {
         char[] var4 = new char[this.d];

         for(int var5 = 0; var5 < var2; ++var5) {
            var1 += 2;
            var3[var5] = this.readClass(var1, var4);
         }
      }

      return var3;
   }

   void a(ClassWriter var1) {
      char[] var2 = new char[this.d];
      int var3 = this.a.length;
      Item[] var4 = new Item[var3];

      int var5;
      for(var5 = 1; var5 < var3; ++var5) {
         int var6 = this.a[var5];
         byte var7 = this.b[var6 - 1];
         Item var8 = new Item(var5);
         switch(var7) {
         case 1:
            String var10 = this.c[var5];
            if (var10 == null) {
               var6 = this.a[var5];
               var10 = this.c[var5] = this.a(var6 + 2, this.readUnsignedShort(var6), var2);
            }

            var8.a(var7, var10, (String)null, (String)null);
            break;
         case 2:
         case 7:
         case 8:
         default:
            var8.a(var7, this.readUTF8(var6, var2), (String)null, (String)null);
            break;
         case 3:
            var8.a(this.readInt(var6));
            break;
         case 4:
            var8.a(Float.intBitsToFloat(this.readInt(var6)));
            break;
         case 5:
            var8.a(this.readLong(var6));
            ++var5;
            break;
         case 6:
            var8.a(Double.longBitsToDouble(this.readLong(var6)));
            ++var5;
            break;
         case 9:
         case 10:
         case 11:
            int var9 = this.a[this.readUnsignedShort(var6 + 2)];
            var8.a(var7, this.readClass(var6, var2), this.readUTF8(var9, var2), this.readUTF8(var9 + 2, var2));
            break;
         case 12:
            var8.a(var7, this.readUTF8(var6, var2), this.readUTF8(var6 + 2, var2), (String)null);
         }

         int var11 = var8.j % var4.length;
         var8.k = var4[var11];
         var4[var11] = var8;
      }

      var5 = this.a[1] - 1;
      var1.d.putByteArray(this.b, var5, this.header - var5);
      var1.e = var4;
      var1.f = (int)(0.75D * (double)var3);
      var1.c = var3;
   }

   public ClassReader(InputStream var1) throws IOException {
      this(a(var1));
   }

   public ClassReader(String var1) throws IOException {
      this(ClassLoader.getSystemResourceAsStream(var1.replace('.', '/') + ".class"));
   }

   private static byte[] a(InputStream var0) throws IOException {
      if (var0 == null) {
         throw new IOException("Class not found");
      } else {
         byte[] var1 = new byte[var0.available()];
         int var2 = 0;

         while(true) {
            int var3 = var0.read(var1, var2, var1.length - var2);
            if (var3 == -1) {
               if (var2 < var1.length) {
                  byte[] var6 = new byte[var2];
                  System.arraycopy(var1, 0, var6, 0, var2);
                  var1 = var6;
               }

               return var1;
            }

            var2 += var3;
            if (var2 == var1.length) {
               int var4 = var0.read();
               if (var4 < 0) {
                  return var1;
               }

               byte[] var5 = new byte[var1.length + 1000];
               System.arraycopy(var1, 0, var5, 0, var2);
               var5[var2++] = (byte)var4;
               var1 = var5;
            }
         }
      }
   }

   public void accept(ClassVisitor var1, int var2) {
      this.accept(var1, new Attribute[0], var2);
   }

   public void accept(ClassVisitor var1, Attribute[] var2, int var3) {
      byte[] var4 = this.b;
      char[] var5 = new char[this.d];
      int var6 = 0;
      int var7 = 0;
      Attribute var8 = null;
      int var9 = this.header;
      int var10 = this.readUnsignedShort(var9);
      String var11 = this.readClass(var9 + 2, var5);
      int var12 = this.a[this.readUnsignedShort(var9 + 4)];
      String var13 = var12 == 0 ? null : this.readUTF8(var12, var5);
      String[] var14 = new String[this.readUnsignedShort(var9 + 6)];
      int var15 = 0;
      var9 += 8;

      int var16;
      for(var16 = 0; var16 < var14.length; ++var16) {
         var14[var16] = this.readClass(var9, var5);
         var9 += 2;
      }

      boolean var17 = (var3 & 1) != 0;
      boolean var18 = (var3 & 2) != 0;
      boolean var19 = (var3 & 8) != 0;
      var16 = this.readUnsignedShort(var9);

      int var20;
      for(var12 = var9 + 2; var16 > 0; --var16) {
         var20 = this.readUnsignedShort(var12 + 6);

         for(var12 += 8; var20 > 0; --var20) {
            var12 += 6 + this.readInt(var12 + 2);
         }
      }

      var16 = this.readUnsignedShort(var12);

      for(var12 += 2; var16 > 0; --var16) {
         var20 = this.readUnsignedShort(var12 + 6);

         for(var12 += 8; var20 > 0; --var20) {
            var12 += 6 + this.readInt(var12 + 2);
         }
      }

      String var21 = null;
      String var22 = null;
      String var23 = null;
      String var24 = null;
      String var25 = null;
      String var26 = null;
      var16 = this.readUnsignedShort(var12);

      String var27;
      int var28;
      Attribute var29;
      for(var12 += 2; var16 > 0; --var16) {
         var27 = this.readUTF8(var12, var5);
         if ("SourceFile".equals(var27)) {
            var22 = this.readUTF8(var12 + 6, var5);
         } else if ("InnerClasses".equals(var27)) {
            var15 = var12 + 6;
         } else if ("EnclosingMethod".equals(var27)) {
            var24 = this.readClass(var12 + 6, var5);
            var28 = this.readUnsignedShort(var12 + 8);
            if (var28 != 0) {
               var25 = this.readUTF8(this.a[var28], var5);
               var26 = this.readUTF8(this.a[var28] + 2, var5);
            }
         } else if ("Signature".equals(var27)) {
            var21 = this.readUTF8(var12 + 6, var5);
         } else if ("RuntimeVisibleAnnotations".equals(var27)) {
            var6 = var12 + 6;
         } else if ("Deprecated".equals(var27)) {
            var10 |= 131072;
         } else if ("Synthetic".equals(var27)) {
            var10 |= 266240;
         } else if ("SourceDebugExtension".equals(var27)) {
            var28 = this.readInt(var12 + 2);
            var23 = this.a(var12 + 6, var28, new char[var28]);
         } else if ("RuntimeInvisibleAnnotations".equals(var27)) {
            var7 = var12 + 6;
         } else {
            var29 = this.a(var2, var27, var12 + 6, this.readInt(var12 + 2), var5, -1, (Label[])null);
            if (var29 != null) {
               var29.a = var8;
               var8 = var29;
            }
         }

         var12 += 6 + this.readInt(var12 + 2);
      }

      var1.visit(this.readInt(4), var10, var11, var21, var13, var14);
      if (!var18 && (var22 != null || var23 != null)) {
         var1.visitSource(var22, var23);
      }

      if (var24 != null) {
         var1.visitOuterClass(var24, var25, var26);
      }

      for(var16 = 1; var16 >= 0; --var16) {
         var12 = var16 == 0 ? var7 : var6;
         if (var12 != 0) {
            var20 = this.readUnsignedShort(var12);

            for(var12 += 2; var20 > 0; --var20) {
               var12 = this.a(var12 + 2, var5, true, var1.visitAnnotation(this.readUTF8(var12, var5), var16 != 0));
            }
         }
      }

      while(var8 != null) {
         var29 = var8.a;
         var8.a = null;
         var1.visitAttribute(var8);
         var8 = var29;
      }

      if (var15 != 0) {
         var16 = this.readUnsignedShort(var15);

         for(var15 += 2; var16 > 0; --var16) {
            var1.visitInnerClass(this.readUnsignedShort(var15) == 0 ? null : this.readClass(var15, var5), this.readUnsignedShort(var15 + 2) == 0 ? null : this.readClass(var15 + 2, var5), this.readUnsignedShort(var15 + 4) == 0 ? null : this.readUTF8(var15 + 4, var5), this.readUnsignedShort(var15 + 6));
            var15 += 8;
         }
      }

      var16 = this.readUnsignedShort(var9);

      String var30;
      int var32;
      for(var9 += 2; var16 > 0; --var16) {
         var10 = this.readUnsignedShort(var9);
         var11 = this.readUTF8(var9 + 2, var5);
         var30 = this.readUTF8(var9 + 4, var5);
         var28 = 0;
         var21 = null;
         var6 = 0;
         var7 = 0;
         var8 = null;
         var20 = this.readUnsignedShort(var9 + 6);

         for(var9 += 8; var20 > 0; --var20) {
            var27 = this.readUTF8(var9, var5);
            if ("ConstantValue".equals(var27)) {
               var28 = this.readUnsignedShort(var9 + 6);
            } else if ("Signature".equals(var27)) {
               var21 = this.readUTF8(var9 + 6, var5);
            } else if ("Deprecated".equals(var27)) {
               var10 |= 131072;
            } else if ("Synthetic".equals(var27)) {
               var10 |= 266240;
            } else if ("RuntimeVisibleAnnotations".equals(var27)) {
               var6 = var9 + 6;
            } else if ("RuntimeInvisibleAnnotations".equals(var27)) {
               var7 = var9 + 6;
            } else {
               var29 = this.a(var2, var27, var9 + 6, this.readInt(var9 + 2), var5, -1, (Label[])null);
               if (var29 != null) {
                  var29.a = var8;
                  var8 = var29;
               }
            }

            var9 += 6 + this.readInt(var9 + 2);
         }

         FieldVisitor var31 = var1.visitField(var10, var11, var30, var21, var28 == 0 ? null : this.readConst(var28, var5));
         if (var31 != null) {
            for(var20 = 1; var20 >= 0; --var20) {
               var12 = var20 == 0 ? var7 : var6;
               if (var12 != 0) {
                  var32 = this.readUnsignedShort(var12);

                  for(var12 += 2; var32 > 0; --var32) {
                     var12 = this.a(var12 + 2, var5, true, var31.visitAnnotation(this.readUTF8(var12, var5), var20 != 0));
                  }
               }
            }

            while(var8 != null) {
               var29 = var8.a;
               var8.a = null;
               var31.visitAttribute(var8);
               var8 = var29;
            }

            var31.visitEnd();
         }
      }

      var16 = this.readUnsignedShort(var9);

      for(var9 += 2; var16 > 0; --var16) {
         var28 = var9 + 6;
         var10 = this.readUnsignedShort(var9);
         var11 = this.readUTF8(var9 + 2, var5);
         var30 = this.readUTF8(var9 + 4, var5);
         var21 = null;
         var6 = 0;
         var7 = 0;
         int var68 = 0;
         int var33 = 0;
         int var34 = 0;
         var8 = null;
         var12 = 0;
         var15 = 0;
         var20 = this.readUnsignedShort(var9 + 6);

         for(var9 += 8; var20 > 0; --var20) {
            var27 = this.readUTF8(var9, var5);
            int var35 = this.readInt(var9 + 2);
            var9 += 6;
            if ("Code".equals(var27)) {
               if (!var17) {
                  var12 = var9;
               }
            } else if ("Exceptions".equals(var27)) {
               var15 = var9;
            } else if ("Signature".equals(var27)) {
               var21 = this.readUTF8(var9, var5);
            } else if ("Deprecated".equals(var27)) {
               var10 |= 131072;
            } else if ("RuntimeVisibleAnnotations".equals(var27)) {
               var6 = var9;
            } else if ("AnnotationDefault".equals(var27)) {
               var68 = var9;
            } else if ("Synthetic".equals(var27)) {
               var10 |= 266240;
            } else if ("RuntimeInvisibleAnnotations".equals(var27)) {
               var7 = var9;
            } else if ("RuntimeVisibleParameterAnnotations".equals(var27)) {
               var33 = var9;
            } else if ("RuntimeInvisibleParameterAnnotations".equals(var27)) {
               var34 = var9;
            } else {
               var29 = this.a(var2, var27, var9, var35, var5, -1, (Label[])null);
               if (var29 != null) {
                  var29.a = var8;
                  var8 = var29;
               }
            }

            var9 += var35;
         }

         String[] var69;
         if (var15 == 0) {
            var69 = null;
         } else {
            var69 = new String[this.readUnsignedShort(var15)];
            var15 += 2;

            for(var20 = 0; var20 < var69.length; ++var20) {
               var69[var20] = this.readClass(var15, var5);
               var15 += 2;
            }
         }

         MethodVisitor var36 = var1.visitMethod(var10, var11, var30, var21, var69);
         if (var36 != null) {
            if (var36 instanceof MethodWriter) {
               MethodWriter var37 = (MethodWriter)var36;
               if (var37.b.J == this && var21 == var37.g) {
                  boolean var38 = false;
                  if (var69 == null) {
                     var38 = var37.j == 0;
                  } else if (var69.length == var37.j) {
                     var38 = true;

                     for(var20 = var69.length - 1; var20 >= 0; --var20) {
                        var15 -= 2;
                        if (var37.k[var20] != this.readUnsignedShort(var15)) {
                           var38 = false;
                           break;
                        }
                     }
                  }

                  if (var38) {
                     var37.h = var28;
                     var37.i = var9 - var28;
                     continue;
                  }
               }
            }

            if (var68 != 0) {
               AnnotationVisitor var70 = var36.visitAnnotationDefault();
               this.a(var68, var5, (String)null, var70);
               if (var70 != null) {
                  var70.visitEnd();
               }
            }

            for(var20 = 1; var20 >= 0; --var20) {
               var15 = var20 == 0 ? var7 : var6;
               if (var15 != 0) {
                  var32 = this.readUnsignedShort(var15);

                  for(var15 += 2; var32 > 0; --var32) {
                     var15 = this.a(var15 + 2, var5, true, var36.visitAnnotation(this.readUTF8(var15, var5), var20 != 0));
                  }
               }
            }

            if (var33 != 0) {
               this.a(var33, var30, var5, true, var36);
            }

            if (var34 != 0) {
               this.a(var34, var30, var5, false, var36);
            }

            while(var8 != null) {
               var29 = var8.a;
               var8.a = null;
               var36.visitAttribute(var8);
               var8 = var29;
            }
         }

         if (var36 != null && var12 != 0) {
            int var71 = this.readUnsignedShort(var12);
            int var72 = this.readUnsignedShort(var12 + 2);
            int var39 = this.readInt(var12 + 4);
            var12 += 8;
            int var40 = var12;
            int var41 = var12 + var39;
            var36.visitCode();
            Label[] var42 = new Label[var39 + 2];
            this.readLabel(var39 + 1, var42);

            label737:
            while(true) {
               int var43;
               label735:
               while(var12 < var41) {
                  var15 = var12 - var40;
                  var43 = var4[var12] & 255;
                  switch(ClassWriter.a[var43]) {
                  case 0:
                  case 4:
                     ++var12;
                     break;
                  case 1:
                  case 3:
                  case 10:
                     var12 += 2;
                     break;
                  case 2:
                  case 5:
                  case 6:
                  case 11:
                  case 12:
                     var12 += 3;
                     break;
                  case 7:
                     var12 += 5;
                     break;
                  case 8:
                     this.readLabel(var15 + this.readShort(var12 + 1), var42);
                     var12 += 3;
                     break;
                  case 9:
                     this.readLabel(var15 + this.readInt(var12 + 1), var42);
                     var12 += 5;
                     break;
                  case 13:
                     var12 = var12 + 4 - (var15 & 3);
                     this.readLabel(var15 + this.readInt(var12), var42);
                     var20 = this.readInt(var12 + 8) - this.readInt(var12 + 4) + 1;
                     var12 += 12;

                     while(true) {
                        if (var20 <= 0) {
                           continue label735;
                        }

                        this.readLabel(var15 + this.readInt(var12), var42);
                        var12 += 4;
                        --var20;
                     }
                  case 14:
                     var12 = var12 + 4 - (var15 & 3);
                     this.readLabel(var15 + this.readInt(var12), var42);
                     var20 = this.readInt(var12 + 4);
                     var12 += 8;

                     while(true) {
                        if (var20 <= 0) {
                           continue label735;
                        }

                        this.readLabel(var15 + this.readInt(var12 + 4), var42);
                        var12 += 8;
                        --var20;
                     }
                  case 15:
                  default:
                     var12 += 4;
                     break;
                  case 16:
                     var43 = var4[var12 + 1] & 255;
                     if (var43 == 132) {
                        var12 += 6;
                     } else {
                        var12 += 4;
                     }
                  }
               }

               var20 = this.readUnsignedShort(var12);

               int var46;
               for(var12 += 2; var20 > 0; --var20) {
                  Label var73 = this.readLabel(this.readUnsignedShort(var12), var42);
                  Label var44 = this.readLabel(this.readUnsignedShort(var12 + 2), var42);
                  Label var45 = this.readLabel(this.readUnsignedShort(var12 + 4), var42);
                  var46 = this.readUnsignedShort(var12 + 6);
                  if (var46 == 0) {
                     var36.visitTryCatchBlock(var73, var44, var45, (String)null);
                  } else {
                     var36.visitTryCatchBlock(var73, var44, var45, this.readUTF8(this.a[var46], var5));
                  }

                  var12 += 8;
               }

               var43 = 0;
               int var74 = 0;
               int var75 = 0;
               var46 = 0;
               int var47 = 0;
               byte var48 = 0;
               int var49 = 0;
               int var50 = 0;
               int var51 = 0;
               int var52 = 0;
               Object[] var53 = null;
               Object[] var54 = null;
               boolean var55 = true;
               var8 = null;
               var20 = this.readUnsignedShort(var12);

               int var56;
               for(var12 += 2; var20 > 0; --var20) {
                  var27 = this.readUTF8(var12, var5);
                  Label var10000;
                  if ("LocalVariableTable".equals(var27)) {
                     if (!var18) {
                        var43 = var12 + 6;
                        var32 = this.readUnsignedShort(var12 + 6);

                        for(var15 = var12 + 8; var32 > 0; --var32) {
                           var56 = this.readUnsignedShort(var15);
                           if (var42[var56] == null) {
                              var10000 = this.readLabel(var56, var42);
                              var10000.a |= 1;
                           }

                           var56 += this.readUnsignedShort(var15 + 2);
                           if (var42[var56] == null) {
                              var10000 = this.readLabel(var56, var42);
                              var10000.a |= 1;
                           }

                           var15 += 10;
                        }
                     }
                  } else if ("LocalVariableTypeTable".equals(var27)) {
                     var74 = var12 + 6;
                  } else if ("LineNumberTable".equals(var27)) {
                     if (!var18) {
                        var32 = this.readUnsignedShort(var12 + 6);

                        for(var15 = var12 + 8; var32 > 0; --var32) {
                           var56 = this.readUnsignedShort(var15);
                           if (var42[var56] == null) {
                              var10000 = this.readLabel(var56, var42);
                              var10000.a |= 1;
                           }

                           var42[var56].b = this.readUnsignedShort(var15 + 2);
                           var15 += 4;
                        }
                     }
                  } else if ("StackMapTable".equals(var27)) {
                     if ((var3 & 4) == 0) {
                        var75 = var12 + 8;
                        var46 = this.readInt(var12 + 2);
                        var47 = this.readUnsignedShort(var12 + 6);
                     }
                  } else if ("StackMap".equals(var27)) {
                     if ((var3 & 4) == 0) {
                        var75 = var12 + 8;
                        var46 = this.readInt(var12 + 2);
                        var47 = this.readUnsignedShort(var12 + 6);
                        var55 = false;
                     }
                  } else {
                     for(var32 = 0; var32 < var2.length; ++var32) {
                        if (var2[var32].type.equals(var27)) {
                           var29 = var2[var32].read(this, var12 + 6, this.readInt(var12 + 2), var5, var40 - 8, var42);
                           if (var29 != null) {
                              var29.a = var8;
                              var8 = var29;
                           }
                        }
                     }
                  }

                  var12 += 6 + this.readInt(var12 + 2);
               }

               if (var75 != 0) {
                  var53 = new Object[var72];
                  var54 = new Object[var71];
                  if (var19) {
                     int var57 = 0;
                     if ((var10 & 8) == 0) {
                        if ("<init>".equals(var11)) {
                           var53[var57++] = Opcodes.UNINITIALIZED_THIS;
                        } else {
                           var53[var57++] = this.readClass(this.header + 2, var5);
                        }
                     }

                     var20 = 1;

                     label673:
                     while(true) {
                        while(true) {
                           var32 = var20;
                           switch(var30.charAt(var20++)) {
                           case 'B':
                           case 'C':
                           case 'I':
                           case 'S':
                           case 'Z':
                              var53[var57++] = Opcodes.INTEGER;
                              break;
                           case 'D':
                              var53[var57++] = Opcodes.DOUBLE;
                              break;
                           case 'E':
                           case 'G':
                           case 'H':
                           case 'K':
                           case 'M':
                           case 'N':
                           case 'O':
                           case 'P':
                           case 'Q':
                           case 'R':
                           case 'T':
                           case 'U':
                           case 'V':
                           case 'W':
                           case 'X':
                           case 'Y':
                           default:
                              var50 = var57;
                              break label673;
                           case 'F':
                              var53[var57++] = Opcodes.FLOAT;
                              break;
                           case 'J':
                              var53[var57++] = Opcodes.LONG;
                              break;
                           case 'L':
                              while(var30.charAt(var20) != ';') {
                                 ++var20;
                              }

                              var53[var57++] = var30.substring(var32 + 1, var20++);
                              break;
                           case '[':
                              while(var30.charAt(var20) == '[') {
                                 ++var20;
                              }

                              if (var30.charAt(var20) == 'L') {
                                 ++var20;

                                 while(var30.charAt(var20) != ';') {
                                    ++var20;
                                 }
                              }

                              int var10001 = var57++;
                              ++var20;
                              var53[var10001] = var30.substring(var32, var20);
                           }
                        }
                     }
                  }

                  var49 = -1;

                  for(var20 = var75; var20 < var75 + var46 - 2; ++var20) {
                     if (var4[var20] == 8) {
                        var32 = this.readUnsignedShort(var20 + 1);
                        if (var32 >= 0 && var32 < var39 && (var4[var40 + var32] & 255) == 187) {
                           this.readLabel(var32, var42);
                        }
                     }
                  }
               }

               var12 = var40;

               while(true) {
                  int var59;
                  int var60;
                  Label var78;
                  label632:
                  while(var12 < var41) {
                     var15 = var12 - var40;
                     var78 = var42[var15];
                     if (var78 != null) {
                        var36.visitLabel(var78);
                        if (!var18 && var78.b > 0) {
                           var36.visitLineNumber(var78.b, var78);
                        }
                     }

                     while(true) {
                        int var58;
                        while(var53 != null && (var49 == var15 || var49 == -1)) {
                           if (var55 && !var19) {
                              if (var49 != -1) {
                                 var36.visitFrame(var48, var51, var53, var52, var54);
                              }
                           } else {
                              var36.visitFrame(-1, var50, var53, var52, var54);
                           }

                           if (var47 > 0) {
                              if (var55) {
                                 var58 = var4[var75++] & 255;
                              } else {
                                 var58 = 255;
                                 var49 = -1;
                              }

                              var51 = 0;
                              if (var58 < 64) {
                                 var59 = var58;
                                 var48 = 3;
                                 var52 = 0;
                              } else if (var58 < 128) {
                                 var59 = var58 - 64;
                                 var75 = this.a(var54, 0, var75, var5, var42);
                                 var48 = 4;
                                 var52 = 1;
                              } else {
                                 var59 = this.readUnsignedShort(var75);
                                 var75 += 2;
                                 if (var58 == 247) {
                                    var75 = this.a(var54, 0, var75, var5, var42);
                                    var48 = 4;
                                    var52 = 1;
                                 } else if (var58 >= 248 && var58 < 251) {
                                    var48 = 2;
                                    var51 = 251 - var58;
                                    var50 -= var51;
                                    var52 = 0;
                                 } else if (var58 == 251) {
                                    var48 = 3;
                                    var52 = 0;
                                 } else if (var58 < 255) {
                                    var20 = var19 ? var50 : 0;

                                    for(var32 = var58 - 251; var32 > 0; --var32) {
                                       var75 = this.a(var53, var20++, var75, var5, var42);
                                    }

                                    var48 = 1;
                                    var51 = var58 - 251;
                                    var50 += var51;
                                    var52 = 0;
                                 } else {
                                    var48 = 0;
                                    var60 = var51 = var50 = this.readUnsignedShort(var75);
                                    var75 += 2;

                                    for(var20 = 0; var60 > 0; --var60) {
                                       var75 = this.a(var53, var20++, var75, var5, var42);
                                    }

                                    var60 = var52 = this.readUnsignedShort(var75);
                                    var75 += 2;

                                    for(var20 = 0; var60 > 0; --var60) {
                                       var75 = this.a(var54, var20++, var75, var5, var42);
                                    }
                                 }
                              }

                              var49 += var59 + 1;
                              this.readLabel(var49, var42);
                              --var47;
                           } else {
                              var53 = null;
                           }
                        }

                        var58 = var4[var12] & 255;
                        switch(ClassWriter.a[var58]) {
                        case 0:
                           var36.visitInsn(var58);
                           ++var12;
                           continue label632;
                        case 1:
                           var36.visitIntInsn(var58, var4[var12 + 1]);
                           var12 += 2;
                           continue label632;
                        case 2:
                           var36.visitIntInsn(var58, this.readShort(var12 + 1));
                           var12 += 3;
                           continue label632;
                        case 3:
                           var36.visitVarInsn(var58, var4[var12 + 1] & 255);
                           var12 += 2;
                           continue label632;
                        case 4:
                           if (var58 > 54) {
                              var58 -= 59;
                              var36.visitVarInsn(54 + (var58 >> 2), var58 & 3);
                           } else {
                              var58 -= 26;
                              var36.visitVarInsn(21 + (var58 >> 2), var58 & 3);
                           }

                           ++var12;
                           continue label632;
                        case 5:
                           var36.visitTypeInsn(var58, this.readClass(var12 + 1, var5));
                           var12 += 3;
                           continue label632;
                        case 6:
                        case 7:
                           int var64 = this.a[this.readUnsignedShort(var12 + 1)];
                           String var65;
                           if (var58 == 186) {
                              var65 = "java/lang/dyn/Dynamic";
                           } else {
                              var65 = this.readClass(var64, var5);
                              var64 = this.a[this.readUnsignedShort(var64 + 2)];
                           }

                           String var66 = this.readUTF8(var64, var5);
                           String var67 = this.readUTF8(var64 + 2, var5);
                           if (var58 < 182) {
                              var36.visitFieldInsn(var58, var65, var66, var67);
                           } else {
                              var36.visitMethodInsn(var58, var65, var66, var67);
                           }

                           if (var58 != 185 && var58 != 186) {
                              var12 += 3;
                              continue label632;
                           }

                           var12 += 5;
                           continue label632;
                        case 8:
                           var36.visitJumpInsn(var58, var42[var15 + this.readShort(var12 + 1)]);
                           var12 += 3;
                           continue label632;
                        case 9:
                           var36.visitJumpInsn(var58 - 33, var42[var15 + this.readInt(var12 + 1)]);
                           var12 += 5;
                           continue label632;
                        case 10:
                           var36.visitLdcInsn(this.readConst(var4[var12 + 1] & 255, var5));
                           var12 += 2;
                           continue label632;
                        case 11:
                           var36.visitLdcInsn(this.readConst(this.readUnsignedShort(var12 + 1), var5));
                           var12 += 3;
                           continue label632;
                        case 12:
                           var36.visitIincInsn(var4[var12 + 1] & 255, var4[var12 + 2]);
                           var12 += 3;
                           continue label632;
                        case 13:
                           var12 = var12 + 4 - (var15 & 3);
                           var56 = var15 + this.readInt(var12);
                           var59 = this.readInt(var12 + 4);
                           var60 = this.readInt(var12 + 8);
                           var12 += 12;
                           Label[] var61 = new Label[var60 - var59 + 1];

                           for(var20 = 0; var20 < var61.length; ++var20) {
                              var61[var20] = var42[var15 + this.readInt(var12)];
                              var12 += 4;
                           }

                           var36.visitTableSwitchInsn(var59, var60, var42[var56], var61);
                           continue label632;
                        case 14:
                           var12 = var12 + 4 - (var15 & 3);
                           var56 = var15 + this.readInt(var12);
                           var20 = this.readInt(var12 + 4);
                           var12 += 8;
                           int[] var62 = new int[var20];
                           Label[] var63 = new Label[var20];

                           for(var20 = 0; var20 < var62.length; ++var20) {
                              var62[var20] = this.readInt(var12);
                              var63[var20] = var42[var15 + this.readInt(var12 + 4)];
                              var12 += 8;
                           }

                           var36.visitLookupSwitchInsn(var42[var56], var62, var63);
                           continue label632;
                        case 15:
                        default:
                           var36.visitMultiANewArrayInsn(this.readClass(var12 + 1, var5), var4[var12 + 3] & 255);
                           var12 += 4;
                           continue label632;
                        case 16:
                           var58 = var4[var12 + 1] & 255;
                           if (var58 == 132) {
                              var36.visitIincInsn(this.readUnsignedShort(var12 + 2), this.readShort(var12 + 4));
                              var12 += 6;
                           } else {
                              var36.visitVarInsn(var58, this.readUnsignedShort(var12 + 2));
                              var12 += 4;
                           }
                           continue label632;
                        }
                     }
                  }

                  var78 = var42[var41 - var40];
                  if (var78 != null) {
                     var36.visitLabel(var78);
                  }

                  if (!var18 && var43 != 0) {
                     int[] var76 = null;
                     if (var74 != 0) {
                        var32 = this.readUnsignedShort(var74) * 3;
                        var15 = var74 + 2;

                        for(var76 = new int[var32]; var32 > 0; var15 += 10) {
                           --var32;
                           var76[var32] = var15 + 6;
                           --var32;
                           var76[var32] = this.readUnsignedShort(var15 + 8);
                           --var32;
                           var76[var32] = this.readUnsignedShort(var15);
                        }
                     }

                     var32 = this.readUnsignedShort(var43);

                     for(var15 = var43 + 2; var32 > 0; --var32) {
                        var59 = this.readUnsignedShort(var15);
                        var60 = this.readUnsignedShort(var15 + 2);
                        int var77 = this.readUnsignedShort(var15 + 8);
                        String var79 = null;
                        if (var76 != null) {
                           for(int var80 = 0; var80 < var76.length; var80 += 3) {
                              if (var76[var80] == var59 && var76[var80 + 1] == var77) {
                                 var79 = this.readUTF8(var76[var80 + 2], var5);
                                 break;
                              }
                           }
                        }

                        var36.visitLocalVariable(this.readUTF8(var15 + 4, var5), this.readUTF8(var15 + 6, var5), var79, var42[var59], var42[var59 + var60], var77);
                        var15 += 10;
                     }
                  }

                  while(var8 != null) {
                     var29 = var8.a;
                     var8.a = null;
                     var36.visitAttribute(var8);
                     var8 = var29;
                  }

                  var36.visitMaxs(var71, var72);
                  break label737;
               }
            }
         }

         if (var36 != null) {
            var36.visitEnd();
         }
      }

      var1.visitEnd();
   }

   private void a(int var1, String var2, char[] var3, boolean var4, MethodVisitor var5) {
      int var6 = this.b[var1++] & 255;
      int var7 = Type.getArgumentTypes(var2).length - var6;

      int var8;
      AnnotationVisitor var9;
      for(var8 = 0; var8 < var7; ++var8) {
         var9 = var5.visitParameterAnnotation(var8, "Ljava/lang/Synthetic;", false);
         if (var9 != null) {
            var9.visitEnd();
         }
      }

      while(var8 < var6 + var7) {
         int var10 = this.readUnsignedShort(var1);

         for(var1 += 2; var10 > 0; --var10) {
            var9 = var5.visitParameterAnnotation(var8, this.readUTF8(var1, var3), var4);
            var1 = this.a(var1 + 2, var3, true, var9);
         }

         ++var8;
      }

   }

   private int a(int var1, char[] var2, boolean var3, AnnotationVisitor var4) {
      int var5 = this.readUnsignedShort(var1);
      var1 += 2;
      if (var3) {
         while(var5 > 0) {
            var1 = this.a(var1 + 2, var2, this.readUTF8(var1, var2), var4);
            --var5;
         }
      } else {
         while(var5 > 0) {
            var1 = this.a(var1, var2, (String)null, var4);
            --var5;
         }
      }

      if (var4 != null) {
         var4.visitEnd();
      }

      return var1;
   }

   private int a(int var1, char[] var2, String var3, AnnotationVisitor var4) {
      if (var4 == null) {
         switch(this.b[var1] & 255) {
         case 64:
            return this.a(var1 + 3, var2, true, (AnnotationVisitor)null);
         case 91:
            return this.a(var1 + 1, var2, false, (AnnotationVisitor)null);
         case 101:
            return var1 + 5;
         default:
            return var1 + 3;
         }
      } else {
         switch(this.b[var1++] & 255) {
         case 64:
            var1 = this.a(var1 + 2, var2, true, var4.visitAnnotation(var3, this.readUTF8(var1, var2)));
         case 65:
         case 69:
         case 71:
         case 72:
         case 75:
         case 76:
         case 77:
         case 78:
         case 79:
         case 80:
         case 81:
         case 82:
         case 84:
         case 85:
         case 86:
         case 87:
         case 88:
         case 89:
         case 92:
         case 93:
         case 94:
         case 95:
         case 96:
         case 97:
         case 98:
         case 100:
         case 102:
         case 103:
         case 104:
         case 105:
         case 106:
         case 107:
         case 108:
         case 109:
         case 110:
         case 111:
         case 112:
         case 113:
         case 114:
         default:
            break;
         case 66:
            var4.visit(var3, new Byte((byte)this.readInt(this.a[this.readUnsignedShort(var1)])));
            var1 += 2;
            break;
         case 67:
            var4.visit(var3, new Character((char)this.readInt(this.a[this.readUnsignedShort(var1)])));
            var1 += 2;
            break;
         case 68:
         case 70:
         case 73:
         case 74:
            var4.visit(var3, this.readConst(this.readUnsignedShort(var1), var2));
            var1 += 2;
            break;
         case 83:
            var4.visit(var3, new Short((short)this.readInt(this.a[this.readUnsignedShort(var1)])));
            var1 += 2;
            break;
         case 90:
            var4.visit(var3, this.readInt(this.a[this.readUnsignedShort(var1)]) == 0 ? Boolean.FALSE : Boolean.TRUE);
            var1 += 2;
            break;
         case 91:
            int var5 = this.readUnsignedShort(var1);
            var1 += 2;
            if (var5 == 0) {
               return this.a(var1 - 2, var2, false, var4.visitArray(var3));
            }

            int var7;
            switch(this.b[var1++] & 255) {
            case 66:
               byte[] var6 = new byte[var5];

               for(var7 = 0; var7 < var5; ++var7) {
                  var6[var7] = (byte)this.readInt(this.a[this.readUnsignedShort(var1)]);
                  var1 += 3;
               }

               var4.visit(var3, var6);
               --var1;
               return var1;
            case 67:
               char[] var10 = new char[var5];

               for(var7 = 0; var7 < var5; ++var7) {
                  var10[var7] = (char)this.readInt(this.a[this.readUnsignedShort(var1)]);
                  var1 += 3;
               }

               var4.visit(var3, var10);
               --var1;
               return var1;
            case 68:
               double[] var14 = new double[var5];

               for(var7 = 0; var7 < var5; ++var7) {
                  var14[var7] = Double.longBitsToDouble(this.readLong(this.a[this.readUnsignedShort(var1)]));
                  var1 += 3;
               }

               var4.visit(var3, var14);
               --var1;
               return var1;
            case 69:
            case 71:
            case 72:
            case 75:
            case 76:
            case 77:
            case 78:
            case 79:
            case 80:
            case 81:
            case 82:
            case 84:
            case 85:
            case 86:
            case 87:
            case 88:
            case 89:
            default:
               var1 = this.a(var1 - 3, var2, false, var4.visitArray(var3));
               return var1;
            case 70:
               float[] var13 = new float[var5];

               for(var7 = 0; var7 < var5; ++var7) {
                  var13[var7] = Float.intBitsToFloat(this.readInt(this.a[this.readUnsignedShort(var1)]));
                  var1 += 3;
               }

               var4.visit(var3, var13);
               --var1;
               return var1;
            case 73:
               int[] var11 = new int[var5];

               for(var7 = 0; var7 < var5; ++var7) {
                  var11[var7] = this.readInt(this.a[this.readUnsignedShort(var1)]);
                  var1 += 3;
               }

               var4.visit(var3, var11);
               --var1;
               return var1;
            case 74:
               long[] var12 = new long[var5];

               for(var7 = 0; var7 < var5; ++var7) {
                  var12[var7] = this.readLong(this.a[this.readUnsignedShort(var1)]);
                  var1 += 3;
               }

               var4.visit(var3, var12);
               --var1;
               return var1;
            case 83:
               short[] var9 = new short[var5];

               for(var7 = 0; var7 < var5; ++var7) {
                  var9[var7] = (short)this.readInt(this.a[this.readUnsignedShort(var1)]);
                  var1 += 3;
               }

               var4.visit(var3, var9);
               --var1;
               return var1;
            case 90:
               boolean[] var8 = new boolean[var5];

               for(var7 = 0; var7 < var5; ++var7) {
                  var8[var7] = this.readInt(this.a[this.readUnsignedShort(var1)]) != 0;
                  var1 += 3;
               }

               var4.visit(var3, var8);
               --var1;
               return var1;
            }
         case 99:
            var4.visit(var3, Type.getType(this.readUTF8(var1, var2)));
            var1 += 2;
            break;
         case 101:
            var4.visitEnum(var3, this.readUTF8(var1, var2), this.readUTF8(var1 + 2, var2));
            var1 += 4;
            break;
         case 115:
            var4.visit(var3, this.readUTF8(var1, var2));
            var1 += 2;
         }

         return var1;
      }
   }

   private int a(Object[] var1, int var2, int var3, char[] var4, Label[] var5) {
      int var6 = this.b[var3++] & 255;
      switch(var6) {
      case 0:
         var1[var2] = Opcodes.TOP;
         break;
      case 1:
         var1[var2] = Opcodes.INTEGER;
         break;
      case 2:
         var1[var2] = Opcodes.FLOAT;
         break;
      case 3:
         var1[var2] = Opcodes.DOUBLE;
         break;
      case 4:
         var1[var2] = Opcodes.LONG;
         break;
      case 5:
         var1[var2] = Opcodes.NULL;
         break;
      case 6:
         var1[var2] = Opcodes.UNINITIALIZED_THIS;
         break;
      case 7:
         var1[var2] = this.readClass(var3, var4);
         var3 += 2;
         break;
      default:
         var1[var2] = this.readLabel(this.readUnsignedShort(var3), var5);
         var3 += 2;
      }

      return var3;
   }

   protected Label readLabel(int var1, Label[] var2) {
      if (var2[var1] == null) {
         var2[var1] = new Label();
      }

      return var2[var1];
   }

   private Attribute a(Attribute[] var1, String var2, int var3, int var4, char[] var5, int var6, Label[] var7) {
      for(int var8 = 0; var8 < var1.length; ++var8) {
         if (var1[var8].type.equals(var2)) {
            return var1[var8].read(this, var3, var4, var5, var6, var7);
         }
      }

      return (new Attribute(var2)).read(this, var3, var4, (char[])null, -1, (Label[])null);
   }

   public int getItem(int var1) {
      return this.a[var1];
   }

   public int readByte(int var1) {
      return this.b[var1] & 255;
   }

   public int readUnsignedShort(int var1) {
      byte[] var2 = this.b;
      return (var2[var1] & 255) << 8 | var2[var1 + 1] & 255;
   }

   public short readShort(int var1) {
      byte[] var2 = this.b;
      return (short)((var2[var1] & 255) << 8 | var2[var1 + 1] & 255);
   }

   public int readInt(int var1) {
      byte[] var2 = this.b;
      return (var2[var1] & 255) << 24 | (var2[var1 + 1] & 255) << 16 | (var2[var1 + 2] & 255) << 8 | var2[var1 + 3] & 255;
   }

   public long readLong(int var1) {
      long var2 = (long)this.readInt(var1);
      long var4 = (long)this.readInt(var1 + 4) & 4294967295L;
      return var2 << 32 | var4;
   }

   public String readUTF8(int var1, char[] var2) {
      int var3 = this.readUnsignedShort(var1);
      String var4 = this.c[var3];
      if (var4 != null) {
         return var4;
      } else {
         var1 = this.a[var3];
         return this.c[var3] = this.a(var1 + 2, this.readUnsignedShort(var1), var2);
      }
   }

   private String a(int var1, int var2, char[] var3) {
      int var4 = var1 + var2;
      byte[] var5 = this.b;
      int var6 = 0;
      byte var7 = 0;
      char var8 = 0;

      while(true) {
         while(var1 < var4) {
            byte var9 = var5[var1++];
            switch(var7) {
            case 0:
               int var10 = var9 & 255;
               if (var10 < 128) {
                  var3[var6++] = (char)var10;
               } else {
                  if (var10 < 224 && var10 > 191) {
                     var8 = (char)(var10 & 31);
                     var7 = 1;
                     continue;
                  }

                  var8 = (char)(var10 & 15);
                  var7 = 2;
               }
               break;
            case 1:
               var3[var6++] = (char)(var8 << 6 | var9 & 63);
               var7 = 0;
               break;
            case 2:
               var8 = (char)(var8 << 6 | var9 & 63);
               var7 = 1;
            }
         }

         return new String(var3, 0, var6);
      }
   }

   public String readClass(int var1, char[] var2) {
      return this.readUTF8(this.a[this.readUnsignedShort(var1)], var2);
   }

   public Object readConst(int var1, char[] var2) {
      int var3 = this.a[var1];
      switch(this.b[var3 - 1]) {
      case 3:
         return new Integer(this.readInt(var3));
      case 4:
         return new Float(Float.intBitsToFloat(this.readInt(var3)));
      case 5:
         return new Long(this.readLong(var3));
      case 6:
         return new Double(Double.longBitsToDouble(this.readLong(var3)));
      case 7:
         return Type.getObjectType(this.readUTF8(var3, var2));
      default:
         return this.readUTF8(var3, var2);
      }
   }
}

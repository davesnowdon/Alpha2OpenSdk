package org.codehaus.jackson.org.objectweb.asm;

public class ByteVector {
   byte[] a;
   int b;

   public ByteVector() {
      this.a = new byte[64];
   }

   public ByteVector(int var1) {
      this.a = new byte[var1];
   }

   public ByteVector putByte(int var1) {
      int var2 = this.b;
      if (var2 + 1 > this.a.length) {
         this.a(1);
      }

      this.a[var2++] = (byte)var1;
      this.b = var2;
      return this;
   }

   ByteVector a(int var1, int var2) {
      int var3 = this.b;
      if (var3 + 2 > this.a.length) {
         this.a(2);
      }

      byte[] var4 = this.a;
      var4[var3++] = (byte)var1;
      var4[var3++] = (byte)var2;
      this.b = var3;
      return this;
   }

   public ByteVector putShort(int var1) {
      int var2 = this.b;
      if (var2 + 2 > this.a.length) {
         this.a(2);
      }

      byte[] var3 = this.a;
      var3[var2++] = (byte)(var1 >>> 8);
      var3[var2++] = (byte)var1;
      this.b = var2;
      return this;
   }

   ByteVector b(int var1, int var2) {
      int var3 = this.b;
      if (var3 + 3 > this.a.length) {
         this.a(3);
      }

      byte[] var4 = this.a;
      var4[var3++] = (byte)var1;
      var4[var3++] = (byte)(var2 >>> 8);
      var4[var3++] = (byte)var2;
      this.b = var3;
      return this;
   }

   public ByteVector putInt(int var1) {
      int var2 = this.b;
      if (var2 + 4 > this.a.length) {
         this.a(4);
      }

      byte[] var3 = this.a;
      var3[var2++] = (byte)(var1 >>> 24);
      var3[var2++] = (byte)(var1 >>> 16);
      var3[var2++] = (byte)(var1 >>> 8);
      var3[var2++] = (byte)var1;
      this.b = var2;
      return this;
   }

   public ByteVector putLong(long var1) {
      int var3 = this.b;
      if (var3 + 8 > this.a.length) {
         this.a(8);
      }

      byte[] var4 = this.a;
      int var5 = (int)(var1 >>> 32);
      var4[var3++] = (byte)(var5 >>> 24);
      var4[var3++] = (byte)(var5 >>> 16);
      var4[var3++] = (byte)(var5 >>> 8);
      var4[var3++] = (byte)var5;
      var5 = (int)var1;
      var4[var3++] = (byte)(var5 >>> 24);
      var4[var3++] = (byte)(var5 >>> 16);
      var4[var3++] = (byte)(var5 >>> 8);
      var4[var3++] = (byte)var5;
      this.b = var3;
      return this;
   }

   public ByteVector putUTF8(String var1) {
      int var2 = var1.length();
      int var3 = this.b;
      if (var3 + 2 + var2 > this.a.length) {
         this.a(2 + var2);
      }

      byte[] var4 = this.a;
      var4[var3++] = (byte)(var2 >>> 8);
      var4[var3++] = (byte)var2;

      label67:
      for(int var5 = 0; var5 < var2; ++var5) {
         char var6 = var1.charAt(var5);
         if (var6 < 1 || var6 > 127) {
            int var7 = var5;

            int var8;
            for(var8 = var5; var8 < var2; ++var8) {
               var6 = var1.charAt(var8);
               if (var6 >= 1 && var6 <= 127) {
                  ++var7;
               } else if (var6 > 2047) {
                  var7 += 3;
               } else {
                  var7 += 2;
               }
            }

            var4[this.b] = (byte)(var7 >>> 8);
            var4[this.b + 1] = (byte)var7;
            if (this.b + 2 + var7 > var4.length) {
               this.b = var3;
               this.a(2 + var7);
               var4 = this.a;
            }

            var8 = var5;

            while(true) {
               if (var8 >= var2) {
                  break label67;
               }

               var6 = var1.charAt(var8);
               if (var6 >= 1 && var6 <= 127) {
                  var4[var3++] = (byte)var6;
               } else if (var6 > 2047) {
                  var4[var3++] = (byte)(224 | var6 >> 12 & 15);
                  var4[var3++] = (byte)(128 | var6 >> 6 & 63);
                  var4[var3++] = (byte)(128 | var6 & 63);
               } else {
                  var4[var3++] = (byte)(192 | var6 >> 6 & 31);
                  var4[var3++] = (byte)(128 | var6 & 63);
               }

               ++var8;
            }
         }

         var4[var3++] = (byte)var6;
      }

      this.b = var3;
      return this;
   }

   public ByteVector putByteArray(byte[] var1, int var2, int var3) {
      if (this.b + var3 > this.a.length) {
         this.a(var3);
      }

      if (var1 != null) {
         System.arraycopy(var1, var2, this.a, this.b, var3);
      }

      this.b += var3;
      return this;
   }

   private void a(int var1) {
      int var2 = 2 * this.a.length;
      int var3 = this.b + var1;
      byte[] var4 = new byte[var2 > var3 ? var2 : var3];
      System.arraycopy(this.a, 0, var4, 0, this.b);
      this.a = var4;
   }
}

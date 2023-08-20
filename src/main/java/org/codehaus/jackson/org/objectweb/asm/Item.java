package org.codehaus.jackson.org.objectweb.asm;

final class Item {
   int a;
   int b;
   int c;
   long d;
   String g;
   String h;
   String i;
   int j;
   Item k;

   Item() {
   }

   Item(int var1) {
      this.a = var1;
   }

   Item(int var1, Item var2) {
      this.a = var1;
      this.b = var2.b;
      this.c = var2.c;
      this.d = var2.d;
      this.g = var2.g;
      this.h = var2.h;
      this.i = var2.i;
      this.j = var2.j;
   }

   void a(int var1) {
      this.b = 3;
      this.c = var1;
      this.j = 2147483647 & this.b + var1;
   }

   void a(long var1) {
      this.b = 5;
      this.d = var1;
      this.j = 2147483647 & this.b + (int)var1;
   }

   void a(float var1) {
      this.b = 4;
      this.c = Float.floatToRawIntBits(var1);
      this.j = 2147483647 & this.b + (int)var1;
   }

   void a(double var1) {
      this.b = 6;
      this.d = Double.doubleToRawLongBits(var1);
      this.j = 2147483647 & this.b + (int)var1;
   }

   void a(int var1, String var2, String var3, String var4) {
      this.b = var1;
      this.g = var2;
      this.h = var3;
      this.i = var4;
      switch(var1) {
      case 1:
      case 7:
      case 8:
      case 13:
         this.j = 2147483647 & var1 + var2.hashCode();
         return;
      case 2:
      case 3:
      case 4:
      case 5:
      case 6:
      case 9:
      case 10:
      case 11:
      default:
         this.j = 2147483647 & var1 + var2.hashCode() * var3.hashCode() * var4.hashCode();
         return;
      case 12:
         this.j = 2147483647 & var1 + var2.hashCode() * var3.hashCode();
      }
   }

   boolean a(Item var1) {
      switch(this.b) {
      case 1:
      case 7:
      case 8:
      case 13:
         return var1.g.equals(this.g);
      case 2:
      case 9:
      case 10:
      case 11:
      default:
         return var1.g.equals(this.g) && var1.h.equals(this.h) && var1.i.equals(this.i);
      case 3:
      case 4:
         return var1.c == this.c;
      case 5:
      case 6:
      case 15:
         return var1.d == this.d;
      case 12:
         return var1.g.equals(this.g) && var1.h.equals(this.h);
      case 14:
         return var1.c == this.c && var1.g.equals(this.g);
      }
   }
}

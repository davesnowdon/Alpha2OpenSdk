package org.codehaus.jackson.sym;

public abstract class Name {
   protected final String _name;
   protected final int _hashCode;

   protected Name(String name, int hashCode) {
      this._name = name;
      this._hashCode = hashCode;
   }

   public String getName() {
      return this._name;
   }

   public abstract boolean equals(int var1);

   public abstract boolean equals(int var1, int var2);

   public abstract boolean equals(int[] var1, int var2);

   public String toString() {
      return this._name;
   }

   public final int hashCode() {
      return this._hashCode;
   }

   public boolean equals(Object o) {
      return o == this;
   }
}

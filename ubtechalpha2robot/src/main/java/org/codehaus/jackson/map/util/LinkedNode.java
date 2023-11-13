package org.codehaus.jackson.map.util;

public final class LinkedNode {
   final T _value;
   final LinkedNode _next;

   public LinkedNode(T value, LinkedNode next) {
      this._value = value;
      this._next = next;
   }

   public LinkedNode next() {
      return this._next;
   }

   public T value() {
      return this._value;
   }

   public static <ST> boolean contains(LinkedNode<ST> node, ST value) {
      while(node != null) {
         if (node.value() == value) {
            return true;
         }

         node = node.next();
      }

      return false;
   }
}

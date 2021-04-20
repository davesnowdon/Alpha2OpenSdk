package org.codehaus.jackson.map.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public final class ArrayBuilders {
   ArrayBuilders.BooleanBuilder _booleanBuilder = null;
   ArrayBuilders.ByteBuilder _byteBuilder = null;
   ArrayBuilders.ShortBuilder _shortBuilder = null;
   ArrayBuilders.IntBuilder _intBuilder = null;
   ArrayBuilders.LongBuilder _longBuilder = null;
   ArrayBuilders.FloatBuilder _floatBuilder = null;
   ArrayBuilders.DoubleBuilder _doubleBuilder = null;

   public ArrayBuilders() {
   }

   public ArrayBuilders.BooleanBuilder getBooleanBuilder() {
      if (this._booleanBuilder == null) {
         this._booleanBuilder = new ArrayBuilders.BooleanBuilder();
      }

      return this._booleanBuilder;
   }

   public ArrayBuilders.ByteBuilder getByteBuilder() {
      if (this._byteBuilder == null) {
         this._byteBuilder = new ArrayBuilders.ByteBuilder();
      }

      return this._byteBuilder;
   }

   public ArrayBuilders.ShortBuilder getShortBuilder() {
      if (this._shortBuilder == null) {
         this._shortBuilder = new ArrayBuilders.ShortBuilder();
      }

      return this._shortBuilder;
   }

   public ArrayBuilders.IntBuilder getIntBuilder() {
      if (this._intBuilder == null) {
         this._intBuilder = new ArrayBuilders.IntBuilder();
      }

      return this._intBuilder;
   }

   public ArrayBuilders.LongBuilder getLongBuilder() {
      if (this._longBuilder == null) {
         this._longBuilder = new ArrayBuilders.LongBuilder();
      }

      return this._longBuilder;
   }

   public ArrayBuilders.FloatBuilder getFloatBuilder() {
      if (this._floatBuilder == null) {
         this._floatBuilder = new ArrayBuilders.FloatBuilder();
      }

      return this._floatBuilder;
   }

   public ArrayBuilders.DoubleBuilder getDoubleBuilder() {
      if (this._doubleBuilder == null) {
         this._doubleBuilder = new ArrayBuilders.DoubleBuilder();
      }

      return this._doubleBuilder;
   }

   public static <T> HashSet<T> arrayToSet(T[] elements) {
      HashSet<T> result = new HashSet();
      if (elements != null) {
         Object[] arr$ = elements;
         int len$ = elements.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            T elem = arr$[i$];
            result.add(elem);
         }
      }

      return result;
   }

   public static <T> List<T> addToList(List<T> list, T element) {
      if (list == null) {
         list = new ArrayList();
      }

      ((List)list).add(element);
      return (List)list;
   }

   public static <T> T[] insertInList(T[] array, T element) {
      int len = array.length;
      T[] result = (Object[])((Object[])Array.newInstance(array.getClass().getComponentType(), len + 1));
      if (len > 0) {
         System.arraycopy(array, 0, result, 1, len);
      }

      result[0] = element;
      return result;
   }

   public static <T> T[] insertInListNoDup(T[] array, T element) {
      int len = array.length;

      for(int ix = 0; ix < len; ++ix) {
         if (array[ix] == element) {
            if (ix == 0) {
               return array;
            }

            T[] result = (Object[])((Object[])Array.newInstance(array.getClass().getComponentType(), len));
            System.arraycopy(array, 0, result, 1, ix);
            array[0] = element;
            return result;
         }
      }

      T[] result = (Object[])((Object[])Array.newInstance(array.getClass().getComponentType(), len + 1));
      if (len > 0) {
         System.arraycopy(array, 0, result, 1, len);
      }

      result[0] = element;
      return result;
   }

   public static <T> Iterator<T> arrayAsIterator(T[] array) {
      return new ArrayBuilders.ArrayIterator(array);
   }

   public static <T> Iterable<T> arrayAsIterable(T[] array) {
      return new ArrayBuilders.ArrayIterator(array);
   }

   private static final class ArrayIterator<T> implements Iterator<T>, Iterable<T> {
      private final T[] _array;
      private int _index;

      public ArrayIterator(T[] array) {
         this._array = array;
         this._index = 0;
      }

      public boolean hasNext() {
         return this._index < this._array.length;
      }

      public T next() {
         if (this._index >= this._array.length) {
            throw new NoSuchElementException();
         } else {
            return this._array[this._index++];
         }
      }

      public void remove() {
         throw new UnsupportedOperationException();
      }

      public Iterator<T> iterator() {
         return this;
      }
   }

   public static final class DoubleBuilder extends PrimitiveArrayBuilder<double[]> {
      public DoubleBuilder() {
      }

      public final double[] _constructArray(int len) {
         return new double[len];
      }
   }

   public static final class FloatBuilder extends PrimitiveArrayBuilder<float[]> {
      public FloatBuilder() {
      }

      public final float[] _constructArray(int len) {
         return new float[len];
      }
   }

   public static final class LongBuilder extends PrimitiveArrayBuilder<long[]> {
      public LongBuilder() {
      }

      public final long[] _constructArray(int len) {
         return new long[len];
      }
   }

   public static final class IntBuilder extends PrimitiveArrayBuilder<int[]> {
      public IntBuilder() {
      }

      public final int[] _constructArray(int len) {
         return new int[len];
      }
   }

   public static final class ShortBuilder extends PrimitiveArrayBuilder<short[]> {
      public ShortBuilder() {
      }

      public final short[] _constructArray(int len) {
         return new short[len];
      }
   }

   public static final class ByteBuilder extends PrimitiveArrayBuilder<byte[]> {
      public ByteBuilder() {
      }

      public final byte[] _constructArray(int len) {
         return new byte[len];
      }
   }

   public static final class BooleanBuilder extends PrimitiveArrayBuilder<boolean[]> {
      public BooleanBuilder() {
      }

      public final boolean[] _constructArray(int len) {
         return new boolean[len];
      }
   }
}

package org.codehaus.jackson.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import org.codehaus.jackson.io.NumberInput;

public final class TextBuffer {
   static final char[] NO_CHARS = new char[0];
   static final int MIN_SEGMENT_LEN = 1000;
   static final int MAX_SEGMENT_LEN = 262144;
   private final BufferRecycler _allocator;
   private char[] _inputBuffer;
   private int _inputStart;
   private int _inputLen;
   private ArrayList<char[]> _segments;
   private boolean _hasSegments = false;
   private int _segmentSize;
   private char[] _currentSegment;
   private int _currentSize;
   private String _resultString;
   private char[] _resultArray;

   public TextBuffer(BufferRecycler allocator) {
      this._allocator = allocator;
   }

   public void releaseBuffers() {
      if (this._allocator == null) {
         this.resetWithEmpty();
      } else if (this._currentSegment != null) {
         this.resetWithEmpty();
         char[] buf = this._currentSegment;
         this._currentSegment = null;
         this._allocator.releaseCharBuffer(BufferRecycler.CharBufferType.TEXT_BUFFER, buf);
      }

   }

   public void resetWithEmpty() {
      this._inputStart = -1;
      this._currentSize = 0;
      this._inputLen = 0;
      this._inputBuffer = null;
      this._resultString = null;
      this._resultArray = null;
      if (this._hasSegments) {
         this.clearSegments();
      }

   }

   public void resetWithShared(char[] buf, int start, int len) {
      this._resultString = null;
      this._resultArray = null;
      this._inputBuffer = buf;
      this._inputStart = start;
      this._inputLen = len;
      if (this._hasSegments) {
         this.clearSegments();
      }

   }

   public void resetWithCopy(char[] buf, int start, int len) {
      this._inputBuffer = null;
      this._inputStart = -1;
      this._inputLen = 0;
      this._resultString = null;
      this._resultArray = null;
      if (this._hasSegments) {
         this.clearSegments();
      } else if (this._currentSegment == null) {
         this._currentSegment = this.findBuffer(len);
      }

      this._currentSize = this._segmentSize = 0;
      this.append(buf, start, len);
   }

   public void resetWithString(String value) {
      this._inputBuffer = null;
      this._inputStart = -1;
      this._inputLen = 0;
      this._resultString = value;
      this._resultArray = null;
      if (this._hasSegments) {
         this.clearSegments();
      }

      this._currentSize = 0;
   }

   private final char[] findBuffer(int needed) {
      return this._allocator != null ? this._allocator.allocCharBuffer(BufferRecycler.CharBufferType.TEXT_BUFFER, needed) : new char[Math.max(needed, 1000)];
   }

   private final void clearSegments() {
      this._hasSegments = false;
      this._segments.clear();
      this._currentSize = this._segmentSize = 0;
   }

   public int size() {
      return this._inputStart >= 0 ? this._inputLen : this._segmentSize + this._currentSize;
   }

   public int getTextOffset() {
      return this._inputStart >= 0 ? this._inputStart : 0;
   }

   public char[] getTextBuffer() {
      if (this._inputStart >= 0) {
         return this._inputBuffer;
      } else {
         return !this._hasSegments ? this._currentSegment : this.contentsAsArray();
      }
   }

   public String contentsAsString() {
      if (this._resultString == null) {
         if (this._resultArray != null) {
            this._resultString = new String(this._resultArray);
         } else if (this._inputStart >= 0) {
            if (this._inputLen < 1) {
               return this._resultString = "";
            }

            this._resultString = new String(this._inputBuffer, this._inputStart, this._inputLen);
         } else {
            int segLen = this._segmentSize;
            int currLen = this._currentSize;
            if (segLen == 0) {
               this._resultString = currLen == 0 ? "" : new String(this._currentSegment, 0, currLen);
            } else {
               StringBuilder sb = new StringBuilder(segLen + currLen);
               if (this._segments != null) {
                  int i = 0;

                  for(int len = this._segments.size(); i < len; ++i) {
                     char[] curr = (char[])this._segments.get(i);
                     sb.append(curr, 0, curr.length);
                  }
               }

               sb.append(this._currentSegment, 0, this._currentSize);
               this._resultString = sb.toString();
            }
         }
      }

      return this._resultString;
   }

   public char[] contentsAsArray() {
      char[] result = this._resultArray;
      if (result == null) {
         this._resultArray = result = this.buildResultArray();
      }

      return result;
   }

   public BigDecimal contentsAsDecimal() throws NumberFormatException {
      if (this._resultArray != null) {
         return new BigDecimal(this._resultArray);
      } else if (this._inputStart >= 0) {
         return new BigDecimal(this._inputBuffer, this._inputStart, this._inputLen);
      } else {
         return this._segmentSize == 0 ? new BigDecimal(this._currentSegment, 0, this._currentSize) : new BigDecimal(this.contentsAsArray());
      }
   }

   public double contentsAsDouble() throws NumberFormatException {
      return NumberInput.parseDouble(this.contentsAsString());
   }

   public void ensureNotShared() {
      if (this._inputStart >= 0) {
         this.unshare(16);
      }

   }

   public void append(char c) {
      if (this._inputStart >= 0) {
         this.unshare(16);
      }

      this._resultString = null;
      this._resultArray = null;
      char[] curr = this._currentSegment;
      if (this._currentSize >= curr.length) {
         this.expand(1);
         curr = this._currentSegment;
      }

      curr[this._currentSize++] = c;
   }

   public void append(char[] c, int start, int len) {
      if (this._inputStart >= 0) {
         this.unshare(len);
      }

      this._resultString = null;
      this._resultArray = null;
      char[] curr = this._currentSegment;
      int max = curr.length - this._currentSize;
      if (max >= len) {
         System.arraycopy(c, start, curr, this._currentSize, len);
         this._currentSize += len;
      } else {
         if (max > 0) {
            System.arraycopy(c, start, curr, this._currentSize, max);
            start += max;
            len -= max;
         }

         this.expand(len);
         System.arraycopy(c, start, this._currentSegment, 0, len);
         this._currentSize = len;
      }

   }

   public void append(String str, int offset, int len) {
      if (this._inputStart >= 0) {
         this.unshare(len);
      }

      this._resultString = null;
      this._resultArray = null;
      char[] curr = this._currentSegment;
      int max = curr.length - this._currentSize;
      if (max >= len) {
         str.getChars(offset, offset + len, curr, this._currentSize);
         this._currentSize += len;
      } else {
         if (max > 0) {
            str.getChars(offset, offset + max, curr, this._currentSize);
            len -= max;
            offset += max;
         }

         this.expand(len);
         str.getChars(offset, offset + len, this._currentSegment, 0);
         this._currentSize = len;
      }

   }

   public char[] getCurrentSegment() {
      if (this._inputStart >= 0) {
         this.unshare(1);
      } else {
         char[] curr = this._currentSegment;
         if (curr == null) {
            this._currentSegment = this.findBuffer(0);
         } else if (this._currentSize >= curr.length) {
            this.expand(1);
         }
      }

      return this._currentSegment;
   }

   public final char[] emptyAndGetCurrentSegment() {
      this._inputStart = -1;
      this._currentSize = 0;
      this._inputLen = 0;
      this._inputBuffer = null;
      this._resultString = null;
      this._resultArray = null;
      if (this._hasSegments) {
         this.clearSegments();
      }

      char[] curr = this._currentSegment;
      if (curr == null) {
         this._currentSegment = curr = this.findBuffer(0);
      }

      return curr;
   }

   public int getCurrentSegmentSize() {
      return this._currentSize;
   }

   public void setCurrentLength(int len) {
      this._currentSize = len;
   }

   public char[] finishCurrentSegment() {
      if (this._segments == null) {
         this._segments = new ArrayList();
      }

      this._hasSegments = true;
      this._segments.add(this._currentSegment);
      int oldLen = this._currentSegment.length;
      this._segmentSize += oldLen;
      int newLen = Math.min(oldLen + (oldLen >> 1), 262144);
      char[] curr = this._charArray(newLen);
      this._currentSize = 0;
      this._currentSegment = curr;
      return curr;
   }

   public char[] expandCurrentSegment() {
      char[] curr = this._currentSegment;
      int len = curr.length;
      int newLen = len == 262144 ? 262145 : Math.min(262144, len + (len >> 1));
      this._currentSegment = this._charArray(newLen);
      System.arraycopy(curr, 0, this._currentSegment, 0, len);
      return this._currentSegment;
   }

   public String toString() {
      return this.contentsAsString();
   }

   private void unshare(int needExtra) {
      int sharedLen = this._inputLen;
      this._inputLen = 0;
      char[] inputBuf = this._inputBuffer;
      this._inputBuffer = null;
      int start = this._inputStart;
      this._inputStart = -1;
      int needed = sharedLen + needExtra;
      if (this._currentSegment == null || needed > this._currentSegment.length) {
         this._currentSegment = this.findBuffer(needed);
      }

      if (sharedLen > 0) {
         System.arraycopy(inputBuf, start, this._currentSegment, 0, sharedLen);
      }

      this._segmentSize = 0;
      this._currentSize = sharedLen;
   }

   private void expand(int minNewSegmentSize) {
      if (this._segments == null) {
         this._segments = new ArrayList();
      }

      char[] curr = this._currentSegment;
      this._hasSegments = true;
      this._segments.add(curr);
      this._segmentSize += curr.length;
      int oldLen = curr.length;
      int sizeAddition = oldLen >> 1;
      if (sizeAddition < minNewSegmentSize) {
         sizeAddition = minNewSegmentSize;
      }

      curr = this._charArray(Math.min(262144, oldLen + sizeAddition));
      this._currentSize = 0;
      this._currentSegment = curr;
   }

   private char[] buildResultArray() {
      if (this._resultString != null) {
         return this._resultString.toCharArray();
      } else {
         char[] result;
         if (this._inputStart >= 0) {
            if (this._inputLen < 1) {
               return NO_CHARS;
            }

            result = this._charArray(this._inputLen);
            System.arraycopy(this._inputBuffer, this._inputStart, result, 0, this._inputLen);
         } else {
            int size = this.size();
            if (size < 1) {
               return NO_CHARS;
            }

            int offset = 0;
            result = this._charArray(size);
            if (this._segments != null) {
               int i = 0;

               for(int len = this._segments.size(); i < len; ++i) {
                  char[] curr = (char[])((char[])this._segments.get(i));
                  int currLen = curr.length;
                  System.arraycopy(curr, 0, result, offset, currLen);
                  offset += currLen;
               }
            }

            System.arraycopy(this._currentSegment, 0, result, offset, this._currentSize);
         }

         return result;
      }
   }

   private final char[] _charArray(int len) {
      return new char[len];
   }
}

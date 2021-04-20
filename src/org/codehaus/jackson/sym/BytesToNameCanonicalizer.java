package org.codehaus.jackson.sym;

import java.util.Arrays;
import org.codehaus.jackson.util.InternCache;

public final class BytesToNameCanonicalizer {
   protected static final int DEFAULT_TABLE_SIZE = 64;
   protected static final int MAX_TABLE_SIZE = 65536;
   static final int MAX_ENTRIES_FOR_REUSE = 6000;
   static final int MIN_HASH_SIZE = 16;
   static final int INITIAL_COLLISION_LEN = 32;
   static final int LAST_VALID_BUCKET = 254;
   final BytesToNameCanonicalizer _parent;
   final boolean _intern;
   private int _count;
   private int _mainHashMask;
   private int[] _mainHash;
   private Name[] _mainNames;
   private BytesToNameCanonicalizer.Bucket[] _collList;
   private int _collCount;
   private int _collEnd;
   private transient boolean _needRehash;
   private boolean _mainHashShared;
   private boolean _mainNamesShared;
   private boolean _collListShared;

   public static BytesToNameCanonicalizer createRoot() {
      return new BytesToNameCanonicalizer(64, true);
   }

   public synchronized BytesToNameCanonicalizer makeChild(boolean canonicalize, boolean intern) {
      return new BytesToNameCanonicalizer(this, intern);
   }

   public void release() {
      if (this.maybeDirty() && this._parent != null) {
         this._parent.mergeChild(this);
         this.markAsShared();
      }

   }

   private BytesToNameCanonicalizer(int hashSize, boolean intern) {
      this._parent = null;
      this._intern = intern;
      if (hashSize < 16) {
         hashSize = 16;
      } else if ((hashSize & hashSize - 1) != 0) {
         int curr;
         for(curr = 16; curr < hashSize; curr += curr) {
         }

         hashSize = curr;
      }

      this.initTables(hashSize);
   }

   private BytesToNameCanonicalizer(BytesToNameCanonicalizer parent, boolean intern) {
      this._parent = parent;
      this._intern = intern;
      this._count = parent._count;
      this._mainHashMask = parent._mainHashMask;
      this._mainHash = parent._mainHash;
      this._mainNames = parent._mainNames;
      this._collList = parent._collList;
      this._collCount = parent._collCount;
      this._collEnd = parent._collEnd;
      this._needRehash = false;
      this._mainHashShared = true;
      this._mainNamesShared = true;
      this._collListShared = true;
   }

   private void initTables(int hashSize) {
      this._count = 0;
      this._mainHash = new int[hashSize];
      this._mainNames = new Name[hashSize];
      this._mainHashShared = false;
      this._mainNamesShared = false;
      this._mainHashMask = hashSize - 1;
      this._collListShared = true;
      this._collList = null;
      this._collEnd = 0;
      this._needRehash = false;
   }

   private synchronized void mergeChild(BytesToNameCanonicalizer child) {
      int childCount = child._count;
      if (childCount > this._count) {
         if (child.size() > 6000) {
            this.initTables(64);
         } else {
            this._count = child._count;
            this._mainHash = child._mainHash;
            this._mainNames = child._mainNames;
            this._mainHashShared = true;
            this._mainNamesShared = true;
            this._mainHashMask = child._mainHashMask;
            this._collList = child._collList;
            this._collCount = child._collCount;
            this._collEnd = child._collEnd;
         }

      }
   }

   private void markAsShared() {
      this._mainHashShared = true;
      this._mainNamesShared = true;
      this._collListShared = true;
   }

   public int size() {
      return this._count;
   }

   public boolean maybeDirty() {
      return !this._mainHashShared;
   }

   public static Name getEmptyName() {
      return Name1.getEmptyName();
   }

   public Name findName(int firstQuad) {
      int hash = calcHash(firstQuad);
      int ix = hash & this._mainHashMask;
      int val = this._mainHash[ix];
      if ((val >> 8 ^ hash) << 8 == 0) {
         Name name = this._mainNames[ix];
         if (name == null) {
            return null;
         }

         if (name.equals(firstQuad)) {
            return name;
         }
      } else if (val == 0) {
         return null;
      }

      val &= 255;
      if (val > 0) {
         --val;
         BytesToNameCanonicalizer.Bucket bucket = this._collList[val];
         if (bucket != null) {
            return bucket.find(hash, firstQuad, 0);
         }
      }

      return null;
   }

   public Name findName(int firstQuad, int secondQuad) {
      int hash = calcHash(firstQuad, secondQuad);
      int ix = hash & this._mainHashMask;
      int val = this._mainHash[ix];
      if ((val >> 8 ^ hash) << 8 == 0) {
         Name name = this._mainNames[ix];
         if (name == null) {
            return null;
         }

         if (name.equals(firstQuad, secondQuad)) {
            return name;
         }
      } else if (val == 0) {
         return null;
      }

      val &= 255;
      if (val > 0) {
         --val;
         BytesToNameCanonicalizer.Bucket bucket = this._collList[val];
         if (bucket != null) {
            return bucket.find(hash, firstQuad, secondQuad);
         }
      }

      return null;
   }

   public Name findName(int[] quads, int qlen) {
      int hash = calcHash(quads, qlen);
      int ix = hash & this._mainHashMask;
      int val = this._mainHash[ix];
      if ((val >> 8 ^ hash) << 8 == 0) {
         Name name = this._mainNames[ix];
         if (name == null || name.equals(quads, qlen)) {
            return name;
         }
      } else if (val == 0) {
         return null;
      }

      val &= 255;
      if (val > 0) {
         --val;
         BytesToNameCanonicalizer.Bucket bucket = this._collList[val];
         if (bucket != null) {
            return bucket.find(hash, quads, qlen);
         }
      }

      return null;
   }

   public Name addName(String symbolStr, int q1, int q2) {
      if (this._intern) {
         symbolStr = InternCache.instance.intern(symbolStr);
      }

      int hash = q2 == 0 ? calcHash(q1) : calcHash(q1, q2);
      Name symbol = constructName(hash, symbolStr, q1, q2);
      this._addSymbol(hash, symbol);
      return symbol;
   }

   public Name addName(String symbolStr, int[] quads, int qlen) {
      if (this._intern) {
         symbolStr = InternCache.instance.intern(symbolStr);
      }

      int hash = calcHash(quads, qlen);
      Name symbol = constructName(hash, symbolStr, quads, qlen);
      this._addSymbol(hash, symbol);
      return symbol;
   }

   public static final int calcHash(int firstQuad) {
      int hash = firstQuad ^ firstQuad >>> 16;
      hash ^= hash >>> 8;
      return hash;
   }

   public static final int calcHash(int firstQuad, int secondQuad) {
      int hash = firstQuad * 31 + secondQuad;
      hash ^= hash >>> 16;
      hash ^= hash >>> 8;
      return hash;
   }

   public static final int calcHash(int[] quads, int qlen) {
      int hash = quads[0];

      for(int i = 1; i < qlen; ++i) {
         hash = hash * 31 + quads[i];
      }

      hash ^= hash >>> 16;
      hash ^= hash >>> 8;
      return hash;
   }

   private void _addSymbol(int hash, Name symbol) {
      if (this._mainHashShared) {
         this.unshareMain();
      }

      if (this._needRehash) {
         this.rehash();
      }

      ++this._count;
      int ix = hash & this._mainHashMask;
      int entryValue;
      int hashQuarter;
      if (this._mainNames[ix] == null) {
         this._mainHash[ix] = hash << 8;
         if (this._mainNamesShared) {
            this.unshareNames();
         }

         this._mainNames[ix] = symbol;
      } else {
         if (this._collListShared) {
            this.unshareCollision();
         }

         ++this._collCount;
         entryValue = this._mainHash[ix];
         hashQuarter = entryValue & 255;
         if (hashQuarter == 0) {
            if (this._collEnd <= 254) {
               hashQuarter = this._collEnd++;
               if (hashQuarter >= this._collList.length) {
                  this.expandCollision();
               }
            } else {
               hashQuarter = this.findBestBucket();
            }

            this._mainHash[ix] = entryValue & -256 | hashQuarter + 1;
         } else {
            --hashQuarter;
         }

         this._collList[hashQuarter] = new BytesToNameCanonicalizer.Bucket(symbol, this._collList[hashQuarter]);
      }

      entryValue = this._mainHash.length;
      if (this._count > entryValue >> 1) {
         hashQuarter = entryValue >> 2;
         if (this._count > entryValue - hashQuarter) {
            this._needRehash = true;
         } else if (this._collCount >= hashQuarter) {
            this._needRehash = true;
         }
      }

   }

   private void rehash() {
      this._needRehash = false;
      this._mainNamesShared = false;
      int[] oldMainHash = this._mainHash;
      int len = oldMainHash.length;
      int newLen = len + len;
      if (newLen > 65536) {
         this.nukeSymbols();
      } else {
         this._mainHash = new int[newLen];
         this._mainHashMask = newLen - 1;
         Name[] oldNames = this._mainNames;
         this._mainNames = new Name[newLen];
         int symbolsSeen = 0;

         int oldEnd;
         int i;
         for(oldEnd = 0; oldEnd < len; ++oldEnd) {
            Name symbol = oldNames[oldEnd];
            if (symbol != null) {
               ++symbolsSeen;
               i = symbol.hashCode();
               int ix = i & this._mainHashMask;
               this._mainNames[ix] = symbol;
               this._mainHash[ix] = i << 8;
            }
         }

         oldEnd = this._collEnd;
         if (oldEnd != 0) {
            this._collCount = 0;
            this._collEnd = 0;
            this._collListShared = false;
            BytesToNameCanonicalizer.Bucket[] oldBuckets = this._collList;
            this._collList = new BytesToNameCanonicalizer.Bucket[oldBuckets.length];

            for(i = 0; i < oldEnd; ++i) {
               for(BytesToNameCanonicalizer.Bucket curr = oldBuckets[i]; curr != null; curr = curr._next) {
                  ++symbolsSeen;
                  Name symbol = curr._name;
                  int hash = symbol.hashCode();
                  int ix = hash & this._mainHashMask;
                  int val = this._mainHash[ix];
                  if (this._mainNames[ix] == null) {
                     this._mainHash[ix] = hash << 8;
                     this._mainNames[ix] = symbol;
                  } else {
                     ++this._collCount;
                     int bucket = val & 255;
                     if (bucket == 0) {
                        if (this._collEnd <= 254) {
                           bucket = this._collEnd++;
                           if (bucket >= this._collList.length) {
                              this.expandCollision();
                           }
                        } else {
                           bucket = this.findBestBucket();
                        }

                        this._mainHash[ix] = val & -256 | bucket + 1;
                     } else {
                        --bucket;
                     }

                     this._collList[bucket] = new BytesToNameCanonicalizer.Bucket(symbol, this._collList[bucket]);
                  }
               }
            }

            if (symbolsSeen != this._count) {
               throw new RuntimeException("Internal error: count after rehash " + symbolsSeen + "; should be " + this._count);
            }
         }
      }
   }

   private void nukeSymbols() {
      this._count = 0;
      Arrays.fill(this._mainHash, 0);
      Arrays.fill(this._mainNames, (Object)null);
      Arrays.fill(this._collList, (Object)null);
      this._collCount = 0;
      this._collEnd = 0;
   }

   private int findBestBucket() {
      BytesToNameCanonicalizer.Bucket[] buckets = this._collList;
      int bestCount = 2147483647;
      int bestIx = -1;
      int i = 0;

      for(int len = this._collEnd; i < len; ++i) {
         int count = buckets[i].length();
         if (count < bestCount) {
            if (count == 1) {
               return i;
            }

            bestCount = count;
            bestIx = i;
         }
      }

      return bestIx;
   }

   private void unshareMain() {
      int[] old = this._mainHash;
      int len = this._mainHash.length;
      this._mainHash = new int[len];
      System.arraycopy(old, 0, this._mainHash, 0, len);
      this._mainHashShared = false;
   }

   private void unshareCollision() {
      BytesToNameCanonicalizer.Bucket[] old = this._collList;
      if (old == null) {
         this._collList = new BytesToNameCanonicalizer.Bucket[32];
      } else {
         int len = old.length;
         this._collList = new BytesToNameCanonicalizer.Bucket[len];
         System.arraycopy(old, 0, this._collList, 0, len);
      }

      this._collListShared = false;
   }

   private void unshareNames() {
      Name[] old = this._mainNames;
      int len = old.length;
      this._mainNames = new Name[len];
      System.arraycopy(old, 0, this._mainNames, 0, len);
      this._mainNamesShared = false;
   }

   private void expandCollision() {
      BytesToNameCanonicalizer.Bucket[] old = this._collList;
      int len = old.length;
      this._collList = new BytesToNameCanonicalizer.Bucket[len + len];
      System.arraycopy(old, 0, this._collList, 0, len);
   }

   private static Name constructName(int hash, String name, int q1, int q2) {
      return (Name)(q2 == 0 ? new Name1(name, hash, q1) : new Name2(name, hash, q1, q2));
   }

   private static Name constructName(int hash, String name, int[] quads, int qlen) {
      if (qlen < 4) {
         switch(qlen) {
         case 1:
            return new Name1(name, hash, quads[0]);
         case 2:
            return new Name2(name, hash, quads[0], quads[1]);
         case 3:
            return new Name3(name, hash, quads[0], quads[1], quads[2]);
         }
      }

      int[] buf = new int[qlen];

      for(int i = 0; i < qlen; ++i) {
         buf[i] = quads[i];
      }

      return new NameN(name, hash, buf, qlen);
   }

   static final class Bucket {
      protected final Name _name;
      protected final BytesToNameCanonicalizer.Bucket _next;

      Bucket(Name name, BytesToNameCanonicalizer.Bucket next) {
         this._name = name;
         this._next = next;
      }

      public int length() {
         int len = 1;

         for(BytesToNameCanonicalizer.Bucket curr = this._next; curr != null; curr = curr._next) {
            ++len;
         }

         return len;
      }

      public Name find(int hash, int firstQuad, int secondQuad) {
         if (this._name.hashCode() == hash && this._name.equals(firstQuad, secondQuad)) {
            return this._name;
         } else {
            for(BytesToNameCanonicalizer.Bucket curr = this._next; curr != null; curr = curr._next) {
               Name currName = curr._name;
               if (currName.hashCode() == hash && currName.equals(firstQuad, secondQuad)) {
                  return currName;
               }
            }

            return null;
         }
      }

      public Name find(int hash, int[] quads, int qlen) {
         if (this._name.hashCode() == hash && this._name.equals(quads, qlen)) {
            return this._name;
         } else {
            for(BytesToNameCanonicalizer.Bucket curr = this._next; curr != null; curr = curr._next) {
               Name currName = curr._name;
               if (currName.hashCode() == hash && currName.equals(quads, qlen)) {
                  return currName;
               }
            }

            return null;
         }
      }
   }
}

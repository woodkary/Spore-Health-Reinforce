package com.Harbinger.Spore.ExtremelySusThings;

import java.util.BitSet;
import java.util.function.Predicate;

public class BooleanCache {
   public final boolean NullFlag;
   private final int CACHE_MEMORY;
   private final BitSet validityCache;
   private final int[] entityHashCache;
   private int cacheIndex = 0;
   private final Predicate TestPredicate;

   public BooleanCache(int memorySize, Predicate testPredicate, boolean nullFlag) {
      this.CACHE_MEMORY = memorySize;
      this.validityCache = new BitSet(this.CACHE_MEMORY);
      this.entityHashCache = new int[this.CACHE_MEMORY];
      this.TestPredicate = testPredicate;
      this.NullFlag = nullFlag;
   }

   public BooleanCache(int memorySize, Predicate testPredicate) {
      this.CACHE_MEMORY = memorySize;
      this.validityCache = new BitSet(this.CACHE_MEMORY);
      this.entityHashCache = new int[this.CACHE_MEMORY];
      this.TestPredicate = testPredicate;
      this.NullFlag = false;
   }

   public boolean Test(Object t) {
      int hash = System.identityHashCode(t);
      if (hash != 0 && t != null) {
         for(int i = 0; i < this.CACHE_MEMORY; ++i) {
            if (this.entityHashCache[i] == hash) {
               return this.validityCache.get(i);
            }
         }

         boolean flag = this.TestPredicate.test(t);
         this.UpdateCache(flag, hash);
         return flag;
      } else {
         return this.NullFlag;
      }
   }

   private void UpdateCache(boolean result, int hash) {
      this.entityHashCache[this.cacheIndex] = hash;
      this.validityCache.set(this.cacheIndex, result);
      this.cacheIndex = (this.cacheIndex + 1) % this.CACHE_MEMORY;
   }
}

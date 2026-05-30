package com.Harbinger.Spore.Sentities;

public interface VariantKeeper {
   int getTypeVariant();

   void setVariant(int var1);

   default void increaseVariant() {
      this.setVariant(this.getTypeVariant() + 1);
   }

   int amountOfMutations();
}

package com.Harbinger.Spore.Client.Special;

import java.util.List;
import net.minecraft.client.model.geom.ModelPart;

public interface ProtectorBits {
   List<ModelPart> EnderPearlArm();

   List<ModelPart> Helmet();

   List<ModelPart> Feet();

   List<ModelPart> Armor();

   ModelPart root();

   default void setDraw(ModelPart part, boolean armor) {
      if (this.Armor().contains(part)) {
         part.skipDraw = !armor;
      } else {
         part.skipDraw = armor;
      }

   }
}

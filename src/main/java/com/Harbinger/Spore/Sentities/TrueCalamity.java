package com.Harbinger.Spore.Sentities;

import com.Harbinger.Spore.Sentities.BaseEntities.CalamityMultipart;
import java.util.List;
import net.minecraft.world.damagesource.DamageSource;

public interface TrueCalamity {
   boolean hurt(CalamityMultipart var1, DamageSource var2, float var3);

   int chemicalRange();

   List buffs();

   List debuffs();
}

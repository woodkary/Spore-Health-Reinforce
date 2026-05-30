package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.Hyper;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import com.Harbinger.Spore.Sitems.BaseWeapons.LootModifierWeapon;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

public class Reaver extends SwordItem implements LootModifierWeapon {
   private final List<ComboValues> basicInfectedList;
   private final List<ComboValues> evolvedList;
   private final List<ComboValues> hyperList;
   private final List<ComboValues> organoidList;
   private final List<ComboValues> calamityList;

   public Reaver() {
      super(new Tier() {
         public int getUses() {
            return (Integer)SConfig.SERVER.reaver_durability.get();
         }

         public float getSpeed() {
            return -2.0F;
         }

         public float getAttackDamageBonus() {
            return (float)((Integer)SConfig.SERVER.reaver_damage.get() - 1);
         }

         public int getLevel() {
            return 3;
         }

         public int getEnchantmentValue() {
            return 3;
         }

         public Ingredient getRepairIngredient() {
            return Ingredient.of(new ItemLike[]{(ItemLike)Sitems.COMPOUND_PLATE.get()});
         }
      }, 0, -1.0F, new Properties());
      Sitems.TECHNOLOGICAL_ITEMS.add(this);
      this.basicInfectedList = this.calculateMap((List<String>)SConfig.SERVER.reaver_loot.get());
      this.evolvedList = this.calculateMap((List<String>)SConfig.SERVER.reaver_loot1.get());
      this.hyperList = this.calculateMap((List<String>)SConfig.SERVER.reaver_loot2.get());
      this.organoidList = this.calculateMap((List<String>)SConfig.SERVER.reaver_loot3.get());
      this.calamityList = this.calculateMap((List<String>)SConfig.SERVER.reaver_loot4.get());
   }

   public List<ComboValues> calculateMap(List<String> list) {
      List<ComboValues> values = new ArrayList();

      for(String string : list) {
         String[] s = string.split("\\|");
         Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(s[0]));
         int value = Integer.parseInt(s[1]);
         if (item != null && value != 0) {
            ItemStack stack = new ItemStack(item);
            ComboValues comboValues = new ComboValues(stack, value);
            values.add(comboValues);
         }
      }

      return values;
   }

   public int getLootingLevel() {
      return 3;
   }

   public boolean hurtEnemy(ItemStack stack, LivingEntity livingEntity, LivingEntity victim) {
      if (livingEntity instanceof Calamity) {
         return this.shaveLoot(stack, livingEntity, victim, this.getRandomFromList(this.calamityList));
      } else if (livingEntity instanceof Organoid) {
         return this.shaveLoot(stack, livingEntity, victim, this.getRandomFromList(this.organoidList));
      } else if (livingEntity instanceof Hyper) {
         return this.shaveLoot(stack, livingEntity, victim, this.getRandomFromList(this.hyperList));
      } else if (livingEntity instanceof EvolvedInfected) {
         return this.shaveLoot(stack, livingEntity, victim, this.getRandomFromList(this.evolvedList));
      } else {
         return livingEntity instanceof Infected ? this.shaveLoot(stack, livingEntity, victim, this.getRandomFromList(this.basicInfectedList)) : super.hurtEnemy(stack, livingEntity, victim);
      }
   }

   public ComboValues getRandomFromList(List<ComboValues> values) {
      RandomSource source = RandomSource.create();
      return values.get(source.nextInt(values.size()));
   }

   public boolean shaveLoot(ItemStack stack, LivingEntity livingEntity, LivingEntity victim, ComboValues values) {
      Level level = livingEntity.level();
      BlockPos pos = livingEntity.getOnPos();
      if (!level.isClientSide && values != null && Math.random() < (double)values.value * 0.01) {
         ItemEntity item = new ItemEntity(level, (double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), values.stack);
         level.addFreshEntity(item);
         livingEntity.playSound((SoundEvent)Ssounds.REAVER_REAVE.get());
      }

      return super.hurtEnemy(stack, livingEntity, victim);
   }

   public static record ComboValues(ItemStack stack, Integer value) {
   }
}

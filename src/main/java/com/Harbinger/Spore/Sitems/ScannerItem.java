package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.ExtremelySusThings.SporePacketHandler;
import com.Harbinger.Spore.ExtremelySusThings.Package.AdvancementGivingPackage;
import com.Harbinger.Spore.Sentities.BaseEntities.Calamity;
import com.Harbinger.Spore.Sentities.BaseEntities.EvolvedInfected;
import com.Harbinger.Spore.Sentities.BaseEntities.Experiment;
import com.Harbinger.Spore.Sentities.BaseEntities.Hyper;
import com.Harbinger.Spore.Sentities.BaseEntities.Infected;
import com.Harbinger.Spore.Sentities.BaseEntities.Organoid;
import com.Harbinger.Spore.Sentities.BaseEntities.UtilityEntity;
import com.Harbinger.Spore.Sentities.EvolvedInfected.Scamper;
import com.Harbinger.Spore.Sentities.Utility.Illusion;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class ScannerItem extends BaseItem2 {
   public ScannerItem(Properties properties) {
      super(properties);
   }

   public int getUseDuration(ItemStack p_43419_) {
      return 72000;
   }

   public InteractionResultHolder use(Level level, Player player, InteractionHand hand) {
      ItemStack tool = player.getItemInHand(hand);
      if (!level.isClientSide) {
         LivingEntity victim = this.getScannedEntity(player, level);
         if (victim != null) {
            player.playNotifySound((SoundEvent)Ssounds.SCANNER_MOB.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
            this.showInfo(tool, victim, player);
         } else {
            player.playNotifySound((SoundEvent)Ssounds.SCANNER_EMPTY.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
         }

         player.getCooldowns().addCooldown(this, 20);
      }

      return InteractionResultHolder.success(tool);
   }

   @Nullable
   public LivingEntity getScannedEntity(Player player, Level level) {
      AABB hitbox1 = this.getScannerHitBox(player, (double)32.0F);
      List<Entity> list = level.getEntities(player, hitbox1, (entityx) -> entityx instanceof LivingEntity);
      Iterator<Entity> var5 = list.iterator();
      double value = Double.MAX_VALUE;
      LivingEntity entity = null;

      while(var5.hasNext()) {
         LivingEntity $$3 = (LivingEntity)var5.next();
         if (player.distanceToSqr($$3) < value) {
            entity = $$3;
            value = player.distanceToSqr($$3);
         }
      }

      return entity;
   }

   @Nullable
   public AABB getScannerHitBox(Player player, double range) {
      Vec3 lookVec = player.getLookAngle();
      Vec3 endVec = player.position().add(lookVec.scale(range));
      return (new AABB(player.position(), endVec)).inflate((double)1.0F);
   }

   public void showInfo(ItemStack stack, LivingEntity entity, Player player) {
      if (stack.getItem() instanceof ScannerItem) {
         ItemStack offhand = player.getOffhandItem();
         boolean writeToBook = offhand.is(Items.WRITABLE_BOOK);
         List<String> lines = new ArrayList();
         if (entity instanceof Illusion) {
            this.sendScanMessage(player, lines, "spore.scanner.line.15");
         } else {
            entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40));
            String name = entity.getCustomName() != null ? entity.getCustomName().getString() : Component.translatable("spore.scanner.line.0").getString();
            lines.add("------------------");
            String var10001 = Component.translatable("spore.scanner.line.2").getString();
            lines.add(var10001 + name);
            var10001 = Component.translatable("spore.scanner.line.3").getString();
            lines.add(var10001 + Component.translatable(entity.getType().getDescriptionId()).getString());
            var10001 = Component.translatable("spore.scanner.line.4").getString();
            lines.add(var10001 + entity.getHealth() + "/" + entity.getMaxHealth());
            this.addExtraInfo(entity, lines);
            String danger = this.showThreatLevel(entity);
            if (danger != null) {
               lines.add("------------------");
               var10001 = Component.translatable("spore.scanner.line.5").getString();
               lines.add(var10001 + Component.translatable(danger).getString());
            }

            this.addDrops(entity, lines);
            if (writeToBook) {
               this.writeLinesToBook(offhand, lines);
            } else {
               for(String line : lines) {
                  player.displayClientMessage(Component.literal(line), false);
               }
            }

         }
      }
   }

   public String showThreatLevel(LivingEntity living) {
      if (living instanceof Calamity) {
         return "spore.scanner.danger.calamity";
      } else if (living instanceof Hyper) {
         return "spore.scanner.danger.hyper";
      } else if (living instanceof EvolvedInfected) {
         return "spore.scanner.danger.evolved";
      } else if (living instanceof Experiment) {
         return "spore.scanner.danger.experiment";
      } else if (living instanceof Infected) {
         return "spore.scanner.danger.infected";
      } else if (living instanceof Organoid) {
         return "spore.scanner.danger.organoid";
      } else {
         return living instanceof UtilityEntity ? "spore.scanner.danger.utility" : null;
      }
   }

   private void sendScanMessage(Player player, List<String> lines, String key) {
      String msg = Component.translatable(key).getString();
      lines.add(msg);
      player.displayClientMessage(Component.literal(msg), false);
   }

   private void addExtraInfo(LivingEntity entity, List<String> lines) {
      if (entity instanceof Infected infected) {
         String var10001 = Component.translatable("spore.scanner.line.6").getString();
         lines.add(var10001 + infected.getKills());
         var10001 = Component.translatable("spore.scanner.line.7").getString();
         lines.add(var10001 + infected.getEvoPoints());
         if (infected.getEvolutionCoolDown() > 0) {
            var10001 = Component.translatable("spore.scanner.line.8").getString();
            lines.add(var10001 + infected.getEvolutionCoolDown() + "/" + SConfig.SERVER.evolution_age_human.get());
         }

         if (infected.getHunger() > 0) {
            var10001 = Component.translatable("spore.scanner.line.9").getString();
            lines.add(var10001 + infected.getHunger() + "/" + SConfig.SERVER.hunger.get());
         }

         var10001 = Component.translatable("spore.scanner.line.10").getString();
         lines.add(var10001 + infected.getLinked());
         if (infected.getMutation() != null) {
            lines.add(Component.translatable(infected.getMutation()).getString());
         }

         if (infected instanceof Scamper scamper) {
            var10001 = Component.translatable("spore.scanner.line.scamper").getString();
            lines.add(var10001 + scamper.getAge() + "/" + SConfig.SERVER.scamper_age.get());
         }
      }

   }

   private void addDrops(LivingEntity living, List<String> lines) {
      List<? extends String> itemDrops = null;
      if (living instanceof Infected infected) {
         itemDrops = infected.getDropList();
      } else if (living instanceof UtilityEntity utilityEntity) {
         itemDrops = utilityEntity.getDropList();
      }

      if (itemDrops != null && !itemDrops.isEmpty()) {
         lines.add("------------------");
         lines.add(Component.translatable("spore.scanner.line.14").getString());

         for(String string : itemDrops) {
            String[] split = string.split("\\|");
            Item item = (Item)ForgeRegistries.ITEMS.getValue(new ResourceLocation(split[0]));
            if (item != null) {
               lines.add(Component.translatable(item.getDescriptionId()).getString());
            }
         }
      } else {
         lines.add(Component.translatable("spore.scanner.line.13").getString());
      }

   }

   private void writeLinesToBook(ItemStack bookStack, List<String> lines) {
      CompoundTag nbt = bookStack.getOrCreateTag();
      ListTag nbtPages = nbt.contains("pages", 9) ? nbt.getList("pages", 8) : new ListTag();
      List<String> pages = new ArrayList();

      for(int i = 0; i < nbtPages.size(); ++i) {
         pages.add(nbtPages.getString(i));
      }

      StringBuilder lastPage = pages.isEmpty() ? new StringBuilder() : new StringBuilder((String)pages.remove(pages.size() - 1));

      for(String line : lines) {
         if (lastPage.length() + line.length() + 1 > 256) {
            pages.add(lastPage.toString());
            lastPage = new StringBuilder();
         }

         lastPage.append(line).append("\n");
      }

      if (!lastPage.isEmpty()) {
         pages.add(lastPage.toString());
      }

      if (pages.size() > 50) {
         pages = pages.subList(pages.size() - 50, pages.size());
      }

      ListTag newPagesTag = new ListTag();

      for(String pageContent : pages) {
         newPagesTag.add(StringTag.valueOf(pageContent));
      }

      nbt.put("pages", newPagesTag);
   }

   public boolean overrideStackedOnOther(@NotNull ItemStack stack, Slot slot, @NotNull ClickAction clickAction, @NotNull Player player) {
      ItemStack itemStack = slot.getItem();
      Item var7 = itemStack.getItem();
      if (var7 instanceof OrganItem organItem) {
         if (clickAction == ClickAction.SECONDARY) {
            player.playNotifySound((SoundEvent)Ssounds.SCANNER_ITEM.get(), SoundSource.AMBIENT, 1.0F, 1.0F);
            if (organItem.getAdvancementIds() == null) {
               return false;
            }

            SporePacketHandler.sendToServer(new AdvancementGivingPackage(organItem.getAdvancementIds(), player.getId()));
            return true;
         }
      }

      return false;
   }
}

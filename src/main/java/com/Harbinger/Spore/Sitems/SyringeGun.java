package com.Harbinger.Spore.Sitems;

import com.Harbinger.Spore.Client.AnimationTrackers.SGAnimationTracker;
import com.Harbinger.Spore.Client.AnimationTrackers.SGReloadAnimationTracker;
import com.Harbinger.Spore.Core.SConfig;
import com.Harbinger.Spore.Core.Sitems;
import com.Harbinger.Spore.Core.Ssounds;
import com.Harbinger.Spore.Sentities.Projectile.SyringeProjectile;
import com.Harbinger.Spore.Sitems.Agents.ArmorSyringe;
import com.Harbinger.Spore.Sitems.Agents.WeaponSyringe;
import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.Vanishable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

public class SyringeGun extends BaseItem2 implements CustomModelArmorData, Vanishable, GunHeldItem {
   private static final ResourceLocation TEXTURE = new ResourceLocation("spore:textures/item/syringe_gun.png");
   private static final String DATA = "data";
   public static final List<Item> AMMO;

   public SyringeGun() {
      super((new Properties()).stacksTo(1).durability((Integer)SConfig.SERVER.syringe_durability.get()));
   }

   public ResourceLocation getTextureLocation() {
      return TEXTURE;
   }

   public UseAnim getUseAnimation(ItemStack stack) {
      return UseAnim.NONE;
   }

   public boolean onEntitySwing(ItemStack stack, LivingEntity entity) {
      return true;
   }

   public int getUseDuration(ItemStack stack) {
      return 72000;
   }

   private boolean isValidAmmo(ItemStack stack) {
      return !stack.isEmpty() && AMMO.contains(stack.getItem());
   }

   private NonNullList<ItemStack> getMagazine(ItemStack gun) {
      NonNullList<ItemStack> magazine = NonNullList.withSize(4, ItemStack.EMPTY);
      CompoundTag tag = gun.getOrCreateTag();
      if (tag.contains("Magazines", 9)) {
         ListTag magTag = tag.getList("Magazines", 10);

         for(int i = 0; i < 4 && i < magTag.size(); ++i) {
            magazine.set(i, ItemStack.of(magTag.getCompound(i)));
         }
      }

      return magazine;
   }

   public NonNullList<Integer> getClip(ItemStack gun) {
      NonNullList<Integer> clip = NonNullList.withSize(4, 0);
      CompoundTag tag = gun.getOrCreateTag();
      if (tag.contains("Clip", 9)) {
         ListTag clipTag = tag.getList("Clip", 3);

         for(int i = 0; i < 4 && i < clipTag.size(); ++i) {
            clip.set(i, ((IntTag)clipTag.get(i)).getAsInt());
         }
      }

      return clip;
   }

   private void saveMagazineAndClip(ItemStack gun, NonNullList<ItemStack> magazine, NonNullList<Integer> clip) {
      CompoundTag tag = gun.getOrCreateTag();
      ListTag magTag = new ListTag();

      for(ItemStack ammo : magazine) {
         CompoundTag entry = new CompoundTag();
         if (!ammo.isEmpty()) {
            ammo.save(entry);
         }

         magTag.add(entry);
      }

      tag.put("Magazines", magTag);
      ListTag clipTag = new ListTag();

      for(Integer c : clip) {
         clipTag.add(IntTag.valueOf(c));
      }

      tag.put("Clip", clipTag);
   }

   public void setMagazine(ItemStack gun, ItemStack ammo, int slot) {
      NonNullList<ItemStack> magazine = this.getMagazine(gun);
      NonNullList<Integer> clip = this.getClip(gun);
      if (this.isValidAmmo(ammo)) {
         magazine.set(slot, ammo.copy());
         clip.set(slot, this.encodeColors(ammo));
      }

      this.saveMagazineAndClip(gun, magazine, clip);
   }

   public void removeMagazine(ItemStack gun, int slot) {
      NonNullList<ItemStack> magazine = this.getMagazine(gun);
      NonNullList<Integer> clip = this.getClip(gun);
      magazine.set(slot, ItemStack.EMPTY);
      clip.set(slot, 0);
      this.saveMagazineAndClip(gun, magazine, clip);
   }

   private int encodeColors(ItemStack stack) {
      if (stack.isEmpty()) {
         return 0;
      } else if (stack.getItem().equals(Sitems.SYRINGE.get())) {
         return -1;
      } else {
         Item var3 = stack.getItem();
         if (var3 instanceof WeaponSyringe) {
            WeaponSyringe w = (WeaponSyringe)var3;
            return w.getColor();
         } else {
            var3 = stack.getItem();
            if (var3 instanceof ArmorSyringe) {
               ArmorSyringe a = (ArmorSyringe)var3;
               return a.getColor();
            } else {
               return 0;
            }
         }
      }
   }

   private int getCurrentChamber(ItemStack gun) {
      CompoundTag tag = gun.getOrCreateTagElement("data");
      return tag.getInt("CurrentChamber");
   }

   private void setCurrentChamber(ItemStack gun, int value) {
      CompoundTag tag = gun.getOrCreateTagElement("data");
      tag.putInt("CurrentChamber", value);
   }

   private int getReloadTimer(ItemStack gun) {
      CompoundTag tag = gun.getOrCreateTagElement("data");
      return tag.getInt("ReloadTimer");
   }

   private void setReloadTimer(ItemStack gun, int value) {
      CompoundTag tag = gun.getOrCreateTagElement("data");
      tag.putInt("ReloadTimer", value);
   }

   private boolean isReloading(ItemStack gun) {
      CompoundTag tag = gun.getOrCreateTagElement("data");
      return tag.getBoolean("Reloading");
   }

   private void setReloading(ItemStack gun, boolean value) {
      CompoundTag tag = gun.getOrCreateTagElement("data");
      tag.putBoolean("Reloading", value);
   }

   private int getShootCooldown(ItemStack gun) {
      CompoundTag tag = gun.getOrCreateTagElement("data");
      return tag.getInt("ShootCooldown");
   }

   private void setShootCooldown(ItemStack gun, int value) {
      CompoundTag tag = gun.getOrCreateTagElement("data");
      tag.putInt("ShootCooldown", value);
   }

   public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
      if (entity instanceof Player player) {
         boolean inHand = player.getMainHandItem() == stack || player.getOffhandItem() == stack;
         if (inHand && this.isReloading(stack)) {
            if (this.getReloadTimer(stack) == 5) {
               player.playNotifySound((SoundEvent)Ssounds.SYRINGE_SPIN.get(), SoundSource.AMBIENT, 0.5F, 1.0F);
            }

            if (this.getReloadTimer(stack) > 0) {
               this.setReloadTimer(stack, this.getReloadTimer(stack) - 1);
            } else {
               this.reloadOne(stack, player);
               this.setReloadTimer(stack, 10);
            }
         }

         if (this.getShootCooldown(stack) > 0) {
            if (this.getShootCooldown(stack) == 5 && level.isClientSide) {
               int chamber = this.getCurrentChamber(stack);
               player.playNotifySound((SoundEvent)Ssounds.SYRINGE_SPIN.get(), SoundSource.AMBIENT, 0.5F, 1.0F);
               SGReloadAnimationTracker.triggerRotationToChamber(player, chamber, 10);
            }

            this.setShootCooldown(stack, this.getShootCooldown(stack) - 1);
         }

      }
   }

   public void startReload(ItemStack stack) {
      this.setReloading(stack, true);
      this.setReloadTimer(stack, 10);
   }

   private void reloadOne(ItemStack gun, Player player) {
      NonNullList<ItemStack> magazine = this.getMagazine(gun);

      for(int i = 0; i < magazine.size(); ++i) {
         if (((ItemStack)magazine.get(i)).isEmpty()) {
            ItemStack ammo = this.findAmmo(player);
            if (!ammo.isEmpty()) {
               if (player.level().isClientSide) {
                  SGReloadAnimationTracker.triggerRotationToChamber(player, i, 10);
                  player.playNotifySound((SoundEvent)Ssounds.SYRINGE_RELOAD.get(), SoundSource.AMBIENT, 0.5F, 1.0F);
               } else {
                  ItemStack taken = ammo.split(1);
                  this.setMagazine(gun, taken, i);
               }

               return;
            }

            this.setReloading(gun, false);
            break;
         }
      }

      this.setReloading(gun, false);
   }

   private ItemStack findAmmo(Player player) {
      ItemStack offhand = player.getOffhandItem();
      if (this.isValidAmmo(offhand)) {
         return offhand;
      } else if (player.getAbilities().instabuild) {
         return new ItemStack((ItemLike)Sitems.SYRINGE.get());
      } else {
         for(ItemStack invStack : player.getInventory().items) {
            if (this.isValidAmmo(invStack)) {
               return invStack;
            }
         }

         return ItemStack.EMPTY;
      }
   }

   public boolean shoot(ItemStack gun, Player player, Level level, InteractionHand hand) {
      if (this.getShootCooldown(gun) > 0) {
         return false;
      } else {
         NonNullList<ItemStack> magazine = this.getMagazine(gun);
         int chamber = this.getCurrentChamber(gun);
         ItemStack ammo = (ItemStack)magazine.get(chamber);
         if (!ammo.isEmpty()) {
            if (!level.isClientSide) {
               int enchantment = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, gun);
               float power = enchantment > 0 ? (float)enchantment * 1.5F : 0.0F;
               SyringeProjectile arrow = new SyringeProjectile(level, player, (float)(Integer)SConfig.SERVER.syringe_damage.get() + power, ammo);
               Vec3 vec3 = (new Vec3((double)0.0F, (double)0.0F, hand == InteractionHand.MAIN_HAND ? 0.2 : -0.2)).yRot(-player.getYRot() * ((float)Math.PI / 180F) - ((float)Math.PI / 2F));
               arrow.moveTo(player.position().add(vec3.x, 1.4, vec3.z));
               arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 1.0F);
               level.addFreshEntity(arrow);
               player.playNotifySound((SoundEvent)Ssounds.SYRINGE_SHOOT.get(), SoundSource.AMBIENT, 0.5F, 1.0F);
            } else {
               SGAnimationTracker.trigger(player);
            }

            this.removeMagazine(gun, chamber);
            this.setCurrentChamber(gun, (chamber + 1) % 4);
            this.setShootCooldown(gun, 10);
            return true;
         } else {
            player.playNotifySound(SoundEvents.LEVER_CLICK, SoundSource.AMBIENT, 1.0F, 1.0F);
            this.setCurrentChamber(gun, (chamber + 1) % 4);
            this.triggerMagazineRotation(chamber, player);
            return false;
         }
      }
   }

   private void triggerMagazineRotation(int chamber, Player player) {
      if (player.level().isClientSide) {
         SGReloadAnimationTracker.triggerRotationToChamber(player, chamber, 10);
      }

   }

   public InteractionResultHolder use(Level level, Player player, InteractionHand hand) {
      ItemStack gun = player.getItemInHand(hand);
      player.startUsingItem(hand);
      if (player.isShiftKeyDown()) {
         if (!this.isReloading(gun)) {
            this.startReload(gun);
         }

         return InteractionResultHolder.consume(gun);
      } else if (this.getShootCooldown(gun) <= 0 && !this.isReloading(gun)) {
         if (this.shoot(gun, player, level, hand)) {
            gun.hurtAndBreak(1, player, (p_43296_) -> p_43296_.broadcastBreakEvent(hand));
            return InteractionResultHolder.consume(gun);
         } else {
            this.startReload(gun);
            return InteractionResultHolder.fail(gun);
         }
      } else {
         return InteractionResultHolder.fail(gun);
      }
   }

   public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
      return super.canApplyAtEnchantingTable(stack, enchantment) || ImmutableSet.of(Enchantments.POWER_ARROWS).contains(enchantment);
   }

   public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> list, TooltipFlag flag) {
      super.appendHoverText(stack, level, list, flag);

      for(ItemStack ammo : this.getMagazine(stack)) {
         if (!ammo.isEmpty()) {
            list.add(ammo.copy().getDisplayName());
         }
      }

   }

   public boolean isValidRepairItem(ItemStack stack, ItemStack itemStack) {
      return itemStack.is((Item)Sitems.CIRCUIT_BOARD.get());
   }

   public void initializeClient(Consumer<IClientItemExtensions> consumer) {
      consumer.accept(new IClientItemExtensions() {
         public @Nullable ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
            ItemStack stack = entityLiving.getItemInHand(hand);
            return stack.equals(itemStack) ? ArmPose.CROSSBOW_HOLD : null;
         }
      });
   }

   static {
      AMMO = List.of((Item)Sitems.SYRINGE.get(), (Item)Sitems.VAMPIRIC_SYRINGE.get(), (Item)Sitems.CALCIFIED_SYRINGE.get(), (Item)Sitems.BEZERK_SYRINGE.get(), (Item)Sitems.TOXIC_SYRINGE.get(), (Item)Sitems.ROTTEN_SYRINGE.get(), (Item)Sitems.REINFORCED_SYRINGE.get(), (Item)Sitems.SKELETAL_SYRINGE.get(), (Item)Sitems.DROWNED_SYRINGE.get(), (Item)Sitems.CHARRED_SYRINGE.get());
   }
}

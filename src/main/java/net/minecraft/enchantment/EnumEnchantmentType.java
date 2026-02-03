package net.minecraft.enchantment;

import net.minecraft.item.*;

public enum EnumEnchantmentType {
    ALL,
    ARMOR,
    ARMOR_FEET,
    ARMOR_LEGS,
    ARMOR_TORSO,
    ARMOR_HEAD,
    WEAPON,
    DIGGER,
    FISHING_ROD,
    BREAKABLE,
    BOW;

    /**
     * Return true if the item passed can be enchanted by a enchantment of this type.
     */
    public boolean canEnchantItem(Item p_77557_1_) {
        if (ALL == this) {
            return true;
        } else if (BREAKABLE == this && p_77557_1_.isDamageable()) {
            return true;
        } else if (p_77557_1_ instanceof ItemArmor) {
            if (ARMOR == this) {
                return true;
            } else {
                ItemArmor itemarmor = (ItemArmor) p_77557_1_;
                return 0 == itemarmor.armorType ? ARMOR_HEAD == this : (2 == itemarmor.armorType ? ARMOR_LEGS == this : (1 == itemarmor.armorType ? ARMOR_TORSO == this : (3 == itemarmor.armorType && ARMOR_FEET == this)));
            }
        } else {
            return p_77557_1_ instanceof ItemSword ? WEAPON == this : (p_77557_1_ instanceof ItemTool ? DIGGER == this : (p_77557_1_ instanceof ItemBow ? BOW == this : (p_77557_1_ instanceof ItemFishingRod && FISHING_ROD == this)));
        }
    }
}
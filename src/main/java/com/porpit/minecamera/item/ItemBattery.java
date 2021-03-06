package com.porpit.minecamera.item;

import java.util.List;

import javax.annotation.Nullable;

import com.porpit.minecamera.creativetab.CreativeTabsLoader;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBattery extends Item {
	public ItemBattery() {
		super();
		this.setUnlocalizedName("battery");
		this.setCreativeTab(CreativeTabsLoader.tabMineCamera);
		this.setMaxDamage(9);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, ITooltipFlag advanced) {
		String info = (stack.getMaxDamage() - stack.getItemDamage() + 1) + "/" + (stack.getMaxDamage() + 1);
		tooltip.add(TextFormatting.GREEN + I18n.format("lore.battery.damage") + TextFormatting.GRAY + info);
		tooltip.add(TextFormatting.DARK_GRAY + I18n.format("lore.battery.info"));
	}
}

package com.porpit.minecamera.entity;

import javax.annotation.Nullable;

import com.porpit.minecamera.inventory.ContainerCamera;
import com.porpit.minecamera.item.ItemLoader;
import com.porpit.minecamera.network.MessageUpdatePitchYaw;
import com.porpit.minecamera.network.NetworkLoader;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;

public class EntityTripod extends Entity {
	public static boolean islock = false;
	protected ItemStackHandler Inventory = new ItemStackHandler(4){
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }
    };
	private int burnTime;
	private int delay = 0;
	private int timer;

	public EntityTripod(World worldIn) {
		super(worldIn);
		this.delay = 0;
		this.setBurnTime(ContainerCamera.totalBurnTime);
		this.preventEntitySpawning = true;
		this.setSize(1F, 2);
	}

	@Override
	protected void entityInit() {
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		if (CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.equals(capability)) {
			return true;
		}

		return super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.equals(capability)) {
			@SuppressWarnings("unchecked")
			T result = (T) Inventory;
			return result;
		}
		return super.getCapability(capability, facing);
	}

	@Override
	public void setPosition(double x, double y, double z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.setEntityBoundingBox(new AxisAlignedBB(x - 0.5, y, z - 0.5, x + 0.5, y + 2.5, z + 0.5));
	}

	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		AxisAlignedBB axisalignedbb = this.getEntityBoundingBox();
		return axisalignedbb;
	}

/*	@Override
	protected void updateFallState(double y, boolean onGroundIn, IBlockState state, BlockPos pos) {
		if (!this.isInWater())
        {
            this.handleWaterMovement();
        }

        if (!this.world.isRemote && this.fallDistance > 3.0F && onGroundIn)
        {
            float f = (float)MathHelper.ceiling_float_int(this.fallDistance - 3.0F);

            if (!state.getBlock().isAir(state, world, pos))
            {
                double d0 = Math.min((double)(0.2F + f / 15.0F), 2.5D);
                int i = (int)(150.0D * d0);
            }
        }

        super.updateFallState(y, onGroundIn, state, pos);
	}*/

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}
	
/*	@Override
    public void fall(float distance, float damageMultiplier)
    {
        super.fall(distance, damageMultiplier);
        int i = MathHelper.ceiling_float_int((distance - 3.0F) * damageMultiplier);

        if (i > 0)
        {
            this.attackEntityFrom(DamageSource.fall, (float)i);
            int j = MathHelper.floor_double(this.posX);
            int k = MathHelper.floor_double(this.posY - 0.20000000298023224D);
            int l = MathHelper.floor_double(this.posZ);
            IBlockState iblockstate = this.world.getBlockState(new BlockPos(j, k, l));

            if (iblockstate.getMaterial() != Material.AIR)
            {
                SoundType soundtype = iblockstate.getBlock().getSoundType(iblockstate, world, new BlockPos(j, k, l), this);
                this.playSound(soundtype.getFallSound(), soundtype.getVolume() * 0.5F, soundtype.getPitch() * 0.75F);
            }
        }
    }*/
	

	@Override
	public void onUpdate() {
		super.onUpdate();
		// System.out.println((this.getEntityWorld().isRemote?"Client":"Server")+":"+this.burnTime);
		if (this.getEntityWorld().isRemote && !islock && Minecraft.getMinecraft().getRenderViewEntity().equals(this)) {
			EntityPlayerSP player = Minecraft.getMinecraft().player;
			this.rotationYaw = this.rotationYaw += player.rotationYaw - player.prevRotationYawHead;
			this.rotationPitch = player.rotationPitch;
			player.rotationYaw = player.prevRotationYawHead;
			player.rotationYawHead = player.prevRotationYawHead;
			player.rotationPitch = player.prevRotationPitch;
			if (this.timer == 0) {
				MessageUpdatePitchYaw message = new MessageUpdatePitchYaw();
				message.rotationYaw = this.rotationYaw;
				message.rotationPitch = this.rotationPitch;
				NetworkLoader.instance.sendToServer(message);
			}
			this.timer++;
			if (timer >= 30) {
				timer = 0;
			}
		}
	}

	@Override
	public boolean hitByEntity(Entity entityIn) {
		this.setDead();
		return false;
	}

	@Override
	public void setDead() {
		//System.out.println("death and drop");
		if (!this.getEntityWorld().isRemote) {
			Block.spawnAsEntity(this.getEntityWorld(), this.getPosition(), new ItemStack(ItemLoader.itemTripod, 1));
			// last slot useless,so= -2 !=-1
			for (int i = Inventory.getSlots() - 2; i >= 0; --i) {
				if (Inventory.getStackInSlot(i) != null) {
					Block.spawnAsEntity(this.getEntityWorld(), this.getPosition(), Inventory.getStackInSlot(i));
					((IItemHandlerModifiable) Inventory).setStackInSlot(i, ItemStack.EMPTY);
				}
			}
		}
		super.setDead();
	}

	@Nullable
	public AxisAlignedBB getCollisionBox(Entity entityIn) {
		//System.out.println(entityIn.getPosition());
		return new AxisAlignedBB(entityIn.getPosition().getX() - 0.5, entityIn.getPosition().getY() - 0.5,
				entityIn.getPosition().getZ() - 0.5, entityIn.getPosition().getX() + 0.5,
				entityIn.getPosition().getY() + 2, entityIn.getPosition().getZ() + 0.5);
	}

	@Override
	public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
		this.timer = 90;
		return false;
	}

	@Override
	public float getEyeHeight() {
		return 2.5F;
	}

	/*@Override
	public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand stack) {
		return EnumActionResult.PASS;
	}*/

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound compound) {
		this.delay = compound.getInteger("delay");
		this.burnTime = compound.getInteger("burnTime");
		this.Inventory.deserializeNBT(compound.getCompoundTag("Inventory"));
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound compound) {
		compound.setInteger("delay", this.delay);
		compound.setInteger("burnTime", this.burnTime);
		compound.setTag("Inventory", this.Inventory.serializeNBT());
	}

	public int getBurnTime() {
		return burnTime;
	}

	public void setBurnTime(int burnTime) {
		this.burnTime = burnTime;
	}

}
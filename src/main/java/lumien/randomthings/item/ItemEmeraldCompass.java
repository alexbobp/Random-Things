package lumien.randomthings.item;

import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemEmeraldCompass extends ItemBase
{

	public ItemEmeraldCompass()
	{
		super("emeraldcompass");

		this.setMaxStackSize(1);

		this.addPropertyOverride(new ResourceLocation("angle"), new IItemPropertyGetter()
		{
			@SideOnly(Side.CLIENT)
			double rotation;
			@SideOnly(Side.CLIENT)
			double rota;
			@SideOnly(Side.CLIENT)
			long lastUpdateTick;

			@Override
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
			{
				if (entityIn == null && !stack.isOnItemFrame())
				{
					return 0.0F;
				}
				else
				{
					boolean flag = entityIn != null;
					Entity entity = flag ? entityIn : stack.getItemFrame();

					if (worldIn == null)
					{
						worldIn = entity.world;
					}

					double d0;

					boolean hasTarget = stack.getTagCompound() != null && stack.getTagCompound().hasKey("targetX");

					if (hasTarget)
					{
						NBTTagCompound compound = stack.getTagCompound();
						double d1 = flag ? (double) entity.rotationYaw
								: this.getFrameRotation((EntityItemFrame) entity);
						d1 = d1 % 360.0D;
						double d2 = this.getAngleToPos(worldIn, entity, new BlockPos(compound.getInteger("targetX"), 0, compound.getInteger("targetZ")));
						d0 = Math.PI - ((d1 - 90.0D) * 0.01745329238474369D - d2);
					}
					else
					{
						d0 = Math.random() * (Math.PI * 2D);
					}

					if (flag && !hasTarget)
					{
						d0 = this.wobble(worldIn, d0);
					}

					float f = (float) (d0 / (Math.PI * 2D));
					return MathHelper.positiveModulo(f, 1.0F);
				}
			}

			@SideOnly(Side.CLIENT)
			private double wobble(World p_185093_1_, double p_185093_2_)
			{
				if (p_185093_1_.getTotalWorldTime() != this.lastUpdateTick)
				{
					this.lastUpdateTick = p_185093_1_.getTotalWorldTime();
					double d0 = p_185093_2_ - this.rotation;
					d0 = d0 % (Math.PI * 2D);
					d0 = MathHelper.clamp(d0, -1.0D, 1.0D);
					this.rota += d0 * 0.1D;
					this.rota *= 0.8D;
					this.rotation += this.rota;
				}

				return this.rotation;
			}

			@SideOnly(Side.CLIENT)
			private double getFrameRotation(EntityItemFrame p_185094_1_)
			{
				return MathHelper.wrapDegrees(180 + p_185094_1_.facingDirection.getHorizontalIndex() * 90);
			}

			@SideOnly(Side.CLIENT)
			private double getAngleToPos(World p_185092_1_, Entity p_185092_2_, BlockPos pos)
			{
				return Math.atan2(pos.getZ() - p_185092_2_.posZ, pos.getX() - p_185092_2_.posX);
			}
		});
	}
	
	@Override
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged)
	{
		return !ItemStack.areItemsEqual(oldStack, newStack);
	}

	@Override
	public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected)
	{
		super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);

		if (!worldIn.isRemote && worldIn.getTotalWorldTime() % 20 == 0)
		{
			if (stack.hasTagCompound())
			{
				NBTTagCompound compound = stack.getTagCompound();

				if (compound.hasKey("uuid"))
				{
					UUID uuid = UUID.fromString(compound.getString("uuid"));

					EntityPlayerMP targetPlayer = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUUID(uuid);

					if (targetPlayer != null)
					{
						BlockPos targetPos = targetPlayer.getPosition();
						targetPos = new BlockPos(targetPos.getX(), 0, targetPos.getZ());

						compound.setInteger("targetX", targetPos.getX());
						compound.setInteger("targetZ", targetPos.getZ());
					}
					else
					{
						compound.removeTag("targetX");
						compound.removeTag("targetZ");
					}
				}
			}
		}
	}
}

package aqoursoro.teleportcraft.block.cable;

import java.util.ArrayList;

import javax.annotation.Nonnull;

import aqoursoro.teleportcraft.capability.electricenergy.CapabilityElectricEnergyNetManager;
import aqoursoro.teleportcraft.capability.electricenergy.ElectricEnergyNetManager;
import aqoursoro.teleportcraft.creativetabs.ModCreativeTabs;
import aqoursoro.teleportcraft.tileentity.TileEntityElectricCable;
import aqoursoro.teleportcraft.util.ModUtil;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;


public class BlockElectricCable extends BlockCable 
{
	public BlockElectricCable(@Nonnull final String name) 
	{
		super(Material.PISTON);
		this.setSoundType(SoundType.GLASS);
		ModUtil.setRegistryNames(this, name);
		this.setCreativeTab(ModCreativeTabs.CREATIVE_TAB);
	}

	@Override
	public boolean hasTileEntity() 
	{
		return true;
	}
	
	@Override
	public boolean hasTileEntity(@Nonnull IBlockState state) 
	{
	 return true;
	}
	
	@Override
	public TileEntityElectricCable createTileEntity(final World world, final IBlockState state) 
	{
		return new TileEntityElectricCable();
	}
	
	@Override
	protected BlockStateContainer createBlockState() 
	{
		return new BlockStateContainer(this, CONNECTED_PROPERTIES.toArray(new IProperty[CONNECTED_PROPERTIES.size()]));
	}

	@SuppressWarnings("deprecation")
	@Override
	public IBlockState getStateFromMeta(final int meta) 
	{
		return getDefaultState();
	}

	@Override
	public int getMetaFromState(final IBlockState state) 
	{
		return 0;
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) 
	{
		if(world != null)
		{
			final TileEntity tile = world.getTileEntity(pos);
			if(tile != null)
			{
				if(tile instanceof TileEntityElectricCable)
				{
					final TileEntityElectricCable cable = (TileEntityElectricCable)tile;
					world.getCapability(CapabilityElectricEnergyNetManager.ELECTRIC_ENERGY_NET, null).removeCableLocation(cable.getPos());
				}
			}
		}
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public void onBlockExploded(final World world, final BlockPos pos, final Explosion explosion) 
	{
		if (world != null) 
		{
			final TileEntity tile = world.getTileEntity(pos);
			if (tile != null) 
			{
				if (tile instanceof TileEntityElectricCable) 
				{
					final TileEntityElectricCable cable = (TileEntityElectricCable) tile;
					world.getCapability(CapabilityElectricEnergyNetManager.ELECTRIC_ENERGY_NET, null).removeCableLocation(cable.getPos());
				}
			}
		}
		super.onBlockExploded(world, pos, explosion);
	}
}

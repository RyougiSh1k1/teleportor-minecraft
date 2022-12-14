package aqoursoro.teleportcraft.capability.electricenergy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import javax.annotation.Nonnull;

import aqoursoro.teleportcraft.TeleportCraft;
import aqoursoro.teleportcraft.api.IElectricConsumer;
import aqoursoro.teleportcraft.block.cable.BlockElectricCable;
import aqoursoro.teleportcraft.block.machine.BlockMachine;
import aqoursoro.teleportcraft.capability.IEnergyNetManager;
import aqoursoro.teleportcraft.network.ModNetworkManager;
import aqoursoro.teleportcraft.network.SPktSynElectricNetList;
import aqoursoro.teleportcraft.util.Reference;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

public class ElectricEnergyNetManager implements ITickable
{
	
	private World world;
	
	public ElectricEnergyNetManager(@Nonnull World world) 
	{
		this.connectedCables = new HashSet<>(0);
		this.nets = new HashSet<>(0);
		this.world = world;
	}

	private HashSet<BlockPos> connectedCables = new HashSet<BlockPos>();
	private HashSet<ElectricNet> nets = new HashSet<ElectricNet>();

	public HashSet<BlockPos> getCables()
	{
		return connectedCables;
	}
	
	public HashSet<ElectricNet> getNets()
	{
		return nets;
	}
	
	public World getWorld() 
	{
		return world;
	}
	
	public void addCables(final BlockPos pos) 
	{
		this.getCables().add(pos);
		this.onChange(pos);
	}
	
	public void removeCables(final BlockPos pos)
	{
		this.getCables().remove(pos);
		this.onChange(pos);
	}
	
	private boolean shouldRefreshCables() 
	{
		final int check = (int) (this.getCables().size() / 1000f);
		if (check == 0) {
			return true;
		}
		return (this.world.getTotalWorldTime() % check) == 0;
	}

	private boolean shouldRefreshNets() 
	{
		final int check = (int) (this.getCables().size() / 100f);
		if (check == 0) 
		{
			return true;
		}
		return (this.world.getTotalWorldTime() % check) == 0;
	}

	private boolean shouldOutputEnergy() 
	{
		final int check = (int) (this.getCables().size() / 10f);
		if (check == 0) 
		{
			return true;
		}
		return (this.world.getTotalWorldTime() % check) == 0;
	}
	
	public void refreshNets()
	{
		this.nets.clear();
		final HashSet<BlockPos> done = new HashSet<>(0);

		for (final BlockPos pos : this.getCables()) 
		{
			if (!done.contains(pos)) {
				done.add(pos);

				final ElectricNet network = new ElectricNet(this.world);
				this.generateNet(network, pos);
				done.addAll(network.getCables());
				this.nets.add(network);

			}
		}
	}
	
	private void generateNet(final ElectricNet net, final BlockPos pos) 
	{
		if (net.getCables().size() > 200) 
		{

			return;
		}
		
		final TileEntity tile = this.world.getTileEntity(pos);
		if (tile == null) 
		{
			return;
		}
		if(!(tile instanceof IElectricConsumer))
		{
			return;
		}
		net.add(pos);
		for(EnumFacing direction : EnumFacing.VALUES)
		{
			final BlockPos neighbour = pos.offset(direction);
			if(!net.getCables().contains(neighbour))
			{
				this.generateNet(net, neighbour);
			}
		}
		
	}
	
	public void refreshCables()
	{
		for (final BlockPos pos : this.getCables())
		{
			final TileEntity tile = world.getTileEntity(pos);
			if(tile == null)
			{
				this.removeCables(pos);
				continue;
			}
			if(!(tile instanceof IElectricConsumer))
			{
				this.removeCables(pos);
				continue;
			}
		}
	}
	
	@Override
	public void update() 
	{
		if (this.world.isRemote) 
		{
			return;
		}

		if (this.shouldRefreshCables()) 
		{
			this.refreshCables();
		}
		if (this.shouldRefreshNets()) 
		{
			this.refreshNets();
		}
		if (this.shouldOutputEnergy()) 
		{
			for (final ElectricNet net : this.nets) 
			{
				net.outputEnergy();
			}
		}
		
	}
	
	public void onChange(final BlockPos pos) 
	{
		if (!this.world.isRemote) 
		{
			final NBTTagList syncTag = (NBTTagList) CapabilityElectricEnergyNetManager.ELECTRIC_ENERGY_NET.writeNBT(this, null);
			for (final EntityPlayer player : this.world.playerEntities) 
			{
				if (player instanceof EntityPlayerMP) 
				{
					ModNetworkManager.NETWORK.sendTo(new SPktSynElectricNetList(syncTag), (EntityPlayerMP) player);
				}
			}
		} 
		else 
		{

		}
	}
	
	

}

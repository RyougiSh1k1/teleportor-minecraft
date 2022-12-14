package aqoursoro.teleportcraft.inventory.container;

import javax.annotation.Nonnull;

import aqoursoro.teleportcraft.init.ModItems;
import aqoursoro.teleportcraft.recipes.machine.ElectricGrinderRecipes;
import aqoursoro.teleportcraft.tileentity.TileEntityElectricGrinder;
import aqoursoro.teleportcraft.tileentity.TileEntityThermalElectricGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerThermalElectricGenerator extends Container
{

	private TileEntityThermalElectricGenerator tileEntity;
	
	private int burningTime, energy;
	
	public ContainerThermalElectricGenerator(@Nonnull InventoryPlayer player, @Nonnull TileEntityThermalElectricGenerator tileentity)
	{
		IItemHandler handler = tileentity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
		
		tileEntity = tileentity;
		
		this.addSlotToContainer(new SlotItemHandler(handler, 0, 34, 20) 
		{
			@Override
            public boolean isItemValid(ItemStack stack)
            {
				return (!stack.isEmpty()) && stack.getItem() == ModItems.BATTERY && super.isItemValid(stack);
            }
			
			@Override
            public int getItemStackLimit(ItemStack stack)
            {
                return 1;
            }
		});
		
		this.addSlotToContainer(new SlotItemHandler(handler, 1, 34, 57) 
		{
			@Override
            public boolean isItemValid(ItemStack stack)
            {
				return TileEntityFurnace.isItemFuel(stack);
            }
		});
				
		//player's inventory
				for(int y = 0; y < 3; y++)
				{
					for(int x = 0; x < 9; x++)
					{
						this.addSlotToContainer(new Slot(player, x + y*9 + 9, 8 + x*18, 84 + y*18));
					}
				}
				
				for(int x = 0; x < 9; x++)
				{
					this.addSlotToContainer(new Slot(player, x, 8 + x * 18, 142));
				}
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer playerIn) 
	{
		return tileEntity.isUsableByPlayer(playerIn);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void updateProgressBar(@Nonnull final int id, @Nonnull final int data) 
	{
		this.tileEntity.setField(id, data);
	}
	
	@Override
	public void detectAndSendChanges() 
	{
		super.detectAndSendChanges();
		
		for(int i = 0; i < this.listeners.size(); i++) 
		{
			IContainerListener listener = (IContainerListener)this.listeners.get(i);
			
			if(burningTime != tileEntity.getField(0))
			{
				listener.sendWindowProperty(this, 0, tileEntity.getField(0));
			}
			
			if(energy != tileEntity.getField(1))
			{
				listener.sendWindowProperty(this, 1, tileEntity.getField(1));
			}
		}
		
		burningTime = tileEntity.getField(0);
		energy = tileEntity.getField(1);
	}
	
	@Override
	public ItemStack transferStackInSlot(@Nonnull EntityPlayer playerIn, @Nonnull final int index)
	{
		ItemStack stack = ItemStack.EMPTY;
		
		Slot slot = (Slot)this.inventorySlots.get(index);
		
		if (slot != null && slot.getHasStack())
		{
			ItemStack newStack = slot.getStack(); 
	        stack = newStack.copy();
	        
	        if(index != 1 && index != 0) 
	        {
	        	if(!ElectricGrinderRecipes.instance().getGrindingResult(newStack).isEmpty())
	        	{
	        		if(!this.mergeItemStack(newStack, 0, 2, false)) 
					{
						return ItemStack.EMPTY;
					}
	        		else if(index >= 3 && index < 30)
					{
						if(!this.mergeItemStack(newStack, 30, 39, false)) 
						{
							return ItemStack.EMPTY;
						}
					}else if(index >= 30 && index < 39 && !this.mergeItemStack(newStack, 3, 30, false))
					{
						return ItemStack.EMPTY;
					}
	        	}
	        }
	        else if(!this.mergeItemStack(newStack, 3, 39, false)) 
			{
				return ItemStack.EMPTY;
			}
	        
	        if(newStack.isEmpty())
			{
				slot.putStack(ItemStack.EMPTY);
			}
			else
			{
				slot.onSlotChanged();
			}
	        
	        if(newStack.getCount() == stack.getCount())
	        {
	        	return ItemStack.EMPTY;
	        }
            
	        
	        slot.onTake(playerIn, newStack);
		}
		
		return stack;
	}
}

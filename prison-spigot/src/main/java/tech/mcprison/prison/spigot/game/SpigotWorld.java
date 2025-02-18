/*
 *  Prison is a Minecraft plugin for the prison game mode.
 *  Copyright (C) 2017 The Prison Team
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package tech.mcprison.prison.spigot.game;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;

import tech.mcprison.prison.internal.ItemStack;
import tech.mcprison.prison.internal.Player;
import tech.mcprison.prison.internal.PrisonStatsElapsedTimeNanos;
import tech.mcprison.prison.internal.World;
import tech.mcprison.prison.internal.block.Block;
import tech.mcprison.prison.internal.block.MineResetType;
import tech.mcprison.prison.internal.block.MineTargetPrisonBlock;
import tech.mcprison.prison.internal.block.PrisonBlock;
import tech.mcprison.prison.spigot.SpigotUtil;
import tech.mcprison.prison.spigot.block.SpigotBlockGetAtLocation;
import tech.mcprison.prison.spigot.block.SpigotBlockSetAsynchronously;
import tech.mcprison.prison.spigot.block.SpigotBlockSetSynchronously;
import tech.mcprison.prison.spigot.block.SpigotItemStack;
import tech.mcprison.prison.spigot.compat.SpigotCompatibility;
import tech.mcprison.prison.util.Location;

/**
 * @author Faizaan A. Datoo
 */
public class SpigotWorld implements World {

    private org.bukkit.World bukkitWorld;
    
    private SpigotBlockSetAsynchronously setBlockAsync;
    private SpigotBlockSetSynchronously setBlockSync;
    private SpigotBlockGetAtLocation getBlockAtLocation;

    public SpigotWorld(org.bukkit.World bukkitWorld) {
        this.bukkitWorld = bukkitWorld;
    }

    @Override public String getName() {
        return bukkitWorld.getName();
    }

    @Override public List<Player> getPlayers() {
        return Bukkit.getServer().getOnlinePlayers().stream()
            .filter(player -> 
            		player.getWorld().getName().equals(
            					bukkitWorld.getName()))
            .map((Function<org.bukkit.entity.Player, SpigotPlayer>) SpigotPlayer::new)
            .collect(Collectors.toList());
    }

    /**
     * <p>This should be the ONLY usage in the whole Prison plugin that gets the 
     * bukkit block from the world and converts it to a SpigotBlock..
     * </p>
     * 
     * <p>This gets the actual block from the world, but it only reads, and does not 
     * update.  I cannot say this is safe to run asynchronously, but so far I have
     * not see any related problems when it is.
     * 
     */
    @Override 
    public Block getBlockAt( Location location, boolean containsCustomBlocks ) {
    	
    	if ( getBlockAtLocation == null ) {
    		getBlockAtLocation = new SpigotBlockGetAtLocation();
    	}
    	
    	return getBlockAtLocation.getBlockAt(location, containsCustomBlocks, this);
    }
    
    public Block getBlockAt( Location location ) {
    	return getBlockAt( location, false );
    }
 
    
//    public SpigotBlock getSpigotBlockAt(Location location) {
//    	return new SpigotBlock(
//    			bukkitWorld.getBlockAt(SpigotUtil.prisonLocationToBukkit(location)));
//    }
    
    public org.bukkit.Location getBukkitLocation(Location location) {
    	return SpigotUtil.prisonLocationToBukkit(location);
    }
    
    public org.bukkit.inventory.ItemStack getBukkitItemStack( ItemStack itemStack ) {
    	
    	SpigotItemStack sItemStack = (SpigotItemStack) itemStack;
    	
    	return sItemStack.getBukkitStack();
    }
    
    @Override
    public void setBlock( PrisonBlock block, int x, int y, int z ) {
    	
    	Location loc = new Location( this, x, y, z );
    	org.bukkit.block.Block bukkitBlock = 
    			bukkitWorld.getBlockAt(SpigotUtil.prisonLocationToBukkit(loc));
    	
    	SpigotCompatibility.getInstance().updateSpigotBlock( block, bukkitBlock );
    }
    
    
    /**
     * <p>This function should be called from an async task, and it will
     * drop down in to the synchronous thread to first get the block 
     * from the world, then it will change to the specified PrisonBlock type.
     * </p>
     * 
     * @param prisonBlock
     * @param location
     */
	public void setBlockAsync( PrisonBlock prisonBlock, Location location ) {
		
		if ( setBlockAsync == null ) {
			setBlockAsync = new SpigotBlockSetAsynchronously();
		}
		setBlockAsync.setBlockAsync(prisonBlock, location);
		
	}
	
	
	/**
	 * <p>This list of blocks to be updated, should be ran from an asynchronous thread.
	 * This function will take it's code block and run it in bukkit's synchronous 
	 * thread so the block updates will be thread safe.
	 * </p>
	 * 
	 * <p>The MineTargetPrisonBlock List should be a fairly short list of blocks that
	 * will be updated in one synchronous slice.
	 * </p>
	 * 
	 */
	@Override
	public void setBlocksSynchronously( List<MineTargetPrisonBlock> tBlocks, MineResetType resetType, 
			PrisonStatsElapsedTimeNanos nanos ) {
		
		if ( setBlockSync == null ) {
			setBlockSync = new SpigotBlockSetSynchronously();
		}
		setBlockSync.setBlocksSynchronously(tBlocks, resetType, nanos, this );
		
	}

    public org.bukkit.World getWrapper() {
        return bukkitWorld;
    }
    

}

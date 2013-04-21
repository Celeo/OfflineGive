package com.darktidegames.celeo.offlinegive;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * <b>PendingItem</b> object<br>
 * Stores player name and List of ItemStacks to give to a player the next time
 * they login.
 * 
 * @author Celeo
 */
public class PendingItem
{

	/** Accessability to the plugin object */
	public final OfflineGive plugin;
	/** String name of the player for who these items are being stored */
	public final String player;
	/** List of ItemStack objects */
	public List<ItemStack> items;

	/**
	 * 
	 * @param plugin
	 *            OfflineGive
	 * @param player
	 *            String
	 * @param item
	 *            ItemStack
	 */
	public PendingItem(OfflineGive plugin, String player, ItemStack item)
	{
		this.plugin = plugin;
		this.player = player;
		this.items = new ArrayList<ItemStack>();
		this.items.add(item);
	}

	/**
	 * 
	 * @param plugin
	 *            OfflineGive
	 * @param player
	 *            String
	 * @param data
	 *            String
	 */
	public PendingItem(OfflineGive plugin, String player, String data)
	{
		this.plugin = plugin;
		this.player = player;
		this.items = new ArrayList<ItemStack>();
		ItemStack add;
		if (!data.contains(","))
		{
			addItems(data);
			return;
		}
		for (String str : data.split(","))
		{
			add = new ItemStack(OfflineGive.i(str.split(":")[0]));
			add.setAmount(OfflineGive.i(str.split(":")[1]));
			if (str.split(":").length == 3)
				add.setDurability((short) OfflineGive.i(str.split(":")[2]));
			items.add(add);
		}
	}

	/**
	 * Returns a String formatted properly for saving to the configuration file
	 */
	@SuppressWarnings("boxing")
	@Override
	public String toString()
	{
		String ret = "";
		for (ItemStack i : items)
		{
			if (ret.equals(""))
				ret = String.format("%d:%d:%d", i.getTypeId(), i.getAmount(), i.getDurability());
			else
				ret += String.format(",%d:%d:%d", i.getTypeId(), i.getAmount(), i.getDurability());
		}
		return ret;
	}

	/**
	 * Adds an item to the pending list of items
	 * 
	 * @param item
	 *            ItemStack
	 */
	public void addItem(ItemStack item)
	{
		items.add(item);
	}

	/**
	 * Adds items from a String in the form of: [item id]:[amount](:durability)
	 * 
	 * @param data
	 *            String
	 */
	public void addItems(String data)
	{
		ItemStack add;
		for (String str : data.split(","))
		{
			add = new ItemStack(OfflineGive.i(str.split(":")[0]));
			add.setAmount(OfflineGive.i(str.split(":")[1]));
			if (str.split(":").length == 3)
				add.setDurability((short) OfflineGive.i(str.split(":")[2]));
			items.add(add);
		}
	}

	/**
	 * Call when the player logs in
	 */
	public void transfer(Player player)
	{
		Iterator<ItemStack> i = items.iterator();
		ItemStack item;
		while (i.hasNext())
		{
			item = i.next();
			if (getSpace(player.getInventory().getContents(), item.getTypeId()) > item.getAmount())
			{
				player.getInventory().addItem(item);
				i.remove();
			}
		}
		if (items.isEmpty())
			plugin.removePendingFor(this.player);
		else
			player.sendMessage("§7You have pending items to be added to your inventory, but it's full! Clear space and relog.");
	}

	/**
	 * Returns the space in the inventory for the item id
	 * 
	 * @param contents
	 *            ItemStack[]
	 * @param typeId
	 *            int
	 * @return int
	 */
	public static int getSpace(ItemStack[] contents, int typeId)
	{
		int count = 0;
		ItemStack addItem = new ItemStack(typeId, 1);
		int maxSize = addItem.getMaxStackSize();
		for (ItemStack i : contents)
		{
			if (i == null || i.getType() == Material.AIR)
			{
				count += maxSize;
				continue;
			}
			if (i.getTypeId() == typeId && i.getAmount() < i.getMaxStackSize())
				count += maxSize - i.getAmount();
		}
		return count;
	}

}
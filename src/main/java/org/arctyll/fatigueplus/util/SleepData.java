package org.arctyll.fatigueplus.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

import java.util.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;

/**
 * Handles sleep tracking and message logging for players.
 * Tracks how long players have been awake and what message have been sent to them.
 * Data is saved to and loaded from a JSON file in the Minecraft dats directory.
 */ 
public class SleepData {
	
	/**
	 * Map of player names to the number of ticks they have been awake.
	 */
	private static final Map<String, Integer> ticksAwake = new HashMap<>();
	
	/**
	 * Map of player names to a set of messages already sent to them.
	 */
	private static final Map<String, Set<String>> sentMessages = new HashMap<>();
	
	/**
	 * File wherw fatigue data is stored.
	 */
	private static final File dataFile = new File(Minecraft.getMinecraft().mcDataDir, "fatigue_data.json");
	
	/**
	 * Gson instance used for serializing and desirializing JSON.
	 */
	private static final Gson gson = new Gson();
	
	static {
		load();
	}
	
	/**
	 * Increments the awake tick counter for the given player by 1.
	 * Should be called once every tick.
	 * 
	 * @param player the player whose tick counter should be incremented
	 */
	public static void tick(EntityPlayer player) {
		String name = player.getName();
		ticksAwake.put(name, ticksAwake.getOrDefault(name, 0) + 1);
		save();
	}
	
	/**
	 * Gets the number of full Minecraft days (24000 ticks) the player has gone without sleep.
	 * 
	 * @param player the player to check
	 * @return number of days without sleep
	 */
	public static int getDaysWithoutSleep(EntityPlayer player) {
		return ticksAwake.getOrDefault(player.getName(), 0) / 24000;
	}
	
	/**
	 * Resets the awake counter and message history for the specified player.
	 * 
	 * @param player the player to reset
	 */
	public static void reset(EntityPlayer player) {
		ticksAwake.put(player.getName(), 0);
		sentMessages.remove(player.getName());
		save();
	}
	
	/**
	 * Checks whether a specific message has already been sent to the player.
	 * 
	 * @param player the player to check
	 * @param message the message content
	 * @return true if the message has been sent before, false otherwise
	 */
	public static boolean hasReceivedMessage(EntityPlayer player, String message) {
		return sentMessages.getOrDefault(player.getName(), new HashSet<>()).contains(message);
	}
	
	/**
	 * Marks a specific message as sent to player.
	 * 
	 * @param player the player to update
	 * @param message the message to record
	 */
	public static void markMessageSent(EntityPlayer player, String message) {
		String name = player.getName();
		Set<String> messages;
		
		if (!sentMessages.containsKey(name)) {
			messages = new HashSet<String>();
			sentMessages.put(name, messages);
		} else {
			messages = sentMessages.get(name);
		}
		
		messages.add(message);
	}
	
	/**
	 * Saves the current fatigue data and message history to disc as JSON.
	 */
	private static void save() {
		try {
			Map<String, Object> saveData = new HashMap<String, Object>();
			saveData.put("ticksAwake", ticksAwake);
			saveData.put("sentMessages", sentMessages);
			
			FileWriter writer = new FileWriter(dataFile);
			gson.toJson(saveData, writer);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Loads fatigue data and message history from disc, if available.
	 * This is called once when the class is first loaded.
	 */
	@SuppressWarnings("unchecked")
	private static void load() {
		try {
			if (!dataFile.exists()) {
				save();
			}
			
			FileReader reader = new FileReader(dataFile);
			Type type = new TypeToken<Map<String, Object>>(){}.getType();
			Map<String, Object> data = gson.fromJson(reader, type);
			Type mapIntType = new TypeToken<Map<String, Double>>(){}.getType();
			Map<String, Double> loadedTicks = gson.fromJson(gson.toJson(data.get("ticksAwake")), mapIntType);
			
			for (Map.Entry<String, Double> entry : loadedTicks.entrySet()) {
				ticksAwake.put(entry.getKey(), entry.getValue().intValue());
			}
			
			Type msgType = new TypeToken<Map<String, List<String>>>(){}.getType();
			Map<String, List<String>> loadedMessages = gson.fromJson(gson.toJson(data.get("sentMessages")), msgType);
			
			for (Map.Entry<String, List<String>> entry : loadedMessages.entrySet()) {
				sentMessages.put(entry.getKey(), new HashSet<String>(entry.getValue()));
			}
			
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

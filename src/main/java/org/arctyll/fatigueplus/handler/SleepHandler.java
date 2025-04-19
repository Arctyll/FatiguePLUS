package org.arctyll.fatigueplus.handler;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.arctyll.fatigueplus.util.SleepData;

public class SleepHandler {
	
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	/**
	 * Called every tick for each player.
	 * Applies fatigue effects based on how many in-game days the player has gone without sleep.
	 * Only runs in singleplayer mode.
	 */
	@SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
		EntityPlayer player = event.player;
		
		if (!mc.isSingleplayer()) return;
		
		SleepData.tick(player);
		int days = SleepData.getDaysWithoutSleep(player);
	    
		switch (days) {
			case 1:
				sendOnce(player, "§eYou feel a bit tired... maybe some rest?");
				break;
			case 2:
				apply(player, Potion.moveSlowdown, 0);
				apply(player, Potion.digSlowdown, 0);
				apply(player, Potion.weakness, 0);
				sendOnce(player, "§6Fatigue is creeping in. Your body feels heavy.");
				break;
			case 3:
				apply(player, Potion.moveSlowdown, 1);
				apply(player, Potion.digSlowdown, 1);
				apply(player, Potion.weakness, 1);
				apply(player, Potion.blindness, 0);
				sendOnce(player, "§cWARNING §43 days without sleep! Your mind is blurring.");
				break;
			case 4:
				apply(player, Potion.confusion, 0);
				apply(player, Potion.moveSlowdown, 2);
				apply(player, Potion.weakness, 2);
				sendOnce(player, "§4You feel dizzy and disoriented...");
				break;
			case 5:
				apply(player, Potion.moveSlowdown, 3);
				apply(player, Potion.digSlowdown, 2);
				apply(player, Potion.weakness, 2);
				apply(player, Potion.confusion, 1);
				sendOnce(player, "§4Your muscles barely respond. You're exhausted.");
				break;
			case 6:
				apply(player, Potion.blindness, 0);
				apply(player, Potion.hunger, 1);
				sendOnce(player, "§cYou are on the verge of collapse...");
				break;
			case 7:
				player.attackEntityFrom(DamageSource.generic, 1.0F);
				sendOnce(player, "");
				break;
			case 8:
				player.attackEntityFrom(DamageSource.generic, 2.0F);
				break;
			case 9:
				player.attackEntityFrom(DamageSource.starve, 3.0F);
				break;
			case 10:
				player.attackEntityFrom(DamageSource.magic, 6.0F);
				break;
		}
	}
	 
	 /**
	  * Called when a player sleeps in a day.
	  * Resets sleep data and sends recovery messages based on sleep deprivation level.
	  */
	 @SubscribeEvent
	 public void onSleep(PlayerSleepInBedEvent event) {
		 
		 EntityPlayer player = event.entityPlayer;
		 
		 if (!player.worldObj.isRemote && mc.isSingleplayer()) {
			 int days = SleepData.getDaysWithoutSleep(player);
			 sendSleepMessage(player, days);
			 SleepData.reset(player);
		 }
	 }
	 
	 /**
	  * Applies a potion effect to the player if they don't already have it.
	  *
	  * @param player Player to apply the potion effect to
	  * @param potion Potion type
	  * @param level Potion level
	  */
	 private void apply(EntityPlayer player, Potion potion, int level) {
		 if (!player.isPotionActive(potion)) {
			 player.addPotionEffect(new PotionEffect(potion.getId(), 200, level, false, true));
		 }
	 }
	 
	 /**
	  * Sends the fatigue message to the player only once.
	  * Prevents spammy messages by checking with SleepData.
	  * 
	  * @param player Target player
	  * @param message Message to send
	  */
	 private void sendOnce(EntityPlayer player, String message) {
		 if (!SleepData.hasReceivedMessage(player, message)) {
			 player.addChatMessage(new ChatComponentText("§b§l[FATIGUE+] §e> " + message));
			 SleepData.markMessageSent(player, message);
		 }
	 }
	 
	 /**
	  * Sends a recovery message to the player based on how long they've gone without sleep.
	  * 
	  * @param player Target player
	  * @param days Number of days since last sleep
	  */
	 private void sendSleepMessage(EntityPlayer player, int days) {
		 String msg;
		 if (days == 1) {
			 msg = "§aYou feel refreshed after a short rest.";
		 } else if (days < 4) {
			 msg = "§aSleep eases the fatigue... for now.";
		 } else if (days < 7) {
			 msg = "§bYour body thanks you for the sleep.";
		 } else {
			 msg = "§6You feel reborn. Sleep saved you.";
		 }
		 
		 player.addChatMessage(new ChatComponentText("§b§l[FATIGUE+] §e> " + msg));
	 }
}

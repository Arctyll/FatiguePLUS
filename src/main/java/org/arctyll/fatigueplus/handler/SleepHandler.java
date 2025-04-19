package org.arctyll.fatigueplus.handler;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import org.arctyll.fatigueplus.util.SleepData;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles fatigue effects, messages, and resets based on sleep deprivation.
 * Runs on server side at the end of each player tick.
 */
public class SleepHandler {
    /**
     * Tracks the last day count for which a message was sent, to avoid repeats.
     */
    private final Map<String, Integer> lastNotifiedDay = new HashMap<>();

    /**
     * Called each tick for every player. Applies and maintains effects, and sends messages when day count increases.
     */
    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent event) {
        if (event.phase != Phase.END) {
            return;
        }

        EntityPlayer player = event.player;
        String name = player.getName();

        SleepData.tick(player);
        int days = SleepData.getDaysWithoutSleep(player);

        applyEffects(player, days);

        int lastDay = lastNotifiedDay.getOrDefault(name, -1);
        if (days > lastDay) {
            sendDeprivationMessage(player, days);
            lastNotifiedDay.put(name, days);
        }
    }

    /**
     * Called when the player sleeps. Sends recovery message and resets data.
     */
    @SubscribeEvent
    public void onSleep(PlayerSleepInBedEvent event) {
        EntityPlayer player = event.entityPlayer;
        if (player.worldObj.isRemote) {
            return;
        }
		
		if (event.result != EntityPlayer.EnumStatus.OK) {
			return;
		}

        int days = SleepData.getDaysWithoutSleep(player);
        sendRecoveryMessage(player, days);
        SleepData.reset(player);
        lastNotifiedDay.remove(player.getName());
    }

    /**
     * Applies ongoing potion effects for the current deprivation level.
     * Effects last a full in-game day (24000 ticks) before refreshing.
     */
    private void applyEffects(EntityPlayer player, int days) {
        int duration = 24000;
        switch (days) {
            case 2:
                apply(player, Potion.moveSlowdown, 0, duration);
                apply(player, Potion.digSlowdown, 0, duration);
                apply(player, Potion.weakness, 0, duration);
                break;
            case 3:
                apply(player, Potion.moveSlowdown, 1, duration);
                apply(player, Potion.digSlowdown, 1, duration);
                apply(player, Potion.weakness, 1, duration);
                apply(player, Potion.blindness, 0, duration);
                break;
            case 4:
                apply(player, Potion.confusion, 0, duration);
                apply(player, Potion.moveSlowdown, 2, duration);
                apply(player, Potion.weakness, 2, duration);
                break;
            case 5:
                apply(player, Potion.moveSlowdown, 3, duration);
                apply(player, Potion.digSlowdown, 2, duration);
                apply(player, Potion.weakness, 2, duration);
                apply(player, Potion.confusion, 1, duration);
                break;
            case 6:
                apply(player, Potion.blindness, 0, duration);
                apply(player, Potion.hunger, 1, duration);
                break;
            case 7:
                player.attackEntityFrom(DamageSource.generic, 1.0F);
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
            default:
                break;
        }
    }

    /**
     * Applies a potion effect with a specified duration, even if already active.
     */
    private void apply(EntityPlayer player, Potion potion, int level, int duration) {
        PotionEffect current = player.getActivePotionEffect(potion);
		if (current == null || current.getDuration() < 20000 || current.getAmplifier() < level) {
			player.addPotionEffect(new PotionEffect(potion.getId(), duration, level, false, true));
		}
    }

    /**
     * Sends a message when deprivation reaches a new day threshold.
     */
    private void sendDeprivationMessage(EntityPlayer player, int days) {
        String msg;
        switch (days) {
            case 1:
                msg = "§eYou feel a bit tired... maybe some rest?";
                break;
            case 2:
                msg = "§6Fatigue is creeping in. Your body feels heavy.";
                break;
            case 3:
                msg = "§cWARNING: 3 days without sleep! Your mind is blurring...";
                break;
            case 4:
                msg = "§4You feel dizzy and disoriented...";
                break;
            case 5:
                msg = "§4Your muscles barely respond. You're exhausted.";
                break;
            case 6:
                msg = "§cYou are on the verge of collapse...";
                break;
            case 7:
                msg = "§cYour health is draining from no sleep...";
                break;
            default:
                return;
        }
        player.addChatMessage(new ChatComponentText("§b§l[FATIGUE+] §e> " + msg));
    }

    /**
     * Sends a recovery message when the player sleeps.
     */
    private void sendRecoveryMessage(EntityPlayer player, int days) {
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

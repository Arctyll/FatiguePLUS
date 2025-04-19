package org.arctyll.fatigueplus;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.arctyll.fatigueplus.handler.*;

@Mod(modid = "fatigueplus", name = "Fatigue+", version = "0.1")
public class FatiguePLUS {
	
	@Mod.EventHandler
	public void init(FMLInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(new SleepHandler());
	}
}

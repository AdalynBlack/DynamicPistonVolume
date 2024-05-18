package com.adibtw.pistonvolume.forge;

import com.adibtw.pistonvolume.PistonVolume;
import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(PistonVolume.MOD_ID)
public class PistonVolumeForge {
    public PistonVolumeForge() {
        // Submit our event bus to let architectury register our content on the right time
        EventBuses.registerModEventBus(PistonVolume.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());
        PistonVolume.init();
    }
}

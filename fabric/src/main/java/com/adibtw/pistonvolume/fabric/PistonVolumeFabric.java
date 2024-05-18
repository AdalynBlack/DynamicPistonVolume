package com.adibtw.pistonvolume.fabric;

import com.adibtw.pistonvolume.fabriclike.PistonVolumeFabricLike;
import net.fabricmc.api.ModInitializer;

public class PistonVolumeFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        PistonVolumeFabricLike.init();
    }
}

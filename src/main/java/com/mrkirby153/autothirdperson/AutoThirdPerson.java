package com.mrkirby153.autothirdperson;

import com.mrkirby153.autothirdperson.config.Configuration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.RenderTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Mod("autothirdperson")
public class AutoThirdPerson {

    public static Logger LOGGER = LogManager.getLogger();
    public final Configuration configuration = Configuration.INSTANCE;

    private boolean hasCameraSwitched = false;
    private boolean elytraLastTick = false;
    private boolean isRiding = false;
    private PointOfView oldPov = PointOfView.FIRST_PERSON;

    public AutoThirdPerson() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            MinecraftForge.EVENT_BUS.register(this);
            ModLoadingContext.get().registerConfig(Type.CLIENT, Configuration.spec);
        } else {
            LOGGER.warn("This is a client-side only mod that's being run on the server!");
        }
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST,
            () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }

    @SubscribeEvent
    public void onRender(RenderTickEvent event) {
        if (!configuration.enabled.get()) {
            return;
        }
        ClientPlayerEntity cpe = Minecraft.getInstance().player;
        if (cpe == null || Minecraft.getInstance().level == null) {
            return;
        }

        if (configuration.elytra.get()) {
            if (cpe.isFallFlying()) {
                if (!elytraLastTick) {
                    LOGGER.debug("Setting camera to 3rd person because flying");
                    this.oldPov = Minecraft.getInstance().options.getCameraType();
                    Minecraft.getInstance().options.setCameraType(PointOfView.THIRD_PERSON_BACK);
                }
            } else {
                if (elytraLastTick) {
                    LOGGER.debug("Restoring camera due to stopping flying");
                    Minecraft.getInstance().options.setCameraType(this.oldPov);
                }
            }
            elytraLastTick = cpe.isFallFlying();
        }

        if (cpe.isPassenger()) {
            if (hasCameraSwitched) {
                return; // Don't try to switch again
            }
            Entity vehicle = cpe.getVehicle();
            if (vehicle == null) {
                return; // This should never happen
            }
            ResourceLocation resource = vehicle.getType().getRegistryName();
            if (resource == null) {
                return; // This should never happen
            }
            if (!isRiding) {
                isRiding = true;
                if (configuration.debug.get()) {
                    LOGGER.info("Mounted {}", resource.toString());
                }
            }
            if (shouldSwitch(resource.toString())) {
                LOGGER.debug("Mounted {}, switching camera", resource.toString());
                hasCameraSwitched = true;
                this.oldPov = Minecraft.getInstance().options.getCameraType();
                Minecraft.getInstance().options.setCameraType(PointOfView.THIRD_PERSON_BACK);
            }

        } else {
            if (isRiding) {
                isRiding = false;
            }
            if (hasCameraSwitched) {
                LOGGER.debug("Restoring camera due to dismount");
                // Restore the camera
                hasCameraSwitched = false;
                Minecraft.getInstance().options.setCameraType(oldPov);
            }
        }
    }

    private boolean shouldSwitch(String entity) {
        List<? extends String> whitelist = configuration.whitelistedEntities.get();
        List<? extends String> blacklist = configuration.blacklistedEntities.get();

        // If the whitelist is not empty, check if its whitelisted
        // If the blacklist is not empty, check if its blacklisted
        if (whitelist.size() > 0) {
            return whitelist.contains(entity);
        }
        if (blacklist.size() > 0) {
            return !blacklist.contains(entity);
        }
        return true; // Default to true
    }
}

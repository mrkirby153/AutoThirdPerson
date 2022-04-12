package com.mrkirby153.autothirdperson.config;

import com.google.common.collect.Lists;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class Configuration {

    public static ForgeConfigSpec spec;
    public static Configuration INSTANCE;

    static {
        Pair<Configuration, ForgeConfigSpec> pair = new Builder().configure(Configuration::new);
        INSTANCE = pair.getLeft();
        spec = pair.getRight();
    }

    public ForgeConfigSpec.ConfigValue<List<? extends String>> whitelistedEntities;
    public ForgeConfigSpec.ConfigValue<List<? extends String>> blacklistedEntities;
    public ForgeConfigSpec.BooleanValue elytra;
    public ForgeConfigSpec.BooleanValue enabled;
    public ForgeConfigSpec.BooleanValue debug;

    public Configuration(ForgeConfigSpec.Builder builder) {
        this.enabled = builder.comment("Master enable switch").define("enabled", true);
        this.whitelistedEntities = builder.comment("A list of entities that are whitelisted")
            .defineList("whitelist", Lists.newArrayList("examplemod:example_entity"), (o) -> o instanceof String && ResourceLocation.isValidResourceLocation(o.toString()) && !new ResourceLocation(o.toString()).getPath().isEmpty());
        this.blacklistedEntities = builder.comment("A list of entities that are blacklisted")
            .defineList("blacklist", Lists.newArrayList("examplemod:example_entity"), (o) -> o instanceof String && ResourceLocation.isValidResourceLocation(o.toString()) && !new ResourceLocation(o.toString()).getPath().isEmpty());
        this.elytra = builder.comment("Enable switching when flying on an elytra")
            .define("elytra", false);
        this.debug = builder.comment(
                "Enables debug logging (Prints out the entity name in the console) on (dis)mount")
            .define("debug", false);
    }
}

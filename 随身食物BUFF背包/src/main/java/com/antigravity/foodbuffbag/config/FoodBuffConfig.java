package com.antigravity.foodbuffbag.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class FoodBuffConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue MAX_PAGES;
    public static final ForgeConfigSpec.BooleanValue FILTER_HARMFUL;
    public static final ForgeConfigSpec.IntValue SCAN_INTERVAL;

    static {
        BUILDER.push("General Settings");

        MAX_PAGES = BUILDER.comment("随身食物BUFF背包的最大可翻页数量 (1 - 500 页，每页 54 槽位)")
                .defineInRange("maxPages", 100, 1, 500);

        FILTER_HARMFUL = BUILDER.comment("是否自动过滤掉食物带来的负面/有害药水效果 (如中毒、反胃、虚弱、饥饿等)")
                .define("filterHarmfulEffects", true);

        SCAN_INTERVAL = BUILDER.comment("服务端检测并刷新玩家食物 BUFF 的检测周期 (单位: Tick, 10 Tick = 0.5 秒)")
                .defineInRange("scanInterval", 10, 1, 100);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}

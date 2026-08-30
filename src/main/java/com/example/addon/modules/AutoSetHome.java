package com.example.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.Minecraft;

import static com.example.addon.AddonTemplate.CATEGORY;

/**
 * Periodically runs a /sethome-style command so your home stays up to date
 * without you having to remember to do it manually.
 */
public class AutoSetHome extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<String> command = sgGeneral.add(new StringSetting.Builder()
        .name("command")
        .description("Command to run, without the slash. Use %name% for the home name.")
        .defaultValue("sethome %name%")
        .build()
    );

    private final Setting<String> homeName = sgGeneral.add(new StringSetting.Builder()
        .name("home-name")
        .description("The home name to substitute into %name%.")
        .defaultValue("best")
        .build()
    );

    private final Setting<Integer> interval = sgGeneral.add(new IntSetting.Builder()
        .name("interval")
        .description("How often, in seconds, to set your home.")
        .defaultValue(300)
        .min(10)
        .sliderMin(10)
        .sliderMax(3600)
        .build()
    );

    private final Setting<Boolean> notify = sgGeneral.add(new BoolSetting.Builder()
        .name("notify")
        .description("Show a chat message in your client when home is set.")
        .defaultValue(true)
        .build()
    );

    private int timer;

    public AutoSetHome() {
        super(CATEGORY, "auto-set-home", "Periodically sets your home for you.");
    }

    @Override
    public void onActivate() {
        timer = interval.get() * 20;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (timer-- <= 0) {
            String cmd = command.get().replace("%name%", homeName.get());
            ChatUtils.sendPlayerMsg("/" + cmd);

            if (notify.get()) ChatUtils.info("Auto Set Home: ran \"/" + cmd + "\".");

            timer = interval.get() * 20;
        }
    }
}

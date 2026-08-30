package com.example.addon.modules;

import meteordevelopment.meteorclient.events.game.GameLeftEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;

import static com.example.addon.AddonTemplate.CATEGORY;

public class AutoRelog extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Seconds to wait before reconnecting.")
        .defaultValue(5)
        .min(0)
        .sliderMin(0)
        .sliderMax(30)
        .build()
    );

    private final Setting<Integer> attempts = sgGeneral.add(new IntSetting.Builder()
        .name("max-attempts")
        .description("Max reconnect attempts before giving up (0 = unlimited).")
        .defaultValue(0)
        .min(0)
        .sliderMin(0)
        .sliderMax(20)
        .build()
    );

    private ServerInfo lastServer;
    private int timer;
    private int attemptCount;
    private boolean waiting;

    public AutoRelog() {
        super(CATEGORY, "auto-relog", "Automatically reconnects when you're disconnected or kicked.");
    }

    @Override
    public void onActivate() {
        attemptCount = 0;
        waiting = false;
    }

    @EventHandler
    private void onDisconnect(GameLeftEvent event) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ServerInfo entry = mc.getCurrentServerEntry();

        if (entry != null) {
            lastServer = entry;

            if (attempts.get() > 0 && attemptCount >= attempts.get()) {
                ChatUtils.warning("Auto Relog: max attempts reached, not reconnecting.");
                return;
            }

            waiting = true;
            timer = delay.get() * 20;
        }
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (!waiting) return;

        if (timer-- <= 0) {
            waiting = false;
            reconnect();
        }
    }

    private void reconnect() {
        if (lastServer == null) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        ServerAddress address = ServerAddress.parse(lastServer.address);

        attemptCount++;
        ConnectScreen.connect(new MultiplayerScreen(null), mc, address, lastServer, false, null);
    }
}

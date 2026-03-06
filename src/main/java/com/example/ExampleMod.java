package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class ExampleMod implements ModInitializer {
    // Состояния функций
    public static boolean aura = false;
    public static boolean fly = false;
    public static boolean speed = false;
    public static boolean esp = false;

    @Override
    public void onInitialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            // --- МЕНЮ (Right Shift) ---
            if (GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS) {
                client.player.sendMessage(Text.literal("§b--- МЕНЮ: P(Aura), R(Fly), M(Speed), O(ESP) ---"), true);
            }

            // --- ОБРАБОТКА КНОПОК (через задержку в 5 тиков, чтобы не мигало) ---
            if (client.player.age % 5 == 0) {
                if (isPressed(client, GLFW.GLFW_KEY_P)) aura = toggle(client, "Aura", aura);
                if (isPressed(client, GLFW.GLFW_KEY_R)) fly = toggle(client, "Fly", fly);
                if (isPressed(client, GLFW.GLFW_KEY_M)) speed = toggle(client, "Speed", speed);
                if (isPressed(client, GLFW.GLFW_KEY_O)) esp = toggle(client, "ESP", esp);
            }

            // 1. KILL AURA (Улучшенная)
            if (aura) {
                for (Entity e : client.world.getEntities()) {
                    if (e instanceof LivingEntity && e != client.player && client.player.distanceTo(e) < 4.5) {
                        client.interactionManager.attackEntity(client.player, e);
                        client.player.swingHand(Hand.MAIN_HAND);
                    }
                }
            }

            // 2. FLY
            client.player.getAbilities().allowFlying = fly || client.player.isCreative();
            if (fly) client.player.getAbilities().flying = true;

            // 3. SPEED (Ускорение)
            if (speed && client.player.forwardSpeed > 0) {
                client.player.updateVelocity(0.1f, client.player.getVelocity());
            }

            // 4. ESP (Подсветка всех)
            for (Entity entity : client.world.getEntities()) {
                if (entity instanceof LivingEntity && entity != client.player) {
                    entity.setGlowing(esp);
                }
            }
        });
    }

    private boolean toggle(MinecraftClient c, String name, boolean state) {
        c.player.sendMessage(Text.literal("§6[MyCheat] §f" + name + ": " + (!state ? "§aВКЛ" : "§7ВЫКЛ")), true);
        return !state;
    }

    private boolean isPressed(MinecraftClient client, int key) {
        return GLFW.glfwGetKey(client.getWindow().getHandle(), key) == GLFW.GLFW_PRESS;
    }
}

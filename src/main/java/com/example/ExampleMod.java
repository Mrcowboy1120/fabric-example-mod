package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class ExampleMod implements ModInitializer {
    // Состояния функций
    public static boolean aura = false;
    public static boolean fly = false;
    public static boolean speed = false;
    public static boolean noClip = false;
    public static boolean esp = false;

    @Override
    public void onInitialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // --- МЕНЮ УПРАВЛЕНИЯ (Right Shift) ---
            if (GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS) {
                client.player.sendMessage(Text.literal("§b=== [MyCheat MENU] ==="), true);
                client.player.sendMessage(Text.literal("§fP: Aura | R: Fly | M: Speed | N: NoClip | B: ESP"), false);
            }

            // --- ОБРАБОТКА КЛАВИШ ---
            if (isClicked(client, GLFW.GLFW_KEY_P)) aura = toggle(client, "KillAura", aura);
            if (isClicked(client, GLFW.GLFW_KEY_R)) fly = toggle(client, "Fly", fly);
            if (isClicked(client, GLFW.GLFW_KEY_M)) speed = toggle(client, "Speed", speed);
            if (isClicked(client, GLFW.GLFW_KEY_N)) noClip = toggle(client, "NoClip", noClip);

            // --- 1. УЛУЧШЕННАЯ KILL AURA ---
            if (aura) {
                for (Entity e : client.world.getEntities()) {
                    if (e instanceof LivingEntity && e != client.player && client.player.distanceTo(e) < 4.5) {
                        if (client.player.getAttackCooldownProgress(0.5f) >= 1.0f) {
                            client.interactionManager.attackEntity(client.player, e);
                            client.player.swingHand(Hand.MAIN_HAND);
                        }
                    }
                }
            }

            // --- 2. FLY & NOCLIP ---
            if (fly || noClip) {
                client.player.getAbilities().allowFlying = true;
                client.player.getAbilities().flying = true;
                if (noClip) client.player.noClip = true;
            } else if (!client.player.isCreative()) {
                client.player.getAbilities().allowFlying = false;
                client.player.getAbilities().flying = false;
                client.player.noClip = false;
            }

            // --- 3. SPEED (Bhop) ---
            if (speed && client.player.forwardSpeed > 0) {
                if (client.player.isOnGround()) client.player.jump();
                Vec3d v = client.player.getVelocity();
                client.player.setVelocity(v.x * 1.1, v.y, v.z * 1.1);
            }

            // --- 4. ESP (Glow) ---
            for (Entity entity : client.world.getEntities()) {
                if (entity instanceof LivingEntity && entity != client.player) {
                    entity.setGlowing(esp);
                }
            }
        });
    }

    private boolean toggle(MinecraftClient c, String name, boolean state) {
        c.player.sendMessage(Text.literal("§6[Cheat] §f" + name + ": " + (!state ? "§aON" : "§7OFF")), true);
        return !state;
    }

    private boolean isClicked(MinecraftClient client, int key) {
        // Упрощенная проверка нажатия (раз в несколько тиков, чтобы не мигало)
        return GLFW.glfwGetKey(client.getWindow().getHandle(), key) == GLFW.GLFW_PRESS && client.player.age % 5 == 0;
    }
}

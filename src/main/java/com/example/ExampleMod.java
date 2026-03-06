package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

public class ExampleMod implements ModInitializer {
    // Настройки модулей
    public static boolean auraEnabled = false;
    public static boolean flyEnabled = false;
    public static boolean speedEnabled = false;
    public static boolean espEnabled = false;

    @Override
    public void onInitialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            // --- УПРАВЛЕНИЕ МЕНЮ (Right Shift) ---
            if (GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS) {
                // В будущем здесь будет полноценное GUI, а пока — быстрое переключение через чат
                client.player.sendMessage(Text.literal("§b--- МОДУЛИ: P(Aura), R(Fly), M(Speed) ---"), true);
            }

            // --- ГОРЯЧИЕ КЛАВИШИ ---
            if (isPressed(client, GLFW.GLFW_KEY_P)) auraEnabled = !auraEnabled;
            if (isPressed(client, GLFW.GLFW_KEY_R)) flyEnabled = !flyEnabled;
            if (isPressed(client, GLFW.GLFW_KEY_M)) speedEnabled = !speedEnabled;

            // --- 1. УЛУЧШЕННАЯ KILL AURA (С КРИТАМИ) ---
            if (auraEnabled) {
                for (Entity target : client.world.getEntities()) {
                    if (target instanceof LivingEntity && target != client.player && client.player.distanceTo(target) < 5.0) {
                        // Бьем, только если откат оружия прошел
                        if (client.player.getAttackCooldownProgress(0.5f) >= 1.0f) {
                            client.interactionManager.attackEntity(client.player, target);
                            client.player.swingHand(Hand.MAIN_HAND);
                        }
                    }
                }
            }

            // --- 2. FLY (ПОЛЕТ) ---
            if (flyEnabled) {
                client.player.getAbilities().allowFlying = true;
                client.player.getAbilities().flying = true;
            } else if (!client.player.isCreative()) {
                client.player.getAbilities().allowFlying = false;
                client.player.getAbilities().flying = false;
            }

            // --- 3. SPEED (УСКОРЕНИЕ) ---
            if (speedEnabled && (client.player.forwardSpeed > 0 || client.player.sidewaysSpeed > 0)) {
                if (client.player.isOnGround()) {
                    client.player.jump(); // Авто-прыжки для скорости (Bhop)
                }
                Vec3d vel = client.player.getVelocity();
                client.player.setVelocity(vel.x * 1.2, vel.y, vel.z * 1.2);
            }

            // --- 4. ESP (ПОДСВЕТКА ИГРОКОВ) ---
            if (espEnabled) {
                for (Entity e : client.world.getEntities()) {
                    if (e instanceof PlayerEntity && e != client.player) {
                        e.setGlowing(true); // Заставляет игрока светиться сквозь стены
                    }
                }
            }
        });
    }

    private boolean isPressed(MinecraftClient client, int key) {
        return GLFW.glfwGetKey(client.getWindow().getHandle(), key) == GLFW.GLFW_PRESS;
    }
}

package com.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.lwjgl.glfw.GLFW;

public class ExampleMod implements ModInitializer {
    public static boolean active = false;

    @Override
    public void onInitialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) return;

            // Клавиша P - Активация всего чита
            if (GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_P) == GLFW.GLFW_PRESS) {
                active = !active;
                client.player.sendMessage(new LiteralText("§6[MyClient] §fЧит: " + (active ? "§aАКТИВИРОВАН" : "§7ВЫКЛЮЧЕН")), true);
            }

            if (active) {
                // 1. FLY (Полет)
                client.player.getAbilities().allowFlying = true;
                client.player.getAbilities().flying = true;

                // 2. SPEED (Скорость x1.5)
                if (client.player.forwardSpeed > 0) {
                    Vec3d v = client.player.getVelocity();
                    client.player.setVelocity(v.x * 1.15, v.y, v.z * 1.15);
                }

                // 3. NOFALL (Нет урона от падения)
                if (client.player.fallDistance > 2.5f) {
                    client.player.networkHandler.sendPacket(new PlayerMoveC2SPacket(true));
                }

                // 4. JESUS (Хождение по воде)
                if (client.player.isTouchingWater()) {
                    client.player.setVelocity(client.player.getVelocity().x, 0.1, client.player.getVelocity().z);
                }

                // 5. KILL AURA (Авто-удар 5 блоков)
                for (Entity e : client.world.getEntities()) {
                    if (e instanceof LivingEntity && e != client.player && client.player.distanceTo(e) <= 5.0) {
                        if (client.player.getAttackCooldownProgress(0) >= 1.0f) {
                            client.interactionManager.attackEntity(client.player, e);
                            client.player.swingHand(Hand.MAIN_HAND);
                        }
                    }
                }

                // 6. SCAFFOLD (Авто-мост под ноги)
                if (client.world.getBlockState(client.player.getBlockPos().down()).isAir()) {
                   // Логика зажатия правой кнопки (упрощенно)
                   client.options.keyUse.setPressed(true);
                }

            } else {
                // Выключаем полет, если чит выключен (и мы не в креативе)
                if (!client.player.isCreative()) {
                    client.player.getAbilities().allowFlying = false;
                    client.player.getAbilities().flying = false;
                }
                client.options.keyUse.setPressed(false);
            }
        });
    }
}

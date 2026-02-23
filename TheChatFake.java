package com.User.the_chat_fake;

import com.mojang.brigadier.ParseResults;
import net.minecraft.ChatFormatting;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Mod(TheChatFake.MODID)
public class TheChatFake {
    public static final String MODID = "the_chat_fake";
    private static final Random RANDOM = new Random();
    private static final String[] NAMES = {"Null", "Entity303", "Herobrine", "User_404"};
    private boolean trapActive = false;
    private final List<String> chatHistory = new ArrayList<>();
    private int nullSpamCombo = 0;

    public TheChatFake() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide) {
            scheduleNext(player, RANDOM.nextInt(300) + 300);
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide) {
            Player player = event.player;

            if (RANDOM.nextInt(1500) == 0) {
                player.level().playSound(null, player.getX() - 1, player.getY(), player.getZ() - 1,
                        net.minecraft.sounds.SoundEvents.PLAYER_SMALL_FALL, net.minecraft.sounds.SoundSource.AMBIENT, 0.6F, 0.5F);
            }

            if (RANDOM.nextInt(5000) == 0) {
                triggerFlash();
            }

            if (RANDOM.nextInt(2000) == 0) {
                player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 30, 2, false, false));
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, net.minecraft.sounds.SoundSource.AMBIENT, 0.3F, 2.0F);
            }

            if (RANDOM.nextInt(3000) == 0) {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                        net.minecraft.sounds.SoundEvents.CHEST_OPEN, net.minecraft.sounds.SoundSource.BLOCKS, 0.5F, 0.1F);
            }

            if (true) {
                BlockPos pos = player.blockPosition();
                for (BlockPos nearPos : BlockPos.betweenClosed(pos.offset(-7, -3, -7), pos.offset(7, 3, 7))) {
                    BlockEntity be = player.level().getBlockEntity(nearPos);
                    if (be instanceof SignBlockEntity sign) {
                        Component oldText = sign.getFrontText().getMessage(0, false);
                        sign.updateText(text -> text.setMessage(0, Component.literal("NULL IS WATCHING")), true);

                        ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
                        s.schedule(() -> {
                            player.getServer().execute(() -> sign.updateText(text -> text.setMessage(0, oldText), true));
                            s.shutdown();
                        }, 2, TimeUnit.SECONDS);
                        break;
                    }
                }
            }
        }
    }

    private void teleportToNULLWorld(ServerPlayer player) {
        player.getServer().execute(() -> {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));

            player.teleportTo(666, -60, 666);

            player.sendSystemMessage(Component.literal("§4WELCOME TO MY WORLD"));
            player.level().playSound(null, player.blockPosition(), SoundEvents.WITHER_DEATH, SoundSource.HOSTILE, 1.0f, 0.1f);
        });
    }

    @SubscribeEvent
    public void onChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        String playerName = player.getScoreboardName();
        String messageText = event.getRawText();

        if (messageText.equalsIgnoreCase("null")) {
            nullSpamCombo++;

            if (nullSpamCombo >= 5 && nullSpamCombo <= 8) {
                player.getServer().execute(() -> {
                    Component warning = Component.literal("<Null> ")
                            .append(Component.literal("STOP SPAMMING MY NAME IN THE CHAT")
                                    .withStyle(ChatFormatting.RED, ChatFormatting.BOLD));
                    player.getServer().getPlayerList().broadcastSystemMessage(warning, false);

                    player.level().playSound(null, player.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 1.0f, 0.1f);
                });
            }

            if (nullSpamCombo >= 9) {
                teleportToNULLWorld(player);
                nullSpamCombo = 0;
            }
        }

        if (trapActive) {
            event.setCanceled(true);
            String scaryText = "I'M WATCHING YOU LOOK BACK ";
            StringBuilder behindYou = new StringBuilder(scaryText);
            for (int i = 0; i < 7; i++) { behindYou.append("BEHIND YOU "); }

            Component glitchedMessage = Component.literal("<" + playerName + "> ")
                    .append(Component.literal(behindYou.toString()).withStyle(ChatFormatting.RED, ChatFormatting.BOLD));

            player.getServer().getPlayerList().broadcastSystemMessage(glitchedMessage, false);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.ENDERMAN_STARE, SoundSource.PLAYERS, 1.0F, 0.1F);

            Set<RelativeMovement> relativeMovements = EnumSet.of(
                    RelativeMovement.X,
                    RelativeMovement.Y,
                    RelativeMovement.Z,
                    RelativeMovement.X_ROT
            );

            player.connection.send(new ClientboundPlayerPositionPacket(
                    0, 0, 0, 180.0f, 0.0f, relativeMovements, 0
            ));

            MutableComponent joinMsg = Component.translatable("multiplayer.player.joined", "§0Null");

            player.getServer().getPlayerList().broadcastSystemMessage(joinMsg.withStyle(ChatFormatting.YELLOW), false);

            Vec3 spawnPos = player.position().add(player.getLookAngle().multiply(3, 0, 3));
            EnderMan nullEntity = EntityType.ENDERMAN.create(player.level());

            if (nullEntity != null) {
                nullEntity.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, 0, 0);
                nullEntity.setCustomName(Component.literal("§0Null"));
                nullEntity.setTarget(player);
                player.level().addFreshEntity(nullEntity);

                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 255, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP, 100, 255, false, false));
            }

            player.getServer().execute(() -> {
                java.util.concurrent.Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                    player.getServer().execute(() -> {
                        if (RANDOM.nextBoolean()) {
                            player.connection.disconnect(Component.literal("§4[FATAL ERROR]§r\nNull.java:666 - Unexpected Entity Presence\nMemory corrupted at 0x000DEAD"));
                        } else {
                            if (nullEntity != null) nullEntity.discard();
                        }
                    });
                }, 3, java.util.concurrent.TimeUnit.SECONDS);
            });

            trapActive = false;
            return;
        }

        if (messageText.length() > 2) {
            chatHistory.add(messageText);
            if (chatHistory.size() > 30) chatHistory.remove(0);
        }

        if (RANDOM.nextInt(40) == 0) {
            event.setCanceled(true);

            String[] glitchMessages = {
                    "I CAN SEE YOU",
                    "NULL IS HERE",
                    "HAVE YOU EVER WONDERED WHAT WILL HAPPEN TO YOUR WORLD?",
                    "SYSTEM ERROR: PLAYER_SOUL_NOT_FOUND"
            };
            String anomalyText = glitchMessages[RANDOM.nextInt(glitchMessages.length)];

            Component fakeMessage = Component.literal("<" + playerName + "> ")
                    .append(Component.literal(anomalyText).withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));

            player.getServer().getPlayerList().broadcastSystemMessage(fakeMessage, false);
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.ENDERMAN_STARE, SoundSource.PLAYERS, 1.0F, 0.1F);
            return;
        }

        if (RANDOM.nextInt(60) == 0 && !chatHistory.isEmpty()) {
            String echo = chatHistory.get(RANDOM.nextInt(chatHistory.size()));

            player.getServer().execute(() -> {
                Component echoMsg = Component.literal("<Null> ").append(Component.literal(echo).withStyle(ChatFormatting.ITALIC));
                player.getServer().getPlayerList().broadcastSystemMessage(echoMsg, false);
                trapActive = true;
            });
        }
    }

    private void scheduleNext(Player player, int delay) {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            if (player != null && player.getServer() != null && player.isAlive()) {
                player.getServer().execute(() -> runAnomaly(player));
            }
            scheduler.shutdown();
        }, delay, TimeUnit.SECONDS);
    }

    @SubscribeEvent
    public void onSystemMessage(ClientChatReceivedEvent event) {
        if (event.getMessage() instanceof MutableComponent mutable) {
            String key = "";
            if (mutable.getContents() instanceof TranslatableContents translatable) {
                key = translatable.getKey();
            }

            if (key.equals("multiplayer.player.joined")) {
                event.setMessage(Component.literal("§4%s has come for you").withStyle(ChatFormatting.ITALIC));
            } else if (key.equals("container.inventory")) {
                event.setMessage(Component.literal("§8THEY ARE NOT HERE"));
            } else if (key.equals("sleep.skipping_night")) {
                event.setMessage(Component.literal("§0YOU CANNOT HIDE IN DREAMS"));
            }
        }
    }

    @SubscribeEvent
    public void onCommand(CommandEvent event) {
        ParseResults<CommandSourceStack> results = event.getParseResults();
        String command = results.getReader().getString();
        ServerPlayer player = event.getParseResults().getContext().getSource().getPlayer();

        if (player != null && (command.startsWith("/tp") || command.startsWith("/gamemode"))) {
            player.getServer().execute(() -> {
                Component powerMsg = Component.literal("Unknown command: ")
                        .append(Component.literal("YOU HAVE NO POWER HERE").withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.RED));
                player.sendSystemMessage(powerMsg);
                player.level().playSound(null, player.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.MASTER, 0.5f, 0.1f);
            });
        }
    }

    public static int flashTimer = 0;

    public static void triggerFlash() {
        flashTimer = 3;
    }

    public static void decrementFlashTimer() {
        if (flashTimer > 0) flashTimer--;
    }

    private void runAnomaly(Player player) {
        String name = NAMES[RANDOM.nextInt(NAMES.length)];
        player.level().players().forEach(p -> p.sendSystemMessage(
                Component.translatable("multiplayer.player.joined", name).withStyle(ChatFormatting.YELLOW)
        ));
        player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 140, 0));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                net.minecraft.sounds.SoundEvents.GHAST_SCREAM, net.minecraft.sounds.SoundSource.HOSTILE, 1.5F, 0.1F);
        finish(player, name);
        scheduleNext(player, RANDOM.nextInt(900) + 600);
    }

    private void finish(Player player, String name) {
        ScheduledExecutorService quitter = Executors.newSingleThreadScheduledExecutor();
        quitter.schedule(() -> {
            if (player.getServer() != null) {
                player.getServer().execute(() -> {
                    player.level().players().forEach(p -> p.sendSystemMessage(
                            Component.translatable("multiplayer.player.left", name).withStyle(ChatFormatting.YELLOW)
                    ));
                });
            }
            quitter.shutdown();
        }, 10, TimeUnit.SECONDS);
    }
}
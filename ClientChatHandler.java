package com.User.the_chat_fake;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "the_chat_fake", value = Dist.CLIENT)
public class ClientChatHandler {

    @SubscribeEvent
    public static void onChatOpen(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof ChatScreen) {

            if (Math.random() < 0.1D) {

                String[] ghostInputs = {
                        "I AM STILL HERE",
                        "STOP LOOKING FOR ME",
                        "NULL IS WATCHING YOU",
                        "BEHIND YOU",
                        "I CAN SEE YOUR SCREEN",
                        "HELP ME",
                        "YOU SHOULD NOT BE HERE"
                };

                String randomText = ghostInputs[(int) (Math.random() * ghostInputs.length)];

                Minecraft.getInstance().tell(() -> {
                    if (Minecraft.getInstance().screen instanceof ChatScreen currentChat) {
                        currentChat.handleChatInput(randomText, false);
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        if (TheChatFake.flashTimer > 0) {
            GuiGraphics graphics = event.getGuiGraphics();
            int width = event.getWindow().getGuiScaledWidth();
            int height = event.getWindow().getGuiScaledHeight();

            graphics.fill(0, 0, width, height, 0xFF000000);

            graphics.drawCenteredString(Minecraft.getInstance().font,
                    Component.literal("NULL").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                    width / 2, height / 2, 0xFFFFFF);

            TheChatFake.decrementFlashTimer();
        }
    }
}

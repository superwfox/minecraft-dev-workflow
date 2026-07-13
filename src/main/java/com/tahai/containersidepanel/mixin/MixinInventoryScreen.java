package com.tahai.containersidepanel.mixin;

import com.tahai.containersidepanel.config.ModConfig;
import com.tahai.containersidepanel.mapping.ContainerMergeManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(InventoryScreen.class)
public abstract class MixinInventoryScreen {

    @Unique
    private static final ModConfig CONFIG = new ModConfig();

    @Unique
    private static final ContainerMergeManager MERGE_MANAGER = new ContainerMergeManager();

    @Unique
    private boolean panelInteractionActive = false;

    @Unique
    private int panelWidth = 60;

    @Unique
    private int panelX, panelY;

    // Replaces the removed @Invoker: directly call the inherited protected method.
    private Slot getSlotAtInInventory(double mouseX, double mouseY) {
        return ((HandledScreen<?>) (Object) this).getSlotAt(mouseX, mouseY);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        InventoryScreen self = (InventoryScreen) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !CONFIG.isEnabled()) return;

        PlayerInventory inventory = client.player.getInventory();
        MERGE_MANAGER.updateMapping(inventory);

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();
        int guiLeft = self.x;
        int guiTop = self.y;
        int guiWidth = self.width;
        int guiHeight = self.height;

        panelX = guiLeft + guiWidth + 4;
        panelY = guiTop;
        panelWidth = Math.min(screenWidth - panelX - 4, 60);
        int panelHeight = guiHeight;

        // Background
        context.fill(panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0x80000000);
        // Border (using primitive lines because drawBorder does not exist in 1.21)
        context.drawHorizontalLine(panelX, panelX + panelWidth - 1, panelY, 0xFFFFFFFF);
        context.drawHorizontalLine(panelX, panelX + panelWidth - 1, panelY + panelHeight - 1, 0xFFFFFFFF);
        context.drawVerticalLine(panelX, panelY, panelY + panelHeight - 1, 0xFFFFFFFF);
        context.drawVerticalLine(panelX + panelWidth - 1, panelY, panelY + panelHeight - 1, 0xFFFFFFFF);

        // Title placeholder
        context.drawText(client.textRenderer, Text.literal("Side Panel"), panelX + 4, panelY + 4, 0xFFFFFFFF, true);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        InventoryScreen self = (InventoryScreen) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        if (!CONFIG.isEnabled() || client.player == null) return;

        if (mouseX >= panelX && mouseX <= panelX + panelWidth &&
                mouseY >= panelY && mouseY <= panelY + self.height) {
            boolean shift = client.options.sneakKey.isPressed();
            ClientPlayerInteractionManager interaction = client.interactionManager;
            if (interaction == null) return;

            int slotIndex = (int) ((mouseY - panelY) / 18);
            if (slotIndex < 0 || slotIndex >= 27) return;

            Set<Integer> locked = MERGE_MANAGER.getLockedSlots();
            if (locked.contains(slotIndex)) return;

            interaction.clickSlot(
                    client.player.currentScreenHandler.syncId,
                    slotIndex,
                    0,
                    SlotActionType.QUICK_MOVE,
                    client.player
            );
            cir.setReturnValue(true);
            return;
        }

        if (button == 0 && client.options.sprintKey.isPressed()) {
            Slot slot = getSlotAtInInventory(mouseX, mouseY);
            if (slot != null && slot.inventory == client.player.getInventory()) {
                ItemStack stack = slot.getStack();
                if (!stack.isEmpty()) {
                    MERGE_MANAGER.insertItem(client.player.getInventory(), stack);
                    // Further insertion logic would be needed for a full interaction.
                }
            }
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        InventoryScreen self = (InventoryScreen) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        if (!CONFIG.isEnabled() || client.player == null) return;

        if (keyCode == GLFW.GLFW_KEY_R) {
            boolean control = (modifiers & (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SUPER)) != 0;
            if (control) {
                MERGE_MANAGER.reorganize(client.player.getInventory());
                cir.setReturnValue(true);
                return;
            } else {
                double mouseX = client.mouse.getX() * client.getWindow().getScaledWidth() / client.getWindow().getWidth();
                double mouseY = client.mouse.getY() * client.getWindow().getScaledHeight() / client.getWindow().getHeight();
                Slot slot = getSlotAtInInventory(mouseX, mouseY);
                if (slot != null && slot.inventory == client.player.getInventory()) {
                    MERGE_MANAGER.reorganize(client.player.getInventory());
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void onMouseScrolled(double mouseX, double mouseY, double horizontalAmount,
                                 double verticalAmount, CallbackInfoReturnable<Boolean> cir) {
        InventoryScreen self = (InventoryScreen) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();
        if (!CONFIG.isEnabled() || client.player == null) return;

        if (mouseX >= panelX && mouseX <= panelX + panelWidth &&
                mouseY >= panelY && mouseY <= panelY + self.height) {
            int direction = verticalAmount > 0 ? 1 : -1;
            Slot slot = getSlotAtInInventory(mouseX, mouseY);
            if (slot != null) {
                int fromSlot = slot.id;
                int toSlot = fromSlot + direction * 9;
                if (toSlot >= 0 && toSlot < 9 + 27 + 27) {
                    MERGE_MANAGER.simulateScrollMove(client.player.getInventory(), fromSlot, toSlot);
                    ClientPlayerInteractionManager interaction = client.interactionManager;
                    if (interaction != null) {
                        interaction.clickSlot(self.getScreenHandler().syncId, fromSlot, 0,
                                SlotActionType.PICKUP, client.player);
                        interaction.clickSlot(self.getScreenHandler().syncId, toSlot, 0,
                                SlotActionType.PICKUP, client.player);
                    }
                    cir.setReturnValue(true);
                    return;
                }
            }
        }
    }

    @Inject(method = "removed", at = @At("HEAD"))
    private void onRemoved(CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            MERGE_MANAGER.updateMapping(client.player.getInventory());
            panelInteractionActive = false;
        }
    }
}
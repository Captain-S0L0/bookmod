package com.terriblefriends.bookmod.mixin;

import com.terriblefriends.bookmod.Attribute;
import com.terriblefriends.bookmod.JsonNbtTools;
import com.terriblefriends.bookmod.NbtException;
import com.terriblefriends.bookmod.mixin.accessor.NbtCompoundAccessor;
import com.terriblefriends.bookmod.mixin.accessor.NbtListAccessor;
import net.minecraft.client.class_411;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.BookEditScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.*;

@Mixin(BookEditScreen.class)
public abstract class BookEditScreenMixin extends Screen {
    private static final boolean NBT_PAGE_EDIT = Boolean.parseBoolean(System.getProperty("bookmod.nbtPageEdit"));

    @Shadow @Final private boolean writeable;
    @Shadow private boolean signing;
    @Shadow private ButtonWidget doneButton;
    @Shadow private ButtonWidget signButton;
    @Shadow private ButtonWidget finalizeButton;
    @Shadow private ButtonWidget cancelButton;
    @Shadow private String title;
    @Shadow private int totalPages;
    @Shadow private int currentPage;
    @Shadow private NbtList pages;
    //next page button
    @Shadow private class_411 field_1364;
    //previous page button
    @Shadow private class_411 field_1365;
    @Shadow @Final private ItemStack item;
    @Shadow @Final private PlayerEntity reader;

    @Shadow protected abstract void writeText(String text);
    @Shadow protected abstract void updateButtons();

    //custom author
    private TextFieldWidget customAuthorText;

    //NBT editor
    private TextFieldWidget nbtText;
    private boolean editingNbt = false;

    //enchantment adder
    private TextFieldWidget enchantmentIdText;
    private TextFieldWidget enchantmentLevelText;
    private ButtonWidget enchantmentAddButton;

    //attribute adder
    private TextFieldWidget attributeNameText;
    private TextFieldWidget attributeAmountText;
    private TextFieldWidget attributeOperatorText;
    private ButtonWidget attributeAddButton;

    //lore adder
    private TextFieldWidget loreText;
    private ButtonWidget loreAddButton;

    //button math bitwise bits
    private static final int generalButtonBit = (1 << 31);
    private static final int presetMiscButtonBit = (1 << 30);
    private static final int presetEnchantButtonBit = (1 << 29);
    private static final int presetAttributeButtonBit = (1 << 28);
    private static final int formattingButtonBit = (1 << 27);

    //ui element arrays for efficiency
    private TextFieldWidget[] textFields = null;
    private ButtonWidget[] formattingButtons = null;
    private ButtonWidget[] editOnlyButtons = null;

    @Inject(at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/screen/ingame/BookEditScreen;updateButtons()V",shift= At.Shift.BEFORE),method="init")
    private void bookmod$setupCustomButtons(CallbackInfo ci) {
        if (this.writeable) {
            this.editOnlyButtons = new ButtonWidget[17];
            this.formattingButtons = new ButtonWidget[22];

            //general buttons
            this.buttons.add(this.editOnlyButtons[0] = new ButtonWidget(1 + generalButtonBit, this.width / 2 - 100, 25 + 192, 98, 20, "Clear NBT"));
            this.buttons.add(this.editOnlyButtons[1] = new ButtonWidget(2 + generalButtonBit, this.width / 2 + 2, 25 + 192, 98, 20, "Edit NBT"));
            this.buttons.add(this.enchantmentAddButton = new ButtonWidget(3 + generalButtonBit, this.width / 2 + 75, 34, 96, 20, "Add Enchantment"));
            this.buttons.add(this.loreAddButton = new ButtonWidget(4 + generalButtonBit, this.width / 2 + 75, 86, 96, 20, "Add Lore"));
            this.buttons.add(this.attributeAddButton = new ButtonWidget(5 + generalButtonBit, this.width / 2 + 75, 170, 96, 20, "Add Attribute"));

            this.editOnlyButtons[2] = this.enchantmentAddButton;
            this.editOnlyButtons[3] = this.loreAddButton;
            this.editOnlyButtons[4] = this.attributeAddButton;

            //formatting buttons
            this.buttons.add(this.formattingButtons[0] = new ButtonWidget(1 + formattingButtonBit, this.width / 2 - 99, 4, 20, 20, " §0#"));
            this.buttons.add(this.formattingButtons[1] = new ButtonWidget(2 + formattingButtonBit, this.width / 2 - 99, 24, 20, 20, " §1#"));
            this.buttons.add(this.formattingButtons[2] = new ButtonWidget(3 + formattingButtonBit, this.width / 2 - 99, 44, 20, 20, " §2#"));
            this.buttons.add(this.formattingButtons[3] = new ButtonWidget(4 + formattingButtonBit, this.width / 2 - 99, 64, 20, 20, " §3#"));
            this.buttons.add(this.formattingButtons[4] = new ButtonWidget(5 + formattingButtonBit, this.width / 2 - 99, 84, 20, 20, " §4#"));
            this.buttons.add(this.formattingButtons[5] = new ButtonWidget(6 + formattingButtonBit, this.width / 2 - 99, 104, 20, 20, " §5#"));
            this.buttons.add(this.formattingButtons[6] = new ButtonWidget(7 + formattingButtonBit, this.width / 2 - 99, 124, 20, 20, " §6#"));
            this.buttons.add(this.formattingButtons[7] = new ButtonWidget(8 + formattingButtonBit, this.width / 2 - 99, 144, 20, 20, " §7#"));
            this.buttons.add(this.formattingButtons[8] = new ButtonWidget(9 + formattingButtonBit, this.width / 2 - 119, 4, 20, 20, " §8#"));
            this.buttons.add(this.formattingButtons[9] = new ButtonWidget(10 + formattingButtonBit, this.width / 2 - 119, 24, 20, 20, " §9#"));
            this.buttons.add(this.formattingButtons[10] = new ButtonWidget(11 + formattingButtonBit, this.width / 2 - 119, 44, 20, 20, " §a#"));
            this.buttons.add(this.formattingButtons[11] = new ButtonWidget(12 + formattingButtonBit, this.width / 2 - 119, 64, 20, 20, " §b#"));
            this.buttons.add(this.formattingButtons[12] = new ButtonWidget(13 + formattingButtonBit, this.width / 2 - 119, 84, 20, 20, " §c#"));
            this.buttons.add(this.formattingButtons[13] = new ButtonWidget(14 + formattingButtonBit, this.width / 2 - 119, 104, 20, 20, " §d#"));
            this.buttons.add(this.formattingButtons[14] = new ButtonWidget(15 + formattingButtonBit, this.width / 2 - 119, 124, 20, 20, " §e#"));
            this.buttons.add(this.formattingButtons[15] = new ButtonWidget(16 + formattingButtonBit, this.width / 2 - 119, 144, 20, 20, " §f#"));

            this.buttons.add(this.formattingButtons[16] = new ButtonWidget(17 + formattingButtonBit, this.width / 2 - 139, 4, 20, 20, " §k#"));
            this.buttons.add(this.formattingButtons[17] = new ButtonWidget(18 + formattingButtonBit, this.width / 2 - 139, 24, 20, 20, " §l#"));
            this.buttons.add(this.formattingButtons[18] = new ButtonWidget(19 + formattingButtonBit, this.width / 2 - 139, 44, 20, 20, " §m#"));
            this.buttons.add(this.formattingButtons[19] = new ButtonWidget(20 + formattingButtonBit, this.width / 2 - 139, 64, 20, 20, " §n#"));
            this.buttons.add(this.formattingButtons[20] = new ButtonWidget(21 + formattingButtonBit, this.width / 2 - 139, 84, 20, 20, " §o#"));
            this.buttons.add(this.formattingButtons[21] = new ButtonWidget(22 + formattingButtonBit, this.width / 2 - 139, 104, 20, 20, "CLR"));

            //misc buttons

            this.buttons.add(this.editOnlyButtons[5] = new ButtonWidget(1 + presetMiscButtonBit, 4, 4, 70, 20, "High Data"));
            this.buttons.add(this.editOnlyButtons[6] = new ButtonWidget(2 + presetMiscButtonBit, 4, 24, 70, 20, "Tattered"));
            this.buttons.add(this.editOnlyButtons[7] = new ButtonWidget(3 + presetMiscButtonBit, 4, 44, 70, 20, "Unbreakable"));
            this.buttons.add(this.editOnlyButtons[8] = new ButtonWidget(4 + presetMiscButtonBit, 4, 64, 70, 20, "No Ench"));
            this.buttons.add(this.editOnlyButtons[9] = new ButtonWidget(5 + presetMiscButtonBit, 4, 84, 70, 20, "No Attr"));
            this.buttons.add(this.editOnlyButtons[10] = new ButtonWidget(6 + presetMiscButtonBit, 4, 104, 70, 20, "No Lore"));

            //enchantment preset buttons
            this.buttons.add(this.editOnlyButtons[11] = new ButtonWidget(1 + presetEnchantButtonBit, 4, 124, 70, 20, "32k"));
            this.buttons.add(this.editOnlyButtons[12] = new ButtonWidget(2 + presetEnchantButtonBit, 4, 144, 70, 20, "Utility Ench"));
            this.buttons.add(this.editOnlyButtons[13] = new ButtonWidget(3 + presetEnchantButtonBit, 4, 164, 70, 20, "Looting"));
            this.buttons.add(this.editOnlyButtons[14] = new ButtonWidget(4 + presetEnchantButtonBit, 4, 184, 70, 20, "XLooting"));

            //attribute preset buttons
            this.buttons.add(this.editOnlyButtons[15] = new ButtonWidget(1 + presetAttributeButtonBit, 4, 204, 70, 20, "Utility Attr"));
            this.buttons.add(this.editOnlyButtons[16] = new ButtonWidget(2 + presetAttributeButtonBit, 4, 224, 70, 20, "OP Attr"));

            //we have to re-order the sign button's position to the last in the list due to processing order so the book doesn't instantly sign itself
            this.buttons.remove(this.signButton);
            this.buttons.add(this.signButton);

            //text fields
            this.customAuthorText = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 192+27, 200, 20);
            this.customAuthorText.setMaxLength(65536);
            this.nbtText = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 192+27, 200, 20);
            this.nbtText.setMaxLength(65536);
            this.loreText = new TextFieldWidget(this.textRenderer, this.width / 2 +76, 64, 93, 20);
            this.loreText.setMaxLength(65536);
            this.enchantmentIdText = new TextFieldWidget(this.textRenderer, this.width / 2 +76, 12, 45, 20);
            this.enchantmentIdText.setMaxLength(6);
            this.enchantmentLevelText = new TextFieldWidget(this.textRenderer, this.width / 2 +124, 12, 45, 20);
            this.enchantmentLevelText.setMaxLength(6);
            this.attributeNameText = new TextFieldWidget(this.textRenderer, this.width / 2 + 76, 116, 93, 20);
            this.attributeNameText.setMaxLength(100);
            this.attributeAmountText = new TextFieldWidget(this.textRenderer, this.width / 2 + 76, 148, 45, 20);
            this.attributeOperatorText = new TextFieldWidget(this.textRenderer, this.width / 2 + 124, 148, 45, 20);
            this.attributeOperatorText.setMaxLength(1);

            this.textFields = new TextFieldWidget[]{
                    this.nbtText,
                    this.customAuthorText,
                    this.loreText,
                    this.enchantmentIdText,
                    this.enchantmentLevelText,
                    this.attributeNameText,
                    this.attributeAmountText,
                    this.attributeOperatorText
            };
        }
    }

    @Inject(at=@At("HEAD"),method="updateButtons",cancellable = true)
    private void bookmod$updateButtons(CallbackInfo ci) {
        this.field_1364.visible = !this.signing && (this.currentPage < this.totalPages - 1 || this.writeable);
        this.field_1365.visible = !this.signing && this.currentPage > 0;
        this.doneButton.visible = !this.signing;

        if (this.writeable) {
            boolean isOnEditScreen = !this.signing && !this.editingNbt;
            this.signButton.visible = isOnEditScreen;
            this.cancelButton.visible = this.signing;
            this.finalizeButton.visible = this.signing;

            for (ButtonWidget button : this.formattingButtons) {
                button.visible = !this.editingNbt;
            }

            for (ButtonWidget button : this.editOnlyButtons) {
                button.visible = isOnEditScreen;
            }

            this.customAuthorText.setVisible(this.signing);
            this.nbtText.setVisible(this.editingNbt);
            this.loreText.setVisible(isOnEditScreen);
            this.enchantmentIdText.setVisible(isOnEditScreen);
            this.enchantmentLevelText.setVisible(isOnEditScreen);
            this.attributeNameText.setVisible(isOnEditScreen);
            this.attributeAmountText.setVisible(isOnEditScreen);
            this.attributeOperatorText.setVisible(isOnEditScreen);

            for (TextFieldWidget field : this.textFields) {
                if (!field.isVisible()) {
                    field.setFocused(false);
                }
            }

            this.updateAddButtonStates();
        }

        ci.cancel();
    }

    @Inject(at=@At("HEAD"),method="buttonClicked",cancellable = true)
    private void bookmod$handleButton(ButtonWidget button, CallbackInfo ci) {
        if (button.active && button.visible) {
            //handle buttons that are always not null first
            if (button.id == this.doneButton.id) {
                if (!this.editingNbt) {
                    this.field_1229.openScreen(null);
                    if (this.writeable) {
                        this.sendBookData(false);
                    }
                }
                else {
                    this.editingNbt = false;

                    try {
                        NbtCompound editTag = JsonNbtTools.getTagFromJson(this.nbtText.getText());
                        if (NBT_PAGE_EDIT && editTag.contains("pages") && editTag.getList("pages").size() > 0 && ((NbtListAccessor)editTag.getList("pages")).bookmod$getType() == (byte)8) {
                            this.pages = editTag.getList("pages");
                        }
                        this.item.setNbt(editTag);
                    } catch (NbtException e) {
                        this.field_1229.inGameHud.getChatHud().method_898("§4[BookMod] Error! Failed to parse NBT!");
                        e.printStackTrace();
                    }
                }
            }
            else if (button.id == this.field_1364.id) {
                if (this.currentPage < this.totalPages - 1) {
                    this.currentPage++;
                }
                else if (this.writeable) {
                    if (this.pages == null) {
                        this.pages = new NbtList();
                    }
                    this.pages.method_1217(new NbtString(""+ (this.totalPages + 1), ""));
                    this.totalPages++;
                    this.currentPage++;
                }
            }
            else if (button.id == this.field_1365.id) {
                if (this.currentPage > 0) {
                    this.currentPage--;
                }
            }

            //beyond here everything is non-null

            //handle vanilla buttons
            else if (button.id == this.signButton.id) {
                this.signing = true;
            }
            else if (button.id == this.finalizeButton.id) {
                this.sendBookData(true);
                this.field_1229.openScreen(null);
            }
            else if (button.id == this.cancelButton.id) {
                this.signing = false;
            }

            //handle general buttons
            else if ((button.id & generalButtonBit) > 0) {
                switch (button.id ^ generalButtonBit) {
                    case 1:
                        //clear
                        this.item.setNbt(new NbtCompound());
                        this.pages = new NbtList();
                        this.pages.method_1217(new NbtString("1", ""));
                        this.item.putSubNbt("pages", this.pages);
                        this.totalPages = 1;
                        this.currentPage = 0;
                        break;
                    case 2:
                        //edit nbt
                        this.editingNbt = true;
                        if (!this.item.hasNbt()) {
                            this.item.setNbt(new NbtCompound());
                        }
                        NbtCompound editTag = (NbtCompound)this.item.getNbt().copy();
                        if (NBT_PAGE_EDIT) {
                            editTag.put("pages", this.pages);
                        }
                        else if (editTag.contains("pages")) {
                            ((NbtCompoundAccessor)editTag).bookmod$getData().remove("pages");
                        }
                        this.nbtText.setText(editTag.toString());
                        break;
                    case 3:
                        //add enchantment
                        try {
                            short id = Short.parseShort(this.enchantmentIdText.getText());
                            short lvl = Short.parseShort(this.enchantmentLevelText.getText());

                            if (!this.item.hasNbt()) {
                                this.item.setNbt(new NbtCompound());
                            }
                            if (!this.item.getNbt().contains("ench")) {
                                this.item.putSubNbt("ench", new NbtList());
                            }
                            NbtCompound enchantmentTag = new NbtCompound();
                            enchantmentTag.putShort("id", id);
                            enchantmentTag.putShort("lvl", lvl);
                            this.item.getNbt().getList("ench").method_1217(enchantmentTag);
                            this.enchantmentIdText.setText("");
                            this.enchantmentLevelText.setText("");
                        }
                        catch (NumberFormatException e) {
                            this.field_1229.inGameHud.getChatHud().method_898("§4[BookMod] Error! Failed to parse enchantment!");
                            e.printStackTrace();
                        }
                        break;
                    case 4:
                        //lore add
                        if (!this.item.hasNbt()) {
                            this.item.setNbt(new NbtCompound());
                        }
                        if (!this.item.getNbt().contains("display")) {
                            this.item.putSubNbt("display", new NbtCompound());
                        }
                        if (!this.item.getNbt().getCompound("display").contains("Lore")) {
                            this.item.getNbt().getCompound("display").put("Lore", new NbtList());
                        }
                        this.item.getNbt().getCompound("display").getList("Lore").method_1217(new NbtString(null, this.loreText.getText()));
                        this.loreText.setText("");
                        break;
                    case 5:
                        //attribute add
                        try {
                            String attributeName = this.attributeNameText.getText();
                            double attributeAmount = Double.parseDouble(this.attributeAmountText.getText());
                            int attributeOperation = Integer.parseInt(this.attributeOperatorText.getText());

                            if (attributeName.length() == 0) {
                                this.field_1229.inGameHud.getChatHud().method_898("§4[BookMod] Error! No attribute name!");
                                return;
                            }
                            if (attributeOperation < 0 || attributeOperation > 2) {
                                this.field_1229.inGameHud.getChatHud().method_898("§4[BookMod] Error! Invalid attribute operation!");
                                return;
                            }

                            Attribute attribute = new Attribute(attributeName, attributeAmount, attributeOperation);

                            if (!this.item.hasNbt()) {
                                this.item.setNbt(new NbtCompound());
                            }
                            if (!this.item.getNbt().contains("AttributeModifiers")) {
                                this.item.getNbt().put("AttributeModifiers", new NbtList());
                            }
                            this.item.getNbt().getList("AttributeModifiers").method_1217(attribute.writeToTag());
                            this.attributeNameText.setText("");
                            this.attributeAmountText.setText("");
                            this.attributeOperatorText.setText("");
                        } catch (NumberFormatException e) {
                            this.field_1229.inGameHud.getChatHud().method_898("§4[BookMod] Error! Failed to parse attribute!");
                            e.printStackTrace();
                        }
                        break;
                }
            }

            //handle misc buttons
            else if ((button.id & presetMiscButtonBit) > 0) {
                switch (button.id ^ generalButtonBit) {
                    case 1:
                        //high data
                        NbtList highDataPages = new NbtList();
                        Random highDataRandom = new Random();
                        for (int page = 1; page < 51; page++) {
                            StringBuilder pageBuilder = new StringBuilder();
                            for (int character = 0; character < 218; character++) {
                                pageBuilder.append((char)(highDataRandom.nextInt(63488)+2048));
                            }
                            highDataPages.method_1217(new NbtString(""+page,pageBuilder.toString()));
                        }
                        this.pages = highDataPages;
                        this.totalPages = 50;
                        this.currentPage = 0;
                        break;
                    case 2:
                        //tattered
                        this.item.putSubNbt("generation", new NbtInt("", 3));
                        break;
                    case 3:
                        //unbreakable
                        this.item.putSubNbt("Unbreakable", new NbtByte("", (byte)1));
                        break;
                    case 4:
                        //clear enchantments
                        if (!this.item.hasNbt()) {
                            this.item.setNbt(new NbtCompound());
                        }
                        if (this.item.getNbt().contains("ench")) {
                            ((NbtCompoundAccessor)this.item.getNbt()).bookmod$getData().remove("ench");
                        }
                        break;
                    case 5:
                        //clear attributes
                        if (!this.item.hasNbt()) {
                            this.item.setNbt(new NbtCompound());
                        }
                        if (this.item.getNbt().contains("AttributeModifiers")) {
                            ((NbtCompoundAccessor)this.item.getNbt()).bookmod$getData().remove("AttributeModifiers");
                        }
                        break;
                    case 6:
                        //clear lore
                        if (!this.item.hasNbt()) {
                            this.item.setNbt(new NbtCompound());
                        }
                        if (this.item.getNbt().contains("display")) {
                            if (this.item.getNbt().getCompound("display").contains("Lore")) {
                                ((NbtCompoundAccessor)this.item.getNbt().getCompound("display")).bookmod$getData().remove("Lore");
                            }
                            if (this.item.getNbt().getCompound("display").values().size() == 0) {
                                ((NbtCompoundAccessor)this.item.getNbt()).bookmod$getData().remove("display");
                            }
                        }
                        break;
                }
            }

            //handle preset buttons
            else if ((button.id & presetEnchantButtonBit) > 0) {
                HashMap<Short, Short> enchantsToAdd = new HashMap<>();

                switch (button.id ^ presetEnchantButtonBit) {
                    case 1:
                        for (short id = 0; id < 72; id++) {
                            enchantsToAdd.put(id, (short) 32767);
                        }
                        break;
                    case 2:
                        enchantsToAdd.put((short)7, (short) 32767);
                        enchantsToAdd.put((short)16, (short) 32767);
                        enchantsToAdd.put((short)20, (short) 32767);
                        enchantsToAdd.put((short)32, (short) 32767);
                        enchantsToAdd.put((short)71, (short) 1);
                        break;
                    case 3:
                        enchantsToAdd.put((short)7, (short) 32767);
                        enchantsToAdd.put((short)16, (short) 32767);
                        enchantsToAdd.put((short)20, (short) 32767);
                        enchantsToAdd.put((short)32, (short) 32767);
                        enchantsToAdd.put((short)21, (short) 1000);
                        enchantsToAdd.put((short)71, (short) 1);
                        break;
                    case 4:
                        enchantsToAdd.put((short)7, (short) 32767);
                        enchantsToAdd.put((short)16, (short) 32767);
                        enchantsToAdd.put((short)20, (short) 32767);
                        enchantsToAdd.put((short)21, (short) 32767);
                        enchantsToAdd.put((short)22, (short) 32767);
                        enchantsToAdd.put((short)32, (short) 32767);
                        enchantsToAdd.put((short)71, (short) 1);
                        break;
                    default:
                        break;
                }

                NbtList enchantList = new NbtList();
                for (Map.Entry<Short, Short> entry : enchantsToAdd.entrySet()) {
                    NbtCompound enchantTag = new NbtCompound();
                    enchantTag.putShort("id", entry.getKey());
                    enchantTag.putShort("lvl", entry.getValue());
                    enchantList.method_1217(enchantTag);
                }
                this.item.putSubNbt("ench", enchantList);
            }
            else if ((button.id & presetAttributeButtonBit) > 0) {
                List<Attribute> attributesToAdd = new ArrayList<>();

                switch (button.id ^ presetAttributeButtonBit) {
                    case 1:
                        attributesToAdd.add(new Attribute("generic.movementSpeed", 1, 2));
                        attributesToAdd.add(new Attribute("generic.attackDamage", Double.POSITIVE_INFINITY, 0));
                        attributesToAdd.add(new Attribute("generic.maxHealth", 1, 2));
                        break;
                    case 2:
                        attributesToAdd.add(new Attribute("generic.movementSpeed", 1, 2));
                        attributesToAdd.add(new Attribute("generic.attackDamage", Double.POSITIVE_INFINITY, 0));
                        attributesToAdd.add(new Attribute("generic.maxHealth", 9, 2));
                        attributesToAdd.add(new Attribute("generic.attackKnockback", 9, 2));
                        attributesToAdd.add(new Attribute("generic.luck", Double.POSITIVE_INFINITY, 0));
                        attributesToAdd.add(new Attribute("generic.knockbackResistance", 1, 0));
                        attributesToAdd.add(new Attribute("generic.armor", 100, 0));
                        attributesToAdd.add(new Attribute("generic.armorToughness", 100, 0));
                        break;
                    default:
                        break;
                }

                NbtList attributeList = new NbtList();
                
                for (Attribute attribute : attributesToAdd) {
                    attributeList.method_1217(attribute.writeToTag());
                }
                
                this.item.putSubNbt("AttributeModifiers", attributeList);
            }

            //handle formatting buttons
            else if ((button.id & formattingButtonBit) > 0) {
                if (!this.attributeAmountText.isFocused() && !this.attributeNameText.isFocused() && !this.attributeOperatorText.isFocused()
                        && !this.enchantmentIdText.isFocused() && !this.enchantmentLevelText.isFocused()
                ) {

                    String toAdd;
                    if ((button.id ^ formattingButtonBit) == 22) {
                        toAdd = "§r§0";
                    }
                    else {
                        toAdd = button.message.substring(1, button.message.length() - 1);
                    }

                    if (this.signing) {
                        if (this.customAuthorText.isFocused()) {
                            if (this.customAuthorText.getText().length() + toAdd.length() < 65536) {
                                this.customAuthorText.setText(this.customAuthorText.getText() + toAdd);
                            }
                        }
                        else {
                            if (this.title.length() + toAdd.length() <= 16) {
                                this.title += toAdd;
                            }
                        }
                    }
                    else if (!this.editingNbt) {
                        if (this.loreText.isFocused()) {
                            this.loreText.setText(this.loreText.getText().substring(0,this.loreText.getCursor())+toAdd+this.loreText.getText().substring(this.loreText.getCursor()));
                        }
                        else {
                            this.writeText(toAdd);
                        }
                    }
                }
            }

            this.updateButtons();
            this.updateAddButtonStates();
        }

        ci.cancel();
    }

    @Inject(at=@At("HEAD"),method="keyPressed",cancellable = true)
    private void bookmod$updateTextBoxes(char code, int par2, CallbackInfo ci) {
        boolean fieldFocused = false;

        if (this.writeable) {
            for (TextFieldWidget field : this.textFields) {
                if (field.isVisible()) {
                    field.keyPressed(code, par2);
                }
                if (field.isFocused()) {
                    fieldFocused = true;
                }
            }
            this.updateAddButtonStates();
        }

        super.keyPressed(code, par2);

        if (this.editingNbt || fieldFocused) {
            ci.cancel();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) {
        super.mouseClicked(mouseX, mouseY, button);

        if (this.writeable) {
            for (TextFieldWidget field : this.textFields) {
                if (field.isVisible()) {
                    field.mouseClicked(mouseX, mouseY, button);
                }
            }
        }
    }

    @Inject(at=@At("HEAD"),method="render")
    private void bookmod$renderCustom(int mouseY, int tickDelta, float par3, CallbackInfo ci) {
        if (this.writeable) {
            for (TextFieldWidget field : this.textFields) {
                if (field.isVisible()) {
                    field.render();
                }
            }

            if (this.editingNbt) {
                this.nbtText.render();
                this.textRenderer.method_956("NBT", this.width / 2 + 104, 192 + 33, 0xFFFFFF);
            }
            else if (this.signing) {
                this.customAuthorText.render();
                this.textRenderer.method_956("Author", this.width / 2 + 104, 192 + 33, 0xFFFFFF);
            }
            else {
                this.loreText.render();
                this.enchantmentIdText.render();
                this.enchantmentLevelText.render();
                this.attributeNameText.render();
                this.attributeAmountText.render();
                this.attributeOperatorText.render();
                this.textRenderer.method_956("ID", this.width / 2 + 95, 3, 0xFFFFFF);
                this.textRenderer.method_956("Level", this.width / 2 + 134, 3, 0xFFFFFF);
                this.textRenderer.method_956("Lore", this.width / 2 + 110, 55, 0xFFFFFF);
                this.textRenderer.method_956("Attribute", this.width / 2 + 100, 107, 0xFFFFFF);
                this.textRenderer.method_956("Amount", this.width / 2 + 83, 138, 0xFFFFFF);
                this.textRenderer.method_956("Operator", this.width / 2 + 124, 138, 0xFFFFFF);
            }
        }
    }

    @Redirect(at=@At(value="FIELD", target="Lnet/minecraft/client/gui/screen/ingame/BookEditScreen;writeable:Z"),method="render")
    private boolean bookmod$stopCursorBlink(BookEditScreen instance) {
        if (!this.writeable) {
            return false;
        }

        boolean fieldFocused = false;

        for (TextFieldWidget field : this.textFields) {
            if (field.isFocused()) {
                fieldFocused = true;
                break;
            }
        }

        return !fieldFocused;
    }

    private void sendBookData(boolean signing) {
        if (this.writeable && this.pages != null) {
            //clear empty pages
            while (this.pages.size() > 1) {
                NbtString pageTag = (NbtString) this.pages.method_1218(this.pages.size() - 1);

                if (pageTag.value != null && pageTag.value.length() != 0) {
                    break;
                }

                this.pages.remove(this.pages.size() - 1);
            }

            //add new NbtCompound if the book doesn't have it yet
            if (!this.item.hasNbt()) {
                this.item.setNbt(new NbtCompound());
            }

            //set page tag
            this.item.putSubNbt("pages", this.pages);

            //set payload id
            String payloadId = "MC|BEdit";

            if (signing)
            {
                //change payload id for signing
                payloadId = "MC|BSign";

                //if we didn't enter a custom author, use the current player
                if (!customAuthorText.getText().equals("")) {
                    this.item.putSubNbt("author", new NbtString("author", customAuthorText.getText()));
                }
                else {
                    this.item.putSubNbt("author", new NbtString("author", this.reader.username));
                }
                this.item.putSubNbt("title", new NbtString("title", this.title.trim()));
                this.item.id = Item.WRITTEN_BOOK.id;
            }

            //write the packet
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

            try
            {
                Packet.writeItemStack(this.item, dataOutputStream);
                this.field_1229.method_2960().sendPacket(new CustomPayloadC2SPacket(payloadId, byteArrayOutputStream.toByteArray()));
            }
            catch (IllegalArgumentException e) {
                this.field_1229.inGameHud.getChatHud().method_898("§4[BookMod] Error! Book has too much data! (> 32767 bytes)");
            }
            catch (Exception e)
            {
                this.field_1229.inGameHud.getChatHud().method_898("§4[BookMod] Error! Something went wrong! (See logs)");
                e.printStackTrace();
            }
        }
    }

    private void updateAddButtonStates() {
        if (this.writeable && !this.editingNbt && !this.signing) {
            this.loreAddButton.active = this.loreText.getText().length() > 0;

            try {
                Short.parseShort(this.enchantmentIdText.getText());
                Short.parseShort(this.enchantmentLevelText.getText());
                this.enchantmentAddButton.active = true;
            }
            catch (NumberFormatException e) {
                this.enchantmentAddButton.active = false;
            }

            try {
                Double.parseDouble(this.attributeAmountText.getText());
                int attributeOperation = Integer.parseInt(this.attributeOperatorText.getText());

                this.attributeAddButton.active = this.attributeNameText.getText().length() > 0 && attributeOperation >= 0 && attributeOperation <= 2;
            } catch (NumberFormatException e) {
                this.attributeAddButton.active = false;
            }
        }
    }
}

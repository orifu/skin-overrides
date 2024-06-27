package net.orifu.skin_overrides.screen;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.ButtonWidget;
import net.minecraft.client.gui.widget.layout.FrameWidget;
import net.minecraft.client.gui.widget.layout.LayoutSettings;
import net.minecraft.client.gui.widget.layout.LinearLayoutWidget;
import net.minecraft.client.gui.widget.text.TextWidget;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.Text;
import net.orifu.skin_overrides.Library.LibraryEntry;
import net.orifu.skin_overrides.Mod;
import net.orifu.skin_overrides.override.Overridden;
import net.orifu.skin_overrides.override.LibraryCapeOverride.CapeEntry;
import net.orifu.skin_overrides.override.LibrarySkinOverride.SkinEntry;
import net.orifu.skin_overrides.texture.LocalPlayerTexture;
import net.orifu.skin_overrides.texture.LocalSkinTexture;
import net.orifu.skin_overrides.util.PlayerCapeRenderer;
import net.orifu.skin_overrides.util.PlayerSkinRenderer;
import net.orifu.skin_overrides.util.ProfileHelper;
import net.orifu.skin_overrides.util.Util;

public class LibraryScreen extends Screen {
    private static final Text TITLE = Text.translatable("skin_overrides.library.title");
    private static final int HEADER_HEIGHT = 20;
    private static final int SKIN_SCALE = 4;
    private static final int CAPE_SCALE = 8;

    private static final int OPTIONS_PAD = 24;

    public final Overridden ov;
    @Nullable
    private final Screen parent;
    @Nullable
    private final Consumer<LibraryEntry> callback;

    private FrameWidget header;
    private LibraryListWidget libraryList;
    private TextFieldWidget searchBox;
    @Nullable
    private FrameWidget entryPreviewFrame;
    @Nullable
    private TextFieldWidget nameField;

    @Nullable
    protected LibraryListEntry selectedEntry;

    @Nullable
    private GameProfile adding;

    public LibraryScreen(Overridden ov, @Nullable Screen parent, @Nullable Consumer<LibraryEntry> callback) {
        super(TITLE);

        this.ov = ov;
        this.parent = parent;
        this.callback = callback;
    }

    public LibraryScreen(Overridden ov, @Nullable Screen parent) {
        this(ov, parent, null);
    }

    @Override
    protected void init() {
        if (this.libraryList == null) {
            this.libraryList = new LibraryListWidget(this, this.ov);
        }

        if (this.searchBox == null) {
            this.searchBox = new TextFieldWidget(this.textRenderer, 0, 20,
                    Text.translatable("skin_overrides.library.search"));
            this.searchBox.setHint(Text.translatable("skin_overrides.library.search.hint"));
            this.searchBox.setChangedListener(query -> {
                this.libraryList.filter(query);
                this.clearAndInit();
            });
            this.searchBox.setMaxLength(100);
        }

        int optionsWidth = Math.min(this.width * 2 / 5 - OPTIONS_PAD, 150);
        int libraryListWidth = this.selectedEntry == null ? this.width : this.width - optionsWidth - OPTIONS_PAD;

        this.searchBox.setDimensions(libraryListWidth - 60, 20);
        this.libraryList.setDimensions(libraryListWidth, this.height - HEADER_HEIGHT - 20);

        var root = LinearLayoutWidget.createVertical();

        this.header = root.add(new FrameWidget(this.width, HEADER_HEIGHT));
        this.header.add(new TextWidget(TITLE, this.textRenderer));

        var search = root.add(LinearLayoutWidget.createHorizontal());
        search.add(this.searchBox);
        search.add(ButtonWidget
                .builder(Text.translatable("skin_overrides.library.search_add"), btn -> this.addFromSearch())
                .width(60).build());

        var body = root.add(LinearLayoutWidget.createHorizontal());
        body.add(this.libraryList);

        if (this.selectedEntry != null) {
            var controlsFrame = body.add(new FrameWidget(optionsWidth + OPTIONS_PAD, 0));
            var controls = controlsFrame.add(LinearLayoutWidget.createVertical().setSpacing(2));

            // library entry preview
            this.entryPreviewFrame = controls.add(new FrameWidget(
                    this.ov.skin() ? PlayerSkinRenderer.WIDTH * SKIN_SCALE : PlayerCapeRenderer.WIDTH * CAPE_SCALE,
                    this.ov.skin() ? PlayerSkinRenderer.HEIGHT * SKIN_SCALE : PlayerCapeRenderer.HEIGHT * CAPE_SCALE),
                    LayoutSettings.create().alignHorizontallyCenter());

            // padding
            controls.add(new FrameWidget(0, 16));

            // name input
            if (this.nameField == null) {
                this.nameField = new TextFieldWidget(this.textRenderer, optionsWidth, 20,
                        Text.translatable("skin_overrides.library.input.name"));
                this.nameField.setMaxLength(32);
                this.nameField.setChangedListener(this::renameEntry);
                this.nameField.setText(this.selectedEntry.entry.getName());
            }
            controls.add(this.nameField, LayoutSettings.create().alignHorizontallyCenter());

            var smallControls = controls.add(LinearLayoutWidget.createHorizontal());

            // previous entry
            int index = this.libraryList.indexOf(this.selectedEntry);
            boolean isFirst = index == 0;
            smallControls.add(ButtonWidget.builder(Text.literal("<"),
                    btn -> this.libraryList.moveSelection(-1)).width(20)
                    .tooltip(Tooltip.create(Text.translatable("skin_overrides.library.input.back")))
                    .build()).active = !isFirst;
            // swap this and previous entry
            smallControls.add(ButtonWidget.builder(Text.literal("<<"), btn -> {
                this.libraryList.move(index, index - 1);
                this.libraryList.ensureVisible(this.selectedEntry);
                this.clearAndInit();
            }).width(25).tooltip(Tooltip.create(Text.translatable("skin_overrides.library.input.move_back")))
                    .build()).active = !isFirst;

            // use this entry
            int mainControlWidth = (optionsWidth - 40 - 50) / 2;
            smallControls.add(ButtonWidget.builder(Text.literal("+"), btn -> {
                this.callback.accept(this.selectedEntry.entry);
                this.client.setScreen(this.parent);
            }).width(mainControlWidth).tooltip(Tooltip.create(Text.translatable("skin_overrides.library.input.use")))
                    .build()).active = this.callback != null;

            // remove this entry
            smallControls.add(ButtonWidget.builder(Text.literal("-"), btn -> {
                this.libraryList.removeFromLibrary();
            }).width(mainControlWidth).tooltip(Tooltip.create(Text.translatable("skin_overrides.library.input.remove")))
                    .build()).active = this.callback != null;

            // swap this and next entry
            boolean isLast = index == this.libraryList.children().size() - 1;
            smallControls.add(ButtonWidget.builder(Text.literal(">>"), btn -> {
                this.libraryList.move(index, index + 1);
                this.libraryList.ensureVisible(this.selectedEntry);
                this.clearAndInit();
            }).tooltip(Tooltip.create(Text.translatable("skin_overrides.library.input.move_next"))).width(25)
                    .build()).active = !isLast;
            // next entry
            smallControls.add(ButtonWidget.builder(Text.literal(">"),
                    btn -> this.libraryList.moveSelection(1))
                    .tooltip(Tooltip.create(Text.translatable("skin_overrides.library.input.next"))).width(20)
                    .build()).active = !isLast;
        }

        root.visitWidgets(this::addDrawableSelectableElement);
        root.arrangeElements();
        this.focusOn(this.searchBox);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        super.render(graphics, mouseX, mouseY, delta);

        if (this.selectedEntry != null) {
            if (this.selectedEntry.entry instanceof SkinEntry entry) {
                var texture = entry.getTexture();
                var model = entry.getModel();
                PlayerSkinRenderer.draw(graphics, texture, model,
                        this.entryPreviewFrame.getX(), this.entryPreviewFrame.getY(), SKIN_SCALE);
            } else {
                var texture = this.selectedEntry.entry.getTexture();
                PlayerCapeRenderer.draw(graphics, texture, this.entryPreviewFrame.getX(), this.entryPreviewFrame.getY(),
                        CAPE_SCALE);
            }
        }

        // empty list text
        if (this.libraryList.children().isEmpty()) {
            graphics.drawCenteredShadowedText(this.textRenderer, Text.translatable("skin_overrides.library.empty"),
                    this.libraryList.getX() + this.libraryList.getWidth() / 2,
                    this.libraryList.getY() + this.libraryList.getHeight() / 2 - 4, 0xaaaaaa);
        }

        if (this.adding != null) {
            // the skin won't be properly loaded for a few frames
            var skin = Mod.getSkin(this.adding);
            if (skin.textureUrl() != null) {
                if (this.ov.skin()) {
                    SkinEntry.create(this.adding.getName(), skin.texture(), skin.model());
                } else {
                    CapeEntry.create(this.adding.getName(), skin.capeTexture());
                }

                this.adding = null;
                this.libraryList.reload();
                this.clearAndInit();
            }
        }
    }

    @Override
    public void closeScreen() {
        this.client.setScreen(this.parent);
    }

    public void selectEntry(@Nullable LibraryListEntry entry) {
        this.selectedEntry = entry;
        this.nameField = null;
        this.clearAndInit();

        if (entry != null) {
            this.libraryList.ensureVisible(entry);
        }
    }

    public void renameEntry(String newName) {
        if (!newName.equals(this.selectedEntry.entry.getName())) {
            this.ov.library().rename(this.selectedEntry.entry, newName);
            this.clearAndInit();
            this.focusOn(this.nameField);
        }
    }

    @Override
    public void filesDragged(List<Path> paths) {
        if (paths.size() == 0) {
            return;
        }
        Path path = paths.get(0);
        if (!path.toFile().isFile() || !FilenameUtils.isExtension(path.toFile().getName(), "png")) {
            return;
        }
        String guessedName = path.toFile().getName().replace(".png", "").replace("_", " ");

        if (this.ov.skin()) {
            // open name and model input screen
            this.client.setScreen(OverrideInfoEntryScreen.getNameAndModel(this,
                    Util.texture(new LocalSkinTexture(path.toFile())), guessedName,
                    (name, model) -> {
                        // add skin
                        SkinEntry.create(name, path, model);
                        this.libraryList.reload();
                        this.clearAndInit();
                    }));
        } else {
            // open name input screen
            this.client.setScreen(OverrideInfoEntryScreen.getName(this,
                    Util.texture(new LocalPlayerTexture(path.toFile())), guessedName,
                    name -> {
                        // add cape
                        CapeEntry.create(name, path);
                        this.libraryList.reload();
                        this.clearAndInit();
                    }));
        }
    }

    private void addFromSearch() {
        var maybeProfile = ProfileHelper.idToProfile(this.searchBox.getText());
        if (maybeProfile.isEmpty()) {
            this.client.getToastManager().add(new SystemToast(SystemToast.C_ozahoshp.field_47585,
                    Text.translatable("skin_overrides.no_profile.title", this.searchBox.getText()),
                    Text.translatable("skin_overrides.no_profile.description")));
        } else {
            this.adding = maybeProfile.get();
            this.searchBox.setText("");
        }
    }
}

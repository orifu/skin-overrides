package net.orifu.skin_overrides.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.orifu.skin_overrides.Library;

public class LibraryListWidget extends AlwaysSelectedEntryGridWidget<LibraryListEntry> {
    private final LibraryScreen parent;
    private final boolean isSkin;

    public LibraryListWidget(LibraryScreen parent, boolean isSkin) {
        super(MinecraftClient.getInstance(), 0, 0, 0,
                LibraryListEntry.WIDTH,
                isSkin ? LibraryListEntry.SKIN_ENTRY_HEIGHT : LibraryListEntry.CAPE_ENTRY_HEIGHT, 6);

        this.parent = parent;
        this.isSkin = isSkin;

        this.reload();
    }

    public void reload() {
        Library.reload();

        int i = 0;
        for (var entry : this.isSkin ? Library.skinEntries() : Library.capeEntries()) {
            boolean add = true;
            for (var child : this.children()) {
                if (child.entry.getId().equals(entry.getId())) {
                    add = false;
                    child.entry = entry;
                    child.index = i++;
                    break;
                }
            }

            if (add) {
                this.children().add(i, new LibraryListEntry(entry, i++, this.parent));
            }
        }
    }

    public void moveSelection(int amount) {
        var newSelected = this.getEntry(this.getSelectedOrNull().index + amount);
        this.setSelected(newSelected);
        this.ensureVisible(newSelected);
        this.parent.selectEntry(newSelected);
    }

    @Override
    public int getRowWidth() {
        return this.xTiles * this.itemWidth;
    }

    @Override
    public int getScrollbarPositionX() {
        return this.getXEnd() - SCROLLBAR_WIDTH;
    }

    @Override
    public void renderList(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        this.xTiles = this.width / this.itemWidth;
        super.renderList(graphics, mouseX, mouseY, delta);
    }
}

package net.orifu.skin_overrides;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.mojang.blaze3d.texture.NativeImage;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.resource.ResourceManager;

public class OverridenPlayerSkinTexture extends PlayerSkinTexture {
    private final File textureFile;

    public OverridenPlayerSkinTexture(File textureFile, Runnable loadedCallback) {
        super(null, null, null, true, loadedCallback);

        this.textureFile = textureFile;
    }

    public OverridenPlayerSkinTexture(File textureFile) {
        this(textureFile, () -> {
        });
    }

    @Override
    public void load(ResourceManager manager) throws IOException {
        MinecraftClient.getInstance().execute(() -> {
            try {
                NativeImage texture = this.loadTexture(new FileInputStream(this.textureFile));
                this.onTextureLoaded(texture);
            } catch (FileNotFoundException e) {
                // shouldn't happen
            }
        });
    }
}

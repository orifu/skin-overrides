package net.orifu.skin_overrides.override;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import com.mojang.authlib.GameProfile;

import net.orifu.skin_overrides.SkinOverrides;
import net.orifu.skin_overrides.util.OverrideFiles;
import net.orifu.skin_overrides.util.OverrideFiles.Validated;

public abstract class AbstractOverride<E, T> {
    public abstract String rootFolder();

    protected abstract String getFileName(GameProfile profile, E data);

    protected abstract Optional<Validated<E>> validateFile(File file, String name, String ext);

    protected abstract Optional<T> tryGetTextureFromValidated(Validated<E> v);

    public Optional<Validated<E>> getOverrideFile(GameProfile profile) {
        return OverrideFiles.findProfileFile(this.rootFolder(), profile, this::validateFile);
    }

    public Optional<T> getOverride(GameProfile profile) {
        return this.getOverrideFile(profile).flatMap(v -> this.tryGetTextureFromValidated(v));
    }

    public boolean hasOverride(GameProfile profile) {
        return getOverrideFile(profile).isPresent();
    }

    public List<GameProfile> profilesWithOverride() {
        return OverrideFiles.listProfiles(this.rootFolder(), this::validateFile);
    }

    public void copyOverride(GameProfile profile, Path path, E data) {
        this.removeOverride(profile);

        try {
            Path outputPath = Paths.get(this.getFileName(profile, data));

            Files.copy(path, outputPath);
        } catch (IOException e) {
            SkinOverrides.LOGGER.error("failed to copy {}", path, e);
        }
    }

    public void removeOverride(GameProfile profile) {
        OverrideFiles.deleteProfileFiles(this.rootFolder(), this::validateFile, profile);
    }
}

package net.orifu.skin_overrides;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.jetbrains.annotations.Nullable;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.PlayerSkin;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.Identifier;
import net.orifu.skin_overrides.texture.LocalSkinTexture;

public class Library {
    public static final File FILE = new File("skin_overrides/library.json");
    public static final Gson GSON = new Gson();

    protected ArrayList<LibraryEntry> entries;

    public Library() {
        this.reload();
    }

    public List<LibraryEntry> entries() {
        return this.entries;
    }

    public void reload() {
        this.entries = new ArrayList<>();

        try {
            var reader = Files.newReader(FILE, StandardCharsets.UTF_8);
            JsonArray arr = GSON.fromJson(reader, JsonArray.class);

            arr.forEach(j -> LibraryEntry.fromJson(j).ifPresent(this.entries::add));
            reader.close();
            return;
        } catch (FileNotFoundException e) {
        } catch (IOException | JsonParseException | NullPointerException e) {
            SkinOverrides.LOGGER.error("failed to load library file", e);
        }

        // add default player skins
        for (var playerSkin : DefaultSkinHelper.DEFAULT_SKINS) {
            String name = playerSkin.texture().getPath();
            name = name.substring(name.lastIndexOf('/') + 1);
            name = name.substring(0, 1).toUpperCase() + name.substring(1).replace(".png", "");
            name += playerSkin.model().equals(PlayerSkin.Model.WIDE) ? " (wide)" : " (slim)";
            this.entries.add(new LibraryEntry(name, playerSkin.texture(), playerSkin.model()));
        }
    }

    public void save() {
        JsonArray arr = new JsonArray();
        this.entries.forEach(e -> arr.add(e.toJson()));

        try {
            var writer = Files.newWriter(FILE, StandardCharsets.UTF_8);
            writer.write(GSON.toJson(arr));
            writer.close();
        } catch (IOException e) {
            SkinOverrides.LOGGER.error("failed to save library file", e);
        }
    }

    public static class LibraryEntry {
        protected final boolean isFile;
        protected final PlayerSkin.Model model;
        @Nullable
        protected final File skinFile;
        @Nullable
        protected final Identifier texture;

        protected String name;

        public LibraryEntry(String name, File file, PlayerSkin.Model model) {
            this.isFile = true;
            this.model = model;
            this.skinFile = file;
            this.texture = null;

            this.name = name;
        }

        public LibraryEntry(String name, Identifier texture, PlayerSkin.Model model) {
            this.isFile = false;
            this.model = model;
            this.skinFile = null;
            this.texture = texture;

            this.name = name;
        }

        public String getName() {
            return this.name;
        }

        public Identifier getTexture() {
            if (this.isFile) {
                Identifier id = new Identifier("skin_overrides", UUID.randomUUID().toString());
                MinecraftClient.getInstance().getTextureManager().registerTexture(id,
                        new LocalSkinTexture(this.skinFile, null));
                return id;
            } else {
                return this.texture;
            }
        }

        public PlayerSkin.Model getModel() {
            return this.model;
        }

        protected static Optional<LibraryEntry> fromJson(JsonElement el) {
            if (!el.isJsonObject())
                return Optional.empty();

            JsonObject obj = el.getAsJsonObject();
            JsonElement name = obj.get("name");
            JsonElement model = obj.get("model");
            JsonElement file = obj.get("file");
            JsonElement texture = obj.get("texture");

            if (name == null)
                return Optional.empty();
            String nameStr = name.getAsString();
            if (nameStr == null)
                return Optional.empty();
            String modelStr = model.getAsString();
            if (modelStr == null)
                return Optional.empty();

            PlayerSkin.Model playerModel;
            if (modelStr.equals("wide")) {
                playerModel = PlayerSkin.Model.WIDE;
            } else if (modelStr.equals("slim")) {
                playerModel = PlayerSkin.Model.SLIM;
            } else {
                return Optional.empty();
            }

            if (file != null) {
                String fileStr = file.getAsString();
                if (fileStr == null)
                    return Optional.empty();

                return Optional.of(new LibraryEntry(nameStr, new File(fileStr), playerModel));
            } else if (texture != null) {
                String textureStr = texture.getAsString();
                if (textureStr == null)
                    return Optional.empty();
                Identifier textureId = Identifier.tryParse(textureStr);
                if (textureId == null)
                    return Optional.empty();

                return Optional.of(new LibraryEntry(nameStr, textureId, playerModel));
            } else {
                return Optional.empty();
            }
        }

        protected JsonElement toJson() {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", this.name);
            obj.addProperty("model", this.model.equals(PlayerSkin.Model.WIDE) ? "wide" : "slim");
            if (this.isFile) {
                obj.addProperty("file", this.skinFile.toString());
            } else {
                obj.addProperty("texture", this.texture.toString());
            }
            return obj;
        }
    }
}

package com.ultreon.devices.object;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.ultreon.devices.Devices;
import dev.architectury.injectables.annotations.PlatformOnly;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class AppInfo {
    public static final Comparator<AppInfo> SORT_NAME = Comparator.comparing(AppInfo::getName);

    private transient final ResourceLocation APP_ID;
    private transient int iconU = 0; // you do not just set a value that reflection needs to modify as "final"
    private transient int iconV = 0;

    @PlatformOnly("fabric")
    public void setIconU(int iconU) {
        this.iconU = iconU;
    }

    @PlatformOnly("fabric")
    public void setIconV(int iconV) {
        this.iconV = iconV;
    }

    private final transient boolean systemApp;

    private String name;
    private String author;
    private String description;
    private String version;
    private String icon;
    private String[] screenshots;
    private Support support;

    public AppInfo(ResourceLocation identifier, boolean isSystemApp) {
        this.APP_ID = identifier;
        this.systemApp = isSystemApp;
    }

    /**
     * Gets the id of the application
     *
     * @return the app resource location
     */
    public ResourceLocation getId() {
        return APP_ID;
    }

    /**
     * Gets the formatted version of the application's id
     *
     * @return a formatted id
     */
    public String getFormattedId() {
        return APP_ID.getNamespace() + "." + APP_ID.getPath();
    }

    /**
     * Gets the name of the application
     *
     * @return the application name
     */
    public String getName() {
        return name;
    }

    public String getAuthor() {
        return author;
    }

    public String getDescription() {
        return description;
    }

    public String getVersion() {
        return version;
    }

    public String getIcon() {
        return icon;
    }

    public int getIconU() {
        return iconU;
    }

    public int getIconV() {
        return iconV;
    }

    public String[] getScreenshots() {
        return screenshots;
    }

    public Support getSupport() {
        return support;
    }

    public boolean isSystemApp() {
        return systemApp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof AppInfo info)) return false;
        return this == info || getFormattedId().equals(info.getFormattedId());
    }

    public void reload() {
        resetInfo();
        InputStream stream = Devices.class.getResourceAsStream("/assets/" + APP_ID.getNamespace() + "/apps/" + APP_ID.getPath() + ".json");

        if (stream == null)
            throw new RuntimeException("Missing app info json for '" + APP_ID + "'");

        Reader reader = new InputStreamReader(stream);
        JsonElement obj = JsonParser.parseReader(reader);
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(AppInfo.class, new Deserializer(this));
        Gson gson = builder.create();
        gson.fromJson(obj, AppInfo.class);
    }

    private void resetInfo() {
        name = null;
        author = null;
        description = null;
        version = null;
        icon = null;
        screenshots = null;
        support = null;
    }

    private static class Support {
        private String paypal;
        private String patreon;
        private String twitter;
        private String youtube;
    }

    public static class Deserializer implements JsonDeserializer<AppInfo> {
        private static final Pattern LANG = Pattern.compile("\\$\\{[a-z]+}");

        private final AppInfo info;

        public Deserializer(AppInfo info) {
            this.info = info;
        }

        @Override
        public AppInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
            try {
                info.name = convertToLocal(json.getAsJsonObject().get("name").getAsString());
                info.author = convertToLocal(json.getAsJsonObject().get("author").getAsString());
                info.description = convertToLocal(json.getAsJsonObject().get("description").getAsString());
                info.version = json.getAsJsonObject().get("version").getAsString();

                if (json.getAsJsonObject().has("screenshots") && json.getAsJsonObject().get("screenshots").isJsonArray()) {
                    info.screenshots = context.deserialize(json.getAsJsonObject().get("screenshots"), new TypeToken<String[]>() {
                    }.getType());
                }

                if (json.getAsJsonObject().has("icon") && json.getAsJsonObject().get("icon").isJsonPrimitive()) {
                    info.icon = json.getAsJsonObject().get("icon").getAsString();
                }

                if (json.getAsJsonObject().has("support") && json.getAsJsonObject().get("support").getAsJsonObject().size() > 0) {
                    JsonObject supportObj = json.getAsJsonObject().get("support").getAsJsonObject();
                    Support support = new Support();

                    if (supportObj.has("paypal")) {
                        support.paypal = supportObj.get("paypal").getAsString();
                    }
                    if (supportObj.has("patreon")) {
                        support.patreon = supportObj.get("patreon").getAsString();
                    }
                    if (supportObj.has("twitter")) {
                        support.twitter = supportObj.get("twitter").getAsString();
                    }
                    if (supportObj.has("youtube")) {
                        support.youtube = supportObj.get("youtube").getAsString();
                    }

                    info.support = support;
                }
            } catch (JsonParseException e) {
                Devices.LOGGER.error("Malformed app info json for '" + info.getFormattedId() + "'");
            }

            return info;
        }

        private String convertToLocal(String s) {
            Matcher m = LANG.matcher(s);
            while (m.find()) {
                String found = m.group();
                s = s.replace(found, I18n.get("app." + info.getFormattedId() + "." + found.substring(2, found.length() - 1)));
            }
            return s;
        }
    }
}

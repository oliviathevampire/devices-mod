package com.ultreon.devices.programs.gitweb.module;

import com.ultreon.devices.api.app.Component;
import com.ultreon.devices.api.app.Layout;
import com.ultreon.devices.api.app.component.Image;
import com.ultreon.devices.api.app.component.Label;
import com.ultreon.devices.programs.gitweb.component.GitWebFrame;

import java.util.Map;

/**
 * @author MrCrayfish
 */
public class BannerModule extends Module {
    @Override
    public String[] getRequiredData() {
        return new String[]{"image"};
    }

    @Override
    public String[] getOptionalData() {
        return new String[]{"text"};
    }

    @Override
    public int calculateHeight(Map<String, String> data, int width) {
        return 50;
    }

    @Override
    public void generate(GitWebFrame frame, Layout layout, int width, Map<String, String> data) {
        int height = calculateHeight(data, width);
        Image image = new Image(0, 0, width, height);
        image.setImage(data.get("image"));
        layout.addComponent(image);

        if (data.containsKey("text")) {
            String s = GitWebFrame.parseFormatting(data.get("text"));
            Label label = new Label(s, width / 2, (height - 18) / 2);
            label.setAlignment(Component.ALIGN_CENTER);
            label.setScale(2f);
            layout.addComponent(label);
        }
    }
}

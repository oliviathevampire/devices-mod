package com.ultreon.devices.programs.gitweb.module;

import com.ultreon.devices.api.app.Icons;
import com.ultreon.devices.api.app.Layout;
import com.ultreon.devices.api.app.component.Button;
import com.ultreon.devices.programs.gitweb.component.GitWebFrame;
import net.minecraft.client.gui.Gui;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author MrCrayfish
 */
public class NavigationModule extends Module {
    @Override
    public String[] getRequiredData() {
        return new String[0];
    }

    @Override
    public String[] getOptionalData() {
        List<String> optionalData = new ArrayList<>();
        optionalData.add("color");
        for (int i = 0; i < 10; i++) {
            optionalData.add("item-link-" + (i + 1));
            optionalData.add("item-label-" + (i + 1));
            optionalData.add("item-icon-" + (i + 1));
        }
        return optionalData.toArray(new String[0]);
    }

    @Override
    public int calculateHeight(Map<String, String> data, int width) {
        List<Button> navButtons = createNavigationButtons(null, data);
        int currentRow = 0;
        int rowItemCount = 0;
        int remainingWidth = width - 6;
        for (Button button : navButtons) {
            if (remainingWidth < button.getWidth() + rowItemCount * 3) {
                if (rowItemCount != 0) {
                    currentRow++;
                    rowItemCount = 0;
                    remainingWidth = width - 6;
                }
            }
            int offset = button.getWidth();
            remainingWidth -= offset;
            rowItemCount++;
        }
        return currentRow * 18 + 18 + (currentRow + 1) * 3 + 3;
    }

    @Override
    public void generate(GitWebFrame frame, Layout layout, int width, Map<String, String> data) {
        int color = Color.DARK_GRAY.getRGB();
        if (data.containsKey("color")) {
            color = Integer.parseInt(data.get("color"));
        }

        int finalColor = color;
        layout.setBackground((pose, gui, mc, x, y, width1, height, mouseX, mouseY, windowActive) ->
                Gui.fill(pose, x, y, x + width1, y + height, finalColor));

        List<Button> navButtons = createNavigationButtons(frame, data);
        int currentRow = 0;
        int rowItemCount = 0;
        int remainingWidth = width - 6;
        for (Button button : navButtons) {
            if (remainingWidth < button.getWidth() + rowItemCount * 3) {
                if (rowItemCount != 0) {
                    currentRow++;
                    rowItemCount = 0;
                    remainingWidth = width - 6;
                }
            }
            button.left = 3 + rowItemCount * 3 + (width - 6) - remainingWidth;
            button.top = 3 + currentRow * 18 + currentRow * 3;

            int offset = button.getWidth();
            remainingWidth -= offset;

            layout.addComponent(button);
            rowItemCount++;
        }
    }

    private List<Button> createNavigationButtons(@Nullable GitWebFrame frame, Map<String, String> data) {
        List<Button> navButtons = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            if (data.containsKey("item-link-" + i)) {
                navButtons.add(createButton(frame, i, data));
            }
        }
        return navButtons;
    }

    private Button createButton(@Nullable GitWebFrame frame, int index, Map<String, String> data) {
        String label = GitWebFrame.parseFormatting(data.getOrDefault("item-label-" + index, ""));
        Button button = new Button(0, 0, label);
        if (data.containsKey("item-icon-" + index)) {
            Icons icon = Icons.valueOf(data.get("item-icon-" + index));
            button.setIcon(icon);
        }
        if (frame != null && data.containsKey("item-link-" + index)) {
            String link = data.get("item-link-" + index);
            button.setClickListener((mouseX, mouseY, mouseButton) ->
            {
                if (mouseButton == 0) {
                    frame.loadWebsite(link);
                }
            });
        }
        if (button.getText() != null && !button.getText().isEmpty()) {
            button.setSize(button.getWidth(), 18);
        } else {
            button.setSize(18, 18);
        }
        return button;
    }
}

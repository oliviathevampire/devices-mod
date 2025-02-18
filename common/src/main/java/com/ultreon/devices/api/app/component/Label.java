package com.ultreon.devices.api.app.component;

import com.mojang.blaze3d.vertex.PoseStack;
import com.ultreon.devices.api.app.Component;
import com.ultreon.devices.core.Laptop;
import net.minecraft.client.Minecraft;

import java.awt.*;

@SuppressWarnings("unused")
public class Label extends Component {

    protected String text;
    protected int width;
    protected boolean shadow = true;
    protected double scale = 1;
    protected int alignment = ALIGN_LEFT;

    protected int textColor = Color.WHITE.getRGB();

    /**
     * Default label constructor
     *
     * @param text the text to display
     * @param left how many pixels from the left
     * @param top  how many pixels from the top
     */
    public Label(String text, int left, int top) {
        super(left, top);
        this.text = text;
    }

    @Override
    public void render(PoseStack pose, Laptop laptop, Minecraft mc, int x, int y, int mouseX, int mouseY, boolean windowActive, float partialTicks) {
        if (this.visible) {
            pose.pushPose();
            {
                pose.translate(xPosition, yPosition, 0);
                pose.scale((float) scale, (float) scale, (float) scale);
                if (alignment == ALIGN_RIGHT)
                    pose.translate((int) -(mc.font.width(text) * scale), 0, 0);
                if (alignment == ALIGN_CENTER)
                    pose.translate((float) ((int) -(mc.font.width(text) * scale) / (int) (2 * scale)), 0, 0);
                if (shadow) {
                    mc.font.drawShadow(pose, text, 0, 0, textColor);
                } else {
                    mc.font.draw(pose, text, 0, 0, textColor);
                }
            }
            pose.popPose();
        }
    }

    /**
     * Sets the text in the label
     *
     * @param text the text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Sets the text color for this component
     *
     * @param color the text color
     */
    public void setTextColor(Color color) {
        this.textColor = color.getRGB();
    }

    /**
     * Sets the whether shadow should show under the text
     *
     * @param shadow whether to render shadow or not
     */
    public void setShadow(boolean shadow) {
        this.shadow = shadow;
    }

    /**
     * Scales the text, essentially setting the font size. Minecraft
     * does not support proper font resizing. The default scale is 1
     *
     * @param scale the text scale
     */
    public void setScale(double scale) {
        this.scale = scale;
    }

    /**
     * Sets the alignment of the text. Use {@link Component#ALIGN_LEFT} or
     * {@link Component#ALIGN_RIGHT} to set alignment.
     *
     * @param alignment the alignment type
     */
    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }
}

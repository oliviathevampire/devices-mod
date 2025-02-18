package com.ultreon.devices.api.app.component;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.ultreon.devices.api.app.Component;
import com.ultreon.devices.api.app.interfaces.IHighlight;
import com.ultreon.devices.api.app.listener.KeyListener;
import com.ultreon.devices.api.utils.RenderUtil;
import com.ultreon.devices.core.Laptop;
import com.ultreon.devices.util.GLHelper;
import com.ultreon.devices.util.GuiHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.util.Mth;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@SuppressWarnings("unused")
public class TextArea extends Component {
    private static final String UNFORMATTED_SPLIT = "(?<=%1$s)|(?=%1$s)";
    private static final String[] DELIMITERS = {"(\\s|$)(?=(([^\"]*\"){2})*[^\"]*$)", "[\\p{Punct}&&[^@\"]]", "\\p{Digit}+"};
    private static final String SPLIT_REGEX;

    static {
        StringJoiner joiner = new StringJoiner("|");
        for (String s : DELIMITERS) {
            joiner.add(s);
        }
        SPLIT_REGEX = String.format(UNFORMATTED_SPLIT, "(" + joiner + ")");
    }

    protected Font font;

    protected String text = "";
    protected String placeholder = null;
    protected int width, height;
    /* Personalisation */
    protected int placeholderColor = new Color(1f, 1f, 1f, 0.35f).getRGB();
    protected int textColor = Color.WHITE.getRGB();
    protected int backgroundColor = Color.DARK_GRAY.getRGB();
    protected int secondaryBackgroundColor = Color.GRAY.getRGB();
    protected int borderColor = Color.BLACK.getRGB();
    private int padding = 4;
    private boolean isFocused = false;
    private boolean editable = true;
    private List<String> lines = new ArrayList<>();
    private int visibleLines;
    private int maxLines;
    private ScrollBar scrollBar;
    private boolean scrollBarVisible = true;
    private int scrollBarSize = 3;
    private int horizontalScroll;
    private int verticalScroll;
    private int horizontalOffset;
    private int verticalOffset;
    private int cursorTick = 0;
    private int cursorX;
    private int cursorY;
    private int clickedX;
    private int clickedY;
    private boolean wrapText = false;
    private int maxLineWidth;
    private IHighlight highlight = null;
    private KeyListener keyListener = null;

    /**
     * Default text area constructor
     *
     * @param left   how many pixels from the left
     * @param top    how many pixels from the top
     * @param width  the width of the text area
     * @param height the height of the text area
     */
    public TextArea(int left, int top, int width, int height) {
        super(left, top);
        this.font = Laptop.getFont();
        this.width = width;
        this.height = height;
        this.visibleLines = (int) Math.floor((float) ((height - padding * 2 + 1) / font.lineHeight));
        this.lines.add("");
    }

    @Override
    public void handleTick() {
        cursorTick++;
    }

    @Override
    public void render(PoseStack pose, Laptop laptop, Minecraft mc, int x, int y, int mouseX, int mouseY, boolean windowActive, float partialTicks) {
        if (this.visible) {
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            Color bgColor = new Color(color(backgroundColor, getColorScheme().getBackgroundColor()));
            Gui.fill(pose, x, y, x + width, y + height, bgColor.darker().darker().getRGB());
            Gui.fill(pose, x + 1, y + 1, x + width - 1, y + height - 1, bgColor.getRGB());

            if (!isFocused && placeholder != null && (lines.isEmpty() || (lines.size() == 1 && lines.get(0).isEmpty()))) {
                RenderSystem.enableBlend();
                RenderUtil.drawStringClipped(pose, placeholder, x + padding, y + padding, width - padding * 2, placeholderColor, false);
            }

            GLHelper.pushScissor(x + padding, y + padding, width - padding * 2, height - padding * 2);
            for (int i = 0; i < visibleLines && i + verticalScroll < lines.size(); i++) {
                float scrollPercentage = (verticalScroll + verticalOffset) / (float) (lines.size() - visibleLines);
                float pixelsPerUnit = (float) maxLineWidth / (float) (width - padding * 2);
                int scrollX = Mth.clamp(horizontalScroll + (int) (horizontalOffset * pixelsPerUnit), 0, Math.max(0, maxLineWidth - (width - padding * 2)));
                int scrollY = (int) ((lines.size() - visibleLines) * (scrollPercentage));
                int lineY = i + Mth.clamp(scrollY, 0, Math.max(0, lines.size() - visibleLines));
                if (highlight != null) {
                    String[] words = lines.get(lineY).split(SPLIT_REGEX);
                    StringBuilder builder = new StringBuilder();
                    for (String word : words) {
                        ChatFormatting[] formatting = highlight.getKeywordFormatting(word);
                        for (ChatFormatting format : formatting) {
                            builder.append(format);
                        }
                        builder.append(word);
                        builder.append(ChatFormatting.RESET);
                    }
                    font.draw(pose, builder.toString(), x + padding - scrollX, y + padding + i * font.lineHeight, -1);
                } else {
                    font.draw(pose, lines.get(lineY), x + padding - scrollX, y + padding + i * font.lineHeight, color(textColor, getColorScheme().getTextColor()));
                }
            }
            GLHelper.popScissor();

            GLHelper.pushScissor(x + padding, y + padding - 1, width - padding * 2 + 1, height - padding * 2 + 1);
            if (editable && isFocused) {
                float linesPerUnit = (float) lines.size() / (float) visibleLines;
                int scroll = Mth.clamp(verticalScroll + verticalOffset * (int) linesPerUnit, 0, Math.max(0, lines.size() - visibleLines));
                if (cursorY >= scroll && cursorY < scroll + visibleLines) {
                    if ((this.cursorTick / 10) % 2 == 0) {
                        String subString = getActiveLine().substring(0, cursorX);
                        int visibleWidth = width - padding * 2;
                        float pixelsPerUnit = (float) maxLineWidth / (float) (width - padding * 2);
                        int stringWidth = font.width(subString);
                        int posX = x + padding + stringWidth - Mth.clamp(horizontalScroll + (int) (horizontalOffset * pixelsPerUnit), 0, Math.max(0, maxLineWidth - visibleWidth));
                        int posY = y + padding + (cursorY - scroll) * font.lineHeight;
                        Gui.fill(pose, posX, posY - 1, posX + 1, posY + font.lineHeight, Color.WHITE.getRGB());
                    }
                }
            }
            GLHelper.popScissor();

            if (scrollBarVisible) {
                if (lines.size() > visibleLines) {
                    int visibleScrollBarHeight = height - 4;
                    int scrollBarHeight = Math.max(20, (int) ((float) visibleLines / (float) lines.size() * (float) visibleScrollBarHeight));
                    float scrollPercentage = Mth.clamp((verticalScroll + verticalOffset) / (float) (lines.size() - visibleLines), 0f, 1f);
                    int scrollBarY = (int) ((visibleScrollBarHeight - scrollBarHeight) * scrollPercentage);
                    int scrollY = yPosition + 2 + scrollBarY;
                    Gui.fill(pose, x + width - 2 - scrollBarSize, scrollY, x + width - 2, scrollY + scrollBarHeight, placeholderColor);
                }

                if (!wrapText && maxLineWidth >= width - padding * 2) {
                    int visibleWidth = width - padding * 2;
                    int visibleScrollBarWidth = width - 4 - (lines.size() > visibleLines ? scrollBarSize + 1 : 0);
                    float scrollPercentage = (float) (horizontalScroll + 1) / (float) (maxLineWidth - visibleWidth + 1);
                    int scrollBarWidth = Math.max(20, (int) ((float) visibleWidth / (float) maxLineWidth * (float) visibleScrollBarWidth));
                    int relativeScrollX = (int) (scrollPercentage * (visibleScrollBarWidth - scrollBarWidth));
                    int scrollX = xPosition + 2 + Mth.clamp(relativeScrollX + horizontalOffset, 0, visibleScrollBarWidth - scrollBarWidth);
                    Gui.fill(pose, scrollX, y + height - scrollBarSize - 2, scrollX + scrollBarWidth, y + height - 2, placeholderColor);
                }
            }
        }
    }

    @Override
    public void handleMouseClick(int mouseX, int mouseY, int mouseButton) {
        if (!this.visible || !this.enabled) return;

        ScrollBar scrollBar = isMouseInsideScrollBar(mouseX, mouseY);
        if (scrollBar != null) {
            this.scrollBar = scrollBar;
            switch (scrollBar) {
                case HORIZONTAL -> clickedX = mouseX;
                case VERTICAL -> clickedY = mouseY;
            }
            return;
        }

        if (!this.editable) return;

        this.isFocused = GuiHelper.isMouseInside(mouseX, mouseY, xPosition, yPosition, xPosition + width, yPosition + height);

        if (GuiHelper.isMouseWithin(mouseX, mouseY, xPosition + padding, yPosition + padding, width - padding * 2, height - padding * 2)) {
            int lineX = mouseX - xPosition - padding + horizontalScroll;
            int lineY = (mouseY - yPosition - padding) / font.lineHeight + verticalScroll;
            if (lineY >= lines.size()) {
                cursorX = lines.get(Math.max(0, lines.size() - 1)).length();
                cursorY = lines.size() - 1;
            } else {
                cursorX = getClosestLineIndex(lineX, Mth.clamp(lineY, 0, lines.size() - 1));
                cursorY = lineY;
            }
            cursorTick = 0;
            updateScroll();
        }
    }

    @Override
    protected void handleMouseDrag(int mouseX, int mouseY, int mouseButton) {
        if (scrollBar != null) {
            switch (scrollBar) {
                case HORIZONTAL -> horizontalOffset = mouseX - clickedX;
                case VERTICAL -> {
                    int visibleScrollBarHeight = height - 4;
                    int scrollBarHeight = Math.max(20, (int) ((float) visibleLines / (float) lines.size() * (float) visibleScrollBarHeight));
                    float spacing = (float) (visibleScrollBarHeight - scrollBarHeight) / (float) (lines.size() - visibleLines);
                    verticalOffset = (int) ((mouseY - clickedY) / spacing);
                }
            }
        }
    }

    @Override
    protected void handleMouseRelease(int mouseX, int mouseY, int mouseButton) {
        if (scrollBar != null) {
            switch (scrollBar) {
                case HORIZONTAL -> {
                    float scrollPercentage = (float) maxLineWidth / (float) (width - padding * 2);
                    horizontalScroll = Mth.clamp(horizontalScroll + (int) (horizontalOffset * scrollPercentage), 0, maxLineWidth - (width - padding * 2));
                }
                case VERTICAL -> {
                    float scrollPercentage = Mth.clamp((verticalScroll + verticalOffset) / (float) (lines.size() - visibleLines), 0f, 1f);
                    verticalScroll = (int) ((lines.size() - visibleLines) * (scrollPercentage));
                }
            }
            horizontalOffset = 0;
            verticalOffset = 0;
            scrollBar = null;
        }
    }

    @Override
    public void handleCharTyped(char codePoint, int modifiers) {
        if (!this.visible || !this.enabled || !this.isFocused || !this.editable) return;

        System.out.println("TextArea.handleCharTyped: codePoint = " + codePoint + ", modifiers = " + modifiers);

        if (codePoint == '\\') performBackspace();
        else if (Character.isDefined(codePoint)) writeText(codePoint);

        if (keyListener != null) {
            keyListener.onCharTyped(codePoint);
        }
        updateScroll();
    }

    @Override
    public void handleKeyPressed(int keyCode, int scanCode, int modifiers) {
        if (!this.visible || !this.enabled || !this.isFocused || !this.editable) return;

        System.out.println("TextArea.handleKeyPressed: keyCode = " + keyCode + ", scanCode = " + scanCode + ", modifiers = " + modifiers);

        if (Screen.isPaste(keyCode)) {
            String[] lines = Minecraft.getInstance().keyboardHandler.getClipboard().split("\n");
            for (int i = 0; i < lines.length - 1; i++) {
                writeText(lines[i] + "\n");
            }
            writeText(lines[lines.length - 1]);
        } else {
            System.out.println("TextArea.handleKeyTypes: keyCode = " + keyCode);
            switch (keyCode) {
                case InputConstants.KEY_BACKSPACE -> performBackspace(); // TODO: Make delete actually work
                case InputConstants.KEY_RETURN -> performReturn();
                case InputConstants.KEY_TAB -> writeText('\t');
                case InputConstants.KEY_LEFT -> moveCursorLeft(1);
                case InputConstants.KEY_RIGHT -> moveCursorRight(1);
                case InputConstants.KEY_UP -> moveCursorUp();
                case InputConstants.KEY_DOWN -> moveCursorDown();
            }
        }
        updateScroll();
    }

    @Override
    protected void handleMouseScroll(int mouseX, int mouseY, boolean direction) {
        if (GuiHelper.isMouseInside(mouseX, mouseY, xPosition, yPosition, xPosition + width, yPosition + height)) {
            scroll(direction ? -1 : 1);
        }
    }

    @Nullable
    private ScrollBar isMouseInsideScrollBar(int mouseX, int mouseY) {
        if (!scrollBarVisible) return null;

        if (lines.size() > visibleLines) {
            int visibleScrollBarHeight = height - 4;
            float scrollPercentage = (float) verticalScroll / (float) (lines.size() - visibleLines);
            int scrollBarHeight = Math.max(20, (int) ((float) visibleLines / (float) lines.size() * (float) visibleScrollBarHeight));
            int relativeScrollY = (int) (scrollPercentage * (visibleScrollBarHeight - scrollBarHeight));
            int posX = xPosition + width - 2 - scrollBarSize;
            int posY = yPosition + 2 + Mth.clamp(relativeScrollY + verticalOffset, 0, visibleScrollBarHeight - scrollBarHeight);
            if (GuiHelper.isMouseInside(mouseX, mouseY, posX, posY, posX + scrollBarSize, posY + scrollBarHeight)) {
                return ScrollBar.VERTICAL;
            }
        }

        if (!wrapText && maxLineWidth >= width - padding * 2) {
            int visibleWidth = width - padding * 2;
            int visibleScrollBarWidth = width - 4 - (lines.size() > visibleLines ? scrollBarSize + 1 : 0);
            float scrollPercentage = (float) horizontalScroll / (float) (maxLineWidth - visibleWidth + 1);
            int scrollBarWidth = Math.max(20, (int) ((float) visibleWidth / (float) maxLineWidth * (float) visibleScrollBarWidth));
            int relativeScrollX = (int) (scrollPercentage * (visibleScrollBarWidth - scrollBarWidth));
            int posX = xPosition + 2 + Mth.clamp(relativeScrollX, 0, visibleScrollBarWidth - scrollBarWidth);
            int posY = yPosition + height - 2 - scrollBarSize;
            if (GuiHelper.isMouseInside(mouseX, mouseY, posX, posY, posX + scrollBarWidth, posY + scrollBarSize)) {
                return ScrollBar.HORIZONTAL;
            }
        }
        return null;
    }

    private String getActiveLine() {
        return lines.get(cursorY);
    }

    /**
     * Performs a backspace at the current cursor position
     */
    public void performBackspace() {
        if (cursorY == 0 && cursorX == 0) return;

        removeCharAtCursor();
        if (wrapText) {
            if (cursorY + 1 < lines.size()) {
                String activeLine = getActiveLine();
                if (activeLine.contains("\n")) return;

                String result = activeLine + lines.remove(cursorY + 1);
                if (font.width(result) > width - padding * 2) {
                    String trimmed = font.plainSubstrByWidth(result, width - padding * 2);
                    lines.set(cursorY, trimmed);
                    if (trimmed.charAt(trimmed.length() - 1) != '\n') {
                        prependToLine(cursorY + 1, result.substring(trimmed.length()));
                    } else if (cursorY + 1 < lines.size()) {
                        lines.add(cursorY + 1, trimmed);
                    } else {
                        lines.add(trimmed);
                    }
                } else {
                    lines.set(cursorY, result);
                }
            }
        }
        recalculateMaxWidth();
    }

    /**
     * Performs a return at the current cursor position
     */
    public void performReturn() {
        if (maxLines > 0) {
            if (getNewLineCount() == maxLines - 1) {
                return;
            }
        }

        int lineIndex = cursorY;
        String activeLine = getActiveLine();

        //if cursorX is equal to length, line doesn't have new line char
        if (cursorX == activeLine.length()) {
            lines.set(lineIndex, activeLine + "\n");
            if (!wrapText || lineIndex + 1 == lines.size()) {
                lines.add(lineIndex + 1, "");
            }
        } else {
            lines.set(lineIndex, activeLine.substring(0, cursorX) + "\n");
            lines.add(lineIndex + 1, activeLine.substring(cursorX));
        }

        if (cursorY + 1 >= verticalScroll + visibleLines) {
            scroll(1);
        }
        moveCursorRight(1);
        recalculateMaxWidth();
    }

    private int getNewLineCount() {
        int count = 0;
        for (int i = 0; i < lines.size() - 1; i++) {
            if (lines.get(i).endsWith("\n")) {
                count++;
            }
        }
        return count;
    }

    private void removeCharAtCursor() {
        String activeLine = getActiveLine();
        if (cursorX > 0) {
            String head = activeLine.substring(0, cursorX - 1);
            String tail = activeLine.substring(cursorX);
            lines.set(cursorY, head + tail);
            moveCursorLeft(1);
            return;
        }

		/*if(activeLine.isEmpty() || (activeLine.length() == 1 && activeLine.charAt(0) == '\n'))
		{
			if(verticalScroll > 0)
			{
				scroll(-1);
				moveYCursor(1);
			}
		}*/

        if (wrapText) {
            if (activeLine.isEmpty()) {
                lines.remove(cursorY);
            }
            String previousLine = lines.get(cursorY - 1);
            lines.set(cursorY - 1, previousLine.substring(0, Math.max(previousLine.length() - 1, 0)));
            moveCursorLeft(1);
        } else {
            String previousLine = lines.get(cursorY - 1);
            moveCursorLeft(1);
            String substring = previousLine.substring(0, Math.max(previousLine.length() - 1, 0));
            if (!activeLine.isEmpty()) {
                lines.set(cursorY, substring + activeLine);
            } else {
                lines.set(cursorY, substring);
            }
            lines.remove(cursorY + 1);
        }

        if (verticalScroll > 0) {
            scroll(-1);
        }

        recalculateMaxWidth();
    }

    /**
     * Writes a character at the current cursor position
     *
     * @param c the char to write
     */
    public void writeText(char c) {
        int prevCursorY = cursorY;
        writeText(Character.toString(c));
        if (wrapText && prevCursorY != cursorY) {
            moveCursorRight(1);
        }
    }

    /**
     * Writes a String at the current cursor position
     *
     * @param text the String to write
     */
    public void writeText(String text) {
        text = text.replace("\r", "");
        String activeLine = getActiveLine();
        String head = activeLine.substring(0, cursorX);
        String tail = activeLine.substring(cursorX);
        if (wrapText) {
            if (text.endsWith("\n")) {
                String result = head + text;
                if (font.width(result) > width - padding * 2) {
                    String trimmed = font.plainSubstrByWidth(result, width - padding * 2);
                    lines.set(cursorY, trimmed);
                    prependToLine(cursorY + 1, result.substring(trimmed.length()));
                } else {
                    lines.set(cursorY, result);
                }
                prependToLine(cursorY + 1, tail);
            } else {
                String result = head + text + tail;
                if (font.width(result) > width - padding * 2) {
                    String trimmed = font.plainSubstrByWidth(result, width - padding * 2);
                    lines.set(cursorY, trimmed);
                    prependToLine(cursorY + 1, result.substring(trimmed.length()));
                } else {
                    lines.set(cursorY, result);
                }
            }
        } else {
            if (text.endsWith("\n")) {
                lines.set(cursorY, head + text);
                prependToLine(cursorY + 1, tail);
            } else {
                lines.set(cursorY, head + text + tail);
            }
        }
        moveCursorRight(text.length());
        recalculateMaxWidth();
    }

    private void prependToLine(int lineIndex, String text) {
        if (lineIndex == lines.size()) lines.add("");

        if (text.length() <= 0) return;

        if (lineIndex < lines.size()) {
            if (text.charAt(Math.max(0, text.length() - 1)) == '\n') {
                lines.add(lineIndex, text);
                return;
            }
            String result = text + lines.get(lineIndex);
            if (font.width(result) > width - padding * 2) {
                String trimmed = font.plainSubstrByWidth(result, width - padding * 2);
                lines.set(lineIndex, trimmed);
                prependToLine(lineIndex + 1, result.substring(trimmed.length()));
            } else {
                lines.set(lineIndex, result);
            }
        }
    }

    public void moveCursorRight(int amount) {
        if (amount <= 0) return;

        String activeLine = getActiveLine();

        if (cursorY == lines.size() - 1 && cursorX == activeLine.length() || (cursorX > 0 && activeLine.charAt(cursorX - 1) == '\n'))
            return;

        cursorTick = 0;

        if (cursorX < activeLine.length() && activeLine.charAt(cursorX) != '\n') {
            cursorX++;
        } else if (cursorY + 1 < lines.size()) {
            cursorX = 0;
            if (cursorY >= verticalScroll + visibleLines - 1) {
                scroll(1);
            }
            moveYCursor(1);
        }
        moveCursorRight(amount - 1);
    }

    public void moveCursorLeft(int amount) {
        if (amount <= 0) return;

        if (cursorX == 0 && cursorY == 0) return;

        cursorTick = 0;
        if (cursorX > 0) {
            cursorX--;
        } else {
            cursorX = lines.get(cursorY - 1).length();

            if (cursorX > 0 && lines.get(cursorY - 1).charAt(cursorX - 1) == '\n') {
                cursorX--;
            }

            if (cursorY - 1 < verticalScroll) {
                scroll(-1);
            }
            moveYCursor(-1);
        }
        moveCursorLeft(amount - 1);
    }

    private void moveCursorUp() {
        if (cursorY == 0) return;

        cursorTick = 0;
        String previousLine = lines.get(cursorY - 1);
        if (cursorX >= previousLine.length()) {
            cursorX = previousLine.length();
            if (previousLine.contains("\n")) {
                cursorX--;
            }
        }
        if (cursorY - 1 < verticalScroll) {
            scroll(-1);
        }
        moveYCursor(-1);
    }

    private void moveCursorDown() {
        if (cursorY == lines.size() - 1) return;

        cursorTick = 0;
        String nextLine = lines.get(cursorY + 1);
        if (cursorX > nextLine.length()) {
            cursorX = nextLine.length();
            if (nextLine.endsWith("\n")) {
                cursorX--;
            }
        }
        if (cursorY + 1 >= verticalScroll + visibleLines) {
            scroll(1);
        }
        moveYCursor(1);
    }

    private void moveYCursor(int amount) {
        cursorY += amount;
        if (cursorY < 0) {
            cursorY = 0;
            cursorX = 0;
        }
        if (cursorY >= lines.size()) {
            cursorX = lines.get(lines.size() - 1).length();
            cursorY = lines.size() - 1;
        }
    }

    private void scroll(int amount) {
        verticalScroll += amount;
        if (verticalScroll < 0) {
            verticalScroll = 0;
        } else if (verticalScroll > lines.size() - visibleLines) {
            verticalScroll = Math.max(0, lines.size() - visibleLines);
        }
    }

    /**
     * Converts the text from wrapped lines to single lines and vice versa.
     */
    private void updateText() {
        List<String> updatedLines = new ArrayList<>();
        if (wrapText) {
            for (int i = 0; i < lines.size() - 1; i++) {
                String line = lines.get(i);
                if (line.equals("\n")) {
                    updatedLines.add(line);
                    continue;
                }

                List<String> split = font.plainSubstrByWidth(lines.get(i), width - padding * 2).lines().toList();
                for (int j = 0; j < split.size() - 1; j++) {
                    updatedLines.add(split.get(j));
                }
                if (split.size() > 0) {
                    updatedLines.add(split.get(split.size() - 1) + "\n");
                }
            }

            List<String> split = font.plainSubstrByWidth(lines.get(lines.size() - 1), width - padding * 2).lines().toList();
            for (int i = 0; i < split.size() - 1; i++) {
                updatedLines.add(split.get(i));
            }
            if (split.size() > 0) {
                updatedLines.add(split.get(split.size() - 1));
            }

            List<String> activeLine = font.plainSubstrByWidth(lines.get(cursorY), width - padding * 2).lines().toList();
            int totalLength = 0;
            for (String line : activeLine) {
                if (totalLength + line.length() < cursorX) {
                    totalLength += line.length();
                    cursorY++;
                } else {
                    cursorX -= totalLength;
                    break;
                }
            }
        } else {
            int totalLength = 0;
            int lineIndex = 0;
            StringBuilder builder = new StringBuilder();
            do {
                String line = lines.get(lineIndex);
                if (totalLength > 0) {
                    builder.append(" ");
                }
                builder.append(line);

                if (lineIndex == cursorY) {
                    cursorX += totalLength;
                    cursorY = updatedLines.size();
                } else {
                    totalLength += line.length();
                }

                if (!line.endsWith("\n")) {
                    if (lineIndex == lines.size() - 1) {
                        updatedLines.add(builder.toString());
                        break;
                    }
                } else {
                    updatedLines.add(builder.toString());
                    builder.setLength(0);
                    totalLength = 0;
                }
            } while (++lineIndex < lines.size());
        }
        lines = updatedLines;
        recalculateMaxWidth();
    }

    private void updateScroll() {
        if (!wrapText) {
            int visibleWidth = width - padding * 2;
            int textWidth = font.width(lines.get(cursorY).substring(0, cursorX));
            if (textWidth < horizontalScroll) {
                horizontalScroll = Math.max(0, textWidth - 1);
            } else if (textWidth > horizontalScroll + visibleWidth) {
                horizontalScroll = Math.max(0, textWidth - visibleWidth + 1);
            } else if (cursorX == 0) {
                horizontalScroll = 0;
            }
        }

        if (cursorY < verticalScroll) {
            verticalScroll = Math.min(Math.max(0, cursorY - 1), Math.max(0, lines.size() - visibleLines));
        } else if (cursorY >= verticalScroll + visibleLines) {
            verticalScroll = Math.max(0, Math.min(cursorY + 1 - (visibleLines - 1), lines.size() - visibleLines));
        }
    }

    private void recalculateMaxWidth() {
        int maxWidth = 0;
        for (String line : lines) {
            if (font.width(line) > maxWidth) {
                maxWidth = font.width(line);
            }
        }
        maxLineWidth = maxWidth;
    }

    private int getClosestLineIndex(int lineX, int lineY) {
        String line = lines.get(lineY);
        int clickedCharX = font.plainSubstrByWidth(line, lineX).length();
        int nextCharX = Mth.clamp(clickedCharX + 1, 0, line.length());
        int clickedCharWidth = font.width(line.substring(0, clickedCharX));
        int nextCharWidth = font.width(line.substring(0, nextCharX));
        int clickedDistanceX = Math.abs(clickedCharWidth - lineX);
        int nextDistanceX = Math.abs(nextCharWidth - lineX - 1);

        int charX;
        if (Math.min(clickedDistanceX, nextDistanceX) == clickedDistanceX) {
            charX = clickedCharX;
        } else {
            charX = nextCharX;
        }
        if (charX > 0 && lines.get(lineY).charAt(charX - 1) == '\n') {
            charX--;
        }
        return charX;
    }

    /**
     * Clears the text
     */
    public void clear() {
        cursorX = 0;
        cursorY = 0;
        lines.clear();
        lines.add("");
    }

    /**
     * Gets the text in the box
     *
     * @return the text
     */
    public String getText() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < lines.size() - 1; i++) {
            builder.append(lines.get(i));
        }
        builder.append(lines.get(lines.size() - 1));
        return builder.toString();
    }

    /**
     * Sets the text for this component
     *
     * @param text the text
     */
    public void setText(String text) {
        lines.clear();
        String[] splitText = text.replace("\r", "").split("\n");
        for (int i = 0; i < splitText.length - 1; i++) {
            lines.add(splitText[i] + "\n");
        }
        lines.add(splitText[splitText.length - 1]);
        cursorX = splitText[splitText.length - 1].length();
        cursorY = splitText.length - 1;
    }

    /**
     * Sets the placeholder for the text area. This is the text that is shown if no text is present
     * and gives a hint to the user what this text area is for.
     *
     * @param placeholder the placeholder text
     */
    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    /**
     * Sets whether or not the text area should wrap it's contents.
     *
     * @param wrapText if should wrap text
     */
    public void setWrapText(boolean wrapText) {
        this.wrapText = wrapText;
        this.horizontalScroll = 0;
        updateText();
    }

    /**
     * Sets whether or not the scroll bar should be visible
     *
     * @param scrollBarVisible the scroll bar visibility
     */
    public void setScrollBarVisible(boolean scrollBarVisible) {
        this.scrollBarVisible = scrollBarVisible;
    }

    /**
     * Sets the width of the scroll bars.
     *
     * @param scrollBarSize the width of the scroll bar
     */
    public void setScrollBarSize(int scrollBarSize) {
        this.scrollBarSize = Math.max(0, scrollBarSize);
    }

    /**
     * Sets the highlighting for the text area. This is used, for instance, where you want
     * particular keywords to be a different color from the rest.
     *
     * @param highlight the highlight to color the text
     */
    public void setHighlight(IHighlight highlight) {
        this.highlight = highlight;
    }

    /**
     * Sets this text area focused. Makes it available for typing.
     *
     * @param isFocused whether the text area should be focused
     */
    public void setFocused(boolean isFocused) {
        this.isFocused = isFocused;
    }

    /**
     * Sets the padding for the text area
     *
     * @param padding the padding size
     */
    public void setPadding(int padding) {
        this.padding = padding;
        this.visibleLines = (int) Math.floor((float) ((height - padding * 2) / font.lineHeight));
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
     * Sets the background color for this component
     *
     * @param color the background color
     */
    public void setBackgroundColor(Color color) {
        this.backgroundColor = color.getRGB();
    }

    /**
     * Sets the border color for this component
     *
     * @param color the border color
     */
    public void setBorderColor(Color color) {
        this.borderColor = color.getRGB();
    }

    /**
     * Sets whether the user can edit the text
     *
     * @param editable is this component editable
     */
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    /**
     * Sets the maximum amount of lines that the text area can have. If the maximum lines is set to
     * zero or below, the text area will ignore the max line property. It's suggested that this
     * method should not be called in any other place besides the initialization of the component.
     *
     * @param maxLines the maximum amount of lines for the text area
     */
    public void setMaxLines(int maxLines) {
        if (maxLines < 0) maxLines = 0;
        this.maxLines = maxLines;
    }

    public void setKeyListener(KeyListener keyListener) {
        this.keyListener = keyListener;
    }

    private enum ScrollBar {
        HORIZONTAL, VERTICAL
    }
}
package org.tequilacat.tcatris.core;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import org.tequilacat.tcatris.R;

import java.util.ArrayList;
import java.util.List;

/**************************************************
 **************************************************/
public class Ui {
//  public static final int G_LEFT_TOP = Graphics.LEFT | Graphics.TOP;
//  public static final int G_RIGHT_TOP = Graphics.RIGHT | Graphics.TOP;
//  public static final int G_CENTER_TOP = Graphics.HCENTER | Graphics.TOP;

  private static List<String> myItems = new ArrayList<>();
  private static int myCurItem;
  private static int myMenuId;

  private static final Paint _uiPainter;
  private static final Paint _textPainter;

  private static final Paint _framePainter;

  private static final float _lineHeight;

  private static final Paint.FontMetrics _fm;
  private static final Paint _fillPainter;

  static {
    _textPainter = new Paint();
    _textPainter.setAntiAlias(true);
    _textPainter.setTextAlign(Paint.Align.LEFT);

    _framePainter = new Paint();
    _framePainter.setStyle(Paint.Style.STROKE);

    _fillPainter = new Paint();
    _fillPainter.setStyle(Paint.Style.FILL);

    _uiPainter = new Paint();
    float textSize = _uiPainter.getTextSize();
    _fm = _uiPainter.getFontMetrics();
    _lineHeight = Math.abs(_fm.top) + Math.abs(_fm.bottom);
  }

  private static float getLineHeight() {
    return _lineHeight;
  }

  public static float getTextWidth(String s) {
    return _uiPainter.measureText(s);
  }

  /**************************************************
   **************************************************/
  public static void drawShadowText(Canvas g, String text, int x, int y, int fontSize, int textColor, int shadowColor) {

    _textPainter.setColor(shadowColor);
//        g.drawString(text, xPos + 1, yPos - 1, myDisplayIconsVertically ? Ui.G_CENTER_TOP : Ui.G_LEFT_TOP);
    g.drawText(text, x + 1, y - 1, _textPainter);

    _textPainter.setColor(textColor);
//        g.drawString(text, xPos + 2, yPos, myDisplayIconsVertically ? Ui.G_CENTER_TOP : Ui.G_LEFT_TOP);
    g.drawText(text, x + 2, y, _textPainter);
  }

  public static void drawText(Canvas g, String text, int x, int y, int fontSize, int textColor) {
    _textPainter.setTextSize(fontSize);
    _textPainter.setColor(textColor);
    Paint.FontMetrics fm = _textPainter.getFontMetrics();
    g.drawText(text, x, y - fm.ascent, _textPainter);
  }

  /**************************************************
   **************************************************/
  public static String getItemString(int index) {
    return (index < 0 || index >= myItems.size()) ? null : myItems.get(index);
  }

  /**************************************************
   **************************************************/
  public static String getItemAtPoint(int x, int y) {
    return getItemString((int)(y / MenuItemHeight));
  }

  public static final int UI_COLOR_PANEL = ColorCodes.gray;
  public static final int UI_COLOR_DARKSHADOW = ColorCodes.darkGray;
  public static final int UI_COLOR_LIGHTSHADOW = ColorCodes.white;

  public static final int UI_COLOR_SELITEM_BACKGROUND = ColorCodes.red;
  public static final int UI_COLOR_SELITEM_TEXT = ColorCodes.green;

  public static final int UI_COLOR_ITEM_TEXT = ColorCodes.black;

  /**************************************************
   **************************************************/
  private static void drawRoundButton(Canvas g, int x, int y, int w, int h, int color) {
    _uiPainter.setStyle(Paint.Style.FILL);
    _uiPainter.setColor(color);

    g.drawLine(x + 1, y, x + w - 1, y, _uiPainter); // top
    g.drawLine(x, y + 1, x, y + h - 1, _uiPainter); // left

    g.drawLine(x + w, y + 1, x + w, y + h - 1, _uiPainter); // right
    g.drawLine(x + 1, y + h, x + w - 1, y + h, _uiPainter); // bottom
  }

  private static float MenuItemHeight;

  /**************************************************
   **************************************************/
  public static void displayMenu(Canvas c, int canvasWidth, int canvasHeight, String gameLabel) {
    c.drawColor(ColorCodes.gray);

    //Rect textRect = new Rect();
    //_uiPainter.getTextBounds("0", 0, 1, textRect);
    //int fHeight = textRect.height();// g.getFont().getHeight();
    // int itemH = canvasHeight / myItems.size();
    float itemH = (canvasHeight - getLineHeight()) / myItems.size();
    MenuItemHeight = itemH;
    float textDelta = (itemH - getLineHeight()) / 2, y = getLineHeight();

    _uiPainter.setColor(UI_COLOR_ITEM_TEXT);

    // g.drawString(TetrisCanvas.getTimeStr(0), canvasWidth/2, 0, Graphics.HCENTER | Graphics.TOP);
    if (gameLabel != null) {
      c.drawText(gameLabel, 0, 0, _uiPainter);
    }
    c.drawText(GameView.getTimeStr(0), canvasWidth, 0, _uiPainter);


    //Font curFont = g.getFont();
    //Font boldFont = Font.getFont(curFont.getFace(), curFont.getStyle() | Font.STYLE_BOLD, curFont.getSize());

    for (int i = 0; i < myItems.size(); i++) {

//            g.setColor(UI_COLOR_LIGHTSHADOW);
//            g.drawRect(2, y + 2, canvasWidth - 3, itemH - 3);
//            
//            g.setColor(UI_COLOR_DARKSHADOW);
//            g.drawRect(1, y + 1, canvasWidth - 3, itemH - 3);


      drawRoundButton(c, 2, (int)y + 2, canvasWidth - 4, (int)itemH - 3, UI_COLOR_LIGHTSHADOW);
      drawRoundButton(c, 1, (int)y + 1, canvasWidth - 4, (int)itemH - 3, UI_COLOR_DARKSHADOW);


      String itemText = (String) myItems.get(i);
      float textWidth = getTextWidth(itemText);

      if (i == myCurItem) {
        //g.setFont(boldFont);
        // g.setColor(UI_COLOR_LIGHTSHADOW);
        _uiPainter.setColor(UI_COLOR_DARKSHADOW);

        c.drawText(itemText,
          3 + (canvasWidth - 8 - textWidth) / 2,
          -1 + y + textDelta, _uiPainter);
        _uiPainter.setColor(UI_COLOR_SELITEM_TEXT);
      } else {
        //g.setFont(curFont);
        _uiPainter.setColor(UI_COLOR_ITEM_TEXT);
      }
      c.drawText(itemText, 4 + (canvasWidth - 8 - textWidth) / 2, y + textDelta, _uiPainter);

      y += itemH;
    }
  }

  public static void drawRect(Canvas c, int x, int y, int w, int h, int fillColor) {
    _framePainter.setColor(fillColor);
    c.drawRect(x, y, x + w, y + h, _framePainter);
  }

  public static void fillRect(Canvas c, int x, int y, int w, int h, int fillColor) {
    _fillPainter.setColor(fillColor);
    c.drawRect(x, y, x + w, y + h, _fillPainter);
  }

  public static void fillRect(Canvas c, Rect fieldRect, int fillColor) {
    fillRect(c, fieldRect.left, fieldRect.top, fieldRect.width(), fieldRect.height(), fillColor);
  }

  public static void draw3dRect(Canvas g, Rect rect) {
    draw3dRect(g, rect.left, rect.top, rect.width(), rect.height());
  }

  public static void draw3dRect(Canvas g, int x, int y, int w, int h) {
    // fillRect(g, x, y, w, h, UI_COLOR_PANEL);
    x += 2;
    w -= 4;
    y += 2;
    h -= 4;

    _framePainter.setColor(Ui.UI_COLOR_DARKSHADOW);
    g.drawLine(x - 1, y - 1, x - 1, y + h, _framePainter);
    g.drawLine(x - 1, y - 1, x + w, y - 1, _framePainter);

    _framePainter.setColor(Ui.UI_COLOR_LIGHTSHADOW);
    x += w;
    y += h;
    g.drawLine(x, y, x - w, y, _framePainter);
    g.drawLine(x, y, x, y - h, _framePainter);
    //g.drawLine(x-1, y-1, x+w, y-1);
  }
}


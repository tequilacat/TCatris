package org.tequilacat.tcatris.core;

import java.util.Vector;

import javax.microedition.lcdui.*;

/**************************************************
 **************************************************/
public class Ui {
  public static final int G_LEFT_TOP = Graphics.LEFT | Graphics.TOP;
  public static final int G_RIGHT_TOP = Graphics.RIGHT | Graphics.TOP;
  public static final int G_CENTER_TOP = Graphics.HCENTER | Graphics.TOP;

  private static Vector myItems = new Vector();
  private static int myCurItem;
  private static int myMenuId;

  /**************************************************
   **************************************************/
  public static void drawShadowText(Graphics g, String text, int x, int y, int anchor, int textColor, int shadowColor) {
    g.setColor(shadowColor);
//        g.drawString(text, xPos + 1, yPos - 1, myDisplayIconsVertically ? Ui.G_CENTER_TOP : Ui.G_LEFT_TOP);
    g.drawString(text, x + 1, y - 1, anchor);

    g.setColor(textColor);
//        g.drawString(text, xPos + 2, yPos, myDisplayIconsVertically ? Ui.G_CENTER_TOP : Ui.G_LEFT_TOP);
    g.drawString(text, x + 2, y, anchor);
  }

  /**************************************************
   **************************************************/
  public static void draw3dRect(Graphics g, int x, int y, int w, int h) {
    g.setColor(Ui.UI_COLOR_DARKSHADOW);
    g.drawLine(x - 1, y - 1, x - 1, y + h);
    g.drawLine(x - 1, y - 1, x + w, y - 1);

    g.setColor(Ui.UI_COLOR_LIGHTSHADOW);
    x += w;
    y += h;
    g.drawLine(x, y, x - w, y);
    g.drawLine(x, y, x, y - h);
    //g.drawLine(x-1, y-1, x+w, y-1);

  }

  /**************************************************
   **************************************************/
  public static void initMenu(int menuId) {
    myMenuId = menuId;
    myItems.removeAllElements();
    myCurItem = -1;
  }

  /**************************************************
   **************************************************/
  public static int getMenuId() {
    return myMenuId;
  }

  /**************************************************
   **************************************************/
  public static int getCurrentItemIndex() {
    return myCurItem;
  }

  /**************************************************
   **************************************************/
  public static String getItemString(int index) {
    return (index < 0 || index >= myItems.size()) ? null : (String) myItems.elementAt(index);
  }

  /**************************************************
   **************************************************/
  public static void addItem(String item) {
    myItems.addElement(item);
    if (myCurItem == -1) {
      myCurItem = 0;
    }
  }

  public static final int MENU_INGAME = 0;
  public static final int MENU_SELECT_GAME = 1;
  public static final int MENU_OPTIONS = 2;

  public static final String ITEM_BACK = "Back";
  public static final String ITEM_EXIT = "Exit";
  public static final String ITEM_NEWGAME = "New game";
  public static final String ITEM_SHOWSCORES = "Show scores";
  public static final String ITEM_CONTINUE = "Continue";
  public static final String ITEM_OPTIONS = "Options";

  public static final String MSG_GAMEOVER = "Game over\npress FIRE\nto continue";
  public static final String MSG_PRESS_ANYKEY = "Press a key";


  /**************************************************
   **************************************************/
  public static String getItemAtPoint(int x, int y) {
    int pos = y / MenuItemHeight;
    if (pos >= 0 && pos < myItems.size()) {
      return (String) myItems.elementAt(pos);
    } else {
      return null;
    }
  }

  /**************************************************
   * @return item Id or -1
   **************************************************/
  public static String menuKeyPressed(int actionId) {
    //int resItemId = ITEM_NOSELECTION;
    String item = null;

    if (actionId == Canvas.UP || actionId == Canvas.LEFT) {
      myCurItem--;
      if (myCurItem < 0) {
        myCurItem = myItems.size() - 1;
      }
    } else if (actionId == Canvas.DOWN || actionId == Canvas.RIGHT) {
      myCurItem++;
      if (myCurItem >= myItems.size()) {
        myCurItem = 0;
      }
    } else if (actionId == Canvas.FIRE) {
      item = (String) myItems.elementAt(myCurItem);
    }

    return item;
  }

  public static final int UI_COLOR_PANEL = Color.gray;
  public static final int UI_COLOR_DARKSHADOW = Color.darkGray;
  public static final int UI_COLOR_LIGHTSHADOW = Color.white;
  // public static final int UI_COLOR_SELITEMBACKGROUND = Color.white;

  public static final int UI_COLOR_SELITEM_BACKGROUND = Color.red;
  public static final int UI_COLOR_SELITEM_TEXT = Color.green;

  public static final int UI_COLOR_ITEM_TEXT = Color.black;

  /**************************************************
   **************************************************/
  private static void drawRoundButton(Graphics g, int x, int y, int w, int h) {
    g.drawLine(x + 1, y, x + w - 1, y); // top
    g.drawLine(x, y + 1, x, y + h - 1); // left

    g.drawLine(x + w, y + 1, x + w, y + h - 1); // right
    g.drawLine(x + 1, y + h, x + w - 1, y + h); // bottom
  }

  private static int MenuItemHeight;

  /**************************************************
   **************************************************/
  public static void displayMenu(Graphics g, int canvasWidth, int canvasHeight, String gameLabel) {
    g.setColor(Color.gray);
    g.fillRect(0, 0, canvasWidth, canvasHeight);

    int fHeight = g.getFont().getHeight();
    // int itemH = canvasHeight / myItems.size();
    int itemH = (canvasHeight - fHeight) / myItems.size();
    MenuItemHeight = itemH;
    int textDelta = (itemH - fHeight) / 2, y = fHeight;

    g.setColor(UI_COLOR_ITEM_TEXT);

    // g.drawString(TetrisCanvas.getTimeStr(0), canvasWidth/2, 0, Graphics.HCENTER | Graphics.TOP);
    if (gameLabel != null) {
      g.drawString(gameLabel, 0, 0, G_LEFT_TOP);
    }
    g.drawString(TetrisCanvas.getTimeStr(0), canvasWidth, 0, G_RIGHT_TOP);


    Font curFont = g.getFont();
    Font boldFont = Font.getFont(curFont.getFace(), curFont.getStyle() | Font.STYLE_BOLD, curFont.getSize());

    for (int i = 0; i < myItems.size(); i++) {

//            g.setColor(UI_COLOR_LIGHTSHADOW);
//            g.drawRect(2, y + 2, canvasWidth - 3, itemH - 3);
//            
//            g.setColor(UI_COLOR_DARKSHADOW);
//            g.drawRect(1, y + 1, canvasWidth - 3, itemH - 3);


      g.setColor(UI_COLOR_LIGHTSHADOW);
      drawRoundButton(g, 2, y + 2, canvasWidth - 4, itemH - 3);
      g.setColor(UI_COLOR_DARKSHADOW);
      drawRoundButton(g, 1, y + 1, canvasWidth - 4, itemH - 3);


      String itemText = (String) myItems.elementAt(i);

      if (i == myCurItem) {
        g.setFont(boldFont);
        // g.setColor(UI_COLOR_LIGHTSHADOW);
        g.setColor(UI_COLOR_DARKSHADOW);

        g.drawString(itemText,
          3 + (canvasWidth - 8 - g.getFont().stringWidth(itemText)) / 2,
          -1 + y + textDelta, Ui.G_LEFT_TOP);
        g.setColor(UI_COLOR_SELITEM_TEXT);
      } else {
        g.setFont(curFont);
        g.setColor(UI_COLOR_ITEM_TEXT);
      }

      g.drawString(itemText,
        4 + (canvasWidth - 8 - g.getFont().stringWidth(itemText)) / 2,
        y + textDelta, Ui.G_LEFT_TOP);

      y += itemH;
    }
  }
}


package org.tequilacat.tcatris.core;

import android.graphics.Color;

public class ColorCodes {
  /* public int r=0,g=0,b=0;
	
	public ColorCodes(int r, int g, int b){
		this.r = r; this.b = b; this.g = g;
	}
		
	public int intValue(){
		return (r << 16) | (g << 8) | b;
	} */

  public static int intValue(int r, int g, int b) {
    return (r << 16) | (g << 8) | b | 0xff000000;
  }

  public final static int white = ColorCodes.intValue(255, 255, 255);
  public final static int lightGray = ColorCodes.intValue(192, 192, 192);
  public final static int gray = ColorCodes.intValue(128, 128, 128);
  public final static int darkGray = ColorCodes.intValue(64, 64, 64);
  public final static int black = ColorCodes.intValue(0, 0, 0);

  public final static int red = ColorCodes.intValue(255, 0, 0);
  public final static int pink = ColorCodes.intValue(255, 175, 175);
  public final static int orange = ColorCodes.intValue(255, 200, 0);
  public final static int yellow = ColorCodes.intValue(255, 255, 0);
  public final static int green = ColorCodes.intValue(0, 255, 0);
  public final static int magenta = ColorCodes.intValue(255, 0, 255);
  public final static int purple = ColorCodes.intValue(0x80, 00, 0x80);
  public final static int cyan = ColorCodes.intValue(0, 255, 255);
  public final static int blue = ColorCodes.intValue(0, 0, 255);
  public final static int lightBlue = ColorCodes.intValue(183, 228, 255);

  public final static int brown = ColorCodes.intValue(128, 64, 0);
  public final static int lightBrown = ColorCodes.intValue(221, 111, 0);

  public final static int darkGreen = ColorCodes.intValue(0, 0x80, 0);
  public final static int darkYellow = ColorCodes.intValue(0x80, 0x80, 0);
  public final static int darkRed = ColorCodes.intValue(0x80, 0, 0);
  public final static int darkCyan = ColorCodes.intValue(0, 0x80, 0x80);

//  static string[] ColourValues = new string[] {
//          "FF0000", "00FF00", "0000FF", "FFFF00", "FF00FF", "00FFFF", "000000",
//          "800000", "008000", "000080", "808000", "800080", "008080", "808080",
//          "C00000", "00C000", "0000C0", "C0C000", "C000C0", "00C0C0", "C0C0C0",
//          "400000", "004000", "000040", "404000", "400040", "004040", "404040",
//          "200000", "002000", "000020", "202000", "200020", "002020", "202020",
//          "600000", "006000", "000060", "606000", "600060", "006060", "606060",
//          "A00000", "00A000", "0000A0", "A0A000", "A000A0", "00A0A0", "A0A0A0",
//          "E00000", "00E000", "0000E0", "E0E000", "E000E0", "00E0E0", "E0E0E0",
//  };

  public static int lighten(int color, double fraction) {
    int red = Color.red(color);
    int green = Color.green(color);
    int blue = Color.blue(color);
    red = lightenColor(red, fraction);
    green = lightenColor(green, fraction);
    blue = lightenColor(blue, fraction);
    int alpha = Color.alpha(color);
    return Color.argb(alpha, red, green, blue);
  }

  public static int darken(int color, double fraction) {
    int red = Color.red(color);
    int green = Color.green(color);
    int blue = Color.blue(color);
    red = darkenColor(red, fraction);
    green = darkenColor(green, fraction);
    blue = darkenColor(blue, fraction);
    int alpha = Color.alpha(color);

    return Color.argb(alpha, red, green, blue);
  }

  private static int darkenColor(int color, double fraction) {
    return (int)Math.max(color - (color * fraction), 0);
  }

  private static int lightenColor(int color, double fraction) {
    return (int) Math.min(color + (color * fraction), 255);
  }

  /*
   * Lightens a color by a given factor.
   *
   * @param color
   *            The color to lighten
   * @param factor
   *            The factor to lighten the color. 0 will make the color unchanged. 1 will make the
   *            color white.
   * @return lighter version of the specified color.
   */
  /*public static int lighter(int color, float factor) {
    int red = (int) ((Color.red(color) * (1 - factor) / 255 + factor) * 255);
    int green = (int) ((Color.green(color) * (1 - factor) / 255 + factor) * 255);
    int blue = (int) ((Color.blue(color) * (1 - factor) / 255 + factor) * 255);
    return Color.argb(Color.alpha(color), red, green, blue);
  }*/

  public static int intencify(int color, float percent) {
    //var f=parseInt(color.slice(1),16),t=percent<0?0:255,p=percent<0?percent*-1:percent,R=f>>16,G=f>>8&0x00FF,B=f&0x0000FF;
    int f = color;
    int t = percent < 0 ? 0 : 255;
    float p = percent < 0 ? percent * -1 : percent;
    int R = f >> 16;
    int G = f >> 8 & 0x00FF;
    int B = f & 0x0000FF;

    //return "#"+(0x1000000+(Math.round((t-R)*p)+R)*0x10000+(Math.round((t-G)*p)+G)*0x100+(Math.round((t-B)*p)+B)).toString(16).slice(1);
    return 0x1000000 + (Math.round((t - R) * p) + R) * 0x10000 + (Math.round((t - G) * p) + G) * 0x100 + (Math.round((t - B) * p) + B);
  }

}


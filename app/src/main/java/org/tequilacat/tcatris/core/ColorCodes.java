package org.tequilacat.tcatris.core;

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
}


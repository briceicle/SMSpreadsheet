package com.obsolete.fydp.spreadsheet;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

class SpreadSheetCanvas extends Canvas
{
  private int height;
  private int width;

  public SpreadSheetCanvas(){
      height = getHeight();
      width = getWidth();
  }

  public void paint(Graphics paramGraphics)
  {
      paramGraphics.setColor(255, 255, 255);
      paramGraphics.fillRect(0, 0, 1000, 1000);
  }
}
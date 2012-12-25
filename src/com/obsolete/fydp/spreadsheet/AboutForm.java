package com.obsolete.fydp.spreadsheet;


import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;


public class AboutForm extends Form implements CommandListener {
  private static final Command CMD_OK = new Command("OK", 8, 1);
  private SpreadSheet parent;
  private Display display;

  public AboutForm(Display paramDisplay, SpreadSheet paramSpreadSheet){
      super("About");
      this.parent = paramSpreadSheet;
      this.display = paramDisplay;
      addCommand(CMD_OK);
      setCommandListener(this);
      append("SMSpreadsheet application was developed by Brice Nkengsa. \n\nJuly 1, 2011.");
  }


    public void commandAction(Command paramCommand, Displayable paramDisplayable) {
      if (paramCommand == CMD_OK) {
        this.display.setCurrent(this.parent.getMainMenu());
        this.parent.reset();
     }
    }


}

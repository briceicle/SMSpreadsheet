package com.obsolete.fydp.spreadsheet;

/**
 *
 * @author bnkengsa
 */
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;

public class TextInput extends TextBox
  implements CommandListener
{
  private static final Command CMD_OK = new Command("OK", 4, 1);
  private static final Command CMD_CANCEL = new Command("Cancel", 3, 1);
  private Table parent;
  private Display display;
  private boolean saveAs = false;
  private boolean setText = false;

  public TextInput(String paramString, Table paramTable, Display paramDisplay)
  {
    super("Enter Text", paramString, 500, 0);
    this.parent = paramTable;
    this.display = paramDisplay;
    addCommand(CMD_OK);
    addCommand(CMD_CANCEL);
    setCommandListener(this);
    this.setText = true;
  }

  public TextInput(String paramString, Table paramTable, Display paramDisplay, int paramInt)
  {
    super(paramString, "", paramInt, 0);
    this.parent = paramTable;
    this.display = paramDisplay;
    addCommand(CMD_OK);
    addCommand(CMD_CANCEL);
    setCommandListener(this);
    this.saveAs = true;
  }

  public void commandAction(Command paramCommand, Displayable paramDisplayable)
  {
    if (paramCommand == CMD_OK)
    {
      if (this.setText == true)
        this.parent.editCell(getString());
      if (this.saveAs == true)
        this.parent.saveAs(getString());
      this.display.setCurrentItem(this.parent);
    }
    else if (paramCommand == CMD_CANCEL)
    {
      this.display.setCurrentItem(this.parent);
    }
  }
}

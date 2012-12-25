package com.obsolete.fydp.spreadsheet;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.TextField;

public class GotoForm extends Form
  implements CommandListener
{
  private static final Command CMD_OK = new Command("OK", 4, 1);
  private static final Command CMD_CANCEL = new Command("Cancel", 3, 1);
  private Table parent;
  private Display display;
  private TextField rowTextBox = new TextField("Row Index", null, 3, 2);
  private TextField columnTextBox = new TextField("Column Index", null, 3, 2);
  private Spacer spacer = new Spacer(0, 50);

  public GotoForm(Table paramTable, Display paramDisplay)
  {
    super("Enter Row and column");
    this.parent = paramTable;
    this.display = paramDisplay;
    addCommand(CMD_OK);
    addCommand(CMD_CANCEL);
    append(this.spacer);
    append(this.rowTextBox);
    append(this.columnTextBox);
    setCommandListener(this);
  }

  public void commandAction(Command paramCommand, Displayable paramDisplayable)
  {
    if (paramCommand == CMD_OK)
    {
      int i = Integer.parseInt(this.rowTextBox.getString());
      int j = Integer.parseInt(this.columnTextBox.getString());
      if ((i < 20) && (j < 20))
      {
        this.parent.setRowColumn(i, j);
        this.display.setCurrentItem(this.parent);
      } else {
          Alert alert = new Alert("");
          alert.setString("Row or Column index out of bounds");
          this.display.setCurrent(alert, this);
      }
    }
    else if (paramCommand == CMD_CANCEL)
    {
      this.display.setCurrentItem(this.parent);
    }
  }
}
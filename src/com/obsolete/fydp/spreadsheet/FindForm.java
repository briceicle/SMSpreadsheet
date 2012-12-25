package com.obsolete.fydp.spreadsheet;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Spacer;
import javax.microedition.lcdui.TextField;

public class FindForm extends Form
  implements CommandListener
{
  private static final Command CMD_OK = new Command("OK", 4, 1);
  private static final Command CMD_CANCEL = new Command("Cancel", 3, 1);
  private Table parent;
  private Display display;
  private TextField findTextBox = new TextField("Find", null, 50, 0);
  private Spacer spacer = new Spacer(0, 50);
  private List foundList;
  private String[] results = { "No match found" };
  private char[] searchChars;

  public FindForm(Table paramTable, Display paramDisplay)
  {
    super("Enter the value to find");
    this.parent = paramTable;
    this.display = paramDisplay;
    addCommand(CMD_OK);
    addCommand(CMD_CANCEL);
    append(this.spacer);
    append(this.findTextBox);
    setCommandListener(this);
  }

  public void commandAction(Command paramCommand, Displayable paramDisplayable)
  {
    if (paramCommand == CMD_OK)
    {
      this.foundList = new List("Search result", 3, this.results, null);
      String str1 = this.findTextBox.getString();
      this.searchChars = new char[10];
      str1.getChars(0, str1.length(), this.searchChars, 0);
      int j = 0;
      for (int k = 0; k < 20; k++)
        for (int m = 0; m < 20; m++)
        {
          if (this.parent.getCell(k,m).equals(""))
            continue;
          boolean bool = CheckValueExists(k, m, str1.length());
          if (bool != true)
            continue;
          String str3 = "" + k + "," + m + "-" + this.parent.getCell(k,m);
          if (j == 0)
          {
            this.foundList.append(str3, null);
            this.foundList.delete(j);
          }
          else
          {
            this.foundList.append(str3, null);
          }
          j++;
        }
      this.foundList.setCommandListener(this);
      this.display.setCurrent(this.foundList);
    }
    else if (paramCommand == CMD_CANCEL)
    {
      this.display.setCurrentItem(this.parent);
    }
    if (paramDisplayable.equals(this.foundList))
    {
      int i = ((List)paramDisplayable).getSelectedIndex();
      String str2 = ((List)paramDisplayable).getString(i);
      this.parent.findText(str2);
      this.display.setCurrentItem(this.parent);
    }
  }

  private boolean CheckValueExists(int paramInt1, int paramInt2, int paramInt3)
  {
    int i = 0;
    for (int j = 0; (i = this.parent.getCell(paramInt1, paramInt2).indexOf(this.searchChars[0], j)) != -1; j = i + 1)
    {
      if (paramInt3 > this.parent.getCell(paramInt1, paramInt2).length() - i)
        return false;
      for (int k = 0; (k < paramInt3) && (this.searchChars[k] == this.parent.getCell(paramInt1, paramInt2).charAt(i + k)); k++)
        if (k == paramInt3 - 1)
          return true;
    }
    return false;
  }
}
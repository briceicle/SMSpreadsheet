package com.obsolete.fydp.spreadsheet;

import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;

/**
 *
 * @author bnkengsa
 */
public class ConflictHandlerForm extends Form implements CommandListener {

  private Table parent;
  private Display display;
  private List options;

  private static final Command CMD_SELECT = new Command("SELECT", 3, 1);

public ConflictHandlerForm (Table paramTable, Display paramDisplay, Vector list){
      super("Conflict");
      this.parent = paramTable;
      this.display = paramDisplay;

      String[] arrayOfString = new String[list.size()];
      for(int i = 0; i < list.size(); i++)
          arrayOfString[i] = (String)list.elementAt(i);

      this.options = new List("Conflict Resolution", 3, arrayOfString, null);
      this.options.addCommand(CMD_SELECT);
      this.options.setCommandListener(this);
}

public void commandAction(Command c, Displayable paramDisplayable) {
      if (c == CMD_SELECT) {
         int i = ((List)paramDisplayable).getSelectedIndex();
         String str = ((List)paramDisplayable).getString(i);
         parent.editCell(str);
         this.display.setCurrentItem(this.parent);
      }
}

public List getList() {
    return options;
}


}

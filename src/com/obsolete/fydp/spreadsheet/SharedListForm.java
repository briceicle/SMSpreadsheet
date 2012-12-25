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
public class SharedListForm extends Form implements CommandListener {

  private static final Command CMD_ADD = new Command("Add", 8, 1);
  private static final Command CMD_DELETE = new Command("Delete", 8, 1);
  private static final Command CMD_CANCEL = new Command("Close", 3, 1);

  private Table parent;
  private Display display;
  private List contacts;

  public SharedListForm(Table paramTable, Display paramDisplay){
      super("Shared List");
      this.parent = paramTable;
      this.display = paramDisplay;

      Vector list = this.parent.getContacts();
      String[] arrayOfString = new String[list.size()];
      for(int i = 0; i < list.size(); i++)
          arrayOfString[i] = (String)list.elementAt(i);

      this.contacts = new List("Shared List", 3, arrayOfString, null);
      this.contacts.addCommand(CMD_ADD);
      this.contacts.addCommand(CMD_DELETE);
      this.contacts.addCommand(CMD_CANCEL);
      this.contacts.setCommandListener(this);
  }

  public void commandAction(Command paramCommand, Displayable paramDisplayable) {
    int i;
    String str1;
    if (paramCommand == CMD_CANCEL) {
        this.display.setCurrentItem(this.parent);
    }
    else if (paramCommand == CMD_ADD) {
        Object obj = new AddContactForm(this, this.display);
        this.display.setCurrent((Displayable)obj);

    } else if (paramCommand == CMD_DELETE) {
        i = ((List)paramDisplayable).getSelectedIndex();
        if (i < 0)
            return;
        str1 = ((List)paramDisplayable).getString(i);
        if (str1 == null)
            return;
        ((List)paramDisplayable).delete(i);
        this.parent.deleteContact(str1);
    }
      
  }

  public void AddContact(String phoneNo) {
      this.parent.AddContact(phoneNo);
      this.parent.setContactAdded(true);
      this.contacts.append(phoneNo, null);
  }

  public List getList() {
      return this.contacts;
  }

  public void mapContact(String PhoneNo, String permission) {
      this.parent.mapContact(PhoneNo, permission);
  }

}

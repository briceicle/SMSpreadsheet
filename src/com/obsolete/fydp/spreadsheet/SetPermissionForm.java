package com.obsolete.fydp.spreadsheet;

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
public class SetPermissionForm extends Form implements CommandListener {
    private static final Command CMD_OK = new Command("OK", 4, 1);
    private static final Command CMD_CANCEL = new Command("Cancel", 3, 1);
    private AddContactForm parent;
    private Display display;
    private List list;
    private String phoneNo;

    public SetPermissionForm(AddContactForm paramForm, Display paramDisplay, String phoneNo) {
        super("Set Permission");
        this.parent = paramForm;
        this.display = paramDisplay;
        this.phoneNo = phoneNo;

        list = new List("Permission", List.EXCLUSIVE);
        list.append("Reader", null);
        list.append("Writer", null);
        list.addCommand(CMD_OK);
        list.addCommand(CMD_CANCEL);
        list.setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d) {
        int i;
        Contact contact = null;
        if (c == CMD_OK) {
            i = ((List)d).getSelectedIndex();
            if (i == 0)
                this.parent.mapContact(this.phoneNo, "R");
            if (i == 1)
                this.parent.mapContact(this.phoneNo, "W");
            
            this.display.setCurrent(this.parent.getContactList());

        } else if (c == CMD_CANCEL) {
            this.display.setCurrent(parent);
        }
    }

    public List getList() {
        return this.list;
    }

}

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
public class DeleteSheetForm extends Form implements CommandListener {

  private SpreadSheet sheet;
  private RecordsForm recordForm;
  private Display display;
  private List options;

  private static final Command CMD_SELECT = new Command("SELECT", 3, 1);
  private static final Command CMD_CANCEL = new Command("CANCEL", 3, 1);

    public DeleteSheetForm (Display paramDisplay, SpreadSheet paramSheet, RecordsForm paramForm) {
        super("Deleting Shared Item");
        this.display = paramDisplay;
        this.sheet = paramSheet;
        this.recordForm = paramForm;

        String[] list = {"Choose new owner", "Trash for everyone"};
        this.options = new List("Deleting Shared Item", 3, list, null);
        this.options.addCommand(CMD_SELECT);
        this.options.addCommand(CMD_CANCEL);
        this.options.setCommandListener(this);
    }

public void commandAction(Command paramCommand, Displayable paramDisplayable) {
    int i;
    String str;
    if (paramCommand == CMD_CANCEL) {
        this.display.setCurrent(this.recordForm.getRecordList());
    }
    else if (paramCommand == CMD_SELECT) {
        i = ((List)paramDisplayable).getSelectedIndex();
        if (i < 0)
            return;
        if (i == 0) {
            // choose new owner
            SetOwnerForm form = new SetOwnerForm(this.display, this.sheet, this.recordForm);
            this.display.setCurrent(form.getList());
        } else {
            // Delete owner's record
            this.recordForm.deleteRecord();

            // TO DO: Send message to all contacts to delete their record
            String[] contacts = this.sheet.getContactList(this.recordForm.getSelectedRecord());
            if (contacts != null) {
                for (int j = 0; j < contacts.length; j++) {
                    String phoneNo = contacts[j];
                    SMSHeader msg = new SMSHeader(this.recordForm.getSelectedRecord(), "", 0, 1, 1, -2, -2, "");
                    SendSMS sms = new SendSMS(this.sheet, phoneNo, msg.toString(), this.sheet.getPort());
                    sms.start();
                }
            }

            this.display.setCurrent(this.recordForm.getRecordList());
        }
    }
}

public List getList() {
    return options;
}

}

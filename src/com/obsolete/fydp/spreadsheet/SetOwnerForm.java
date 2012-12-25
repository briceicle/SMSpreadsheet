package com.obsolete.fydp.spreadsheet;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.rms.RecordStore;

/**
 *
 * @author bnkengsa
 */
public class SetOwnerForm extends Form implements CommandListener {

  private SpreadSheet sheet;
  private RecordsForm recordForm;
  private Display display;
  private List options;

  private static final Command CMD_SELECT = new Command("SELECT", 3, 1);
  private static final Command CMD_CANCEL = new Command("CANCEL", 3, 1);

    public SetOwnerForm (Display paramDisplay, SpreadSheet paramSheet, RecordsForm paramForm) {
        super("Choose new owner");
        this.display = paramDisplay;
        this.sheet = paramSheet;
        this.recordForm = paramForm;

        String[] list = null;
        try{
          String sharedContacts = this.recordForm.getSelectedRecord() + "/" + "Contacts";
          RecordStore rs = RecordStore.openRecordStore(sharedContacts, true);
          list = new String[rs.getNumRecords()];
          for (int i = 1; i <= rs.getNumRecords(); i++) {
            byte[] arrayOfByte = rs.getRecord(i);
            String str = new String(arrayOfByte);
            list[i-1] = str;
          }
          rs.closeRecordStore();
        }catch (Exception e) {
            e.printStackTrace();
        }

        this.options = new List("Choose new owner", 3, list, null);
        this.options.addCommand(CMD_SELECT);
        this.options.addCommand(CMD_CANCEL);
        this.options.setCommandListener(this);
    }

public void commandAction(Command paramCommand, Displayable paramDisplayable) {
    int i;
    String str;
    if (paramCommand == CMD_CANCEL) {
        DeleteSheetForm deleteForm = new DeleteSheetForm(this.display, this.sheet, this.recordForm);
        this.display.setCurrent(deleteForm.getList());
    }
    else if (paramCommand == CMD_SELECT) {
        i = ((List)paramDisplayable).getSelectedIndex();
        if (i < 0)
            return;
        str = ((List)paramDisplayable).getString(i);

        // send message to the new owner
        SMSHeader msg = new SMSHeader(this.recordForm.getSelectedRecord(), "", 0, 1, 1, -1, -1, "");
        SendSMS sms = new SendSMS(this.sheet, str, msg.toString(), this.sheet.getPort());
        sms.start();

        // send messages to other contacts to remove old owner from their list of contacts
        for (int j = 0; j < ((List)paramDisplayable).size(); j++) {
            if (i != j) {
                String phoneNo = ((List)paramDisplayable).getString(j);
                SMSHeader message = new SMSHeader(this.recordForm.getSelectedRecord(), "", 0, 1, 1, -4, -4, "");
                SendSMS smsThread = new SendSMS(this.sheet, phoneNo, message.toString(), this.sheet.getPort());
                smsThread.start();
            }
        }

        recordForm.deleteRecord();
        this.display.setCurrent(recordForm.getRecordList());
    }
}

public List getList() {
    return options;
}


}

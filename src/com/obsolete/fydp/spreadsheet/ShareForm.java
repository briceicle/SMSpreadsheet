package com.obsolete.fydp.spreadsheet;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.TextField;

/**
 *
 * @author bnkengsa
 */
public class ShareForm extends Form implements CommandListener {
  private static final Command CMD_OK = new Command("OK", 4, 1);
  private static final Command CMD_CANCEL = new Command("Cancel", 3, 1);
  private Table parent;
  private Display display;
  private TextField phoneNo;
  Alert errorMessageAlert;

  public ShareForm(Table paramTable, Display paramDisplay){
      super("Add People");

      phoneNo = new TextField("Phone ","",48,TextField.PHONENUMBER);
      errorMessageAlert = new Alert ("Alert", null, null, AlertType.ERROR);
      errorMessageAlert.setTimeout (5000);

      this.parent = paramTable;
      this.display = paramDisplay;
      addCommand(CMD_OK);
      addCommand(CMD_CANCEL);
      append(this.phoneNo);
      setCommandListener(this);
  }


    public void commandAction(Command paramCommand, Displayable paramDisplayable) {
      if (paramCommand == CMD_OK) {
          String number  = this.phoneNo.getString();
          if(!isValidPhoneNumber(number)){
              errorMessageAlert.setString("Invalid Phone Number");
              this.display.setCurrent(errorMessageAlert, paramDisplayable);
          }else{
              this.parent.AddContact(number);
              this.display.setCurrentItem(this.parent);
          }

      } else if (paramCommand == CMD_CANCEL) {
        this.display.setCurrentItem(this.parent);
      }
    }

    private boolean isValidPhoneNumber (String number) {
        char[] chars = number.toCharArray ();

        if (chars.length == 0) {
            return false;
        }

        int startPos = 0;

        // initial '+' is OK
        if (chars[0] == '+') {
            startPos = 1;
        }

        for (int i = startPos; i < chars.length; ++i) {
            if (!Character.isDigit (chars[i])) {
                return false;
            }
        }

        return true;
    }

}

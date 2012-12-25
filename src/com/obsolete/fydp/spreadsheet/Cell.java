/**
 *
 * @author Susan Liu
 */
package com.obsolete.fydp.spreadsheet;
import java.util.Date;
import java.util.Vector;
import java.util.Stack;
public class Cell {
  private int row;              // row of cell
  private int col;              // column of cell
  private Table parent;         // table that contains the cell
  private int numRows;          // total number of rows in table
  private int numCols;          // total number of cols in table
  private String data;          // pure data (user entered)
  private String displayData;   // data for display (for formulas)
  private double formulaValue;  // numerical formula value
  private double intermValue;   // used for interm calculations; DO NOT USE
  private double cellCount;     // used for interm calculations; DO NOT USE
  private boolean validCell;    // used for interm calculations; DO NOT USE
  private boolean formula;      // true if cell contains a formula
  private boolean badFormula;   // true if cell contains a bad format formula
  private boolean modified;     // true if cell was modified
  private Date lastModified;    // date of last modify
  private Vector listeners;     // listeners to this cell
  public Cell(int r, int c, Date last, Table p) {
    this.row = r;
    this.col = c;
    this.data = "";
    this.displayData = "";
    this.lastModified = last;
    this.parent = p;
    this.modified = false;
    this.formula = false;
    this.badFormula = false;
    this.formulaValue = 0;
    this.numRows = 20;
    this.numCols = 20;
    this.listeners = new Vector();
  }
  public int getRow() {
    return row;
  }
  public int getCol() {
    return col;
  }
  public boolean equals(Object o) {
    if (!(o instanceof Cell)) {
      return false;
    }
    Cell c = (Cell)o;
    return c.row == this.row && c.col == this.col;
  }
  public int hashCode() {
    return (this.row * 100) + this.col;
  }
  public Date getLastModified() {
    return this.lastModified;
  }
  public void setLastModified(Date d) {
    this.lastModified = d;
  }
  public boolean getModified() {
    return this.modified;
  }
  public void setModified(boolean m) {
    this.modified = m;
  }
  public String getData() {
    return this.data;
  }
  public void setData(String d) {
    this.data = d;
    parseData();
  }
  public String getDisplayData() {
    if (formula) {
      return this.displayData;
    } else {
      return this.data;
    }
  }
  public void addListener(Cell listener) {
    if (!this.listeners.contains(listener)) {
      this.listeners.addElement(listener);
    }
  }
  public void notifyChanged(Cell changedCell) {
    parseData();
  }
  private void notifyListeners() {
    for (int i = 0; i < this.listeners.size(); ++i) {
      ((Cell)this.listeners.elementAt(i)).notifyChanged(this);
    }
  }
  private void parseData() {
    boolean isFormula = false;
    if (this.data.length() > 0) {
      String parse = data.trim();
      if (parse.charAt(0) == '=') {
        parse = parse.substring(1);
        if (isSUM(parse)) {
          isFormula = true;
        }
        else if (isAVG(parse)) {
          isFormula = true;
        }
        else if (isMAX(parse)) {
          isFormula = true;
        }
        else if (isMIN(parse)) {
          isFormula = true;
        }
        else if (isMATH(parse)) {
          isFormula = true;
        }
      }
    }
    if (isFormula) {
      this.formula = true;
      if (this.badFormula) {
        this.displayData = "#BAD FORMULA#";
      } else {
        this.displayData = this.formulaValue + "";
      }
    } else {
      this.formula = false;
    }
    notifyListeners();
  } // function parseData
  // returns false if currCell cannot be parsed into a double, true otherwise
  private boolean formulaHelper(Cell currCell) {
    currCell.addListener(this);
    this.intermValue = 0;
    this.validCell = true;
    if (currCell.data.length() == 0) {
      this.validCell = false;
    }
    else if (currCell.formula && !currCell.badFormula) {
      this.intermValue = currCell.formulaValue;
      this.cellCount += 1;
    }
    else if (currCell.formula && currCell.badFormula) {
      this.validCell = false;
    } else {
      try {
        double num = Double.parseDouble(currCell.data);
        this.intermValue = num;
        this.cellCount += 1;
      } catch (NumberFormatException e) {
        this.validCell = false;
        return false;
      }
    }
    return true;
  }
  // format: =SUM(fromRow,fromCol:toRow,toCol)
  private boolean isSUM(String parse) {
    parse = parse.trim().toLowerCase();
    if (parse.startsWith("sum") && parse.endsWith(")")) {
      this.badFormula = true;
      int bracketLoc = parse.indexOf("(");
      if (bracketLoc <= 0) {
        return true;
      }
      String values = parse.substring(bracketLoc + 1, parse.length() - 1);
      int colonLoc = values.indexOf(":");
      if (colonLoc <= 0) {
        return true;
      }
      String first = values.substring(0, colonLoc);
      String second = values.substring(colonLoc + 1);
      int commaLoc1 = first.indexOf(",");
      int commaLoc2 = second.indexOf(",");
      if (commaLoc1 <= 0 || commaLoc2 <= 0) {
        return true;
      }
      try {
        int r1 = Integer.parseInt(first.substring(0, commaLoc1).trim());
        int c1 = Integer.parseInt(first.substring(commaLoc1 + 1).trim());
        int r2 = Integer.parseInt(second.substring(0, commaLoc2).trim());
        int c2 = Integer.parseInt(second.substring(commaLoc2 + 1).trim());
        if (r1 != r2 && c1 != c2) {
          return true;
        }
        if (r1 == r2 && c1 > c2) {
          int temp = c1;
          c1 = c2;
          c2 = temp;
        }
        if (c1 == c2 && r1 > r2) {
          int temp = r1;
          r1 = r2;
          r2 = temp;
        }
        if (r1 < 0 || r2 >= numRows || c1 < 0 || c2 >= numCols) {
          return true;
        }
        // formula is in correct format, apply actual formula
        Cell currCell = parent.getCellObject(r1, c1);
        double sum = 0;
        do {
          // check self dependency
          if (currCell.row == this.row && currCell.col == this.col) {
            this.data = "=SUM(#CIRCULAR_REF)";
            return true;
          }
          // check circular dependencies
          if (this.listeners.contains(currCell)) {
            this.data = "=SUM(#CIRCULAR_REF)";
            return true;
          }
          if (!formulaHelper(currCell)) {
            this.intermValue = 0;
          }
          sum += this.intermValue;
          if (r1 == r2) {
            currCell = parent.getCellObject(r1, currCell.col + 1);
          } else {
            currCell = parent.getCellObject(currCell.row + 1, c1);
          }
        } while (currCell.row < r2 || currCell.col < c2);
        if (r1 == r2 && c1 == c2) {
          // do nothing
        } else {
          // add the last cell
          // check self dependency
          if (currCell.row == this.row && currCell.col == this.col) {
            this.data = "=SUM(#CIRCULAR_REF)";
            return true;
          }
          // check circular dependencies
          if (this.listeners.contains(currCell)) {
            this.data = "=SUM(#CIRCULAR_REF)";
            return true;
          }
          if (!formulaHelper(currCell)) {
            this.intermValue = 0;
          }
          sum += this.intermValue;
        }
        this.formulaValue = sum;
        this.badFormula = false;
        return true;
      } catch (NumberFormatException e) {
        return true;
      }
    }
    return false;
  } // function isSUM
  // format: =AVG(fromRow,fromCol:toRow,toCol)
  private boolean isAVG(String parse) {
    parse = parse.trim().toLowerCase();
    if (parse.startsWith("avg") && parse.endsWith(")")) {
      this.badFormula = true;
      int bracketLoc = parse.indexOf("(");
      if (bracketLoc <= 0) {
        return true;
      }
      String values = parse.substring(bracketLoc + 1, parse.length() - 1);
      int colonLoc = values.indexOf(":");
      if (colonLoc <= 0) {
        return true;
      }
      String first = values.substring(0, colonLoc);
      String second = values.substring(colonLoc + 1);
      int commaLoc1 = first.indexOf(",");
      int commaLoc2 = second.indexOf(",");
      if (commaLoc1 <= 0 || commaLoc2 <= 0) {
        return true;
      }
      try {
        int r1 = Integer.parseInt(first.substring(0, commaLoc1).trim());
        int c1 = Integer.parseInt(first.substring(commaLoc1 + 1).trim());
        int r2 = Integer.parseInt(second.substring(0, commaLoc2).trim());
        int c2 = Integer.parseInt(second.substring(commaLoc2 + 1).trim());
        if (r1 != r2 && c1 != c2) {
          return true;
        }
        if (r1 == r2 && c1 > c2) {
          int temp = c1;
          c1 = c2;
          c2 = temp;
        }
        if (c1 == c2 && r1 > r2) {
          int temp = r1;
          r1 = r2;
          r2 = temp;
        }
        if (r1 < 0 || r2 >= numRows || c1 < 0 || c2 >= numCols) {
          return true;
        }
        // formula is in correct format, apply actual formula
        Cell currCell = parent.getCellObject(r1, c1);
        double sum = 0;
        this.cellCount = 0;
        do {
          // check self dependency
          if (currCell.row == this.row && currCell.col == this.col) {
            this.data = "=SUM(#CIRCULAR_REF)";
            return true;
          }
          // check circular dependencies
          if (this.listeners.contains(currCell)) {
            this.data = "=SUM(#CIRCULAR_REF)";
            return true;
          }
          if (!formulaHelper(currCell)) {
            this.intermValue = 0;
          }
          sum += this.intermValue;
          if (r1 == r2) {
            currCell = parent.getCellObject(r1, currCell.col + 1);
          } else {
            currCell = parent.getCellObject(currCell.row + 1, c1);
          }
        } while (currCell.row < r2 || currCell.col < c2);
        if (r1 == r2 && c1 == c2) {
          // do nothing
        } else {
          // add the last cell
          // check self dependency
          if (currCell.row == this.row && currCell.col == this.col) {
            this.data = "=SUM(#CIRCULAR_REF)";
            return true;
          }
          // check circular dependencies
          if (this.listeners.contains(currCell)) {
            this.data = "=SUM(#CIRCULAR_REF)";
            return true;
          }
          if (!formulaHelper(currCell)) {
            this.intermValue = 0;
          }
          sum += this.intermValue;
        }
        this.formulaValue = sum / this.cellCount;
        this.badFormula = false;
        return true;
      } catch (NumberFormatException e) {
        return true;
      }
    }
    return false;
  } // function isAVG
  // format: =MAX(fromRow,fromCol:toRow,toCol)
  private boolean isMAX(String parse) {
    parse = parse.trim().toLowerCase();
    if (parse.startsWith("max") && parse.endsWith(")")) {
      this.badFormula = true;
      int bracketLoc = parse.indexOf("(");
      if (bracketLoc <= 0) {
        return true;
      }
      String values = parse.substring(bracketLoc + 1, parse.length() - 1);
      int colonLoc = values.indexOf(":");
      if (colonLoc <= 0) {
        return true;
      }
      String first = values.substring(0, colonLoc);
      String second = values.substring(colonLoc + 1);
      int commaLoc1 = first.indexOf(",");
      int commaLoc2 = second.indexOf(",");
      if (commaLoc1 <= 0 || commaLoc2 <= 0) {
        return true;
      }
      try {
        int r1 = Integer.parseInt(first.substring(0, commaLoc1).trim());
        int c1 = Integer.parseInt(first.substring(commaLoc1 + 1).trim());
        int r2 = Integer.parseInt(second.substring(0, commaLoc2).trim());
        int c2 = Integer.parseInt(second.substring(commaLoc2 + 1).trim());
        if (r1 != r2 && c1 != c2) {
          return true;
        }
        if (r1 == r2 && c1 > c2) {
          int temp = c1;
          c1 = c2;
          c2 = temp;
        }
        if (c1 == c2 && r1 > r2) {
          int temp = r1;
          r1 = r2;
          r2 = temp;
        }
        if (r1 < 0 || r2 >= numRows || c1 < 0 || c2 >= numCols) {
          return true;
        }
        // formula is in correct format, apply actual formula
        Cell currCell = parent.getCellObject(r1, c1);
        double max = -1.0 * Double.MAX_VALUE;
        boolean init = false;
        do {
          // check self dependency
          if (currCell.row == this.row && currCell.col == this.col) {
            this.data = "=SUM(#CIRCULAR_REF)";
            return true;
          }
          // check circular dependencies
          if (this.listeners.contains(currCell)) {
            this.data = "=SUM(#CIRCULAR_REF)";
            return true;
          }
          formulaHelper(currCell);
          if (this.validCell && this.intermValue > max) {
            max = this.intermValue;
            init = true;
          }
          if (r1 == r2) {
            currCell = parent.getCellObject(r1, currCell.col + 1);
          } else {
            currCell = parent.getCellObject(currCell.row + 1, c1);
          }
        } while (currCell.row < r2 || currCell.col < c2);
        if (r1 == r2 && c1 == c2) {
          // do nothing
        } else {
          // add the last cell
          // check self dependency
          if (currCell.row == this.row && currCell.col == this.col) {
            this.data = "=SUM(#CIRCULAR_REF)";
            return true;
          }
          // check circular dependencies
          if (this.listeners.contains(currCell)) {
            this.data = "=SUM(#CIRCULAR_REF)";
            return true;
          }
          formulaHelper(currCell);
          if (this.validCell && this.intermValue > max) {
            max = this.intermValue;
            init = true;
          }
        }
        if (!init) {
          max = 0;
        }
        this.formulaValue = max;
        this.badFormula = false;
        return true;
      } catch (NumberFormatException e) {
        return true;
      }
    }
    return false;
  } // function isMAX
  // format: =MIN(fromRow,fromCol:toRow,toCol)
  private boolean isMIN(String parse) {
    parse = parse.trim().toLowerCase();
    if (parse.startsWith("min") && parse.endsWith(")")) {
      this.badFormula = true;
      int bracketLoc = parse.indexOf("(");
      if (bracketLoc <= 0) {
        return true;
      }
      String values = parse.substring(bracketLoc + 1, parse.length() - 1);
      int colonLoc = values.indexOf(":");
      if (colonLoc <= 0) {
        return true;
      }
      String first = values.substring(0, colonLoc);
      String second = values.substring(colonLoc + 1);
      int commaLoc1 = first.indexOf(",");
      int commaLoc2 = second.indexOf(",");
      if (commaLoc1 <= 0 || commaLoc2 <= 0) {
        return true;
      }
      try {
        int r1 = Integer.parseInt(first.substring(0, commaLoc1).trim());
        int c1 = Integer.parseInt(first.substring(commaLoc1 + 1).trim());
        int r2 = Integer.parseInt(second.substring(0, commaLoc2).trim());
        int c2 = Integer.parseInt(second.substring(commaLoc2 + 1).trim());
        if (r1 != r2 && c1 != c2) {
          return true;
        }
        if (r1 == r2 && c1 > c2) {
          int temp = c1;
          c1 = c2;
          c2 = temp;
        }
        if (c1 == c2 && r1 > r2) {
          int temp = r1;
          r1 = r2;
          r2 = temp;
        }
        if (r1 < 0 || r2 >= numRows || c1 < 0 || c2 >= numCols) {
          return true;
        }
        // formula is in correct format, apply actual formula
        Cell currCell = parent.getCellObject(r1, c1);
        double min = Double.MAX_VALUE;
        boolean init = false;
        do {
          // check self dependency
          if (currCell.row == this.row && currCell.col == this.col) {
            this.data = "=SUM(#CIRCULAR_REF)";
            return true;
          }
          // check circular dependencies
          if (this.listeners.contains(currCell)) {
            this.data = "=SUM(#CIRCULAR_REF)";
            return true;
          }
          formulaHelper(currCell);
          if (this.validCell && this.intermValue < min) {
            min = this.intermValue;
            init = true;
          }
          if (r1 == r2) {
            currCell = parent.getCellObject(r1, currCell.col + 1);
          } else {
            currCell = parent.getCellObject(currCell.row + 1, c1);
          }
        } while (currCell.row < r2 || currCell.col < c2);
        if (r1 == r2 && c1 == c2) {
          // do nothing
        } else {
          // add the last cell
          // check self dependency
          if (currCell.row == this.row && currCell.col == this.col) {
            this.data = "=SUM(#CIRCULAR_REF)";
            return true;
          }
          // check circular dependencies
          if (this.listeners.contains(currCell)) {
            this.data = "=SUM(#CIRCULAR_REF)";
            return true;
          }
          formulaHelper(currCell);
          if (this.validCell && this.intermValue < min) {
            min = this.intermValue;
            init = true;
          }
        }
        if (!init) {
          min = 0;
        }
        this.formulaValue = min;
        this.badFormula = false;
        return true;
      } catch (NumberFormatException e) {
        return true;
      }
    }
    return false;
  } // function isMIN
  // format: ={mathematical expression with +, -, *, / and () supported}
  private boolean isMATH(String parse) {
    String[] tokens = parseMath(parse);
    this.badFormula = true;
    this.validCell = true;
    String[] pfTokens = convertToPostfix(tokens);
    if (!this.validCell) {
      return true;
    }
    // now evaluate the postfix
    Stack stack = new Stack();
    for (int i = 0; i < pfTokens.length; ++i) {
      String curr = pfTokens[i];
      try {
        Double num = new Double(Double.parseDouble(curr));
        stack.push(num);
        continue;
      } catch (NumberFormatException e) {
        // do nothing
      }
      if (curr.equals("+")) {
        if (stack.empty()) {
          return true;
        }
        Double op2 = (Double)stack.pop();
        if (stack.empty()) {
          return true;
        }
        Double op1 = (Double)stack.pop();
        double res = op1.doubleValue() + op2.doubleValue();
        stack.push(new Double(res));
      }
      else if (curr.equals("-")) {
        if (stack.empty()) {
          return true;
        }
        Double op2 = (Double)stack.pop();
        if (stack.empty()) {
          return true;
        }
        Double op1 = (Double)stack.pop();
        double res = op1.doubleValue() - op2.doubleValue();
        stack.push(new Double(res));
      }
      else if (curr.equals("*")) {
        if (stack.empty()) {
          return true;
        }
        Double op2 = (Double)stack.pop();
        if (stack.empty()) {
          return true;
        }
        Double op1 = (Double)stack.pop();
        double res = op1.doubleValue() * op2.doubleValue();
        stack.push(new Double(res));
      }
      else if (curr.equals("/")) {
        if (stack.empty()) {
          return true;
        }
        Double op2 = (Double)stack.pop();
        if (stack.empty()) {
          return true;
        }
        Double op1 = (Double)stack.pop();
        double res = op1.doubleValue() / op2.doubleValue();
        stack.push(new Double(res));
      } else {
        int commaLoc = curr.indexOf(",");
        if (commaLoc > 0) {
          try {
            int cellRow = Integer.parseInt(curr.substring(0, commaLoc).trim());
            int cellCol = Integer.parseInt(curr.substring(commaLoc + 1).trim());
            if (cellRow < 0 || cellRow >= numRows || cellCol < 0 || cellCol >= numCols) {
              return true;
            }
            Cell cell = parent.getCellObject(cellRow, cellCol);
            formulaHelper(cell);
            stack.push(new Double(this.intermValue));
          } catch (NumberFormatException e) {
            return true;
          }
        } else {
          return true;
        }
      }
    }
    if (stack.empty()) {
      return true;
    }
    double finalRes = ((Double)stack.pop()).doubleValue();
    if (!stack.empty()) {
      return true;
    }
    this.formulaValue = finalRes;
    this.badFormula = false;
    return true;
  } // function isMATH
  private String[] convertToPostfix(String[] tokens) {
    Vector output = new Vector();
    Stack stack = new Stack();
    for (int i = 0; i < tokens.length; ++i) {
      String curr = tokens[i];
      try {
        double num = Double.parseDouble(curr);
        output.addElement(curr);
        continue;
      } catch (NumberFormatException e) {
        // do nothing
      }
      if (curr.indexOf(",") >= 0) {
        output.addElement(curr);
      }
      else if (curr.equals("+")) {
        while(opOnTop(stack)) {
          output.addElement(stack.pop());
        }
        stack.push(curr);
      }
      else if (curr.equals("-")) {
        while(opOnTop(stack)) {
          output.addElement(stack.pop());
        }
        stack.push(curr);
      }
      else if (curr.equals("*")) {
        while(opOnTop(stack) && (((String)stack.peek()).equals("*") ||
                                 ((String)stack.peek()).equals("/"))) {
          output.addElement(stack.pop());
        }
        stack.push(curr);
      }
      else if (curr.equals("/")) {
        while(opOnTop(stack) && (((String)stack.peek()).equals("*") ||
                                 ((String)stack.peek()).equals("/"))) {
          output.addElement(stack.pop());
        }
        stack.push(curr);
      }
      else if (curr.equals("(")) {
        stack.push(curr);
      }
      else if (curr.equals(")")) {
        while(!stack.empty() && !(((String)stack.peek()).equals("("))) {
          output.addElement(stack.pop());
        }
        if (stack.empty()) {
          // mismatched parentheses
          this.validCell = false;
          return null;
        } else {
          stack.pop();
        }
      }
    }
    while (!stack.empty()) {
      if (((String)stack.peek()).equals("(") || ((String)stack.peek()).equals(")")) {
        // mismatched parentheses
        this.validCell = false;
        return null;
      } else {
        output.addElement(stack.pop());
      }
    }
    String[] result = new String[output.size()];
    output.copyInto(result);
    return result;
  } // function convertToPostfix
  private boolean opOnTop(Stack stack) {
    if (!stack.empty()) {
      if (((String)stack.peek()).equals("+")) {
        return true;
      }
      if (((String)stack.peek()).equals("-")) {
        return true;
      }
      if (((String)stack.peek()).equals("*")) {
        return true;
      }
      if (((String)stack.peek()).equals("/")) {
        return true;
      }
    }
    return false;
  }
  private String[] parseMath(String parseString) {
    String parse = parseString.trim();
    Vector tokens = new Vector();
    String temp = "";
    boolean prevOp = false;
    for (int i = 0; i < parse.length(); ++i) {
      if (parse.charAt(i) == '+') {
        if (prevOp || i == 0) {
          temp += "+";
        } else {
          if (temp.length() > 0) {
            tokens.addElement(temp.trim());
            temp = "";
          }
          tokens.addElement("+");
          prevOp = true;
        }
      }
      else if (parse.charAt(i) == '-') {
        if (prevOp || i == 0) {
          temp += "-";
        } else {
          if (temp.length() > 0) {
            tokens.addElement(temp.trim());
            temp = "";
          }
          tokens.addElement("-");
          prevOp = true;
        }
      }
      else if (parse.charAt(i) == '*') {
        if (temp.length() > 0) {
          tokens.addElement(temp.trim());
          temp = "";
        }
        tokens.addElement("*");
        prevOp = true;
      }
      else if (parse.charAt(i) == '/') {
        if (temp.length() > 0) {
          tokens.addElement(temp.trim());
          temp = "";
        }
        tokens.addElement("/");
        prevOp = true;
      }
      else if (parse.charAt(i) == '(') {
        if (temp.length() > 0) {
          tokens.addElement(temp.trim());
          temp = "";
        }
        tokens.addElement("(");
        prevOp = true;
      }
      else if (parse.charAt(i) == ')') {
        if (temp.length() > 0) {
          tokens.addElement(temp.trim());
          temp = "";
        }
        tokens.addElement(")");
        prevOp = false;
      }
      else if (parse.charAt(i) == ' ') {
        continue;
      } else {
        temp += parse.charAt(i);
        prevOp = false;
      }
    }
    if (temp.length() > 0) {
      tokens.addElement(temp.trim());
    }
    String[] result = new String[tokens.size()];
    tokens.copyInto(result);
    return result;
  }
}
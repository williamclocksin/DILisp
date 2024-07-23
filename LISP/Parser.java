package LISP;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;


// Static class that parses Lisp strings and returns an expression constructed from list Cells
// The syntax rules are slightly different from Standard Lisp
// Symbols parse as String
// Numbers parse as Integer or Double depending
// Booleans (true, false) and null parse as themselves. T and nil not used.
// Anything in double quotes will parse as String

public class Parser
{

  // private constructor to prevent instantiation

  private Parser() {};

  private static final String startNumber = "-.0123456789";
  private static final String floatChars = ".e";
  private static final String hexChars = "0123456789ABCDEFabcdef";


  public static Object parse(String s)
  {
    return parse(new PushbackReader(new StringReader(s)));
  }


  // return an expression from the reader

  private static Object parse(PushbackReader pr)
  {
    try
    {
      int c;
      do {c = pr.read();} while (Character.isWhitespace(c));
      pr.unread(c);
      if ((char) c == '(') return readExpression(pr);
      if ((char) c == '"') return readLiteral(pr);
      if (startNumber.indexOf(c) >= 0) return readNumber(pr);
      String s = readSymbol(pr);
      if (s.equals("true")) return true;
      if (s.equals("false")) return false;
      if (s.equals("null")) return null;
      return s;
    }
    catch (IOException e)
    {
      throw new RuntimeException(e);
    }
  }


  // return a Number (an Integer or Double) value
  // TODO needs to handle wider syntax such as 0xABCD

  private static Object readNumber(PushbackReader pr) throws IOException
  {
    StringBuilder num = new StringBuilder();
    boolean isaFloat = false;
    boolean done = false;
    while(!done)
    {
      int c = pr.read();
      boolean cfloats = (floatChars.indexOf(c) >= 0);
      if (!isaFloat) isaFloat = cfloats;
      if (Character.isDigit(c) || c == '-' || cfloats) num.append((char) c);
      else
      {
        pr.unread(c);
        done = true;
      };
    }
    if (num.isEmpty()) return null;
    if (isaFloat) return Double.parseDouble(num.toString());
    return Integer.parseInt(num.toString());
  }


  // return a symbol

  private static String readSymbol(PushbackReader pr) throws IOException
  {
    StringBuilder sb = new StringBuilder();
    boolean done = false;
    while(!done)
    {
      int c = pr.read();
      if (c == -1) done = true;
      else if (Character.isWhitespace(c) || c == ')' || c == '(')
      {
        pr.unread(c);
        done = true;
      }
      else if (c == '\\') sb.append(readEscape(pr));
      else sb.append((char) c);
    }
    return sb.toString();
  }


  private static char readEscape(PushbackReader pr) throws IOException
  {
    int c = pr.read();
    switch (c)
    {
      case '\\': return '\\';
      case '\'': return '\'';
      case '\"': return '\"';
      case 'n': return '\n';
      case 'r': return '\r';
      case 's': return '\s';
      case 't': return '\t';
      case 'b': return '\b';
      case 'f': return '\f';
      case 'u': return (char) readHexInteger(pr);
    }
    throw new RuntimeException();
  }


  private static int readHexInteger(PushbackReader pr) throws IOException
  {
    StringBuilder num = new StringBuilder();
    boolean done = false;
    while(!done)
    {
      int c = pr.read();
      if (hexChars.indexOf(c) >= 0) num.append((char) c);
      else
      {
        pr.unread(c);
        done = true;
      };
    }
    if (num.isEmpty()) return 0;
    return Integer.parseInt(num.toString(), 16);
  }


  // return a literal, which is a quoted symbol
  // The unquoted String is returned, so the generator has to check if it needs to be quoted

  private static String readLiteral(PushbackReader pr) throws IOException
  {
    StringBuilder sb = new StringBuilder();
    pr.read();  // consume the opening quote
    while (true)
    {
      int c = pr.read();
      if (c == -1) throw new RuntimeException("Reached end of file when reading quoted string");
      if ((char) c == '"') break;  // consume the closing quote and return
      sb.append((char) c);
    }
    return sb.toString();
  }


  // an expression is a list or an atom
  // list points to the first cell created so it can be returned as the list
  // last points to the last cell created so the next item can be appended to the list

  private static Object readExpression(PushbackReader pr) throws IOException
  {
    Cell list = null;
    Cell last = null;
    int c = pr.read();
    if (c != '(') throw new RuntimeException("Expected '(', got " + c);
    while (true)
    {
      c = pr.read();
      if (c == -1) break;
      if (c == ')') break;
      if (Character.isWhitespace(c)) continue;
      pr.unread(c);
      Cell cell = new Cell(parse(pr), null);
      if (list == null) list = cell;
      else last.cdr = cell;
      last = cell;
    }
    return list;
  }


}
package LISP;

import calliope.JMap;  // an implied typedef, simply:  public class JMap extends LinkedHashMap<String, Object>{}

import java.util.ArrayList;
import java.util.List;

//
// These static methods return a LISP String from a Java Object
// The top level call is usually objectToString()
// When generating the characters for a symbol, need to check if it should be put in double quote marks.
// Otherwise, a string that looks like an integer (e.g. the guitar chord 001023) would write as something
// that could be incorrectly parsed as an integer.
// Also has a version that returns a pretty string.  To make a pretty string ps from a Lisp string s,
//     String ps = LISP.Generator.objectToPrettyString(LISP.Eval.eval(LISP.Parser.parse(s)););
//

public class Generator
{

  // private constructor to prevent instantiation

  private Generator() {}


  // return a LISP string from the given object with pretty formatting option
  // this is the top level call. The other calls are public to be accessible to Archiver

  public static String objectToString(Object o)
  {
    StringBuilder sb = new StringBuilder();
    if (o instanceof JMap) sb.append(mapToString((JMap) o));
    else if (o instanceof List) sb.append(listToString((List) o));
    else sb.append(valueToString(o));
    return sb.toString();
  }


  // return the string of null, quoted strings, true, false, integers, doubles

  public static String valueToString(Object s)
  {
    // System.out.println(s.getClass() + " value to string of " + s);
    StringBuilder sb = new StringBuilder();
    if (s == null) sb.append("null");
    else if (s instanceof String)
    {
      if (needsLiteral((String) s))
      {
        sb.append("\"");
        sb.append(s);
        sb.append("\"");
      }
      else sb.append(s);
    }
    else sb.append(s.toString());
    return sb.toString();
  }


  // return whether the string could be mistaken for a non-string (a number or having whitespace etc) on read/eval
  // in which case it will need literal quotes

  private static boolean needsLiteral(String s)
  {
    final String nonString = "\s\t\r\n()";
    final String startNumber = "-.0123456789";
    char c = s.charAt(0);
    if (c == '\"') return false;  // already quoted
    if (startNumber.indexOf(c) >= 0) return true;
    for (int i = 0; i < s.length(); i++)
    {
      c = s.charAt(i);
      if (nonString.indexOf(c) >= 0) return true;
    }
    return false;
  }


  // return a string of the special form (list e1 e2 e3...) at the given indent from the given list
  // List is usually an ArrayList from evalArray

  private static String listToString(List<?> list)
  {
    StringBuilder sb = new StringBuilder();
    if (list == null) sb.append("null");
    else if (list.isEmpty()) sb.append("()");
    else
    {
      final int k = list.size();
      sb.append("(list ");
      for (int i = 0; i < k; i++)
      {
        sb.append(objectToString(list.get(i)));
        if (i != k - 1) sb.append(" ");
      }
      sb.append(")");
    }
    return sb.toString();
  }


  // the map special form

  private static String mapToString(JMap m)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("(map");
    for (String key : m.keySet())
    {
      sb.append("(");
      sb.append(valueToString(key));
      sb.append(" ");
      sb.append(objectToString(m.get(key)));
      sb.append(")");
    }
    sb.append(")");
    return sb.toString();
  }


  //
  // return a Pretty string from the given Java object
  //

  // this is the top level call

  public static String objectToPrettyString(Object o)
  {
    return objectToString(o, 0);
  }


  // return a string from the given object at the given indent level from the given object

  private static String objectToString(Object o, int indent)
  {
    StringBuilder sb = new StringBuilder();
    if (o instanceof JMap) sb.append(mapToString((JMap) o, indent));
    else if (o instanceof List) sb.append(listToString((List) o, indent));
    else sb.append(valueToString(o));
    return sb.toString();
  }


  // return a pretty string of the special form (list e1 e2 e3...) at the given indent from the given list
  // List is usually an ArrayList from evalArray

  private static String listToString(List<?> list, int indent)
  {
    StringBuilder sb = new StringBuilder();
    if (list == null) sb.append("null");
    else if (list.isEmpty()) sb.append("()");
    else
    {
      final int k = list.size();
      sb.append("(list ");
      if (indent + displayLength(list) < 80)
      {
        // the entire list can fit on the line
        for (int i = 0; i < k; i++)
        {
          sb.append(objectToString(list.get(i)));
          if (i != k - 1) sb.append(" ");
        }
      }
      else
      {
        sb.append("\n");
        for (int i = 0; i < k; i++)
        {
          appendNSpaces(sb, indent + 2);
          sb.append(objectToString(list.get(i), indent + 2));
          sb.append("\n");
        }
        appendNSpaces(sb, indent);
      }
      sb.append(")");
    }
    return sb.toString();
  }


  // pretty string for the map special form at the given indent from the given map

  private static String mapToString(JMap m, int indent)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("(map ");
    if (indent + displayLength(m) < 80)
    {
      // the entire map will fit on a line
      for (String key : m.keySet()) sb.append(nameValuePairToString(key, m.get(key)));
    }
    else
    {
      sb.append("\n");
      for (String key : m.keySet())
      {
        appendNSpaces(sb, indent + 2);
        sb.append(nameValuePairToString(key, m.get(key), indent + 2));
        sb.append("\n");
      }
      appendNSpaces(sb, indent);
    }
    sb.append(")");
    return sb.toString();
  }


  // a terminal (can fit on a line) name/value pair

  private static String nameValuePairToString(String name, Object val)
  {
    StringBuilder sb = new StringBuilder();
    sb.append("(");
    sb.append(valueToString(name));
    sb.append(" ");
    sb.append(objectToString(val));
    sb.append(")");
    return sb.toString();
  }


  private static String nameValuePairToString(String name, Object val, int indent)
  {
    StringBuilder sb = new StringBuilder();
    if (indent + displayLength(name) + displayLength(objectToString(val)) + 3 < 80)
    {
      sb.append(nameValuePairToString(name, val));
    }
    else
    {
      sb.append("(");
      sb.append(valueToString(name));
      sb.append("\n");
      appendNSpaces(sb, indent + 2);
      sb.append(objectToString(val, indent + 2));
      sb.append("\n");
      appendNSpaces(sb, indent);
      sb.append(")");
    }
    return sb.toString();
  }

  // return the number of columns exp would require if displayed

  private static int displayLength(Object exp)
  {
    int k = 0;
    if (exp == null) return k + 4;
    if (exp instanceof String) return k + ((String) exp).length();
    if (exp instanceof Number) return k + String.valueOf(exp).length();
    if (exp instanceof Boolean)
    {
      if ((boolean) exp) return k + 4; else return k + 5;
    }
    if (exp instanceof List)
    {
      int n = ((List)exp).size();
      for (int i = 0; i < n; i++)
      {
        k += displayLength(((List)exp).get(i));
        if (i != n - 1) k += 1;
      }
      return k + 2;
    }
    else if (exp instanceof JMap)
    {
      JMap m = (JMap) exp;
      k += 4;
      for (String key : m.keySet())
      {
        k += displayLength(key);
        k += 1;
        k += displayLength(objectToString(m.get(key)));
      }
      return k;
    }
    return k;
  }


  // append n space characters to the given StringBuilder

  private static void appendNSpaces(StringBuilder sb, int n)
  {
    for (int i = 0; i < n; i++) sb.append('\s');
  }

}

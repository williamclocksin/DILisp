package LISP;

import calliope.JMap;  // an implied typedef, simply:  public class JMap extends LinkedHashMap<String, Object>{}
import java.util.ArrayList;

// Static class that evaluates a Lisp expression and returns a Java object: A primitive, a Map, or an ArrayList
// primitives are String, Number, Boolean, null
// map and list expressions are special forms
// This version of eval processes the special forms @id and @ref as themselves. Only the Archiver resolves them.

public class Eval
{

  // private constructor to prevent instantiation

  private Eval() {}


  // eval can be given a LISP String or a LISP expression, and returns a Java object

  public static Object eval(String s)
  {
    return eval(LISP.Parser.parse(s));
  }

  // @id is never encountered as an argument, but only as a name in a (name val) pair
  // @ref is always encountered as an argument, so must create the Map with key "@ref" and val ref id

  public static Object eval(Object exp)
  {
    // first check for primitive
    if (isPrimitive(exp)) return exp;
    // must be a list cell
    if (!(exp instanceof Cell)) throw new RuntimeException("Unrecognised type for eval: " + exp);
    // check for special forms
    String head = (String) ((Cell) exp).car;
    Object tail = ((Cell) exp).cdr;
    if (head.equals("map")) return evalMap(tail);
    if (head.equals("list")) return evalArray(tail);
    if (head.equals("@ref")) return mapRef(tail);
    // default eval
    // (nothing needed here yet, but this is where arithmetic etc would go)
    throw new RuntimeException("Unrecognised head of form for eval: " + head);
  }


  private static boolean isPrimitive(Object exp)
  {
    if (exp == null) return true;
    if (exp instanceof String) return true;
    if (exp instanceof Number) return true;
    if (exp instanceof Boolean) return true;
    return false;
  }


  public static Object evalQuote(Object exp)
  {
    if (isPrimitive(exp)) return exp;
    throw new RuntimeException("Unrecognised type for evalq: " + exp);
  }


  // return a Java JMap of all the (name val) pairs in given list
  // note, here a pair means a list of 2 elements, not a dotted pair.

  private static JMap evalMap(Object list)
  {
    JMap map = new JMap();
    Cell next = (Cell) list;
    while (next != null)
    {
      Object pair = next.car;
      String name = (String) evalQuote(((Cell) pair).car);
      Object val = eval(((Cell)((Cell) pair).cdr).car);  // this is (setq val (eval (cadr pair)))
      map.put(name, val);
      next = (Cell) next.cdr;
    }
    return map;
  }


  private static JMap mapRef(Object list)
  {
    JMap map = new JMap();
    String val = (String) evalQuote(((Cell) list).car);
    map.put("@ref", val);
    return map;
  }

  // return a Java ArrayList of all the elements of given list

  private static ArrayList<Object> evalArray(Object list)
  {
    ArrayList<Object> al = new ArrayList<Object>();
    Cell next = (Cell) list;
    while (next != null)
    {
      Object item = next.car;
      Object val = eval(item);
      al.add(val);
      next = (Cell) next.cdr;
    }
    return al;
  }
}

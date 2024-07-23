package LISP;

// Lisp list cell with car and cdr
// All primitive objects are represented as String or Number (Integer or Double) or Boolean or null

public class Cell
{
  Object car;
  Object cdr;


  // construct a pair

  public Cell(Object a1, Object a2)
  {
    car = a1;
    cdr = a2;
  }


  // users may access car directly instead of this getter

  public Object car()
  {
    return car;
  }


  // users may access cdr directly instead of this getter

  public Object cdr()
  {
    return cdr;
  }


  // this getter is useful because the alternative is ((Cell)((Cell)pair).cdr).car

  public Object cadr()
  {
    return ((Cell)cdr).car;
  }


  // return the string corresponding to the expression

  public static String stringOf(Object e)
  {
    return stringOf(e, false);
  }


  // boolean tail indicates whether the elements of a list are being printed,
  // so we can use standard list form instead of nested form.

  private static String stringOf(Object e, boolean tail)
  {
    if (e == null) return "";
    if (e instanceof String) return (String) e;
    if (e instanceof Number) return e.toString();
    if (e instanceof Boolean) return e.toString();
    StringBuffer sb = new StringBuffer();
    if (!tail) sb.append("(");
    sb.append(stringOf(((Cell)e).car, false));
    if (((Cell)e).cdr != null)
    {
      sb.append(" ");
      sb.append(stringOf(((Cell) e).cdr, true));
    }
    if (!tail) sb.append(")");
    return sb.toString();
  }





}

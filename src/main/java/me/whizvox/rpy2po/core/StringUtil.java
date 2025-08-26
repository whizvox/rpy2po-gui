package me.whizvox.rpy2po.core;

public class StringUtil {

  private static final char[] ILLEGAL_FILE_CHARS = {'/', '<', '>', ':', '"', '\\', '|', '?', '*'};

  public static String sanitizeFileName(String name) {
    StringBuilder sb = new StringBuilder();
    mainLoop: for (char c : name.toCharArray()) {
      for (char illegalChar : ILLEGAL_FILE_CHARS) {
        if (c == illegalChar) {
          sb.append('_');
          continue mainLoop;
        }
      }
      sb.append(c);
    }
    if (!sb.isEmpty() && (sb.charAt(sb.length() - 1) == ' ' || sb.charAt(sb.length() - 1) == '.')) {
      sb.setCharAt(sb.length() - 1, '_');
    }
    return sb.toString();
  }

  public static int getEditDistance(String a, String b) {
    a = a.toLowerCase();
    b = b.toLowerCase();
    // i == 0
    int [] costs = new int [b.length() + 1];
    for (int j = 0; j < costs.length; j++)
      costs[j] = j;
    for (int i = 1; i <= a.length(); i++) {
      // j == 0; nw = lev(i - 1, j)
      costs[0] = i;
      int nw = i - 1;
      for (int j = 1; j <= b.length(); j++) {
        int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
        nw = costs[j];
        costs[j] = cj;
      }
    }
    return costs[b.length()];
  }

  public static String notNullOrBlankOrElse(String str, String def) {
    return str == null || str.isBlank() ? def : str;
  }

}

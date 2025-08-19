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

}

package me.whizvox.rpy2po.gettext;

import java.util.List;

public class PoUtils {

  public record Reference(String file, int line) {
  }

  public static Reference parseReference(String str) {
    int index = str.lastIndexOf(':');
    if (index == -1) {
      throw new IllegalArgumentException("Invalid source reference: " + str);
    }
    return new Reference(str.substring(0, index), Integer.parseInt(str.substring(index + 1)));
  }

}

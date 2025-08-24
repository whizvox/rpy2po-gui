package me.whizvox.rpy2po.gettext;

import java.util.Comparator;

public record SourceReference(String file,
                              int line) implements Comparable<SourceReference> {

  @Override
  public int compareTo(SourceReference other) {
    return Comparator.comparing(SourceReference::file)
        .thenComparing(SourceReference::line)
        .compare(this, other);
  }

  public static SourceReference parse(String str) {
    int index = str.lastIndexOf(':');
    if (index == -1) {
      throw new IllegalArgumentException("Invalid source reference: " + str);
    }
    return new SourceReference(str.substring(0, index), Integer.parseInt(str.substring(index + 1)));
  }

}

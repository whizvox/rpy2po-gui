package me.whizvox.rpy2po.gui;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class SimilarStringsModel extends AbstractListModel<String> {

  private final List<Entry> values;

  public SimilarStringsModel() {
    values = new ArrayList<>();
  }

  public Entry getValue(int index) {
    return values.get(index);
  }

  @Override
  public int getSize() {
    return values.size();
  }

  @Override
  public String getElementAt(int index) {
    return values.get(index).text;
  }

  public record Entry(String id,
                      String text,
                      float similarity) {
  }

}

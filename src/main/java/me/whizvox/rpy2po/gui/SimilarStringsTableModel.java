package me.whizvox.rpy2po.gui;

import com.soberlemur.potentilla.MessageKey;
import me.whizvox.rpy2po.core.SimilarMessage;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class SimilarStringsTableModel extends AbstractTableModel {

  private final List<SimilarMessage> values;

  public SimilarStringsTableModel() {
    values = new ArrayList<>();
  }

  @Override
  public int getRowCount() {
    return values.size();
  }

  @Override
  public int getColumnCount() {
    return 2;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return switch (columnIndex) {
      case 0 -> (int) (values.get(rowIndex).similarity() * 100) + "%";
      case 1 -> values.get(rowIndex).key().msgId();
      default -> "?";
    };
  }

  @Override
  public String getColumnName(int column) {
    return switch (column) {
      case 0 -> "Similarity";
      case 1 -> "String";
      default -> "?";
    };
  }

  public MessageKey getKey(int row) {
    return values.get(row).key();
  }

  public void clear() {
    values.clear();
  }

  public void setValues(List<SimilarMessage> values) {
    clear();
    this.values.addAll(values);
  }

  public void addValue(MessageKey key, float similarity) {
    values.add(new SimilarMessage(key, similarity));
  }

}

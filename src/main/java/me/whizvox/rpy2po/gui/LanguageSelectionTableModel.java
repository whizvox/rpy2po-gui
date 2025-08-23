package me.whizvox.rpy2po.gui;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class LanguageSelectionTableModel extends AbstractTableModel {

  private final Object[][] values;

  public LanguageSelectionTableModel(List<String> languages) {
    values = new Object[languages.size()][];
    for (int i = 0; i < languages.size(); i++) {
      values[i] = new Object[] {languages.get(i), true};
    }
  }

  @Override
  public int getRowCount() {
    return values.length;
  }

  @Override
  public int getColumnCount() {
    return 2;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    return values[rowIndex][columnIndex];
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return switch (columnIndex) {
      case 0 -> String.class;
      case 1 -> Boolean.class;
      default -> throw new IllegalArgumentException("Invalid column: " + columnIndex);
    };
  }

  @Override
  public String getColumnName(int column) {
    return switch (column) {
      case 0 -> "Language";
      case 1 -> "Select";
      default -> "?";
    };
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return columnIndex == 1;
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    values[rowIndex][columnIndex] = aValue;
  }

}

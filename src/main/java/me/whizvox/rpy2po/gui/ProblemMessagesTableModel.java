package me.whizvox.rpy2po.gui;

import com.soberlemur.potentilla.MessageKey;
import me.whizvox.rpy2po.core.Pair;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ProblemMessagesTableModel extends AbstractTableModel {

  private final List<Object[]> values;

  public ProblemMessagesTableModel() {
    this.values = new ArrayList<>();
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
      case 0 -> values.get(rowIndex)[0];
      case 1 -> ((MessageKey) values.get(rowIndex)[1]).msgId();
      default -> "?";
    };
  }

  @Override
  public String getColumnName(int column) {
    return switch (column)  {
      case 0 -> "Resolved";
      case 1 -> "String";
      default -> "?";
    };
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return columnIndex == 0 ? Boolean.class : String.class;
  }

  public MessageKey getKey(int row) {
    return (MessageKey) values.get(row)[1];
  }

  public void markResolved(int row, boolean resolved) {
    values.get(row)[0] = resolved;
  }

  public void markResolved(int row) {
    markResolved(row, true);
  }

  public boolean isResolved(int row) {
    return (boolean) values.get(row)[0];
  }

  public void clear() {
    this.values.clear();
  }

  public void addValue(boolean resolved, MessageKey key) {
    values.add(new Object[] {resolved, key});
  }

  public void setValues(Iterable<Pair<Boolean, MessageKey>> values) {
    clear();
    values.forEach(pair -> this.values.add(new Object[] {pair.left(), pair.right()}));
  }

}

package me.whizvox.rpy2po.gui;

import com.soberlemur.potentilla.MessageKey;
import me.whizvox.rpy2po.core.Pair;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ProblemMessagesTableModel extends AbstractTableModel {

  private List<Object[]> values;
  private final List<Object[]> allValues;
  private boolean showAll;
  private String filter;

  public ProblemMessagesTableModel() {
    allValues = new ArrayList<>();
    values = allValues;
    showAll = true;
    filter = null;
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

  public boolean isShowingAll() {
    return showAll;
  }

  public void markResolved(int row, boolean resolved) {
    values.get(row)[0] = resolved;
    if (resolved && !showAll) {
      values.remove(row);
    }
  }

  public void markResolved(int row) {
    markResolved(row, true);
  }

  public boolean isResolved(int row) {
    return (boolean) values.get(row)[0];
  }

  public void clear() {
    values.clear();
    if (!showAll) {
      allValues.clear();
    }
  }

  public void addValue(boolean resolved, MessageKey key) {
    var value = new Object[] {resolved, key};
    if (showAll) {
      values.add(value);
    } else {
      allValues.add(value);
      if (!resolved) {
        values.add(value);
      }
    }
  }

  public void setValues(Iterable<Pair<Boolean, MessageKey>> values) {
    clear();
    values.forEach(pair -> addValue(pair.left(), pair.right()));
  }

  private void updateRows() {
    if (showAll && filter == null) {
      values = allValues;
    } else {
      String filterLc = filter == null ? null : filter.toLowerCase();
      values = new ArrayList<>(allValues.stream().filter(pair -> {
        boolean c1 = showAll || !(boolean) pair[0];
        boolean c2 = filter == null || ((MessageKey) pair[1]).msgId().toLowerCase().contains(filterLc);
        return c1 && c2;
      }).toList());
    }
  }

  public void toggleShowAll(boolean showAll) {
    if (this.showAll != showAll) {
      this.showAll = showAll;
      updateRows();
    }
  }

  public void setFilter(String filter) {
    if (filter.isBlank()) {
      filter = null;
    }
    if (!Objects.equals(this.filter, filter)) {
      this.filter = filter;
      updateRows();
    }
  }

}

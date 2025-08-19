package me.whizvox.rpy2po.gui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public interface DocumentChangedListener extends DocumentListener {

  void onChanged(DocumentEvent event);

  @Override
  default void insertUpdate(DocumentEvent e) {
    onChanged(e);
  }

  @Override
  default void removeUpdate(DocumentEvent e) {
    onChanged(e);
  }

  @Override
  default void changedUpdate(DocumentEvent e) {
    onChanged(e);
  }

}

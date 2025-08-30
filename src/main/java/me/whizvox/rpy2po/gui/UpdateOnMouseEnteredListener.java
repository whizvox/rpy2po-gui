package me.whizvox.rpy2po.gui;

import javax.swing.text.JTextComponent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class UpdateOnMouseEnteredListener extends MouseAdapter {

  private final JTextComponent comp;
  private final String text;

  public UpdateOnMouseEnteredListener(JTextComponent comp, String text) {
    this.comp = comp;
    this.text = text;
  }

  @Override
  public void mouseEntered(MouseEvent e) {
    comp.setText(text);
  }

  @Override
  public void mouseExited(MouseEvent e) {
    comp.setText(" ");
  }

}

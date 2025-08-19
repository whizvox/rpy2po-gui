package me.whizvox.rpy2po.gui;

import javax.swing.*;
import java.awt.*;

public class GuiUtils {

  public static void showErrorMessage(Component parent, String msg, Exception e) {
    JOptionPane.showMessageDialog(parent, msg + "\n" + e.getClass() + ": " + e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
  }

}

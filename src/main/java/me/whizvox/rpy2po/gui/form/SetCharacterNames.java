package me.whizvox.rpy2po.gui.form;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import me.whizvox.rpy2po.core.Profile;
import me.whizvox.rpy2po.gui.DocumentChangedListener;
import me.whizvox.rpy2po.gui.GuiUtils;
import me.whizvox.rpy2po.gui.RPY2PO;
import me.whizvox.rpy2po.rpytl.CharacterNames;
import me.whizvox.rpy2po.rpytl.CommentGenerator;
import me.whizvox.rpy2po.rpytl.RPY2POConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SetCharacterNames extends JFrame {

  private static final Logger LOGGER = LoggerFactory.getLogger(SetCharacterNames.class);

  private JPanel contentPane;
  private JTextField textFieldName;
  private JLabel labelId;
  private JButton buttonNext;
  private JButton buttonPrev;
  private JTable tableNames;
  private JTextField textFieldNarrName;
  private JTextField textFieldComment;
  private JButton buttonSave;
  private JButton buttonCancel;
  private JButton buttonScan;
  private JScrollPane scrollPaneTable;

  private final Profile profile;
  private DefaultTableModel namesModel;

  public SetCharacterNames(Profile profile) {
    this.profile = profile;
    $$$setupUI$$$();
    setContentPane(contentPane);

    tableNames.getSelectionModel().addListSelectionListener(e -> {
      int selected = tableNames.getSelectionModel().getLeadSelectionIndex();
      labelId.setText((String) namesModel.getValueAt(selected, 0));
      textFieldName.setText((String) namesModel.getValueAt(selected, 1));
      tableNames.scrollRectToVisible(tableNames.getCellRect(selected, 0, true));
    });
    buttonNext.addActionListener(e -> {
      if (namesModel.getRowCount() > 0) {
        int selected = tableNames.getSelectedRow();
        if (selected != -1) {
          for (int i = selected + 1; i < tableNames.getRowCount(); i++) {
            if (((String) namesModel.getValueAt(i, 1)).isBlank()) {
              tableNames.setRowSelectionInterval(i, i);
              return;
            }
          }
          selected++;
          if (selected < namesModel.getRowCount()) {
            tableNames.setRowSelectionInterval(selected, selected);
          } else {
            tableNames.setRowSelectionInterval(0, 0);
          }
        } else {
          tableNames.setRowSelectionInterval(0, 0);
        }
      }
    });
    buttonPrev.addActionListener(e -> {
      if (namesModel.getRowCount() > 0) {
        int last = namesModel.getRowCount() - 1;
        int selected = tableNames.getSelectedRow();
        if (selected != -1) {
          for (int i = selected - 1; i >= 0; i--) {
            if (((String) namesModel.getValueAt(i, 1)).isBlank()) {
              tableNames.setRowSelectionInterval(i, i);
              return;
            }
          }
          selected--;
          if (selected >= 0) {
            tableNames.setRowSelectionInterval(selected, selected);
          } else {
            tableNames.setRowSelectionInterval(last, last);
          }
        } else {
          tableNames.setRowSelectionInterval(last, last);
        }
      }
    });
    textFieldName.getDocument().addDocumentListener((DocumentChangedListener) e -> {
      int selected = tableNames.getSelectedRow();
      if (selected != -1) {
        namesModel.setValueAt(textFieldName.getText(), selected, 1);
      }
    });
    textFieldName.addActionListener(e -> {
      buttonNext.doClick();
    });
    buttonScan.addActionListener(e -> scanForNames());
    buttonCancel.addActionListener(e -> RPY2PO.inst().setFrame(() -> new ProfileActions(profile), profile.getName(), null));
    buttonSave.addActionListener(e -> {
      try {
        Map<String, String> newNamesMap = new HashMap<>();
        for (int i = 0; i < namesModel.getRowCount(); i++) {
          newNamesMap.put((String) namesModel.getValueAt(i, 0), (String) namesModel.getValueAt(i, 1));
        }
        CharacterNames newNames = new CharacterNames(newNamesMap, textFieldNarrName.getText(), textFieldComment.getText());
        profile.setNames(newNames);
        RPY2PO.inst().writeJson(profile.getFile(), profile);
        LOGGER.info("Updated profile to set new character names: {}", profile.getFile());
        RPY2PO.inst().setFrame(() -> new ProfileActions(profile), profile.getName(), null);
      } catch (IOException ex) {
        GuiUtils.showErrorMessage(this, "Could not save profile", ex);
      }
    });
    textFieldNarrName.setText(profile.getNames().narrator());
    textFieldComment.setText(profile.getNames().speakFormat());

    if (profile.getNames().names().isEmpty()) {
      scanForNames();
    } else {
      updateNamesTable();
    }
  }

  private void enableButtons(boolean enable) {
    buttonPrev.setEnabled(enable);
    buttonNext.setEnabled(enable);
    buttonScan.setEnabled(enable);
    buttonCancel.setEnabled(enable);
    buttonSave.setEnabled(enable);
    tableNames.setEnabled(enable);
  }

  private void updateNamesTable() {
    namesModel.setRowCount(0);
    profile.getNames().names().keySet().stream().sorted().forEach(id -> {
      namesModel.addRow(new Object[]{id, profile.getNames().names().get(id)});
    });
    tableNames.updateUI();
  }

  private void scanForNames() {
    enableButtons(false);
    Path tlDir = profile.getTranslationDirectory(profile.getPrimaryLanguage());
    try {
      var converter = new RPY2POConverter(profile.getPrimaryLanguage(), profile.getTranslationFiles(profile.getPrimaryLanguage()), profile.getNames(), null, CommentGenerator.NONE);
      var result = converter.convert();
      if (!result.missingNames().isEmpty()) {
        Map<String, String> newNames = new HashMap<>(profile.getNames().names());
        result.missingNames().forEach(id -> newNames.put(id, ""));
        profile.setNames(new CharacterNames(newNames, profile.getNames().narrator(), profile.getNames().speakFormat()));
        updateNamesTable();
        RPY2PO.inst().writeJson(profile.getFile(), profile);
        LOGGER.info("Finished scanning translation files in <{}>, found {} missing name(s)", tlDir, result.missingNames().size());
      } else {
        LOGGER.info("Finished scanning translation files in <{}>, found no missing names", tlDir);
      }
    } catch (IOException e) {
      LOGGER.error("Could not scan translation files in {}", tlDir, e);
      GuiUtils.showErrorMessage(this, "Could not scan translation files!", e);
    }
    enableButtons(true);
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer
   * >>> IMPORTANT!! <<<
   * DO NOT edit this method OR call it in your code!
   *
   * @noinspection ALL
   */
  private void $$$setupUI$$$() {
    createUIComponents();
    contentPane = new JPanel();
    contentPane.setLayout(new GridLayoutManager(9, 4, new Insets(10, 10, 10, 10), -1, -1));
    scrollPaneTable = new JScrollPane();
    contentPane.add(scrollPaneTable, new GridConstraints(0, 0, 7, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    Font tableNamesFont = this.$$$getFont$$$("Consolas", Font.PLAIN, -1, tableNames.getFont());
    if (tableNamesFont != null) tableNames.setFont(tableNamesFont);
    tableNames.setPreferredScrollableViewportSize(new Dimension(350, 400));
    scrollPaneTable.setViewportView(tableNames);
    final JLabel label1 = new JLabel();
    label1.setText("Name");
    contentPane.add(label1, new GridConstraints(3, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    textFieldName = new JTextField();
    contentPane.add(textFieldName, new GridConstraints(4, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    final Spacer spacer1 = new Spacer();
    contentPane.add(spacer1, new GridConstraints(0, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    final Spacer spacer2 = new Spacer();
    contentPane.add(spacer2, new GridConstraints(6, 2, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    final JLabel label2 = new JLabel();
    label2.setText("Identifier");
    contentPane.add(label2, new GridConstraints(1, 2, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    labelId = new JLabel();
    Font labelIdFont = this.$$$getFont$$$("Consolas", Font.PLAIN, -1, labelId.getFont());
    if (labelIdFont != null) labelId.setFont(labelIdFont);
    labelId.setText(" ");
    contentPane.add(labelId, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    buttonNext = new JButton();
    buttonNext.setText("Next");
    contentPane.add(buttonNext, new GridConstraints(5, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    buttonPrev = new JButton();
    buttonPrev.setText("Previous");
    contentPane.add(buttonPrev, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new GridLayoutManager(1, 4, new Insets(10, 0, 0, 0), -1, -1));
    contentPane.add(panel1, new GridConstraints(8, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    buttonSave = new JButton();
    buttonSave.setText("Save");
    panel1.add(buttonSave, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final Spacer spacer3 = new Spacer();
    panel1.add(spacer3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    buttonCancel = new JButton();
    buttonCancel.setText("Cancel");
    panel1.add(buttonCancel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    buttonScan = new JButton();
    buttonScan.setText("Scan for Names");
    panel1.add(buttonScan, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel2 = new JPanel();
    panel2.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel2, new GridConstraints(7, 0, 1, 4, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    final JLabel label3 = new JLabel();
    label3.setText("Narrator Name");
    panel2.add(label3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    textFieldNarrName = new JTextField();
    panel2.add(textFieldNarrName, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), new Dimension(200, -1), 0, false));
    final JLabel label4 = new JLabel();
    label4.setText("Comment Format");
    panel2.add(label4, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    textFieldComment = new JTextField();
    panel2.add(textFieldComment, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), new Dimension(200, -1), 0, false));
  }

  /**
   * @noinspection ALL
   */
  private Font $$$getFont$$$(String fontName, int style, int size, Font currentFont) {
    if (currentFont == null) return null;
    String resultName;
    if (fontName == null) {
      resultName = currentFont.getName();
    } else {
      Font testFont = new Font(fontName, Font.PLAIN, 10);
      if (testFont.canDisplay('a') && testFont.canDisplay('1')) {
        resultName = fontName;
      } else {
        resultName = currentFont.getName();
      }
    }
    Font font = new Font(resultName, style >= 0 ? style : currentFont.getStyle(), size >= 0 ? size : currentFont.getSize());
    boolean isMac = System.getProperty("os.name", "").toLowerCase(Locale.ENGLISH).startsWith("mac");
    Font fontWithFallback = isMac ? new Font(font.getFamily(), font.getStyle(), font.getSize()) : new StyleContext().getFont(font.getFamily(), font.getStyle(), font.getSize());
    return fontWithFallback instanceof FontUIResource ? fontWithFallback : new FontUIResource(fontWithFallback);
  }

  /**
   * @noinspection ALL
   */
  public JComponent $$$getRootComponent$$$() {
    return contentPane;
  }

  private void createUIComponents() {
    namesModel = new DefaultTableModel(0, 2);
    namesModel.setColumnIdentifiers(new Object[]{"Identifier", "Name"});
    tableNames = new JTable(namesModel);
    tableNames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
  }

}

package me.whizvox.rpy2po.gui.form;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import me.whizvox.rpy2po.core.Profile;
import me.whizvox.rpy2po.gui.GuiUtils;
import me.whizvox.rpy2po.gui.RPY2PO;
import me.whizvox.rpy2po.rpytl.RenPyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class IncludeFiles extends JFrame {

  private static final Logger LOGGER = LoggerFactory.getLogger(IncludeFiles.class);

  private JPanel contentPane;
  private JList<String> listIncluded;
  private JList<String> listExcluded;
  private JButton buttonMove;
  private JButton buttonSave;
  private JButton buttonCancel;
  private JButton buttonScan;
  private JButton buttonIncludeAll;
  private JButton buttonExcludeAll;

  private final DefaultListModel<String> includedModel;
  private final DefaultListModel<String> excludedModel;
  private final Profile profile;

  public IncludeFiles(Profile profile) {
    this.profile = profile;
    includedModel = new DefaultListModel<>();
    excludedModel = new DefaultListModel<>();
    listIncluded.setModel(includedModel);
    listExcluded.setModel(excludedModel);

    setContentPane(contentPane);

    buttonMove.addActionListener(e -> {
      List<String> selected = listIncluded.getSelectedValuesList();
      List<String> newIncluded = new ArrayList<>(profile.getIncludedFiles());
      List<String> newExcluded = new ArrayList<>(profile.getExcludedFiles());
      if (!selected.isEmpty()) {
        newExcluded.addAll(selected);
        newIncluded.removeIf(selected::contains);
      } else {
        selected = listExcluded.getSelectedValuesList();
        if (!selected.isEmpty()) {
          newIncluded.addAll(selected);
          newExcluded.removeIf(selected::contains);
        } else {
          return;
        }
      }
      profile.setIncludedFiles(newIncluded);
      profile.setExcludedFiles(newExcluded);
      updateLists();
    });
    buttonIncludeAll.addActionListener(e -> {
      List<String> newIncluded = new ArrayList<>(profile.getIncludedFiles());
      newIncluded.addAll(profile.getExcludedFiles());
      profile.setIncludedFiles(newIncluded);
      profile.setExcludedFiles(List.of());
      updateLists();
    });
    buttonExcludeAll.addActionListener(e -> {
      List<String> newExcluded = new ArrayList<>(profile.getExcludedFiles());
      newExcluded.addAll(profile.getIncludedFiles());
      profile.setIncludedFiles(List.of());
      profile.setExcludedFiles(newExcluded);
      updateLists();
    });
    buttonScan.addActionListener(e -> scanFiles());
    buttonCancel.addActionListener(e -> RPY2PO.inst().setFrame(() -> new ProfileActions(profile), profile.getName(), null));
    buttonSave.addActionListener(e -> {
      try {
        RPY2PO.inst().writeJson(profile.getFile(), profile);
        RPY2PO.inst().setFrame(() -> new ProfileActions(profile), profile.getName(), null);
      } catch (IOException ex) {
        LOGGER.error("Could not save profile: {}", profile.getBaseDirectory(), ex);
        GuiUtils.showErrorMessage(this, "Could not save profile!", ex);
      }
    });
    if (profile.getIncludedFiles().isEmpty() && profile.getExcludedFiles().isEmpty()) {
      scanFiles();
    } else {
      updateLists();
    }
  }

  private void enableButtons(boolean enable) {
    buttonMove.setEnabled(enable);
    buttonSave.setEnabled(enable);
    buttonCancel.setEnabled(enable);
    buttonScan.setEnabled(enable);
    buttonIncludeAll.setEnabled(enable);
    buttonExcludeAll.setEnabled(enable);
  }

  private void updateLists() {
    includedModel.clear();
    excludedModel.clear();
    profile.getIncludedFiles().stream().sorted().forEach(includedModel::addElement);
    profile.getExcludedFiles().stream().sorted().forEach(excludedModel::addElement);
  }

  private List<Path> findNewFiles() {
    try {
      List<Path> detected = new ArrayList<>(RenPyUtil.scanForTranslationFiles(profile.getRenPyProjectDirectory(), profile.getPrimaryLanguage()));
      List<String> missing = new ArrayList<>();
      Stream.concat(profile.getIncludedFiles().stream(), profile.getExcludedFiles().stream()).forEach(file -> {
        Path path = profile.getTranslationDirectory(profile.getPrimaryLanguage()).resolve(file);
        if (!detected.remove(path)) {
          missing.add(file);
        }
      });
      if (!missing.isEmpty()) {
        LOGGER.debug("Found {} missing file(s): {}", missing.size(), missing);
        List<String> newIncluded = new ArrayList<>(profile.getIncludedFiles());
        newIncluded.removeIf(missing::contains);
        List<String> newExcluded = new ArrayList<>(profile.getExcludedFiles());
        newExcluded.removeIf(missing::contains);
        profile.setIncludedFiles(newIncluded);
        profile.setExcludedFiles(newExcluded);
        LOGGER.info("Updated profile {} ({}) to remove missing files", profile.getName(), profile.getBaseDirectory());
      }
      return detected;
    } catch (IOException e) {
      LOGGER.error("Could not scan for translation files in {}", profile.getTranslationDirectory(profile.getPrimaryLanguage()), e);
      GuiUtils.showErrorMessage(this, "Could not scan for files", e);
    }
    return List.of();
  }

  private void scanFiles() {
    buttonScan.setText(this.$$$getMessageFromBundle$$$("strings", "button.scanFiles.scanning"));
    enableButtons(false);
    List<Path> files = findNewFiles();
    if (files.isEmpty()) {
      LOGGER.info("Finished rescanning project at <{}>, found no new files", profile.getRenPyProjectDirectory());
    } else {
      Path tlDir = profile.getTranslationDirectory(profile.getPrimaryLanguage());
      List<String> newIncluded = new ArrayList<>(profile.getIncludedFiles());
      files.forEach(path -> newIncluded.add(tlDir.relativize(path).normalize().toString()));
      profile.setIncludedFiles(newIncluded);
      updateLists();
      LOGGER.info("Finished rescanning project at <{}> found {} new file(s): {}", profile.getRenPyProjectDirectory(), files.size(), files);
    }
    enableButtons(true);
    buttonScan.setText(this.$$$getMessageFromBundle$$$("strings", "button.scanFiles"));
  }

  {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
    $$$setupUI$$$();
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer
   * >>> IMPORTANT!! <<<
   * DO NOT edit this method OR call it in your code!
   *
   * @noinspection ALL
   */
  private void $$$setupUI$$$() {
    contentPane = new JPanel();
    contentPane.setLayout(new GridLayoutManager(8, 3, new Insets(10, 10, 10, 10), -1, -1));
    final JLabel label1 = new JLabel();
    label1.setText("Included Files");
    contentPane.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label2 = new JLabel();
    label2.setText("Excluded Files");
    contentPane.add(label2, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    buttonMove = new JButton();
    buttonMove.setText("Move");
    contentPane.add(buttonMove, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new GridLayoutManager(1, 4, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel1, new GridConstraints(7, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    buttonSave = new JButton();
    buttonSave.setText("Save");
    panel1.add(buttonSave, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final Spacer spacer1 = new Spacer();
    panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    buttonCancel = new JButton();
    buttonCancel.setText("Cancel");
    panel1.add(buttonCancel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    buttonScan = new JButton();
    this.$$$loadButtonText$$$(buttonScan, this.$$$getMessageFromBundle$$$("strings", "button.scanFiles"));
    panel1.add(buttonScan, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    buttonIncludeAll = new JButton();
    buttonIncludeAll.setText("<< Include All");
    contentPane.add(buttonIncludeAll, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final Spacer spacer2 = new Spacer();
    contentPane.add(spacer2, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    final Spacer spacer3 = new Spacer();
    contentPane.add(spacer3, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    buttonExcludeAll = new JButton();
    buttonExcludeAll.setText("Exclude All >>");
    contentPane.add(buttonExcludeAll, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final Spacer spacer4 = new Spacer();
    contentPane.add(spacer4, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    final JScrollPane scrollPane1 = new JScrollPane();
    contentPane.add(scrollPane1, new GridConstraints(1, 0, 6, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    listIncluded = new JList();
    scrollPane1.setViewportView(listIncluded);
    final JScrollPane scrollPane2 = new JScrollPane();
    contentPane.add(scrollPane2, new GridConstraints(1, 2, 6, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    listExcluded = new JList();
    scrollPane2.setViewportView(listExcluded);
  }

  private static Method $$$cachedGetBundleMethod$$$ = null;

  private String $$$getMessageFromBundle$$$(String path, String key) {
    ResourceBundle bundle;
    try {
      Class<?> thisClass = this.getClass();
      if ($$$cachedGetBundleMethod$$$ == null) {
        Class<?> dynamicBundleClass = thisClass.getClassLoader().loadClass("com.intellij.DynamicBundle");
        $$$cachedGetBundleMethod$$$ = dynamicBundleClass.getMethod("getBundle", String.class, Class.class);
      }
      bundle = (ResourceBundle) $$$cachedGetBundleMethod$$$.invoke(null, path, thisClass);
    } catch (Exception e) {
      bundle = ResourceBundle.getBundle(path);
    }
    return bundle.getString(key);
  }

  /**
   * @noinspection ALL
   */
  private void $$$loadButtonText$$$(AbstractButton component, String text) {
    StringBuffer result = new StringBuffer();
    boolean haveMnemonic = false;
    char mnemonic = '\0';
    int mnemonicIndex = -1;
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) == '&') {
        i++;
        if (i == text.length()) break;
        if (!haveMnemonic && text.charAt(i) != '&') {
          haveMnemonic = true;
          mnemonic = text.charAt(i);
          mnemonicIndex = result.length();
        }
      }
      result.append(text.charAt(i));
    }
    component.setText(result.toString());
    if (haveMnemonic) {
      component.setMnemonic(mnemonic);
      component.setDisplayedMnemonicIndex(mnemonicIndex);
    }
  }

  /**
   * @noinspection ALL
   */
  public JComponent $$$getRootComponent$$$() {
    return contentPane;
  }

}

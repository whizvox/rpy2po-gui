package me.whizvox.rpy2po.gui.form;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.soberlemur.potentilla.PoWriter;
import me.whizvox.rpy2po.core.FileUtils;
import me.whizvox.rpy2po.core.Profile;
import me.whizvox.rpy2po.gui.GuiUtils;
import me.whizvox.rpy2po.gui.RPY2PO;
import me.whizvox.rpy2po.rpytl.CharacterNames;
import me.whizvox.rpy2po.rpytl.CommentGenerator;
import me.whizvox.rpy2po.rpytl.RPY2POConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class ProfileActions extends JFrame {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProfileActions.class);

  private JPanel contentPane;
  private JButton buttonGenTemplate;
  private JButton buttonImport;
  private JButton buttonExport;
  private JButton buttonUpdate;
  private JButton buttonVerify;
  private JTextField textFieldOutLangs;
  private JButton buttonNames;
  private JButton buttonFiles;
  private JLabel labelFeedback;

  private final Profile profile;

  public ProfileActions(Profile profile) {
    this.profile = profile;
    setContentPane(contentPane);

    buttonGenTemplate.addActionListener(e -> generateTemplate());
    buttonImport.addActionListener(e -> importFiles());
    buttonNames.addActionListener(e -> RPY2PO.inst().setFrame(() -> new SetCharacterNames(profile), "Set Names", null));
    buttonFiles.addActionListener(e -> RPY2PO.inst().setFrame(() -> new IncludeFiles(profile), "Set Files", null));
  }

  private void enableActions(boolean enable, boolean includeSettings) {
    buttonGenTemplate.setEnabled(enable);
    buttonImport.setEnabled(enable);
    buttonExport.setEnabled(enable);
    buttonUpdate.setEnabled(enable);
    buttonVerify.setEnabled(enable);
    if (includeSettings) {
      buttonNames.setEnabled(enable);
      buttonFiles.setEnabled(enable);
    }
  }

  private void generateTemplate() {
    Path tlDir = profile.getTranslationDirectory(profile.getPrimaryLanguage());
    boolean showInstructions = true;
    if (Files.exists(tlDir)) {
      int answer = JOptionPane.showConfirmDialog(this, "Do you want to delete the old translation files and generate new ones?\nWill delete directory at " + tlDir, "Question", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
      if (answer == JOptionPane.YES_OPTION) {
        /*try {
          FileUtils.deleteDirectory(tlDir);
          JOptionPane.showMessageDialog(this, "Translation files have been deleted.");
        } catch (IOException e) {
          LOGGER.error("Could not delete translation files at {}", tlDir, e);
          GuiUtils.showErrorMessage(this, "Could not delete translation files", e);
          return;
        }*/
      } else if (answer == JOptionPane.NO_OPTION) {
        showInstructions = false;
      } else if (answer == JOptionPane.CANCEL_OPTION) {
        return;
      }
    }
    if (showInstructions) {
      int answer = PrepTemplateDialog.prompt(this, profile.getPrimaryLanguage());
      if (answer != JOptionPane.YES_OPTION) {
        return;
      }
    }
    List<Path> files;
    if (profile.getIncludedFiles().isEmpty()) {
      int answer = JOptionPane.showConfirmDialog(this, "No translation files have been defined. Would you like to do so now?", "Question", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
      if (answer == JOptionPane.YES_OPTION) {
        RPY2PO.inst().setFrame(() -> new IncludeFiles(profile), "Set Files", null);
        return;
      } else {
        files = List.of();
      }
    } else {
      files = profile.getTranslationFiles(profile.getPrimaryLanguage());
    }
    if (files.isEmpty()) {
      JOptionPane.showMessageDialog(this, "No translation files have been found", "Warning!", JOptionPane.WARNING_MESSAGE);
      return;
    }
    RPY2POConverter converter = new RPY2POConverter(profile.getPrimaryLanguage(), files, profile.getNames(), null, CommentGenerator.SPEAKING);
    try {
      enableActions(false, true);
      var result = converter.convert();
      if (!result.missingNames().isEmpty()) {
        LOGGER.info("Found {} missing name(s): {}", result.missingNames().size(), result.missingNames());
        Map<String, String> newNamesMap = new HashMap<>(profile.getNames().names());
        result.missingNames().forEach(name -> newNamesMap.put(name, ""));
        profile.setNames(new CharacterNames(newNamesMap, profile.getNames().narrator(), profile.getNames().speakFormat()));
        try {
          RPY2PO.inst().writeJson(profile.getFile(), profile);
          LOGGER.info("Updated profile to account for missing names: {}", profile.getFile());
        } catch (IOException e) {
          LOGGER.error("Could not update profile: {}", profile.getFile(), e);
        }
      }
      if (!result.mismatchedFormats().isEmpty()) {
        LOGGER.info("Found {} mismatched formats: {}", result.mismatchedFormats().size(), result.mismatchedFormats());
      }
      Path formatsFile = profile.getBaseDirectory().resolve("formats." + profile.getPrimaryLanguage() + ".json");
      LOGGER.info("Saving formats file {}", formatsFile);
      RPY2PO.inst().writeJson(formatsFile, result.formats());
      Path tempFile = profile.getBaseDirectory().resolve(profile.getPrimaryLanguage() + ".pot");
      PoWriter poWriter = new PoWriter();
      try (OutputStream out = Files.newOutputStream(tempFile)) {
        LOGGER.info("Writing catalog file {}", tempFile);
        poWriter.write(result.catalog(), out);
      }
      JOptionPane.showMessageDialog(this, "Successfully generated template file (" + tempFile.getFileName().toString() + ") and formats file (" + formatsFile.getFileName().toString() + ")");
    } catch (IOException e) {
      LOGGER.error("Could not generate template for profile {} ({})", profile.getName(), profile.getBaseDirectory(), e);
      JOptionPane.showMessageDialog(this, "Could not generate template\n" + e.getClass() + ": " + e.getMessage(), "Error!", JOptionPane.ERROR_MESSAGE);
    } finally {
      enableActions(true, true);
    }
  }

  private void importFiles() {

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
    contentPane.setLayout(new GridLayoutManager(3, 3, new Insets(10, 10, 10, 10), -1, -1));
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new GridLayoutManager(8, 1, new Insets(0, 0, 10, 0), -1, 10));
    contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    buttonGenTemplate = new JButton();
    this.$$$loadButtonText$$$(buttonGenTemplate, this.$$$getMessageFromBundle$$$("strings", "button.generateTemplate"));
    panel1.add(buttonGenTemplate, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 40), null, 0, false));
    final Spacer spacer1 = new Spacer();
    panel1.add(spacer1, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    buttonImport = new JButton();
    buttonImport.setText("Generate PO Translations");
    panel1.add(buttonImport, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 40), null, 0, false));
    buttonUpdate = new JButton();
    buttonUpdate.setText("Update PO Translations");
    panel1.add(buttonUpdate, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 40), null, 0, false));
    buttonVerify = new JButton();
    buttonVerify.setText("Verify PO Translations");
    panel1.add(buttonVerify, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 40), null, 0, false));
    final JLabel label1 = new JLabel();
    label1.setText("Actions");
    panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final Spacer spacer2 = new Spacer();
    panel1.add(spacer2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    buttonExport = new JButton();
    buttonExport.setText("Export Ren'Py Translations");
    panel1.add(buttonExport, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 40), null, 0, false));
    final JPanel panel2 = new JPanel();
    panel2.setLayout(new GridLayoutManager(9, 1, new Insets(0, 0, 10, 0), -1, -1));
    contentPane.add(panel2, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    final JLabel label2 = new JLabel();
    label2.setText("Settings");
    panel2.add(label2, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final Spacer spacer3 = new Spacer();
    panel2.add(spacer3, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    final Spacer spacer4 = new Spacer();
    panel2.add(spacer4, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    final JLabel label3 = new JLabel();
    label3.setText("Output Language(s)");
    panel2.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    textFieldOutLangs = new JTextField();
    panel2.add(textFieldOutLangs, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    buttonNames = new JButton();
    buttonNames.setText("Character Names");
    panel2.add(buttonNames, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    buttonFiles = new JButton();
    buttonFiles.setText("Input Files");
    panel2.add(buttonFiles, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final Spacer spacer5 = new Spacer();
    panel2.add(spacer5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 10), null, 0, false));
    final Spacer spacer6 = new Spacer();
    panel2.add(spacer6, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(-1, 10), null, 0, false));
    final JSeparator separator1 = new JSeparator();
    separator1.setOrientation(1);
    contentPane.add(separator1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(3, -1), null, 0, false));
    labelFeedback = new JLabel();
    labelFeedback.setText(" ");
    contentPane.add(labelFeedback, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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

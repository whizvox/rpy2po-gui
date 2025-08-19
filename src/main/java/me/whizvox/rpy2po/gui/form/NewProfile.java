package me.whizvox.rpy2po.gui.form;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import me.whizvox.rpy2po.core.Profile;
import me.whizvox.rpy2po.core.StringUtil;
import me.whizvox.rpy2po.gui.DocumentChangedListener;
import me.whizvox.rpy2po.gui.RPY2PO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.ResourceBundle;

public class NewProfile extends JFrame {

  private static final Logger LOGGER = LoggerFactory.getLogger(NewProfile.class);

  private JPanel contentPane;
  private JTextField textFieldName;
  private JTextField textFieldRenPyDir;
  private JButton buttonBrowseRenPyDir;
  private JTextField textFieldPrimaryLang;
  private JButton buttonCreate;
  private JLabel labelNameFeedback;
  private JLabel labelRenPyDirFeedback;
  private JLabel labelPrimaryLangFeedback;
  private JButton buttonCancel;
  private JTextField textFieldProfileDir;
  private JButton buttonBrowseProfileDir;
  private JLabel labelProfileDirFeedback;
  private boolean syncNameAndDir;
  private boolean ignoreProfileDirChanges;
  private boolean nameWasBlank;

  public NewProfile() {
    setContentPane(contentPane);
    syncNameAndDir = true;
    ignoreProfileDirChanges = false;
    nameWasBlank = true;

    textFieldName.getDocument().addDocumentListener((DocumentChangedListener) event -> {
      checkName();
      updateProfileDir();
    });
    textFieldProfileDir.getDocument().addDocumentListener((DocumentChangedListener) event -> {
      checkProfileDir();
      if (!ignoreProfileDirChanges) {
        syncNameAndDir = false;
      }
    });
    textFieldRenPyDir.getDocument().addDocumentListener((DocumentChangedListener) event -> checkRenPyDir());
    textFieldPrimaryLang.getDocument().addDocumentListener((DocumentChangedListener) event -> checkPrimaryLang());
    buttonBrowseProfileDir.addActionListener(e -> {
      JFileChooser fc = new JFileChooser();
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      if (fc.showDialog(this, "Choose") == JFileChooser.APPROVE_OPTION) {
        textFieldProfileDir.setText(fc.getSelectedFile().getAbsolutePath());
        syncNameAndDir = false;
      }
    });
    buttonBrowseRenPyDir.addActionListener(e -> {
      JFileChooser fc = new JFileChooser();
      fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      if (fc.showDialog(this, "Choose") == JFileChooser.APPROVE_OPTION) {
        textFieldRenPyDir.setText(fc.getSelectedFile().getAbsolutePath());
      }
    });
    buttonCancel.addActionListener(e -> RPY2PO.inst().setFrame(SelectProfile::new, "Select Profile", new Dimension(400, 400)));
    buttonCreate.addActionListener(e -> createProfile());
  }

  private void checkName() {
    String name = textFieldName.getText();
    labelNameFeedback.setText(" ");
    var profiles = RPY2PO.inst().getProfiles();
    if (name.isBlank()) {
      labelNameFeedback.setText("Must not be blank");
    } else {
      String profileDirName = StringUtil.sanitizeFileName(name);
      for (Profile profile : profiles) {
        if (profile.getName().equals(name)) {
          labelNameFeedback.setText("Same name as another profile");
          break;
        }
        if (profile.getBaseDirectory().getFileName().toString().equals(profileDirName)) {
          labelNameFeedback.setText("Same directory as another profile (" + profile.getName() + "), try using a more unique name");
          break;
        }
      }
    }
  }

  private void updateProfileDir() {
    ignoreProfileDirChanges = true;
    String potentialDir = StringUtil.sanitizeFileName(textFieldName.getText());
    String profileDir = textFieldProfileDir.getText();
    if (profileDir.isBlank()) {
      Path path = Paths.get("profiles", potentialDir).normalize().toAbsolutePath();
      textFieldProfileDir.setText(path.toString());
      syncNameAndDir = true;
      checkProfileDir();
    } else {
      Path path;
      if (nameWasBlank) {
        path = Paths.get(textFieldProfileDir.getText()).resolve(potentialDir);
      } else {
        path = Paths.get(textFieldProfileDir.getText()).getParent().resolve(potentialDir);
      }
      if (syncNameAndDir) {
        textFieldProfileDir.setText(path.toString());
        checkProfileDir();
      } else if (path.toString().equals(textFieldProfileDir.toString())) {
        syncNameAndDir = true;
      }
    }
    ignoreProfileDirChanges = false;
    nameWasBlank = textFieldName.getText().isBlank();
  }

  private void checkProfileDir() {
    labelProfileDirFeedback.setText(" ");
    String dirPath = textFieldProfileDir.getText();
    if (!dirPath.isBlank()) {
      try {
        Path dir = Paths.get(dirPath);
        if (Files.exists(dir)) {
          if (Files.isDirectory(dir)) {
            try (var pathStream = Files.list(dir)) {
              if (pathStream.findAny().isPresent()) {
                labelProfileDirFeedback.setText("Directory must be empty");
              }
            } catch (IOException e) {
              LOGGER.warn("Could not access directory: {}", dir, e);
              labelProfileDirFeedback.setText("Could not access directory");
            }
          } else {
            labelProfileDirFeedback.setText("Must be a directory, not a file");
          }
        }
      } catch (InvalidPathException e) {
        labelProfileDirFeedback.setText("Invalid profile directory path");
      }
    } else {
      labelProfileDirFeedback.setText("Must not be blank");
    }
  }

  private void checkRenPyDir() {
    String renPyDir = textFieldRenPyDir.getText();
    labelRenPyDirFeedback.setText(" ");
    if (renPyDir.isBlank()) {
      labelRenPyDirFeedback.setText("Must not be blank");
    } else {
      Path renPyProjDir = Paths.get(renPyDir);
      var profiles = RPY2PO.inst().getProfiles();
      if (!Files.exists(renPyProjDir)) {
        labelRenPyDirFeedback.setText("Directory does not exist");
      } else if (!Files.isDirectory(renPyProjDir)) {
        labelRenPyDirFeedback.setText("Not a directory");
      } else {
        Path gameDir = renPyProjDir.resolve("game");
        if (!Files.exists(gameDir) || !Files.isDirectory(gameDir)) {
          labelRenPyDirFeedback.setText("Invalid Ren'Py project directory, missing game directory");
        } else {
          for (Profile profile : profiles) {
            if (profile.getBaseDirectory().equals(renPyProjDir)) {
              labelRenPyDirFeedback.setText("Another profile (" + profile.getName() + ") is already using that project");
              break;
            }
          }
        }
      }
    }
  }

  private void checkPrimaryLang() {
    String primaryLang = textFieldPrimaryLang.getText();
    labelPrimaryLangFeedback.setText(" ");
    if (primaryLang.isBlank()) {
      labelPrimaryLangFeedback.setText("Must not be blank");
    }
  }

  private void createProfile() {
    checkName();
    checkProfileDir();
    checkRenPyDir();
    checkPrimaryLang();
    if (labelNameFeedback.getText().isBlank() && labelProfileDirFeedback.getText().isBlank() &&
        labelRenPyDirFeedback.getText().isBlank() && labelPrimaryLangFeedback.getText().isBlank()) {
      try {
        Path profileDir = Paths.get(textFieldProfileDir.getText());
        Files.createDirectories(profileDir);
        Path renPyDir = Paths.get(textFieldRenPyDir.getText());
        Profile profile = new Profile(profileDir, textFieldName.getText(), renPyDir, textFieldPrimaryLang.getText(), LocalDateTime.now());
        Path profileFile = profileDir.resolve("profile.json");
        RPY2PO.inst().writeJson(profileFile, profile);
        RPY2PO.inst().addProfile(profileDir.toString());
        RPY2PO.inst().setFrame(() -> new ProfileActions(profile), profile.getName(), null);
      } catch (IOException | InvalidPathException e) {
        LOGGER.warn("Could not create profile directory: {}", textFieldProfileDir.getText(), e);
        JOptionPane.showMessageDialog(this, "Could not create profile directory!\n" + e.getClass() + ": " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
      }
    }
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
    contentPane.setLayout(new GridLayoutManager(4, 1, new Insets(10, 10, 10, 10), -1, -1));
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new GridLayoutManager(9, 5, new Insets(0, 0, 10, 0), -1, -1));
    contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    final JLabel label1 = new JLabel();
    this.$$$loadLabelText$$$(label1, this.$$$getMessageFromBundle$$$("strings", "label.profileName"));
    panel1.add(label1, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label2 = new JLabel();
    this.$$$loadLabelText$$$(label2, this.$$$getMessageFromBundle$$$("strings", "label.renPyProjectDirectory"));
    panel1.add(label2, new GridConstraints(4, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label3 = new JLabel();
    this.$$$loadLabelText$$$(label3, this.$$$getMessageFromBundle$$$("strings", "label.primaryLanguage"));
    panel1.add(label3, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    labelNameFeedback = new JLabel();
    Font labelNameFeedbackFont = this.$$$getFont$$$(null, Font.PLAIN, -1, labelNameFeedback.getFont());
    if (labelNameFeedbackFont != null) labelNameFeedback.setFont(labelNameFeedbackFont);
    labelNameFeedback.setForeground(new Color(-65536));
    labelNameFeedback.setText(" ");
    panel1.add(labelNameFeedback, new GridConstraints(1, 3, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    labelRenPyDirFeedback = new JLabel();
    Font labelRenPyDirFeedbackFont = this.$$$getFont$$$(null, Font.PLAIN, -1, labelRenPyDirFeedback.getFont());
    if (labelRenPyDirFeedbackFont != null) labelRenPyDirFeedback.setFont(labelRenPyDirFeedbackFont);
    labelRenPyDirFeedback.setForeground(new Color(-65536));
    labelRenPyDirFeedback.setText(" ");
    panel1.add(labelRenPyDirFeedback, new GridConstraints(5, 3, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    labelPrimaryLangFeedback = new JLabel();
    Font labelPrimaryLangFeedbackFont = this.$$$getFont$$$(null, Font.PLAIN, -1, labelPrimaryLangFeedback.getFont());
    if (labelPrimaryLangFeedbackFont != null) labelPrimaryLangFeedback.setFont(labelPrimaryLangFeedbackFont);
    labelPrimaryLangFeedback.setForeground(new Color(-65536));
    labelPrimaryLangFeedback.setText(" ");
    panel1.add(labelPrimaryLangFeedback, new GridConstraints(7, 3, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    textFieldPrimaryLang = new JTextField();
    textFieldPrimaryLang.setText("en");
    panel1.add(textFieldPrimaryLang, new GridConstraints(6, 3, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), new Dimension(200, -1), 0, false));
    textFieldName = new JTextField();
    panel1.add(textFieldName, new GridConstraints(0, 3, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), new Dimension(200, -1), 0, false));
    textFieldRenPyDir = new JTextField();
    panel1.add(textFieldRenPyDir, new GridConstraints(4, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    buttonBrowseRenPyDir = new JButton();
    this.$$$loadButtonText$$$(buttonBrowseRenPyDir, this.$$$getMessageFromBundle$$$("strings", "button.browseDirectory"));
    panel1.add(buttonBrowseRenPyDir, new GridConstraints(4, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label4 = new JLabel();
    this.$$$loadLabelText$$$(label4, this.$$$getMessageFromBundle$$$("strings", "label.profileDirectory"));
    panel1.add(label4, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    textFieldProfileDir = new JTextField();
    panel1.add(textFieldProfileDir, new GridConstraints(2, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    buttonBrowseProfileDir = new JButton();
    this.$$$loadButtonText$$$(buttonBrowseProfileDir, this.$$$getMessageFromBundle$$$("strings", "button.browseDirectory"));
    panel1.add(buttonBrowseProfileDir, new GridConstraints(2, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    labelProfileDirFeedback = new JLabel();
    Font labelProfileDirFeedbackFont = this.$$$getFont$$$(null, Font.PLAIN, -1, labelProfileDirFeedback.getFont());
    if (labelProfileDirFeedbackFont != null) labelProfileDirFeedback.setFont(labelProfileDirFeedbackFont);
    labelProfileDirFeedback.setForeground(new Color(-65536));
    labelProfileDirFeedback.setText(" ");
    panel1.add(labelProfileDirFeedback, new GridConstraints(3, 3, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final Spacer spacer1 = new Spacer();
    contentPane.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    final Spacer spacer2 = new Spacer();
    contentPane.add(spacer2, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    final JPanel panel2 = new JPanel();
    panel2.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel2, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    buttonCreate = new JButton();
    buttonCreate.setEnabled(true);
    this.$$$loadButtonText$$$(buttonCreate, this.$$$getMessageFromBundle$$$("strings", "button.createProfile"));
    panel2.add(buttonCreate, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final Spacer spacer3 = new Spacer();
    panel2.add(spacer3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    buttonCancel = new JButton();
    this.$$$loadButtonText$$$(buttonCancel, this.$$$getMessageFromBundle$$$("strings", "button.cancel"));
    panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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
  private void $$$loadLabelText$$$(JLabel component, String text) {
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
      component.setDisplayedMnemonic(mnemonic);
      component.setDisplayedMnemonicIndex(mnemonicIndex);
    }
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

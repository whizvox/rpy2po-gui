package me.whizvox.rpy2po.gui.form;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import com.soberlemur.potentilla.Catalog;
import com.soberlemur.potentilla.Message;
import com.soberlemur.potentilla.MessageKey;
import com.soberlemur.potentilla.PoParser;
import me.whizvox.rpy2po.core.MessageSourceReferenceComparator;
import me.whizvox.rpy2po.core.Profile;
import me.whizvox.rpy2po.core.SimilarMessage;
import me.whizvox.rpy2po.core.StringUtil;
import me.whizvox.rpy2po.gettext.ProblemResolution;
import me.whizvox.rpy2po.gettext.SourceReference;
import me.whizvox.rpy2po.gui.ProblemMessagesTableModel;
import me.whizvox.rpy2po.gui.RPY2PO;
import me.whizvox.rpy2po.gui.SimilarStringsTableModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.StyleContext;
import java.awt.*;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

public class ResolveTranslationProblems extends JFrame {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResolveTranslationProblems.class);

  private JPanel contentPane;
  private JComboBox<String> comboBoxTplFiles;
  private JButton buttonSelectFiles;
  private JTextArea textAreaTplString;
  private JLabel labelTplComment;
  private JLabel labelTplContext;
  private JTextArea textAreaLangString;
  private JLabel labelLangComment;
  private JLabel labelLangContext;
  private JTable tableProblems;
  private JTable tableSimilar;
  private JButton buttonScan;
  private JButton buttonAllNew;
  private JTextField textFieldSearch;
  private JCheckBox checkBoxAllFiles;
  private JButton buttonUpdated;
  private JButton buttonNew;
  private JLabel labelFiles;
  private JLabel labelTplRef;
  private JLabel labelLangRef;
  private JPanel panelTemplate;
  private JPanel panelLang;
  private JButton buttonCancel;
  private JButton buttonRescan;
  private JButton buttonFinish;
  private JButton buttonAutoResolve;
  private JButton buttonUnmarkResolved;
  private JButton buttonUnresolveAll;
  private JCheckBox checkBoxShowAllProblems;

  private final ProblemMessagesTableModel problemStringsModel;
  private final SimilarStringsTableModel similarStringsModel;

  private final Profile profile;
  private final List<String> languages;
  private int languageIndex;
  private final Set<MessageKey> missingStrings;
  private final Map<MessageKey, List<SimilarMessage>> problemStrings;
  private final Set<String> tplFiles;
  private final Set<String> langFiles;
  private String currentFile;
  private final Map<String, List<String>> searchingFiles;
  private final Map<MessageKey, ProblemResolution> resolutions;
  private final Map<MessageKey, ProblemResolution> resolutionsReverse;

  private Catalog template;
  private Catalog translations;

  public ResolveTranslationProblems(Profile profile, List<String> languages) {
    this.profile = profile;
    this.languages = Collections.unmodifiableList(languages);
    languageIndex = 0;
    missingStrings = new HashSet<>();
    problemStrings = new HashMap<>();
    tplFiles = new HashSet<>();
    langFiles = new HashSet<>();
    currentFile = null;
    searchingFiles = new HashMap<>();
    resolutions = new HashMap<>();
    resolutionsReverse = new HashMap<>();
    template = new Catalog();
    translations = new Catalog();

    comboBoxTplFiles.addItemListener(e -> setCurrentFile((String) e.getItem()));
    buttonSelectFiles.addActionListener(e -> {
      var newFiles = SearchFilesDialog.prompt(this, langFiles, searchingFiles.getOrDefault(currentFile, List.of()));
      if (!newFiles.isEmpty()) {
        List<String> files = searchingFiles.computeIfAbsent(currentFile, l -> new ArrayList<>());
        files.clear();
        files.addAll(newFiles);
        files.sort(String::compareTo);
        updateSearchingFiles();
      }
    });
    problemStringsModel = new ProblemMessagesTableModel();
    tableProblems.setModel(problemStringsModel);
    tableProblems.getSelectionModel().addListSelectionListener(e -> onProblemStringSelected(getSelectedProblemRow()));
    checkBoxShowAllProblems.addActionListener(e -> {
      problemStringsModel.toggleShowAll(checkBoxShowAllProblems.isSelected());
      problemStringsModel.fireTableDataChanged();
    });

    buttonUnmarkResolved.addActionListener(e -> unmarkResolved());
    buttonUnresolveAll.addActionListener(e -> unresolveAll());
    buttonAutoResolve.addActionListener(e -> autoResolveProblems());
    buttonAllNew.addActionListener(e -> markAllNew());

    similarStringsModel = new SimilarStringsTableModel();
    tableSimilar.setModel(similarStringsModel);
    tableSimilar.getSelectionModel().addListSelectionListener(e -> updateSimilarStringDetails(getSelectedSimilarRow()));

    buttonUpdated.addActionListener(e -> markUpdated());

    buttonScan.addActionListener(e -> {
      if (tableProblems.getSelectedRow() != -1) {
        String filter = textFieldSearch.getText().trim();
        scanStrings(0.7F, filter.isEmpty() ? null : filter, checkBoxAllFiles.isSelected());
      }
    });

    buttonRescan.addActionListener(e -> initialize());
    buttonCancel.addActionListener(e -> RPY2PO.inst().setFrame(() -> new ProfileActions(profile), profile.getName(), null));

    setContentPane(contentPane);
    initialize();
    onProblemStringSelected(-1);
  }

  private int getSelectedProblemRow() {
    return tableProblems.getSelectionModel().getLeadSelectionIndex();
  }

  private int getSelectedSimilarRow() {
    return tableSimilar.getSelectionModel().getLeadSelectionIndex();
  }

  private void initialize() {
    try {
      Path tplPath = profile.getBaseDirectory().resolve(profile.getPrimaryLanguage() + ".pot");
      Path langPath = profile.getBaseDirectory().resolve(languages.get(languageIndex) + ".po");
      template = new PoParser().parseCatalog(tplPath.toFile());
      translations = new PoParser().parseCatalog(langPath.toFile());
      problemStrings.clear();
      tplFiles.clear();
      for (Message msg : template) {
        msg.getSourceReferences().stream().map(str -> SourceReference.parse(str).file()).forEach(tplFiles::add);
        MessageKey key = new MessageKey(msg);
        if (!translations.contains(key)) {
          problemStrings.put(key, new ArrayList<>());
        }
      }
      missingStrings.clear();
      langFiles.clear();
      for (Message msg : translations) {
        msg.getSourceReferences().stream().map(str -> SourceReference.parse(str).file()).forEach(langFiles::add);
        MessageKey key = new MessageKey(msg);
        if (!template.contains(key)) {
          missingStrings.add(key);
        }
      }
      comboBoxTplFiles.removeAllItems();
      tplFiles.stream().sorted().forEach(file -> {
        comboBoxTplFiles.addItem(file);
        if (currentFile == null) {
          currentFile = file;
        }
      });
      setCurrentFile(currentFile);
      ((TitledBorder) panelTemplate.getBorder()).setTitle(tplPath.getFileName().toString());
      ((TitledBorder) panelLang.getBorder()).setTitle(langPath.getFileName().toString());
      searchingFiles.clear();
      if (langFiles.contains(currentFile)) {
        searchingFiles.computeIfAbsent(currentFile, s -> new ArrayList<>()).add(currentFile);
      }
      updateSearchingFiles();
    } catch (Exception e) {
      LOGGER.error("Could not initialize", e);
    }
  }

  private void setCurrentFile(String file) {
    currentFile = file;
    problemStringsModel.clear();
    problemStrings.keySet().stream()
        .map(key -> template.get(key))
        .filter(msg -> msg.getSourceReferences().stream().map(str -> SourceReference.parse(str).file()).anyMatch(str -> str.equals(file)))
        .sorted(new MessageSourceReferenceComparator())
        .forEach(msg -> problemStringsModel.addValue(false, new MessageKey(msg)));
    problemStringsModel.fireTableDataChanged();
    List<String> files = searchingFiles.computeIfAbsent(currentFile, s -> new ArrayList<>());
    if (files.isEmpty() && langFiles.contains(currentFile)) {
      files.add(currentFile);
    }
    updateSearchingFiles();
  }

  private void updateSearchingFiles() {
    List<String> files = searchingFiles.computeIfAbsent(currentFile, l -> new ArrayList<>());
    if (files.isEmpty()) {
      labelFiles.setText("(nothing...)");
      labelFiles.setForeground(Color.RED);
      similarStringsModel.clear();
    } else {
      labelFiles.setText(String.join(", ", files));
      labelFiles.setForeground(Color.BLACK);
    }
    similarStringsModel.fireTableDataChanged();
  }

  private void onProblemStringSelected(int row) {
    if (row < 0 || row >= problemStringsModel.getRowCount()) {
      labelTplRef.setText(" ");
      labelTplContext.setText(" ");
      labelTplComment.setText(" ");
      textAreaTplString.setText("");
      similarStringsModel.clear();
      similarStringsModel.fireTableDataChanged();
      updateSimilarStringDetails(-1);
    } else {
      MessageKey key = problemStringsModel.getKey(row);
      Message msg = template.get(key);
      labelTplRef.setText(String.join(", ", msg.getSourceReferences()));
      labelTplContext.setText(key.msgContext());
      labelTplComment.setText(String.join(", ", msg.getExtractedComments()));
      textAreaTplString.setText(key.msgId());
      similarStringsModel.setValues(problemStrings.computeIfAbsent(key, k -> new ArrayList<>()));
      similarStringsModel.fireTableDataChanged();
      if (similarStringsModel.getRowCount() > 0) {
        tableSimilar.clearSelection();
        tableSimilar.setRowSelectionInterval(0, 0);
        updateSimilarStringDetails(0);
      }
    }
  }

  private void unmarkResolved() {
    int row = getSelectedProblemRow();
    if (problemStringsModel.isResolved(row)) {
      MessageKey key = problemStringsModel.getKey(row);
      ProblemResolution resolution = resolutions.get(key);
      resolutions.remove(key);
      if (resolution.langKey() != null) {
        resolutionsReverse.remove(resolution.langKey());
      }
      problemStringsModel.markResolved(row, false);
      problemStringsModel.fireTableRowsUpdated(row, row);
    }
  }

  private void unresolveAll() {
    for (int row = 0; row < problemStringsModel.getRowCount(); row++) {
      if (problemStringsModel.isResolved(row)) {
        MessageKey key = problemStringsModel.getKey(row);
        ProblemResolution resolution = resolutions.get(key);
        resolutions.remove(key);
        if (resolution.langKey() != null) {
          resolutionsReverse.remove(resolution.langKey());
        }
        problemStringsModel.markResolved(row, false);
      }
    }
    problemStringsModel.fireTableRowsUpdated(0, problemStringsModel.getRowCount());
  }

  private void markUpdated() {
    int problemRow = getSelectedProblemRow();
    int similarRow = getSelectedSimilarRow();
    if (problemRow != -1 && similarRow != -1) {
      MessageKey tplKey = problemStringsModel.getKey(problemRow);
      MessageKey langKey = similarStringsModel.getKey(similarRow);
      Message langMsg = translations.get(langKey);
      if (tplKey != null && langKey != null && langMsg != null) {
        ProblemResolution oldResolution = resolutionsReverse.get(langKey);
        if (oldResolution != null) {
          int answer = JOptionPane.showConfirmDialog(this, "This string has already been marked as being updated to " + oldResolution.templateKey().msgContext() + ". Do you want to override this?", "Question", JOptionPane.YES_NO_OPTION);
          if (answer == JOptionPane.YES_OPTION) {
            resolutions.remove(oldResolution.templateKey());
            resolutionsReverse.remove(langKey);
            for (int row = 0; row < problemStringsModel.getRowCount(); row++) {
              if (problemStringsModel.getKey(row).equals(oldResolution.templateKey())) {
                problemStringsModel.markResolved(row, false);
                break;
              }
            }
          } else {
            return;
          }
        }
        ProblemResolution resolution = new ProblemResolution(tplKey, langKey, langMsg.getMsgstr(), false, false);
        resolutions.put(tplKey, resolution);
        resolutionsReverse.put(langKey, resolution);
        problemStringsModel.markResolved(problemRow);
        problemStringsModel.fireTableDataChanged();
        if (!problemStringsModel.isShowingAll() && problemStringsModel.getRowCount() > 0) {
          tableProblems.setRowSelectionInterval(0, 0);
        }
      }
    }
  }

  private void autoResolveProblems() {
    if (searchingFiles.get(currentFile).isEmpty()) {
      JOptionPane.showMessageDialog(this, "No search files are selected.");
    } else {
      ProgressMonitor monitor = new ProgressMonitor(this, "Finding similar strings...", "", 0, problemStringsModel.getRowCount());
      monitor.setMillisToPopup(100);
      Thread thread = new Thread(() -> {
        List<Integer> rows = new ArrayList<>();
        for (int row = 0; row < problemStringsModel.getRowCount(); row++) {
          if (monitor.isCanceled()) {
            break;
          }
          if (!problemStringsModel.isResolved(row)) {
            MessageKey key = problemStringsModel.getKey(row);
            if (key.msgId().length() > 50) {
              monitor.setNote(key.msgId().substring(0, 50) + "...");
            } else {
              monitor.setNote(key.msgId());
            }
            List<SimilarMessage> similar = findSimilarStrings(key, 0.7F, null, false);
            if (similar.size() == 1) {
              SimilarMessage msg = similar.getFirst();
              ProblemResolution resolution = new ProblemResolution(key, msg.key(), msg.key().msgId(), false, false);
              resolutions.put(key, resolution);
              resolutionsReverse.put(msg.key(), resolution);
              rows.add(row);
            } else if (similar.isEmpty()) {
              ProblemResolution resolution = new ProblemResolution(key, null, null, false, true);
              resolutions.put(key, resolution);
              rows.add(row);
            }
          }
          monitor.setProgress(row + 1);
        }
        // if show all is unchecked
        for (int i = rows.size() - 1; i >= 0; i--) {
          problemStringsModel.markResolved(rows.get(i));
        }
        problemStringsModel.fireTableDataChanged();
      });
      thread.start();
    }
  }

  private void markAllNew() {
    for (int row = 0; row < problemStringsModel.getRowCount(); row++) {
      if (!problemStringsModel.isResolved(row)) {
        MessageKey key = problemStringsModel.getKey(row);
        resolutions.put(key, new ProblemResolution(key, null, null, false, true));
        problemStringsModel.markResolved(row);
      }
    }
    problemStringsModel.fireTableRowsUpdated(0, problemStringsModel.getRowCount());
  }

  private void updateSimilarStringDetails(int row) {
    if (row < 0 || row >= similarStringsModel.getRowCount()) {
      labelLangRef.setText(" ");
      labelLangContext.setText(" ");
      labelLangComment.setText(" ");
      textAreaLangString.setText("");
    } else {
      MessageKey key = similarStringsModel.getKey(row);
      Message msg = translations.get(key);
      labelLangRef.setText(String.join(", ", msg.getSourceReferences()));
      labelLangContext.setText(key.msgContext());
      labelLangComment.setText(String.join(", ", msg.getExtractedComments()));
      textAreaLangString.setText(key.msgId());
    }
  }

  private List<SimilarMessage> findSimilarStrings(MessageKey key, float threshold, String filter, boolean scanAllFiles) {
    List<SimilarMessage> similar = problemStrings.computeIfAbsent(key, k -> new ArrayList<>());
    similar.clear();
    Collection<String> files;
    if (scanAllFiles) {
      files = langFiles;
    } else {
      files = searchingFiles.get(currentFile);
    }
    Stream<Message> stream = missingStrings.stream()
        .filter(langKey -> !resolutionsReverse.containsKey(langKey))
        .map(langKey -> translations.get(langKey))
        .sorted(new MessageSourceReferenceComparator());
    if (filter == null) {
      Message tplMsg = template.get(key);
      stream.forEach(msg -> {
        if (msg.getSourceReferences().stream().map(str -> SourceReference.parse(str).file()).anyMatch(files::contains)) {
          int max = Math.max(tplMsg.getMsgId().length(), msg.getMsgId().length());
          int dist = StringUtil.getEditDistance(tplMsg.getMsgId(), msg.getMsgId());
          float similarity = 1.0F - (float) dist / max;
          if (similarity >= threshold) {
            similar.add(new SimilarMessage(new MessageKey(msg), similarity));
          }
        }
      });
    } else {
      String actualFilter = filter.toLowerCase();
      stream.forEach(msg -> {
        if (msg.getSourceReferences().stream().map(str -> SourceReference.parse(str).file()).anyMatch(files::contains)) {
          if (msg.getMsgId().toLowerCase().contains(actualFilter)) {
            similar.add(new SimilarMessage(new MessageKey(msg), 1.0F));
          }
        }
      });
    }
    similar.sort((o1, o2) -> Float.compare(o2.similarity(), o1.similarity()));
    return similar;
  }

  private void scanStrings(float threshold, String filter, boolean scanAllFiles) {
    MessageKey key = problemStringsModel.getKey(getSelectedProblemRow());
    List<SimilarMessage> similar = findSimilarStrings(key, threshold, filter, scanAllFiles);
    similarStringsModel.setValues(similar);
    similarStringsModel.fireTableDataChanged();
  }

  private void finish() {
    List<MessageKey> unresolved = problemStrings.keySet().stream()
        .filter(key -> !resolutions.containsKey(key))
        .sorted()
        .toList();
    if (!unresolved.isEmpty()) {
      int answer = JOptionPane.showConfirmDialog(this, "Found ");
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
    contentPane.setLayout(new GridLayoutManager(4, 3, new Insets(10, 10, 10, 10), -1, -1));
    final JLabel label1 = new JLabel();
    label1.setText("Current File");
    contentPane.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    comboBoxTplFiles = new JComboBox();
    contentPane.add(comboBoxTplFiles, new GridConstraints(0, 1, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label2 = new JLabel();
    label2.setText("Search File(s)");
    contentPane.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    labelFiles = new JLabel();
    Font labelFilesFont = this.$$$getFont$$$(null, Font.PLAIN, -1, labelFiles.getFont());
    if (labelFilesFont != null) labelFiles.setFont(labelFilesFont);
    labelFiles.setText("definitions.rpy");
    contentPane.add(labelFiles, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    buttonSelectFiles = new JButton();
    buttonSelectFiles.setText("Select...");
    contentPane.add(buttonSelectFiles, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel1, new GridConstraints(2, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    panelTemplate = new JPanel();
    panelTemplate.setLayout(new GridLayoutManager(13, 1, new Insets(5, 5, 5, 5), -1, -1));
    panel1.add(panelTemplate, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    panelTemplate.setBorder(BorderFactory.createTitledBorder(null, "From en.pot", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, null, null));
    final JLabel label3 = new JLabel();
    label3.setText("Context");
    panelTemplate.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    labelTplContext = new JLabel();
    Font labelTplContextFont = this.$$$getFont$$$("Consolas", Font.PLAIN, -1, labelTplContext.getFont());
    if (labelTplContextFont != null) labelTplContext.setFont(labelTplContextFont);
    labelTplContext.setText("str_id_123");
    panelTemplate.add(labelTplContext, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label4 = new JLabel();
    label4.setText("String");
    panelTemplate.add(label4, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label5 = new JLabel();
    label5.setText("Translator Comment");
    panelTemplate.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    labelTplComment = new JLabel();
    Font labelTplCommentFont = this.$$$getFont$$$(null, Font.PLAIN, -1, labelTplComment.getFont());
    if (labelTplCommentFont != null) labelTplComment.setFont(labelTplCommentFont);
    labelTplComment.setText("Narrator speaking");
    panelTemplate.add(labelTplComment, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JScrollPane scrollPane1 = new JScrollPane();
    panelTemplate.add(scrollPane1, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(300, 100), null, 0, false));
    textAreaTplString = new JTextArea();
    textAreaTplString.setEditable(false);
    textAreaTplString.setLineWrap(true);
    textAreaTplString.setOpaque(false);
    scrollPane1.setViewportView(textAreaTplString);
    final JPanel panel2 = new JPanel();
    panel2.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
    panelTemplate.add(panel2, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    buttonAllNew = new JButton();
    buttonAllNew.setText("Mark All as New");
    panel2.add(buttonAllNew, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final Spacer spacer1 = new Spacer();
    panel2.add(spacer1, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    buttonAutoResolve = new JButton();
    buttonAutoResolve.setText("Auto-Resolve All");
    panel2.add(buttonAutoResolve, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    buttonUnresolveAll = new JButton();
    buttonUnresolveAll.setText("Unresolve All");
    panel2.add(buttonUnresolveAll, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JScrollPane scrollPane2 = new JScrollPane();
    panelTemplate.add(scrollPane2, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    tableProblems = new JTable();
    tableProblems.setPreferredScrollableViewportSize(new Dimension(300, 200));
    scrollPane2.setViewportView(tableProblems);
    final JSeparator separator1 = new JSeparator();
    panelTemplate.add(separator1, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    final JPanel panel3 = new JPanel();
    panel3.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
    panelTemplate.add(panel3, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    buttonNew = new JButton();
    buttonNew.setText("Mark as New");
    panel3.add(buttonNew, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final Spacer spacer2 = new Spacer();
    panel3.add(spacer2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    buttonUnmarkResolved = new JButton();
    buttonUnmarkResolved.setText("Unmark Resolved");
    panel3.add(buttonUnmarkResolved, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label6 = new JLabel();
    label6.setText("Reference");
    panelTemplate.add(label6, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    labelTplRef = new JLabel();
    Font labelTplRefFont = this.$$$getFont$$$("Consolas", Font.PLAIN, -1, labelTplRef.getFont());
    if (labelTplRefFont != null) labelTplRef.setFont(labelTplRefFont);
    labelTplRef.setText("file.rpy:123");
    panelTemplate.add(labelTplRef, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel4 = new JPanel();
    panel4.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
    panelTemplate.add(panel4, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    final JLabel label7 = new JLabel();
    label7.setText("Problematic Strings");
    panel4.add(label7, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final Spacer spacer3 = new Spacer();
    panel4.add(spacer3, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    checkBoxShowAllProblems = new JCheckBox();
    checkBoxShowAllProblems.setSelected(true);
    checkBoxShowAllProblems.setText("Show All");
    panel4.add(checkBoxShowAllProblems, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    panelLang = new JPanel();
    panelLang.setLayout(new GridLayoutManager(13, 1, new Insets(5, 5, 5, 5), -1, -1));
    panel1.add(panelLang, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    panelLang.setBorder(BorderFactory.createTitledBorder(null, "From ru.po", TitledBorder.CENTER, TitledBorder.DEFAULT_POSITION, null, null));
    final JLabel label8 = new JLabel();
    label8.setText("Context");
    panelLang.add(label8, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    labelLangContext = new JLabel();
    Font labelLangContextFont = this.$$$getFont$$$("Consolas", Font.PLAIN, -1, labelLangContext.getFont());
    if (labelLangContextFont != null) labelLangContext.setFont(labelLangContextFont);
    labelLangContext.setText("str_id_456");
    panelLang.add(labelLangContext, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label9 = new JLabel();
    label9.setText("Translator Comment");
    panelLang.add(label9, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    labelLangComment = new JLabel();
    Font labelLangCommentFont = this.$$$getFont$$$(null, Font.PLAIN, -1, labelLangComment.getFont());
    if (labelLangCommentFont != null) labelLangComment.setFont(labelLangCommentFont);
    labelLangComment.setText("Narrator speaking");
    panelLang.add(labelLangComment, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label10 = new JLabel();
    label10.setText("String");
    panelLang.add(label10, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JScrollPane scrollPane3 = new JScrollPane();
    panelLang.add(scrollPane3, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(300, 100), null, 0, false));
    textAreaLangString = new JTextArea();
    textAreaLangString.setEditable(false);
    textAreaLangString.setLineWrap(true);
    textAreaLangString.setOpaque(false);
    scrollPane3.setViewportView(textAreaLangString);
    final JSeparator separator2 = new JSeparator();
    panelLang.add(separator2, new GridConstraints(9, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    final JLabel label11 = new JLabel();
    label11.setText("Similar Strings");
    panelLang.add(label11, new GridConstraints(10, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JScrollPane scrollPane4 = new JScrollPane();
    panelLang.add(scrollPane4, new GridConstraints(11, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    tableSimilar = new JTable();
    tableSimilar.setPreferredScrollableViewportSize(new Dimension(300, 200));
    scrollPane4.setViewportView(tableSimilar);
    final JPanel panel5 = new JPanel();
    panel5.setLayout(new GridLayoutManager(2, 2, new Insets(0, 0, 0, 0), -1, -1));
    panelLang.add(panel5, new GridConstraints(12, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    buttonScan = new JButton();
    buttonScan.setText("Scan");
    panel5.add(buttonScan, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    textFieldSearch = new JTextField();
    panel5.add(textFieldSearch, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    final JLabel label12 = new JLabel();
    label12.setText("Search Strings");
    panel5.add(label12, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_SOUTHWEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    checkBoxAllFiles = new JCheckBox();
    checkBoxAllFiles.setText("All Files");
    panel5.add(checkBoxAllFiles, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel6 = new JPanel();
    panel6.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
    panelLang.add(panel6, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    final Spacer spacer4 = new Spacer();
    panel6.add(spacer4, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    buttonUpdated = new JButton();
    buttonUpdated.setText("Select Updated");
    panel6.add(buttonUpdated, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label13 = new JLabel();
    label13.setText("Reference");
    panelLang.add(label13, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    labelLangRef = new JLabel();
    Font labelLangRefFont = this.$$$getFont$$$("Consolas", Font.PLAIN, -1, labelLangRef.getFont());
    if (labelLangRefFont != null) labelLangRef.setFont(labelLangRefFont);
    labelLangRef.setText("file.rpy:456");
    panelLang.add(labelLangRef, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel7 = new JPanel();
    panel7.setLayout(new GridLayoutManager(1, 5, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel7, new GridConstraints(3, 0, 1, 3, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    buttonCancel = new JButton();
    this.$$$loadButtonText$$$(buttonCancel, this.$$$getMessageFromBundle$$$("strings", "button.cancel"));
    panel7.add(buttonCancel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final Spacer spacer5 = new Spacer();
    panel7.add(spacer5, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    buttonRescan = new JButton();
    buttonRescan.setText("Rescan");
    panel7.add(buttonRescan, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final Spacer spacer6 = new Spacer();
    panel7.add(spacer6, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    buttonFinish = new JButton();
    this.$$$loadButtonText$$$(buttonFinish, this.$$$getMessageFromBundle$$$("strings", "button.finish"));
    panel7.add(buttonFinish, new GridConstraints(0, 4, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
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

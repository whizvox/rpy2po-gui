package me.whizvox.rpy2po.rpytl;

import com.soberlemur.potentilla.Catalog;
import com.soberlemur.potentilla.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * An object that will attempt to convert <code>.rpy</code> files into <code>.po</code> files.
 * @param language The language of the final <code>.po</code> file, and the language that will be checked throughout all
 *                 of the <code>.rpy</code> translation entries
 * @param inputs All input <code>.rpy</code> files that will be read from
 * @param names Character names that will appear as translator comments in the <code>.po</code> file messages. If
 *              <code>null</code>, then no such comments will be applied.
 * @param validateStatements Statements that will be used to validate against those found in the Ren'Py translation entries. If
 *                   <code>null</code>, then no validation occurs, and instead all statements will be included in the
 *                   final result of {@link #convert()}.
 * @param commentGenerator A generator for adding translator comments to <code>.po</code> file entries.
 */
public record RPY2POConverter(String language,
                              List<Path> inputs,
                              CharacterNames names,
                              Statements validateStatements,
                              CommentGenerator commentGenerator) {

  private static final Logger LOGGER = LoggerFactory.getLogger(RPY2POConverter.class);

  public RPY2POConverter(String language, List<Path> inputs, CharacterNames names, Statements validateStatements,
                         CommentGenerator commentGenerator) {
    this.language = language;
    this.inputs = Collections.unmodifiableList(inputs);
    this.names = names;
    this.validateStatements = validateStatements;
    this.commentGenerator = commentGenerator;
  }

  /**
   * Reads all <code>.rpy</code> input files and converts them into a single <code>.po</code> file via a {@link Catalog}
   * object. Will also track any missing character names if one doesn't exist in the {@link #names()} object.
   * If {@link #validateStatements()} is not <code>null</code>, this will also track any dialogue formats that don't
   * match.
   * @return The result of converting the files
   * @throws IOException If reading any of the input files fails
   */
  public Result convert() throws IOException {
    Catalog catalog = new Catalog();
    Map<String, String> dialogue = new HashMap<>();
    Map<String, Statement> plain = new HashMap<>();
    List<String> mismatchedFormats = new ArrayList<>();
    List<String> missingNames = new ArrayList<>();
    TranslationContext ctx = new TranslationContext(names);
    for (Path inPath : inputs) {
      LOGGER.info("Reading input file <{}>...", inPath);
      try (InputStream in = Files.newInputStream(inPath)) {
        TranslationFile file = TranslationFile.read(in);
        String fileLang = file.getLanguage(true);
        if (!fileLang.equals(language)) {
          throw new IllegalArgumentException("File language does not match exporter's configured language");
        }
        for (TranslationEntry entry : file) {
          Message msg = new Message();
          msg.addSourceReference(entry.file(), entry.line());
          List<String> comments;
          if (entry.isStatement()) {
            msg.setMsgContext(entry.id());
            Dialogue origDialogue = entry.parseOriginalDialogue();
            Dialogue tlDialogue = entry.parseTranslatedDialogue();
            // Check if the line contains actual dialogue instead of some other code (i.e. `nvl clear`)
            if (origDialogue.what() != null && tlDialogue.what() != null) {
              if (origDialogue.nameOnly()) {
                msg.setMsgId(origDialogue.who() + " :: " + origDialogue.what());
              } else {
                msg.setMsgId(origDialogue.what());
              }
              if (tlDialogue.nameOnly()) {
                msg.setMsgstr(tlDialogue.who() + " :: " + tlDialogue.what());
              } else {
                msg.setMsgstr(tlDialogue.what());
              }
            }
            if (validateStatements == null) {
              if (origDialogue.isPlainStatement()) {
                plain.put(entry.id(), new Statement(entry.id(), origDialogue.format(), entry.file(), entry.line()));
              } else {
                dialogue.put(entry.id(), origDialogue.format());
              }
            } else if (!validateStatements.matches(entry.id(), origDialogue.format()) || !validateStatements.matches(entry.id(), tlDialogue.format())) {
              mismatchedFormats.add(entry.id());
            }
            comments = commentGenerator.generate(entry, origDialogue, ctx);
            if (origDialogue.who() != null && !origDialogue.nameOnly() && !names.contains(origDialogue.who()) && !missingNames.contains(origDialogue.who())) {
              missingNames.add(origDialogue.who());
            }
          } else {
            msg.setMsgId(entry.originalText());
            msg.setMsgstr(entry.translatedText());
            comments = commentGenerator.generate(entry, null, ctx);
          }
          if (!comments.isEmpty()) {
            comments.forEach(msg::addExtractedComment);
          }
          if (msg.getMsgId() != null) {
            catalog.add(msg);
          }
        }
      }
    }
    return new Result(catalog, new Statements(plain, dialogue), mismatchedFormats, missingNames);
  }

  /**
   * The result of converting <code>.rpy</code> files into a <code>.po/.pot</code> file.
   * @param statements All statements found in the Ren'Py file
   * @param mismatchedFormats The IDs ({@link TranslationEntry#id()}) of all entries that did not match the format found
   *                          in {@link #validateStatements()}. However, if <code>null</code>, this will be empty.
   * @param missingNames All Ren'Py character identifiers that could not be found in {@link #names()}
   */
  public record Result(Catalog catalog,
                       Statements statements,
                       List<String> mismatchedFormats,
                       List<String> missingNames) {
  }

}


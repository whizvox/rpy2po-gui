package me.whizvox.rpy2po.rpytl;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TranslationFile implements Iterable<TranslationEntry> {

  private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd kk:mm:ss");

  private final List<TranslationEntry> entries;

  private TranslationFile(List<TranslationEntry> entries) {
    this.entries = entries;
  }

  public TranslationFile() {
    this(new ArrayList<>());
  }

  @Override
  public Iterator<TranslationEntry> iterator() {
    return entries.iterator();
  }

  public Stream<TranslationEntry> stream() {
    return entries.stream();
  }

  /**
   * Get the language that is being translated into in this file.
   * @param exhaustive Whether to perform an exhaustive search and check that all entries are translating into the same
   *                   language. If this is false, only the language of the first entry is returned.
   * @return The language of this file (i.e. <code>en</code>, <code>fr</code>, <code>zh_hans</code>), or
   * <code>null</code> if there are no entries
   * @throws IllegalStateException If <code>exhaustive</code> is true and there is at least one entry with a different
   * language in this file
   * @see #getLanguage()
   */
  public String getLanguage(boolean exhaustive) {
    if (entries.isEmpty()) {
      return null;
    }
    String lang = entries.getFirst().language();
    if (exhaustive && stream().anyMatch(entry -> !entry.language().equals(lang))) {
      throw new IllegalStateException("Multiple languages found in file");
    }
    return lang;
  }

  /**
   * Get the language that is being translated into in this file. Unlike {@link #getLanguage(boolean)}, this will
   * not perform an exhaustive search and only return the first entry's language.
   * @return The language of this file (i.e. <code>en</code>, <code>fr</code>, <code>zh_hans</code>), or
   * <code>null</code> if there are no entries
   */
  public String getLanguage() {
    return getLanguage(false);
  }

  public void write(OutputStream out, boolean includeTimestamp) throws IOException {
    try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8))) {
      if (includeTimestamp) {
        writer.write("# Translation saved " + TIMESTAMP_FORMAT.format(LocalDateTime.now()));
        writer.newLine();
        writer.newLine();
      }
      String stringsLang = null;
      for (TranslationEntry entry : this) {
        if (entry.isStatement()) {
          if (stringsLang != null) {
            stringsLang = null;
          }
          writer.write("# " + entry.file() + ":" + entry.line());
          writer.newLine();
          writer.write("translate " + entry.language() + " " + entry.id() + ":");
          writer.newLine();
          writer.newLine();
          for (String line : entry.originalText().split("\n")) {
            writer.write("    # " + line.trim());
            writer.newLine();
          }
          for (String line : entry.translatedText().split("\n")) {
            writer.write("    " + line.trim());
            writer.newLine();
          }
        } else {
          if (stringsLang == null || !stringsLang.equals(entry.language())) {
            stringsLang = entry.language();
            writer.write("translate " + stringsLang + " strings:");
            writer.newLine();
            writer.newLine();
          }
          writer.write("    # " + entry.file() + ":" + entry.line());
          writer.newLine();
          writer.write("    old \"" + entry.originalText() + "\"");
          writer.newLine();
          writer.write("    new \"" + entry.translatedText() + "\"");
          writer.newLine();
        }
        writer.newLine();
      }
    }
  }

  public void sort() {
    entries.sort((o1, o2) -> Comparator.comparing(TranslationEntry::file).thenComparing(TranslationEntry::line).compare(o1, o2));
  }

  public void add(TranslationEntry entry) {
    entries.add(entry);
  }

  public void remove(TranslationEntry entry) {
    entries.remove(entry);
  }

  public void remove(Collection<TranslationEntry> entries) {
    this.entries.removeAll(entries);
  }

  public void clear() {
    entries.clear();
  }

  private static final Pattern
      PATTERN_GENERIC_OCCURRENCE  = Pattern.compile("^ *# (.+\\.rpy):(\\d+)$"),
      PATTERN_DIALOGUE_HEADER     = Pattern.compile("^translate (.+) (.+):$"),
      PATTERN_DIALOGUE_ORIGINAL   = Pattern.compile("^ {4}# (.*)$"),
      PATTERN_DIALOGUE_TRANSLATED = Pattern.compile("^ {4}(.*)$"),
      PATTERN_STRINGS_HEADER      = Pattern.compile("^translate (.+) strings:$"),
      PATTERN_STRINGS_ORIGINAL    = Pattern.compile("^ {4}old \"(.*)\"$"),
      PATTERN_STRINGS_TRANSLATED  = Pattern.compile("^ {4}new \"(.*)\"$");

  public static TranslationFile read(InputStream in) throws IOException {
    var entries = new ArrayList<TranslationEntry>();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
      String lang = null;
      String id = null;
      String orig = null;
      String translated = null;
      String file = null;
      int srcLine = 0;
      int lineNum = 0;
      String line;
      while ((line = reader.readLine()) != null) {
        lineNum++;
        if (line.isEmpty()) {
          continue;
        }
        if (line.startsWith("\uFEFF")) {
          line = line.substring(1);
        }
        Matcher m;
        if ((m = PATTERN_GENERIC_OCCURRENCE.matcher(line)).find()) {
          if (file != null && orig != null) {
            entries.add(new TranslationEntry(id, lang, orig, translated, file, srcLine));
            orig = null;
            translated = null;
          }
          file = m.group(1);
          try {
            srcLine = Integer.parseInt(m.group(2));
          } catch (NumberFormatException e) {
            throw new IOException("Error parsing translation file at line " + lineNum + ": Line number is not a valid int");
          }
        } else if ((m = PATTERN_STRINGS_HEADER.matcher(line)).find()) {
          if (file != null) {
            entries.add(new TranslationEntry(id, lang, orig, translated, file, srcLine));
            orig = null;
            translated = null;
            file = null;
          }
          lang = m.group(1);
          id = null;
        } else if ((m = PATTERN_STRINGS_ORIGINAL.matcher(line)).find()) {
          orig = m.group(1);
        } else if ((m = PATTERN_STRINGS_TRANSLATED.matcher(line)).find()) {
          translated = m.group(1);
        } else if ((m = PATTERN_DIALOGUE_HEADER.matcher(line)).find()) {
          lang = m.group(1);
          id = m.group(2);
        } else if ((m = PATTERN_DIALOGUE_ORIGINAL.matcher(line)).find()) {
          if (orig == null) {
            orig = m.group(1);
          } else {
            orig += "\n" + m.group(1);
          }
        } else if ((m = PATTERN_DIALOGUE_TRANSLATED.matcher(line)).find()) {
          if (translated == null) {
            translated = m.group(1);
          } else {
            translated += "\n" + m.group(1);
          }
        } else if (!line.startsWith("#")) {
          throw new IOException("Line " + lineNum + ": Invalid syntax -- " + line);
        }
      }
      if (translated != null) {
        entries.add(new TranslationEntry(id, lang, orig, translated, file, srcLine));
      }
    }
    return new TranslationFile(entries);
  }

}

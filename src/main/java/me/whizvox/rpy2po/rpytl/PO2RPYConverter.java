package me.whizvox.rpy2po.rpytl;

import com.soberlemur.potentilla.Catalog;
import com.soberlemur.potentilla.PoParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public record PO2RPYConverter(String language,
                              Path input,
                              DialogueFormats formats) {

  private static final Logger LOGGER = LoggerFactory.getLogger(PO2RPYConverter.class);

  public Map<String, TranslationFile> convert() throws IOException {
    Map<String, TranslationFile> files = new HashMap<>();
    Catalog catalog = new PoParser().parseCatalog(input.toFile());
    LOGGER.info("Finished parsing catalog at {}", input);
    catalog.forEach(msg -> {
      if (!msg.isObsolete()) {
        try {
          TranslationEntry entry = formats.format(msg, language);
          files.computeIfAbsent(entry.file(), k -> new TranslationFile()).add(entry);
        } catch (IllegalArgumentException e) {
          LOGGER.error("Found malformed message: {}", msg, e);
        }
      }
    });
    return files;
  }

  public Map<String, Exception> write(Map<String, TranslationFile> files, Path outputDir) {
    Map<String, Exception> exceptions = new HashMap<>();
    files.forEach((filePath, tlFile) -> {
      // don't use game directory
      if (filePath.startsWith("game/")) {
        filePath = filePath.substring(5);
      }
      Path path = outputDir.resolve(filePath).normalize().toAbsolutePath();
      try {
        Files.createDirectories(path.getParent());
        try (OutputStream out = Files.newOutputStream(path)) {
          tlFile.write(out, true);
          LOGGER.info("Successfully wrote Ren'Py translation file to {}", path);
        }
      } catch (IOException e) {
        exceptions.put(filePath, e);
      }
    });
    if (!exceptions.isEmpty()) {
      LOGGER.error("Could not write {} Ren'Py translation files:", exceptions.size());
      exceptions.keySet().stream()
          .sorted()
          .forEach(filePath -> LOGGER.error(filePath, exceptions.get(filePath)));
      return exceptions;
    }
    return Map.of();
  }

}

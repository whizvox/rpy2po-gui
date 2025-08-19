package me.whizvox.rpy2po.rpytl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class RenPyUtil {

  public static List<Path> scanForTranslationFiles(Path projectDir, String language, boolean includeCommon) throws IOException {
    List<Path> output = new ArrayList<>();
    try (Stream<Path> walk = Files.walk(projectDir.resolve("game").resolve("tl").resolve(language))) {
      walk.filter(path -> {
        if (!includeCommon && path.endsWith("common.rpy")) {
          return false;
        }
        return path.toString().endsWith(".rpy");
      }).forEach(output::add);
    }
    return Collections.unmodifiableList(output);
  }

  public static List<Path> scanForTranslationFiles(Path projectDir, String language) throws IOException {
    return scanForTranslationFiles(projectDir, language, false);
  }

}

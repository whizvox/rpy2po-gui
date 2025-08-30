package me.whizvox.rpy2po.core;

import com.fasterxml.jackson.annotation.JsonIncludeProperties;
import me.whizvox.rpy2po.rpytl.CharacterNames;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@JsonIncludeProperties({"name", "renPyProjectDirectory", "primaryLanguage", "outputLanguages", "names", "includedFiles",
    "excludedFiles", "lastOpened"})
public class Profile {

  private Path baseDirectory;
  private String name;
  private Path renPyProjectDirectory;
  private String primaryLanguage;
  private List<String> outputLanguages;
  private CharacterNames names;
  private List<String> includedFiles;
  private List<String> excludedFiles;
  private LocalDateTime lastOpened;

  private Profile(Path baseDirectory, String name, Path renPyProjectDir, String primaryLanguage, List<String> outputLanguages, CharacterNames names, List<String> includedFiles, List<String> excludedFiles, LocalDateTime lastOpened) {
    this.baseDirectory = baseDirectory;
    this.name = name;
    this.renPyProjectDirectory = renPyProjectDir;
    this.primaryLanguage = primaryLanguage;
    this.outputLanguages = outputLanguages;
    this.names = names;
    this.includedFiles = includedFiles;
    this.excludedFiles = excludedFiles;
    this.lastOpened = lastOpened;
  }

  public Profile(Path baseDirectory, String name, Path renPyProjectDir, String primaryLang, LocalDateTime lastOpened) {
    this(baseDirectory, name, renPyProjectDir, primaryLang, List.of(), new CharacterNames(Map.of()), List.of(), List.of(), lastOpened);
  }

  public Profile() {
    this(null, "", null, "en", List.of(), new CharacterNames(Map.of()), List.of(), List.of(), null);
  }

  public Path getBaseDirectory() {
    return baseDirectory;
  }

  public String getName() {
    return name;
  }

  public Path getRenPyProjectDirectory() {
    return renPyProjectDirectory;
  }

  public String getPrimaryLanguage() {
    return primaryLanguage;
  }

  public List<String> getOutputLanguages() {
    return outputLanguages;
  }

  public CharacterNames getNames() {
    return names;
  }

  public List<String> getIncludedFiles() {
    return includedFiles;
  }

  public List<String> getExcludedFiles() {
    return excludedFiles;
  }

  public LocalDateTime getLastOpened() {
    return lastOpened;
  }

  public void setProfileDirectory(Path dir) {
    this.baseDirectory = dir;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setRenPyProjectDirectory(Path renPyProjectDirectory) {
    this.renPyProjectDirectory = renPyProjectDirectory;
  }

  public void setPrimaryLanguage(String primaryLanguage) {
    this.primaryLanguage = primaryLanguage;
  }

  public void setOutputLanguages(List<String> outputLangs) {
    this.outputLanguages = outputLangs;
  }

  public void setNames(CharacterNames names) {
    this.names = names;
  }

  public void setIncludedFiles(List<String> includedFiles) {
    this.includedFiles = Collections.unmodifiableList(includedFiles);
  }

  public void setExcludedFiles(List<String> excludedFiles) {
    this.excludedFiles = Collections.unmodifiableList(excludedFiles);
  }

  public void setLastOpened(LocalDateTime lastOpened) {
    this.lastOpened = lastOpened;
  }

  public Path getFile() {
    return baseDirectory.resolve("profile.json");
  }

  public Path getStatementsFile() {
    return baseDirectory.resolve("statements.json");
  }

  public Path getTemplateFile() {
    return baseDirectory.resolve(primaryLanguage + ".pot");
  }

  public Path getLanguageFile(String lang) {
    return baseDirectory.resolve("lang/" + lang + ".po").normalize();
  }

  public Path getStagingDirectory() {
    return baseDirectory.resolve("stage");
  }

  public Path getStagedLanguageDirectory(String lang) {
    return getStagingDirectory().resolve(lang);
  }

  public Path getTranslationDirectory(String language) {
    return renPyProjectDirectory.resolve("game").resolve("tl").resolve(language);
  }

  public List<Path> getTranslationFiles(String language) {
    Path tlDir = getTranslationDirectory(language);
    return includedFiles.stream().map(tlDir::resolve).toList();
  }

}

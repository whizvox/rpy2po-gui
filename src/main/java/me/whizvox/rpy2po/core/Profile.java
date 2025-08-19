package me.whizvox.rpy2po.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import me.whizvox.rpy2po.rpytl.CharacterNames;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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

  @JsonIgnore
  public Path getFile() {
    return baseDirectory.resolve("profile.json");
  }

  public Path getTranslationDirectory(String language) {
    return renPyProjectDirectory.resolve("game").resolve("tl").resolve(language);
  }

  public List<Path> getTranslationFiles(String language) {
    Path tlDir = getTranslationDirectory(language);
    return includedFiles.stream().map(tlDir::resolve).toList();
  }

  /*public static final JsonSerializer<Profile> SERIALIZER = new JsonSerializer<>() {
    @Override
    public void serialize(Profile profile, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      gen.writeStartObject();
      {
        gen.writeStringField("name", profile.name);
        gen.writeStringField("baseDir", profile.baseDir.toAbsolutePath().normalize().toString());
        gen.writeStringField("renPyProjectDir", profile.renPyProjectDir.toAbsolutePath().normalize().toString());
        gen.writeStringField("primaryLang", profile.primaryLang);
        gen.writeFieldName("outputLangs");
        gen.writeArray(profile.outputLangs.toArray(String[]::new), 0, profile.outputLangs.size());
        gen.writeObjectField("names", profile.names);
        gen.writeFieldName("includedFiles");
        gen.writeArray(profile.includedFiles.toArray(String[]::new), 0, profile.includedFiles.size());
        gen.writeFieldName("excludedFiles");
        gen.writeArray(profile.excludedFiles.toArray(String[]::new), 0, profile.excludedFiles.size());
        gen.writeNumberField("lastOpened", profile.lastOpened.toEpochSecond(ZoneOffset.UTC));
      }
      gen.writeEndObject();
    }
  };

  public static final JsonDeserializer<Profile> DESERIALIZER = new JsonDeserializer<>() {
    @Override
    public Profile deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      JsonNode n = p.getCodec().readTree(p);
      String name = n.get("name").asText();
      String baseDir = n.get("baseDir").asText();
      String renPyProjectDir = n.get("renPyProjectDir").asText();
      String primaryLang = n.get("primaryLang").asText();
      ArrayNode outputLangsNode = (ArrayNode) n.get("outputLangs");
      List<String> outputLangs = new ArrayList<>();
      for (JsonNode langNode : outputLangsNode) {
        outputLangs.add(langNode.asText());
      }
      ObjectNode namesNode = (ObjectNode) n.get("names");
      CharacterNames names = namesNode.traverse().readValueAsTree();
      long lastOpenedSeconds = n.get("lastOpened").asLong();
      return new Profile(Paths.get(baseDir), name, Paths.get(renPyProjectDir), primaryLang, outputLangs, LocalDateTime.ofEpochSecond(lastOpenedSeconds, 0, ZoneOffset.UTC));
    }
  };*/

}

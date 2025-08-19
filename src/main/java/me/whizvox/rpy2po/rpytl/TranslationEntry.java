package me.whizvox.rpy2po.rpytl;

public record TranslationEntry(String id,
                               String language,
                               String originalText,
                               String translatedText,
                               String file,
                               int line) {

  public boolean isDialogue() {
    return id != null;
  }

  public Dialogue parseOriginalDialogue() {
    return Dialogue.parse(originalText);
  }

  public Dialogue parseTranslatedDialogue() {
    return Dialogue.parse(translatedText);
  }

}

package me.whizvox.rpy2po.rpytl;

import java.util.Collections;
import java.util.Map;

public record CharacterNames(Map<String, String> names,
                             String narrator,
                             String speakFormat) {

  private static final String NARRATOR_SHORTHAND = "@";

  public CharacterNames(Map<String, String> names, String narrator, String speakFormat) {
    this.names = Collections.unmodifiableMap(names);
    this.narrator = narrator;
    this.speakFormat = speakFormat;
  }

  public CharacterNames(Map<String, String> names) {
    this(names, "Narrator", "%s speaking");
  }

  public boolean contains(String who) {
    return names.containsKey(who);
  }

  public String get(String who) {
    if (who == null || who.equals(NARRATOR_SHORTHAND)) {
      return narrator;
    }
    return names.get(who);
  }

  public String formatSpeaker(String speaker) {
    return speakFormat.formatted(speaker);
  }

}

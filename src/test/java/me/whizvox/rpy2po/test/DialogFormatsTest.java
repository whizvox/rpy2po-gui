package me.whizvox.rpy2po.test;

import com.soberlemur.potentilla.Message;
import me.whizvox.rpy2po.rpytl.DialogueFormats;
import me.whizvox.rpy2po.rpytl.TranslationEntry;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DialogFormatsTest {

  private static final DialogueFormats FORMATS = new DialogueFormats(Map.ofEntries(
      // name only
      Map.entry("abc_100", "\"[who]\" \"[what]\""),
      Map.entry("abc_101", "\"[who]\" \"[what]\" nointeract"),
      Map.entry("abc_102", "\"[who]\" \"[what]\" with vpunch"),
      Map.entry("abc_103", "\"[who]\" \"[what]\" (who_color=\"#000\")"),
      Map.entry("abc_104", "\"[who]\" \"[what]\" (who_color=\"#000\") with SomeTransition(\"some args\")"),
      // name only multiline
      Map.entry("abc_110", "\"[who]\" \"[what]\"\nnvl clear"),
      Map.entry("abc_111", "nvl clear\n\"[who]\" \"[what]\""),
      Map.entry("abc_112", "\"[who]\" \"[what]\" with vpunch\nnvl clear"),
      Map.entry("abc_113", "nvl clear\n\"[who]\" \"[what]\" with vpunch"),
      Map.entry("abc_114", "\"[who]\" \"[what]\" (who_color=\"#000\")\nnvl clear"),
      Map.entry("abc_115", "nvl clear\n\"[who]\" \"[what]\" (who_color=\"#000\")"),
      Map.entry("abc_116", "\"[who]\" \"[what]\" (who_color=\"#000\") with SomeTransition(\"some args\")\nnvl clear"),
      Map.entry("abc_117", "nvl clear\n\"[who]\" \"[what]\" (who_color=\"#000\") with SomeTransition(\"some args\")"),
      // character
      Map.entry("abc_120", "mc \"[what]\""),
      Map.entry("abc_121", "mc \"[what]\" nointeract"),
      Map.entry("abc_122", "mc \"[what]\" with vpunch"),
      Map.entry("abc_123", "mc \"[what]\" (who_color=\"#000\")"),
      Map.entry("abc_124", "mc \"[what]\" (who_color=\"#000\") with SomeTransition(\"some args\")"),
      // character multiline
      Map.entry("abc_130", "mc \"[what]\"\nnvl clear"),
      Map.entry("abc_131", "nvl clear\nmc \"[what]\""),
      Map.entry("abc_132", "mc \"[what]\" with vpunch\nnvl clear"),
      Map.entry("abc_133", "nvl clear\nmc \"[what]\" with vpunch"),
      Map.entry("abc_134", "mc \"[what]\" (who_color=\"#000\")\nnvl clear"),
      Map.entry("abc_135", "nvl clear\nmc \"[what]\" (who_color=\"#000\")"),
      Map.entry("abc_136", "mc \"[what]\" (who_color=\"#000\") with SomeTransition(\"some args\")\nnvl clear"),
      Map.entry("abc_137", "nvl clear\nmc \"[what]\" (who_color=\"#000\") with SomeTransition(\"some args\")"),
      // narration
      Map.entry("abc_140", "\"[what]\""),
      Map.entry("abc_141", "\"[what]\" nointeract"),
      Map.entry("abc_142", "\"[what]\" with vpunch"),
      Map.entry("abc_143", "\"[what]\" (who_color=\"#000\")"),
      Map.entry("abc_144", "\"[what]\" (who_color=\"#000\") with SomeTransition(\"some args\")"),
      // narration multiline
      Map.entry("abc_150", "\"[what]\"\nnvl clear"),
      Map.entry("abc_151", "nvl clear\n\"[what]\""),
      Map.entry("abc_152", "\"[what]\" with vpunch\nnvl clear"),
      Map.entry("abc_153", "nvl clear\n\"[what]\" with vpunch"),
      Map.entry("abc_154", "\"[what]\" (who_color=\"#000\")\nnvl clear"),
      Map.entry("abc_155", "nvl clear\n\"[what]\" (who_color=\"#000\")"),
      Map.entry("abc_156", "\"[what]\" (who_color=\"#000\") with SomeTransition(\"some args\")\nnvl clear"),
      Map.entry("abc_157", "nvl clear\n\"[what]\" (who_color=\"#000\") with SomeTransition(\"some args\")"),
      // nvl clear
      Map.entry("abc_160", "nvl clear")
  ));

  private static Message getNameOnlyMessage(String ctx) {
    Message msg = new Message();
    msg.getSourceReferences().add("script.rpy:456");
    msg.setMsgContext(ctx);
    msg.setMsgId("Main Character :: Hello there.");
    msg.setMsgstr("Personaje Principal :: Hola.");
    return msg;
  }

  private static Message getNormalMessage(String ctx) {
    Message msg = new Message();
    msg.getSourceReferences().add("script.rpy:789");
    msg.setMsgContext(ctx);
    msg.setMsgId("Hello there.");
    msg.setMsgstr("Hola.");
    return msg;
  }

  @Test
  void format_nameOnly() {
    assertEquals(new TranslationEntry("abc_100", "es", "\"Main Character\" \"Hello there.\"", "\"Personaje Principal\" \"Hola.\"", "script.rpy", 456), FORMATS.format(getNameOnlyMessage("abc_100"), "es"));
    assertEquals(new TranslationEntry("abc_101", "es", "\"Main Character\" \"Hello there.\" nointeract", "\"Personaje Principal\" \"Hola.\" nointeract", "script.rpy", 456), FORMATS.format(getNameOnlyMessage("abc_101"), "es"));
    assertEquals(new TranslationEntry("abc_102", "es", "\"Main Character\" \"Hello there.\" with vpunch", "\"Personaje Principal\" \"Hola.\" with vpunch", "script.rpy", 456), FORMATS.format(getNameOnlyMessage("abc_102"), "es"));
    assertEquals(new TranslationEntry("abc_103", "es", "\"Main Character\" \"Hello there.\" (who_color=\"#000\")", "\"Personaje Principal\" \"Hola.\" (who_color=\"#000\")", "script.rpy", 456), FORMATS.format(getNameOnlyMessage("abc_103"), "es"));
    assertEquals(new TranslationEntry("abc_104", "es", "\"Main Character\" \"Hello there.\" (who_color=\"#000\") with SomeTransition(\"some args\")", "\"Personaje Principal\" \"Hola.\" (who_color=\"#000\") with SomeTransition(\"some args\")", "script.rpy", 456), FORMATS.format(getNameOnlyMessage("abc_104"), "es"));
  }

  @Test
  void format_nameOnly_multiline() {
    assertEquals(new TranslationEntry("abc_110", "es", "\"Main Character\" \"Hello there.\"\nnvl clear", "\"Personaje Principal\" \"Hola.\"\nnvl clear", "script.rpy", 456), FORMATS.format(getNameOnlyMessage("abc_110"), "es"));
    assertEquals(new TranslationEntry("abc_111", "es", "nvl clear\n\"Main Character\" \"Hello there.\"", "nvl clear\n\"Personaje Principal\" \"Hola.\"", "script.rpy", 456), FORMATS.format(getNameOnlyMessage("abc_111"), "es"));
    assertEquals(new TranslationEntry("abc_112", "es", "\"Main Character\" \"Hello there.\" with vpunch\nnvl clear", "\"Personaje Principal\" \"Hola.\" with vpunch\nnvl clear", "script.rpy", 456), FORMATS.format(getNameOnlyMessage("abc_112"), "es"));
    assertEquals(new TranslationEntry("abc_113", "es", "nvl clear\n\"Main Character\" \"Hello there.\" with vpunch", "nvl clear\n\"Personaje Principal\" \"Hola.\" with vpunch", "script.rpy", 456), FORMATS.format(getNameOnlyMessage("abc_113"), "es"));
    assertEquals(new TranslationEntry("abc_114", "es", "\"Main Character\" \"Hello there.\" (who_color=\"#000\")\nnvl clear", "\"Personaje Principal\" \"Hola.\" (who_color=\"#000\")\nnvl clear", "script.rpy", 456), FORMATS.format(getNameOnlyMessage("abc_114"), "es"));
    assertEquals(new TranslationEntry("abc_115", "es", "nvl clear\n\"Main Character\" \"Hello there.\" (who_color=\"#000\")", "nvl clear\n\"Personaje Principal\" \"Hola.\" (who_color=\"#000\")", "script.rpy", 456), FORMATS.format(getNameOnlyMessage("abc_115"), "es"));
    assertEquals(new TranslationEntry("abc_116", "es", "\"Main Character\" \"Hello there.\" (who_color=\"#000\") with SomeTransition(\"some args\")\nnvl clear", "\"Personaje Principal\" \"Hola.\" (who_color=\"#000\") with SomeTransition(\"some args\")\nnvl clear", "script.rpy", 456), FORMATS.format(getNameOnlyMessage("abc_116"), "es"));
    assertEquals(new TranslationEntry("abc_117", "es", "nvl clear\n\"Main Character\" \"Hello there.\" (who_color=\"#000\") with SomeTransition(\"some args\")", "nvl clear\n\"Personaje Principal\" \"Hola.\" (who_color=\"#000\") with SomeTransition(\"some args\")", "script.rpy", 456), FORMATS.format(getNameOnlyMessage("abc_117"), "es"));
  }

  @Test
  void format_character() {
    assertEquals(new TranslationEntry("abc_120", "es", "mc \"Hello there.\"", "mc \"Hola.\"", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_120"), "es"));
    assertEquals(new TranslationEntry("abc_121", "es", "mc \"Hello there.\" nointeract", "mc \"Hola.\" nointeract", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_121"), "es"));
    assertEquals(new TranslationEntry("abc_122", "es", "mc \"Hello there.\" with vpunch", "mc \"Hola.\" with vpunch", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_122"), "es"));
    assertEquals(new TranslationEntry("abc_123", "es", "mc \"Hello there.\" (who_color=\"#000\")", "mc \"Hola.\" (who_color=\"#000\")", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_123"), "es"));
    assertEquals(new TranslationEntry("abc_124", "es", "mc \"Hello there.\" (who_color=\"#000\") with SomeTransition(\"some args\")", "mc \"Hola.\" (who_color=\"#000\") with SomeTransition(\"some args\")", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_124"), "es"));
  }

  @Test
  void format_character_multiline() {
    assertEquals(new TranslationEntry("abc_130", "es", "mc \"Hello there.\"\nnvl clear", "mc \"Hola.\"\nnvl clear", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_130"), "es"));
    assertEquals(new TranslationEntry("abc_131", "es", "nvl clear\nmc \"Hello there.\"", "nvl clear\nmc \"Hola.\"", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_131"), "es"));
    assertEquals(new TranslationEntry("abc_132", "es", "mc \"Hello there.\" with vpunch\nnvl clear", "mc \"Hola.\" with vpunch\nnvl clear", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_132"), "es"));
    assertEquals(new TranslationEntry("abc_133", "es", "nvl clear\nmc \"Hello there.\" with vpunch", "nvl clear\nmc \"Hola.\" with vpunch", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_133"), "es"));
    assertEquals(new TranslationEntry("abc_134", "es", "mc \"Hello there.\" (who_color=\"#000\")\nnvl clear", "mc \"Hola.\" (who_color=\"#000\")\nnvl clear", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_134"), "es"));
    assertEquals(new TranslationEntry("abc_135", "es", "nvl clear\nmc \"Hello there.\" (who_color=\"#000\")", "nvl clear\nmc \"Hola.\" (who_color=\"#000\")", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_135"), "es"));
    assertEquals(new TranslationEntry("abc_136", "es", "mc \"Hello there.\" (who_color=\"#000\") with SomeTransition(\"some args\")\nnvl clear", "mc \"Hola.\" (who_color=\"#000\") with SomeTransition(\"some args\")\nnvl clear", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_136"), "es"));
    assertEquals(new TranslationEntry("abc_137", "es", "nvl clear\nmc \"Hello there.\" (who_color=\"#000\") with SomeTransition(\"some args\")", "nvl clear\nmc \"Hola.\" (who_color=\"#000\") with SomeTransition(\"some args\")", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_137"), "es"));
  }

  @Test
  void format_narration() {
    assertEquals(new TranslationEntry("abc_140", "es", "\"Hello there.\"", "\"Hola.\"", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_140"), "es"));
    assertEquals(new TranslationEntry("abc_141", "es", "\"Hello there.\" nointeract", "\"Hola.\" nointeract", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_141"), "es"));
    assertEquals(new TranslationEntry("abc_142", "es", "\"Hello there.\" with vpunch", "\"Hola.\" with vpunch", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_142"), "es"));
    assertEquals(new TranslationEntry("abc_143", "es", "\"Hello there.\" (who_color=\"#000\")", "\"Hola.\" (who_color=\"#000\")", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_143"), "es"));
    assertEquals(new TranslationEntry("abc_144", "es", "\"Hello there.\" (who_color=\"#000\") with SomeTransition(\"some args\")", "\"Hola.\" (who_color=\"#000\") with SomeTransition(\"some args\")", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_144"), "es"));
  }

  @Test
  void format_narration_multiline() {
    assertEquals(new TranslationEntry("abc_150", "es", "\"Hello there.\"\nnvl clear", "\"Hola.\"\nnvl clear", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_150"), "es"));
    assertEquals(new TranslationEntry("abc_151", "es", "nvl clear\n\"Hello there.\"", "nvl clear\n\"Hola.\"", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_151"), "es"));
    assertEquals(new TranslationEntry("abc_152", "es", "\"Hello there.\" with vpunch\nnvl clear", "\"Hola.\" with vpunch\nnvl clear", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_152"), "es"));
    assertEquals(new TranslationEntry("abc_153", "es", "nvl clear\n\"Hello there.\" with vpunch", "nvl clear\n\"Hola.\" with vpunch", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_153"), "es"));
    assertEquals(new TranslationEntry("abc_154", "es", "\"Hello there.\" (who_color=\"#000\")\nnvl clear", "\"Hola.\" (who_color=\"#000\")\nnvl clear", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_154"), "es"));
    assertEquals(new TranslationEntry("abc_155", "es", "nvl clear\n\"Hello there.\" (who_color=\"#000\")", "nvl clear\n\"Hola.\" (who_color=\"#000\")", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_155"), "es"));
    assertEquals(new TranslationEntry("abc_156", "es", "\"Hello there.\" (who_color=\"#000\") with SomeTransition(\"some args\")\nnvl clear", "\"Hola.\" (who_color=\"#000\") with SomeTransition(\"some args\")\nnvl clear", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_156"), "es"));
    assertEquals(new TranslationEntry("abc_157", "es", "nvl clear\n\"Hello there.\" (who_color=\"#000\") with SomeTransition(\"some args\")", "nvl clear\n\"Hola.\" (who_color=\"#000\") with SomeTransition(\"some args\")", "script.rpy", 789), FORMATS.format(getNormalMessage("abc_157"), "es"));
  }

}

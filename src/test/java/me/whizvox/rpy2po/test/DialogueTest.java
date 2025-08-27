package me.whizvox.rpy2po.test;

import me.whizvox.rpy2po.rpytl.Dialogue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DialogueTest {

  @Test
  void parse_nameOnly() {
    assertEquals(new Dialogue("Main Character", true, "Hello there.", "\"[who]\" \"[what]\""), Dialogue.parse("\"Main Character\" \"Hello there.\""));
    assertEquals(new Dialogue("Main Character", true, "Hello there.", "\"[who]\" \"[what]\" nointeract"), Dialogue.parse("\"Main Character\" \"Hello there.\" nointeract"));
    assertEquals(new Dialogue("Main Character", true, "Hello there.", "\"[who]\" \"[what]\" with vpunch"), Dialogue.parse("\"Main Character\" \"Hello there.\" with vpunch"));
    assertEquals(new Dialogue("Main Character", true, "Hello there.", "\"[who]\" \"[what]\" (who_color=\"#000\")"), Dialogue.parse("\"Main Character\" \"Hello there.\" (who_color=\"#000\")"));
    assertEquals(new Dialogue("Main Character", true, "Hello there.", "\"[who]\" \"[what]\" (who_color=\"#000\") with SomeTransition(\"some args\")"), Dialogue.parse("\"Main Character\" \"Hello there.\" (who_color=\"#000\") with SomeTransition(\"some args\")"));
  }

  @Test
  void parse_nameOnly_multiline() {
    assertEquals(new Dialogue("Main Character", true, "Hello there.", "\"[who]\" \"[what]\"\nnvl clear"), Dialogue.parse("\"Main Character\" \"Hello there.\"\nnvl clear"));
    assertEquals(new Dialogue("Main Character", true, "Hello there.", "nvl clear\n\"[who]\" \"[what]\""), Dialogue.parse("nvl clear\n\"Main Character\" \"Hello there.\""));
    assertEquals(new Dialogue("Main Character", true, "Hello there.", "\"[who]\" \"[what]\" with vpunch\nnvl clear"), Dialogue.parse("\"Main Character\" \"Hello there.\" with vpunch\nnvl clear"));
    assertEquals(new Dialogue("Main Character", true, "Hello there.", "nvl clear\n\"[who]\" \"[what]\" with vpunch"), Dialogue.parse("nvl clear\n\"Main Character\" \"Hello there.\" with vpunch"));
    assertEquals(new Dialogue("Main Character", true, "Hello there.", "\"[who]\" \"[what]\" (who_color=\"#000\")\nnvl clear"), Dialogue.parse("\"Main Character\" \"Hello there.\" (who_color=\"#000\")\nnvl clear"));
    assertEquals(new Dialogue("Main Character", true, "Hello there.", "nvl clear\n\"[who]\" \"[what]\" (who_color=\"#000\")"), Dialogue.parse("nvl clear\n\"Main Character\" \"Hello there.\" (who_color=\"#000\")"));
    assertEquals(new Dialogue("Main Character", true, "Hello there.", "\"[who]\" \"[what]\" (who_color=\"#000\") with SomeTransition(\"some args\")\nnvl clear"), Dialogue.parse("\"Main Character\" \"Hello there.\" (who_color=\"#000\") with SomeTransition(\"some args\")\nnvl clear"));
    assertEquals(new Dialogue("Main Character", true, "Hello there.", "nvl clear\n\"[who]\" \"[what]\" (who_color=\"#000\") with SomeTransition(\"some args\")"), Dialogue.parse("nvl clear\n\"Main Character\" \"Hello there.\" (who_color=\"#000\") with SomeTransition(\"some args\")"));
  }

  @Test
  void parse_character() {
    assertEquals(new Dialogue("mc", false, "Hello there.", "mc \"[what]\""), Dialogue.parse("mc \"Hello there.\""));
    assertEquals(new Dialogue("mc", false, "Hello there.", "mc \"[what]\" nointeract"), Dialogue.parse("mc \"Hello there.\" nointeract"));
    assertEquals(new Dialogue("mc", false, "Hello there.", "mc \"[what]\" with vpunch"), Dialogue.parse("mc \"Hello there.\" with vpunch"));
    assertEquals(new Dialogue("mc", false, "Hello there.", "mc \"[what]\" (who_color=\"#000\")"), Dialogue.parse("mc \"Hello there.\" (who_color=\"#000\")"));
    assertEquals(new Dialogue("mc", false, "Hello there.", "mc \"[what]\" (who_color=\"#000\") with SomeTransition(\"some args\")"), Dialogue.parse("mc \"Hello there.\" (who_color=\"#000\") with SomeTransition(\"some args\")"));
  }

  @Test
  void parse_character_multiline() {
    assertEquals(new Dialogue("mc", false, "Hello there.", "mc \"[what]\"\nnvl clear"), Dialogue.parse("mc \"Hello there.\"\nnvl clear"));
    assertEquals(new Dialogue("mc", false, "Hello there.", "nvl clear\nmc \"[what]\""), Dialogue.parse("nvl clear\nmc \"Hello there.\""));
    assertEquals(new Dialogue("mc", false, "Hello there.", "mc \"[what]\" with vpunch\nnvl clear"), Dialogue.parse("mc \"Hello there.\" with vpunch\nnvl clear"));
    assertEquals(new Dialogue("mc", false, "Hello there.", "nvl clear\nmc \"[what]\" with vpunch"), Dialogue.parse("nvl clear\nmc \"Hello there.\" with vpunch"));
    assertEquals(new Dialogue("mc", false, "Hello there.", "mc \"[what]\" (who_color=\"#000\")\nnvl clear"), Dialogue.parse("mc \"Hello there.\" (who_color=\"#000\")\nnvl clear"));
    assertEquals(new Dialogue("mc", false, "Hello there.", "nvl clear\nmc \"[what]\" (who_color=\"#000\")"), Dialogue.parse("nvl clear\nmc \"Hello there.\" (who_color=\"#000\")"));
    assertEquals(new Dialogue("mc", false, "Hello there.", "mc \"[what]\" (who_color=\"#000\") with SomeTransition(\"some args\")\nnvl clear"), Dialogue.parse("mc \"Hello there.\" (who_color=\"#000\") with SomeTransition(\"some args\")\nnvl clear"));
    assertEquals(new Dialogue("mc", false, "Hello there.", "nvl clear\nmc \"[what]\" (who_color=\"#000\") with SomeTransition(\"some args\")"), Dialogue.parse("nvl clear\nmc \"Hello there.\" (who_color=\"#000\") with SomeTransition(\"some args\")"));
  }

  @Test
  void parse_narration() {
    assertEquals(new Dialogue(null, false, "Hello there.", "\"[what]\""), Dialogue.parse("\"Hello there.\""));
    assertEquals(new Dialogue(null, false, "Hello there.", "\"[what]\" nointeract"), Dialogue.parse("\"Hello there.\" nointeract"));
    assertEquals(new Dialogue(null, false, "Hello there.", "\"[what]\" with vpunch"), Dialogue.parse("\"Hello there.\" with vpunch"));
    assertEquals(new Dialogue(null, false, "Hello there.", "\"[what]\" (who_color=\"#000\")"), Dialogue.parse("\"Hello there.\" (who_color=\"#000\")"));
    assertEquals(new Dialogue(null, false, "Hello there.", "\"[what]\" (who_color=\"#000\") with SomeTransition(\"some args\")"), Dialogue.parse("\"Hello there.\" (who_color=\"#000\") with SomeTransition(\"some args\")"));
  }

  @Test
  void parse_narration_multiline() {
    assertEquals(new Dialogue(null, false, "Hello there.", "\"[what]\"\nnvl clear"), Dialogue.parse("\"Hello there.\"\nnvl clear"));
    assertEquals(new Dialogue(null, false, "Hello there.", "nvl clear\n\"[what]\""), Dialogue.parse("nvl clear\n\"Hello there.\""));
    assertEquals(new Dialogue(null, false, "Hello there.", "\"[what]\" with vpunch\nnvl clear"), Dialogue.parse("\"Hello there.\" with vpunch\nnvl clear"));
    assertEquals(new Dialogue(null, false, "Hello there.", "nvl clear\n\"[what]\" with vpunch"), Dialogue.parse("nvl clear\n\"Hello there.\" with vpunch"));
    assertEquals(new Dialogue(null, false, "Hello there.", "\"[what]\" (who_color=\"#000\")\nnvl clear"), Dialogue.parse("\"Hello there.\" (who_color=\"#000\")\nnvl clear"));
    assertEquals(new Dialogue(null, false, "Hello there.", "nvl clear\n\"[what]\" (who_color=\"#000\")"), Dialogue.parse("nvl clear\n\"Hello there.\" (who_color=\"#000\")"));
    assertEquals(new Dialogue(null, false, "Hello there.", "\"[what]\" (who_color=\"#000\") with SomeTransition(\"some args\")\nnvl clear"), Dialogue.parse("\"Hello there.\" (who_color=\"#000\") with SomeTransition(\"some args\")\nnvl clear"));
    assertEquals(new Dialogue(null, false, "Hello there.", "nvl clear\n\"[what]\" (who_color=\"#000\") with SomeTransition(\"some args\")"), Dialogue.parse("nvl clear\n\"Hello there.\" (who_color=\"#000\") with SomeTransition(\"some args\")"));
  }

  @Test
  void parse_nonDialogue() {
    assertEquals(new Dialogue(null, false, null, "nvl clear"), Dialogue.parse("nvl clear"));
    assertEquals(new Dialogue(null, false, null, "khaskhdakjsdkj"), Dialogue.parse("khaskhdakjsdkj"));
    assertEquals(new Dialogue(null, false, null, "translate en strings:"), Dialogue.parse("translate en strings:"));
  }

}

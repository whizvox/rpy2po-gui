package me.whizvox.rpy2po.rpytl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A representation of a single line of Ren'Py dialogue code.
 * @param who Who is speaking, which could either be an identifier or a proper name if using a name-only character
 * @param nameOnly If this piece of dialogue is spoken by a name-only character
 * @param what What the character is saying
 * @param format The format of the line of code, which will contain a <code>[what]</code> token, and a
 *               <code>[who]</code> token if this is spoken by a name-only character. For name-only dialogue, the format
 *               will look something like this:
                 <pre>
                 "[who]" "[what]"
                 </pre>
 *               For normal speaking characters, the format will look something like this:
                 <pre>
                 obi "[what]"
                 </pre>
 *               Finally, for narration, the format will look something like this:
                 <pre>
                 "[what]"
                 </pre>
 */
public record Dialogue(String who,
                       boolean nameOnly,
                       String what,
                       String format) {



  // some additional properties that can be applied to Ren'Py dialogue statements
  // - nointeract: Only appears when Ren'Py generates translations for dialogue that appears during dialogue menus
  // - with <transition>: Can apply screen transitions
  // - (<property>=<value>): Can apply exceptional properties to a particular line of dialogue (i.e. who_color)
  private static final String OPTIONALS = "( nointeract)?(( with .+)+)?( \\(\\w+=.+\\)+)?";

  private static final Pattern
      PATTERN_NAME_ONLY = Pattern.compile("^\"(.+)\" \"(.*)\"" + OPTIONALS + "$", Pattern.MULTILINE),
      PATTERN_CHARACTER = Pattern.compile("^(.+) \"(.*)\"" + OPTIONALS + "$", Pattern.MULTILINE),
      PATTERN_NARRATION = Pattern.compile("^\"(.*)\"" + OPTIONALS + "$", Pattern.MULTILINE);

  private static final Logger LOGGER = LoggerFactory.getLogger(Dialogue.class);

  /**
   * Parse a line of Ren'Py dialogue code (i.e. <code>obi "Hello, there."</code>)
   * @param line The line of Ren'Py code
   * @return The dialogue object, or <code>null</code> if the line was not a valid line of Ren'Py dialogue code
   */
  public static Dialogue parse(String line) {
    String who;
    boolean nameOnly = false;
    String what;
    String format;
    Matcher m;
    if ((m = PATTERN_NAME_ONLY.matcher(line)).find()) {
      who = m.group(1);
      nameOnly = true;
      what = m.group(2);
      format = line.substring(0, m.start(1)) + "[who]" + line.substring(m.end(1), m.start(2)) + "[what]" + line.substring(m.end(2));
    } else if ((m = PATTERN_CHARACTER.matcher(line)).find()) {
      who = m.group(1);
      what = m.group(2);
      format = line.substring(0, m.start(2)) + "[what]" + line.substring(m.end(2));
    } else if ((m = PATTERN_NARRATION.matcher(line)).find()) {
      who = null;
      what = m.group(1);
      format = line.substring(0, m.start(1)) + "[what]" + line.substring(m.end(1));
    } else {
      who = null;
      what = null;
      format = line;
    }
    return new Dialogue(who, nameOnly, what, format);
  }

}

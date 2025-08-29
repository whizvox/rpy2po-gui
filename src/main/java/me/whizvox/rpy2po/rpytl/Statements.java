package me.whizvox.rpy2po.rpytl;

import com.soberlemur.potentilla.Message;
import me.whizvox.rpy2po.core.StringUtil;
import me.whizvox.rpy2po.gettext.SourceReference;

import java.util.Collections;
import java.util.Map;

public record Statements(Map<String, Statement> plain,
                         Map<String, String> dialogue) {

  public Statements(Map<String, Statement> plain, Map<String, String> dialogue) {
    this.plain = Collections.unmodifiableMap(plain);
    this.dialogue = Collections.unmodifiableMap(dialogue);
  }

  public boolean contains(String id) {
    return dialogue.containsKey(id);
  }

  public boolean matches(String id, String format) {
    String storedFormat = dialogue.get(id);
    if (storedFormat == null) {
      return false;
    }
    return storedFormat.equals(format);
  }

  public TranslationEntry format(Message msg, String language) {
    SourceReference ref = SourceReference.parse(msg.getSourceReferences().getFirst());
    if (msg.getMsgContext() == null) {
      return new TranslationEntry(null, language, msg.getMsgId(), msg.getMsgstr(), ref.file(), ref.line());
    }
    String format = dialogue.get(msg.getMsgContext());
    if (format == null) {
      throw new IllegalArgumentException("Invalid message, msgctxt does not correlate to any statement");
    }
    String original = doFormat(format, msg.getMsgId());
    String translated = doFormat(format, msg.getMsgstr());
    return new TranslationEntry(msg.getMsgContext(), language, original, translated, ref.file(), ref.line());
  }

  private static String doFormat(String format, String msg) {
    int index = msg.indexOf("::");
    if (index > 0) {
      String who = StringUtil.escape(msg.substring(0, index).trim());
      String what = StringUtil.escape(msg.substring(index + 2).trim());
      return format.replace("[who]", who).replace("[what]", what);
    }
    return format.replace("[what]", StringUtil.escape(msg));
  }

}

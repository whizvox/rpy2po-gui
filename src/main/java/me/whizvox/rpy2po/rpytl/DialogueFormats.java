package me.whizvox.rpy2po.rpytl;

import com.soberlemur.potentilla.Message;

import java.util.Collections;
import java.util.Map;

public record DialogueFormats(Map<String, String> formats) {

  public DialogueFormats(Map<String, String> formats) {
    this.formats = Collections.unmodifiableMap(formats);
  }

  public boolean contains(String id) {
    return formats.containsKey(id);
  }

  public boolean matches(String id, String format) {
    String storedFormat = formats.get(id);
    if (storedFormat == null) {
      return false;
    }
    return storedFormat.equals(format);
  }

  public TranslationEntry format(Message msg, String language) {
    String ref = msg.getSourceReferences().getFirst();
    int index = ref.indexOf(':');
    String file = ref.substring(0, index);
    int line = Integer.parseInt(ref.substring(index + 1));
    if (msg.getMsgContext() == null) {
      return new TranslationEntry(null, language, msg.getMsgId(), msg.getMsgstr(), file, line);
    }
    String format = formats.get(msg.getMsgContext());
    if (format == null) {
      throw new IllegalArgumentException("Invalid message, msgctxt does not correlate to any formats");
    }
    String original = doFormat(format, msg.getMsgId());
    String translated = doFormat(format, msg.getMsgContext());
    return new TranslationEntry(msg.getMsgContext(), language, original, translated, file, line);
  }

  private static String doFormat(String format, String msg) {
    int index = msg.indexOf("::");
    if (index > 0) {
      String who = msg.substring(0, index).trim();
      String what = msg.substring(index + 1).trim();
      return format.replace("[who]", who).replace("[what]", what);
    }
    return format.replace("[what]", msg);
  }

}

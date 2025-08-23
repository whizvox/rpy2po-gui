package me.whizvox.rpy2po.gettext;

import com.soberlemur.potentilla.Message;
import com.soberlemur.potentilla.MessageKey;
import me.whizvox.rpy2po.core.Pair;

import java.util.List;

public record UpdateProblem(MessageKey key,
                            List<Pair<Message, Float>> similar,
                            Type type) {

  public enum Type {
    HAS_CONFLICTS,
    NO_MATCHES,
    ABSENT_FROM_TEMPLATE
  }

}

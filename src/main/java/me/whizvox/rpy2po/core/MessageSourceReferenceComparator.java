package me.whizvox.rpy2po.core;

import com.soberlemur.potentilla.Message;
import me.whizvox.rpy2po.gettext.SourceReference;

import java.util.Comparator;

public class MessageSourceReferenceComparator implements Comparator<Message> {

  @SuppressWarnings("OptionalGetWithoutIsPresent")
  @Override
  public int compare(Message o1, Message o2) {
    if (o1.getSourceReferences().isEmpty() || o2.getSourceReferences().isEmpty()) {
      return 0;
    }
    SourceReference ref1;
    if (o1.getSourceReferences().size() == 1) {
      ref1 = SourceReference.parse(o1.getSourceReferences().getFirst());
    } else {
      ref1 = o1.getSourceReferences().stream().map(SourceReference::parse).sorted().findFirst().get();
    }
    SourceReference ref2;
    if (o2.getSourceReferences().size() == 1) {
      ref2 = SourceReference.parse(o2.getSourceReferences().getFirst());
    } else {
      ref2 = o2.getSourceReferences().stream().map(SourceReference::parse).sorted().findFirst().get();
    }
    return ref1.compareTo(ref2);
  }

}

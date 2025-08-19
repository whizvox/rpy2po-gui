package me.whizvox.rpy2po.rpytl;

import java.util.List;

public interface CommentGenerator {

  /**
   * Generate any comments to be put into a <code>.po</code> file
   * @param entry The translation entry
   * @param dialogue The associated dialogue. Will be <code>null</code> if the original entry is not dialogue.
   * @param ctx The translation context
   * @return A list of comments to be added to the <code>.po</code> file entry. If no comments should be added, the
   * list should be empty.
   */
  List<String> generate(TranslationEntry entry, Dialogue dialogue, TranslationContext ctx);

  CommentGenerator NONE = (entry, dialogue, ctx) -> List.of();

  CommentGenerator SPEAKING = (entry, dialogue, ctx) -> {
    if (dialogue == null) {
      return List.of();
    }
    if (dialogue.nameOnly()) {
      return List.of(ctx.names().formatSpeaker(dialogue.who()));
    }
    String speaker = ctx.names().get(dialogue.who());
    if (speaker == null) {
      return List.of();
    }
    return List.of(ctx.names().formatSpeaker(speaker));
  };

}

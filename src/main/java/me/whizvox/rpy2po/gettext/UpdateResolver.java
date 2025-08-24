package me.whizvox.rpy2po.gettext;

import com.soberlemur.potentilla.Catalog;
import com.soberlemur.potentilla.Message;
import com.soberlemur.potentilla.MessageKey;
import me.whizvox.rpy2po.core.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class UpdateResolver {

  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateResolver.class);

  private final Catalog template;
  private final Catalog other;
  private final Catalog result;
  private final List<UpdateProblem> problems;
  private final List<ProblemResolution> resolutions;

  private final Map<MessageKey, UpdateProblem> problemsMap;
  private final Map<MessageKey, ProblemResolution> resolutionsMap;

  public UpdateResolver(Catalog template, Catalog other, List<ProblemResolution> resolutions) {
    this.template = template;
    this.other = other;
    result = new Catalog();
    problems = new ArrayList<>();
    problemsMap = new HashMap<>();
    if (resolutions == null) {
      resolutions = List.of();
    }
    this.resolutions = new ArrayList<>(resolutions);
    resolutionsMap = new HashMap<>();
    resolutions.forEach(r -> resolutionsMap.put(r.templateKey(), r));
  }

  public Catalog getTemplate() {
    return template;
  }

  public Catalog getOther() {
    return other;
  }

  public Catalog getResult() {
    return result;
  }

  public List<UpdateProblem> getProblems() {
    return problems;
  }

  public List<ProblemResolution> getResolutions() {
    return resolutions;
  }

  public void addResolution(ProblemResolution resolution) {
    resolutions.add(resolution);
    resolutionsMap.put(resolution.templateKey(), resolution);
  }

  private void addProblem(UpdateProblem problem) {
    problems.add(problem);
    problemsMap.put(problem.key(), problem);
  }

  public static int distance(String a, String b) {
    a = a.toLowerCase();
    b = b.toLowerCase();
    // i == 0
    int [] costs = new int [b.length() + 1];
    for (int j = 0; j < costs.length; j++)
      costs[j] = j;
    for (int i = 1; i <= a.length(); i++) {
      // j == 0; nw = lev(i - 1, j)
      costs[0] = i;
      int nw = i - 1;
      for (int j = 1; j <= b.length(); j++) {
        int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
        nw = costs[j];
        costs[j] = cj;
      }
    }
    return costs[b.length()];
  }

  public void update() {
    List<Message> newMessages = new ArrayList<>();
    List<Message> noMatches = new ArrayList<>();
    List<Message> absentFromTemplate = new ArrayList<>();
    Set<MessageKey> foundMatch = new HashSet<>();
    LOGGER.info("Iterating through template file...");
    for (Message msg : template) {
      MessageKey key = new MessageKey(msg);
      Message newMsg = new Message();
      newMsg.getExtractedComments().addAll(msg.getExtractedComments());
      newMsg.getSourceReferences().addAll(msg.getSourceReferences());
      newMsg.setMsgContext(msg.getMsgContext());
      newMsg.setMsgId(msg.getMsgId());
      Message oldMsg = other.get(key);
      if (oldMsg == null) {
        ProblemResolution prevResolution = resolutionsMap.get(key);
        if (prevResolution == null) {
          // could be a new entry or an old entry that has an updated ID or text. it's impossible for updated entries
          // to have the same ID but different text (unless a future Ren'Py update causes shenanigans).
          List<Pair<Message, Float>> matches = new ArrayList<>();
          for (Message oMsg : other) {
            if ((oMsg.getMsgContext() == null) == (msg.getMsgContext() == null)) {
              if (oMsg.getMsgId().equals(msg.getMsgId())) {
                matches.add(new Pair<>(oMsg, 1.0F));
              }
            }
          }
          if (matches.size() == 1) {
            Message oMsg = matches.getFirst().left();
            MessageKey oKey = new MessageKey(oMsg);
            foundMatch.add(oKey);
            newMsg.setMsgstr(oMsg.getMsgstr());
            addResolution(new ProblemResolution(key, oKey, oMsg.getMsgstr(), false, false));
          } else if (matches.isEmpty()) {
            noMatches.add(msg);
            addProblem(new UpdateProblem(key, new ArrayList<>(), UpdateProblem.Type.NO_MATCHES));
          } else {
            addProblem(new UpdateProblem(key, matches, UpdateProblem.Type.HAS_CONFLICTS));
            matches.forEach(pair -> foundMatch.add(new MessageKey(pair.left())));
          }
        } else {
          if (prevResolution.updateMsgstr() != null) {
            newMsg.setMsgstr(prevResolution.updateMsgstr());
          }
        }
      } else {
        newMsg.setMsgstr(oldMsg.getMsgstr());
      }
      newMessages.add(newMsg);
    }
    LOGGER.info("Iterating through old translation file...");
    for (Message msg : other) {
      MessageKey key = new MessageKey(msg);
      if (!template.contains(key) && !foundMatch.contains(key)) {
        Message obMsg = new Message();
        obMsg.getExtractedComments().addAll(msg.getExtractedComments());
        obMsg.getSourceReferences().addAll(msg.getSourceReferences());
        obMsg.setMsgContext(msg.getMsgContext());
        obMsg.setMsgId(msg.getMsgId());
        ProblemResolution prevResolution = resolutionsMap.get(key);
        if (prevResolution == null) {
          LOGGER.debug("Found potentially obsolete string: {}", key);
          addProblem(new UpdateProblem(key, null, UpdateProblem.Type.ABSENT_FROM_TEMPLATE));
          absentFromTemplate.add(msg);
        } else {
          if (prevResolution.markObsolete()) {
            obMsg.markObsolete();
          }
        }
        newMessages.add(obMsg);
      }
    }
    if (!noMatches.isEmpty()) {
      LOGGER.info("Iterating through non-matching template strings to find similar strings...");
    }
    noMatches.forEach(msg -> {
      List<Pair<Message, Float>> similarStrings = new ArrayList<>();
      absentFromTemplate.forEach(oMsg -> {
        int max = Math.max(msg.getMsgId().length(), oMsg.getMsgId().length());
        int dist = distance(msg.getMsgId(), oMsg.getMsgId());
        float score = (float) dist / max;
        if (score < 0.5F) {
          LOGGER.debug("Found similar string to \"{}\" ({}): \"{}\" ({}) / {}", msg.getMsgId(), msg.getMsgContext(), oMsg.getMsgId(), oMsg.getMsgContext(), score);
          similarStrings.add(Pair.of(oMsg, score));
        }
      });
      MessageKey key = new MessageKey(msg);
      if (similarStrings.isEmpty()) {
        // no similar strings found, assume it's a new string
        UpdateProblem problem = problemsMap.remove(key);
        if (problem == null) {
          LOGGER.warn("Could not find associated problem with key: {}", key);
        } else {
          problems.remove(problem);
          addResolution(new ProblemResolution(key, null, null, false, true));
        }
      } else {
        // at least 1 similar string found, assume it's possibly an updated string
        UpdateProblem problem = problemsMap.get(key);
        if (problem == null) {
          LOGGER.warn("Could not find associated problem with key: {}", key);
        } else {
          problem.similar().addAll(similarStrings);
        }
      }
    });
    newMessages
        .stream()
        .sorted((o1, o2) -> {
          if (o1.getSourceReferences().isEmpty() || o2.getSourceReferences().isEmpty()) {
            return o1.getSourceReferences().getFirst().compareTo(o2.getSourceReferences().getFirst());
          }
          return 0;
        })
        .forEach(result::add);
  }

  public void solveProblems() {
    for (ProblemResolution resolution : resolutions) {
      Message msg = result.get(resolution.templateKey());
      if (msg == null) {
        LOGGER.warn("Attempted to solve a merge conflict with a non-existent key: {}", resolution.templateKey());
      } else {
        if (resolution.updateMsgstr() != null) {
          result.get(resolution.templateKey()).setMsgstr(resolution.updateMsgstr());
        } else if (resolution.markObsolete()) {
          result.get(resolution.templateKey()).markObsolete();
        } else if (!resolution.markNew()) {
          LOGGER.warn("Attempted to solve a merge conflict with an invalid resolution: {}", resolution.templateKey());
          continue;
        }
        UpdateProblem problem = problemsMap.get(resolution.templateKey());
        if (problem == null) {
          LOGGER.warn("Problem resolution has no corresponding problem: {}", resolution.templateKey());
        } else {
          problems.remove(problem);
          problemsMap.remove(resolution.templateKey());
        }
      }
    }
  }

}

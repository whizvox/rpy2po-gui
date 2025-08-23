package me.whizvox.rpy2po.gettext;

import com.soberlemur.potentilla.MessageKey;

public record ProblemResolution(MessageKey templateKey,
                                MessageKey oldKey,
                                String setMsgstr,
                                boolean markObsolete,
                                boolean markNew) {
}

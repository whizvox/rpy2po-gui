package me.whizvox.rpy2po.gettext;

import com.soberlemur.potentilla.MessageKey;

public record ProblemResolution(MessageKey templateKey,
                                MessageKey langKey,
                                String updateMsgstr,
                                boolean markObsolete,
                                boolean markNew) {
}

package me.whizvox.rpy2po.core;

import com.soberlemur.potentilla.MessageKey;

public record SimilarMessage(MessageKey key,
                             float similarity) {
}

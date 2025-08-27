package me.whizvox.rpy2po.rpytl;

import java.util.Collections;
import java.util.Map;

public record Statements(Map<String, Statement> statements) {

  public Statements(Map<String, Statement> statements) {
    this.statements = Collections.unmodifiableMap(statements);
  }

}

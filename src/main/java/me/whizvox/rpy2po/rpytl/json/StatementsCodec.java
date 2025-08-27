package me.whizvox.rpy2po.rpytl.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import me.whizvox.rpy2po.gettext.SourceReference;
import me.whizvox.rpy2po.rpytl.Statement;
import me.whizvox.rpy2po.rpytl.Statements;

import java.io.IOException;
import java.util.*;

public class StatementsCodec {

  public static final JsonSerializer<Statements> SERIALIZER = new JsonSerializer<>() {
    @Override
    public void serialize(Statements value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      Map<String, List<Statement>> map = new HashMap<>();
      value.statements().forEach((id, entry) -> map.computeIfAbsent(entry.statement(), k -> new ArrayList<>()).add(entry));
      List<String> statements = map.keySet().stream().sorted().toList();
      gen.writeStartArray();
      for (String stmt : statements) {
        List<Statement> entries = map.get(stmt);
        entries.sort((o1, o2) -> Comparator.comparing(Statement::file).thenComparing(Statement::line).compare(o1, o2));
        gen.writeStartObject();
        gen.writeStringField("statement", stmt);
        gen.writeArrayFieldStart("entries");
        for (Statement stmtObj : entries) {
          gen.writeStartObject();
          gen.writeStringField("location", stmtObj.file() + ":" + stmtObj.line());
          gen.writeStringField("id", stmtObj.id());
          gen.writeEndObject();
        }
        gen.writeEndArray();
        gen.writeEndObject();
      }
      gen.writeEndArray();
    }
  };

  public static final JsonDeserializer<Statements> DESERIALIZER = new JsonDeserializer<>() {
    @Override
    public Statements deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      JsonNode n = p.getCodec().readTree(p);
      ArrayNode statementsNode = (ArrayNode) n;
      Map<String, Statement> statements = new HashMap<>();
      for (JsonNode stmtNode : statementsNode) {
        String statement = stmtNode.get("statement").asText();
        ArrayNode entriesNode = (ArrayNode) stmtNode.get("entries");
        for (JsonNode entryNode : entriesNode) {
          String location = entryNode.get("location").asText();
          String id = entryNode.get("id").asText();
          SourceReference ref = SourceReference.parse(location);
          Statement stmtObj = new Statement(id, statement, ref.file(), ref.line());
          statements.put(stmtObj.id(), stmtObj);
        }
      }
      return new Statements(statements);
    }
  };

}

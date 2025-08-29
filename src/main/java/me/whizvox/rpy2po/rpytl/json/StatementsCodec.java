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
      gen.writeStartObject();
      {
        gen.writeArrayFieldStart("plain");
        {
          Map<String, List<Statement>> plain = new HashMap<>();
          value.plain().forEach((id, entry) -> plain.computeIfAbsent(entry.statement(), k -> new ArrayList<>()).add(entry));
          List<String> statements = plain.keySet().stream().sorted().toList();
          for (String stmt : statements) {
            List<Statement> entries = plain.get(stmt);
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
        }
        gen.writeEndArray();
        gen.writeArrayFieldStart("dialogue");
        {
          Map<String, List<String>> export = new HashMap<>();
          value.dialogue().forEach((id, format) -> export.computeIfAbsent(format, s -> new ArrayList<>()).add(id));
          export.values().forEach(ids -> ids.sort(Comparator.naturalOrder()));
          for (var entry : export.entrySet()) {
            gen.writeStartObject();
            gen.writeStringField("format", entry.getKey());
            gen.writeArrayFieldStart("ids");
            for (String id : entry.getValue()) {
              gen.writeString(id);
            }
            gen.writeEndArray();
            gen.writeEndObject();
          }
        }
        gen.writeEndArray();
      }
      gen.writeEndObject();
    }
  };

  public static final JsonDeserializer<Statements> DESERIALIZER = new JsonDeserializer<>() {
    @Override
    public Statements deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      JsonNode n = p.getCodec().readTree(p);
      ArrayNode plainNode = (ArrayNode) n.get("plain");
      Map<String, Statement> plain = new HashMap<>();
      for (JsonNode stmtNode : plainNode) {
        String statement = stmtNode.get("statement").asText();
        ArrayNode entriesNode = (ArrayNode) stmtNode.get("entries");
        for (JsonNode entryNode : entriesNode) {
          String location = entryNode.get("location").asText();
          String id = entryNode.get("id").asText();
          SourceReference ref = SourceReference.parse(location);
          Statement stmtObj = new Statement(id, statement, ref.file(), ref.line());
          plain.put(stmtObj.id(), stmtObj);
        }
      }
      ArrayNode dialogueNode = (ArrayNode) n.get("dialogue");
      Map<String, String> dialogue = new HashMap<>();
      for (JsonNode stmtNode : dialogueNode) {
        String format = stmtNode.get("format").asText();
        ArrayNode idsNode = (ArrayNode) stmtNode.get("ids");
        for (JsonNode idNode : idsNode) {
          dialogue.put(idNode.asText(), format);
        }
      }
      return new Statements(plain, dialogue);
    }
  };

}

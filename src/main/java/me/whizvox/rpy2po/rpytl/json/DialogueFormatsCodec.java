package me.whizvox.rpy2po.rpytl.json;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ArrayNode;
import me.whizvox.rpy2po.rpytl.DialogueFormats;

import java.io.IOException;
import java.util.*;

public class DialogueFormatsCodec {

  public static final JsonSerializer<DialogueFormats> SERIALIZER = new JsonSerializer<>() {
    @Override
    public void serialize(DialogueFormats value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      Map<String, List<String>> export = new HashMap<>();
      value.formats().forEach((id, format) -> export.computeIfAbsent(format, s -> new ArrayList<>()).add(id));
      export.values().forEach(ids -> ids.sort(Comparator.naturalOrder()));
      gen.writeStartObject();
      for (var entry : export.entrySet()) {
        gen.writeArrayFieldStart(entry.getKey());
        for (String id : entry.getValue()) {
          gen.writeString(id);
        }
        gen.writeEndArray();
      }
      gen.writeEndObject();
    }
  };

  public static final JsonDeserializer<DialogueFormats> DESERIALIZER = new JsonDeserializer<>() {
    @Override
    public DialogueFormats deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
      Map<String, String> formats = new HashMap<>();
      JsonNode formatsNode = p.getCodec().readTree(p);
      for (Map.Entry<String, JsonNode> formatNode : formatsNode.properties()) {
        String format = formatNode.getKey();
        ArrayNode ids = (ArrayNode) formatNode.getValue();
        for (JsonNode id : ids) {
          formats.put(id.asText(), format);
        }
      }
      return new DialogueFormats(formats);
    }
  };

}

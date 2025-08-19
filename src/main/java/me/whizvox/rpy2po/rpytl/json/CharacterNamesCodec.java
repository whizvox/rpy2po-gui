package me.whizvox.rpy2po.rpytl.json;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import me.whizvox.rpy2po.rpytl.CharacterNames;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CharacterNamesCodec {

  public static final JsonSerializer<CharacterNames> SERIALIZER = new JsonSerializer<>() {
    @Override
    public void serialize(CharacterNames value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
      gen.writeStartObject();
      {
        gen.writeObjectFieldStart("names");
        {
          var entries = value.names().entrySet().stream().sorted(Map.Entry.comparingByKey()).toList();
          for (var entry : entries) {
            gen.writeStringField(entry.getKey(), entry.getValue());
          }
        }
        gen.writeEndObject();
        gen.writeStringField("narrator", value.narrator());
        gen.writeStringField("speakFormat", value.speakFormat());
      }
      gen.writeEndObject();
    }
  };

  public static final JsonDeserializer<CharacterNames> DESERIALIZER = new JsonDeserializer<>() {
    @Override
    public CharacterNames deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
      JsonNode n = p.getCodec().readTree(p);
      ObjectNode namesNode = (ObjectNode) n.get("names");
      Map<String, String> names = new HashMap<>();
      namesNode.propertyStream().forEach(entry -> names.put(entry.getKey(), entry.getValue().asText()));
      String narrator = n.get("narrator").asText();
      String speakFormat = n.get("speakFormat").asText();
      return new CharacterNames(names, narrator, speakFormat);
    }
  };

}

package me.whizvox.rpy2po.core.json;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SimplePathJsonDeserializer extends JsonDeserializer<Path> {

  @Override
  public Path deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JacksonException {
    JsonNode n = p.getCodec().readTree(p);
    return Paths.get(n.asText());
  }

}

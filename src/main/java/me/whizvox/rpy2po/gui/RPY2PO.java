package me.whizvox.rpy2po.gui;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import me.whizvox.rpy2po.core.Profile;
import me.whizvox.rpy2po.core.json.EpochSecondLocalDateTimeDeserializer;
import me.whizvox.rpy2po.core.json.EpochSecondLocalDateTimeSerializer;
import me.whizvox.rpy2po.core.json.SimplePathJsonDeserializer;
import me.whizvox.rpy2po.core.json.SimplePathJsonSerializer;
import me.whizvox.rpy2po.gui.form.SelectProfile;
import me.whizvox.rpy2po.rpytl.CharacterNames;
import me.whizvox.rpy2po.rpytl.Statements;
import me.whizvox.rpy2po.rpytl.json.CharacterNamesCodec;
import me.whizvox.rpy2po.rpytl.json.StatementsCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class RPY2PO {

  private static final Logger LOGGER = LoggerFactory.getLogger(RPY2PO.class);

  private final Path workingDir;
  private final ObjectMapper mapper;
  private JFrame frame;

  public RPY2PO(Path workingDir) {
    this.workingDir = workingDir;
    Path profilesDir = getDefaultProfilesDirectory();
    try {
      Files.createDirectories(profilesDir);
    } catch (IOException e) {
      LOGGER.warn("Could not create profiles directory: {}", profilesDir, e);
    }
    mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(CharacterNames.class, CharacterNamesCodec.SERIALIZER);
    module.addDeserializer(CharacterNames.class, CharacterNamesCodec.DESERIALIZER);
    module.addSerializer(Statements.class, StatementsCodec.SERIALIZER);
    module.addDeserializer(Statements.class, StatementsCodec.DESERIALIZER);
    module.addSerializer(Path.class, new SimplePathJsonSerializer());
    module.addDeserializer(Path.class, new SimplePathJsonDeserializer());
    module.addSerializer(LocalDateTime.class, new EpochSecondLocalDateTimeSerializer());
    module.addDeserializer(LocalDateTime.class, new EpochSecondLocalDateTimeDeserializer());
    mapper.registerModule(module);
    frame = null;
  }

  private List<String> loadProfiles() {
    Path profilesFile = workingDir.resolve("profiles.json");
    if (Files.exists(profilesFile)) {
      try (InputStream in = Files.newInputStream(profilesFile)) {
        return mapper.readValue(in, new TypeReference<>() {});
      } catch (IOException e) {
        LOGGER.warn("Could not read file: {}", profilesFile, e);
      }
    }
    return List.of();
  }

  private void saveProfiles(List<String> profilePaths) {
    Path profilesFile = workingDir.resolve("profiles.json");
    try {
      writeJson(profilesFile, profilePaths);
    } catch (IOException e) {
      LOGGER.warn("Could not save file: {}", profilesFile, e);
    }
  }

  public ObjectMapper getMapper() {
    return mapper;
  }

  public void writeJson(Path outputPath, Object value) throws IOException {
    try (OutputStream out = Files.newOutputStream(outputPath)) {
      mapper.writerWithDefaultPrettyPrinter().writeValue(out, value);
    }
  }

  public Path getWorkingDirectory() {
    return workingDir;
  }

  public Path getDefaultProfilesDirectory() {
    return workingDir.resolve("profiles");
  }

  public List<Profile> getProfiles() {
    Path profilesFile = workingDir.resolve("profiles.json");
    if (Files.exists(profilesFile)) {
      List<String> profilePaths = null;
      try (InputStream in = Files.newInputStream(profilesFile)) {
        profilePaths = mapper.readValue(in, new TypeReference<>() {});
      } catch (IOException e) {
        LOGGER.warn("Could not read file: {}", profilesFile, e);
      }
      if (profilePaths != null) {
        List<Profile> profiles = new ArrayList<>();
        for (String profilePathStr : profilePaths) {
          try {
            Path profilePath = Paths.get(profilePathStr);
            try (InputStream in = Files.newInputStream(profilePath.resolve("profile.json"))) {
              Profile profile = mapper.readValue(in, Profile.class);
              profile.setProfileDirectory(profilePath);
              LOGGER.debug("Successfully read profile at {}", profilePath);
              profiles.add(profile);
            }
          } catch (InvalidPathException | IOException e) {
            LOGGER.warn("Could not load profile: {}", profilePathStr, e);
          }
        }
        return profiles;
      }
    }
    return List.of();
  }

  public void removeProfile(String path) {
    List<String> profiles = new ArrayList<>(loadProfiles());
    if (profiles.remove(path)) {
      saveProfiles(profiles);
    } else {
      LOGGER.warn("Tried to remove profile path that did not exist: {}", path);
    }
  }

  public void removeProfile(Path path) {
    List<String> profiles = new ArrayList<>(loadProfiles());
    if (profiles.removeIf(s -> Paths.get(s).equals(path))) {
      saveProfiles(profiles);
    } else {
      LOGGER.warn("Tried to remove profile path that did not exist: {}", path);
    }
  }

  public void addProfile(String path) {
    List<String> profiles = new ArrayList<>(loadProfiles());
    if (profiles.contains(path)) {
      LOGGER.warn("Tried to add duplicate profile path: {}", path);
    } else {
      profiles.add(path);
      saveProfiles(profiles);
    }
  }

  public void setFrame(Supplier<JFrame> newFrame, String title, Dimension size) {
    if (frame != null) {
      frame.dispose();
    }
    frame = newFrame.get();
    /*frame.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        super.windowClosing(e);
      }
    });*/
    frame.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(ComponentEvent e) {
        LOGGER.info("{}, {}", e.getComponent().getWidth(), e.getComponent().getHeight());
      }
    });
    if (title == null) {
      frame.setTitle("RPY2PO");
    } else {
      frame.setTitle(title + " | RPY2PO");
    }
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    if (size == null) {
      frame.pack();
    } else {
      frame.setSize(size);
    }
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  private static RPY2PO app;

  public static RPY2PO inst() {
    return app;
  }

  public static void main(String[] args) {
    app = new RPY2PO(Paths.get("."));
    app.setFrame(SelectProfile::new, "Select Profile", new Dimension(400, 400));
  }

}

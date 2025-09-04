package me.whizvox.rpy2po.gui;

import me.whizvox.rpy2po.core.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProfileSelection extends JComponent {

  private static final Logger LOGGER = LoggerFactory.getLogger(ProfileSelection.class);

  private final List<Profile> cachedProfiles;
  private final List<ProfileListing> profiles;
  private final GroupLayout layout;
  private ProfileListing.ProfileSelectedListener profileSelectedListener;
  private Profile selected;

  private String lastFilter;

  public ProfileSelection(List<Profile> profiles) {
    cachedProfiles = new ArrayList<>(profiles);
    this.profiles = new ArrayList<>();
    layout = new GroupLayout(this);
    lastFilter = null;
    profileSelectedListener = source -> {};
    selected = null;
    filterProfiles(null);
    setLayout(layout);
  }

  public ProfileSelection() {
    this(List.of());
  }

  private void handleSelection(ProfileListing source) {
    selected = null;
    if (source.isSelected()) {
      for (ProfileListing listing : profiles) {
        if (listing != source && listing.isSelected()) {
          listing.setSelected(false);
        }
      }
      for (Profile profile : cachedProfiles) {
        if (profile.getName().equals(source.getProfileName())) {
          selected = profile;
          break;
        }
      }
      if (selected == null) {
        LOGGER.warn("Unknown profile selected, could not find one with name: {}", source.getProfileName());
      }
    }
    profileSelectedListener.onSelected(source);
  }

  public Profile getSelectedProfile() {
    return selected;
  }

  public void filterProfiles(String filter) {
    profiles.forEach(this::remove);
    profiles.clear();
    profileSelectedListener.onSelected(null);
    var newProfilesStream = cachedProfiles.stream();
    if (filter != null && !filter.isBlank()) {
      newProfilesStream = newProfilesStream
          .filter(profile -> profile.getName().toLowerCase().contains(filter.toLowerCase()));
    }
    newProfilesStream
        .sorted((o1, o2) -> o2.getLastOpened().compareTo(o1.getLastOpened()))
        .forEach(profile -> profiles.add(new ProfileListing(profile.getName(), profile.getBaseDirectory().toString())));
    if (!profiles.isEmpty()) {
      var hGroup = layout.createParallelGroup();
      var vGroup = layout.createSequentialGroup();
      for (ProfileListing profile : profiles) {
        hGroup.addComponent(profile, GroupLayout.Alignment.LEADING, 400, 400, 400);
        vGroup.addComponent(profile);
        profile.setProfileSelectedListener(this::handleSelection);
      }
      layout.setHorizontalGroup(hGroup);
      layout.setVerticalGroup(vGroup);
    }
    lastFilter = filter;
  }

  public void setProfileSelectedListener(ProfileListing.ProfileSelectedListener listener) {
    profileSelectedListener = listener;
  }

  public void updateCaches(List<Profile> newProfiles) {
    cachedProfiles.clear();
    cachedProfiles.addAll(newProfiles);
    filterProfiles(lastFilter);
  }

}

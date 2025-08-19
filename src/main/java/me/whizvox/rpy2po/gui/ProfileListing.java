package me.whizvox.rpy2po.gui;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ProfileListing extends JPanel {

  private final String name;
  private final String path;
  private boolean selected;
  private ProfileSelectedListener profileSelectedListener;

  public ProfileListing(String name, String path) {
    this.name = name;
    this.path = path;
    JLabel labelName = new JLabel(name);
    JLabel labelPath = new JLabel(path);
    selected = false;
    profileSelectedListener = source -> {};
    Font currFont = labelName.getFont();
    labelPath.setFont(new Font(currFont.getName(), Font.PLAIN, currFont.getSize()));
    GroupLayout layout = new GroupLayout(this);
    layout.setHorizontalGroup(
        layout.createParallelGroup()
            .addComponent(labelName)
            .addComponent(labelPath)
    );
    layout.setVerticalGroup(
        layout.createSequentialGroup()
            .addGap(5)
            .addComponent(labelName)
            .addComponent(labelPath)
            .addGap(5)
    );
    setLayout(layout);
    setBorder(new EmptyBorder(2, 2, 2, 2));

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        setSelected(!selected);
        profileSelectedListener.onSelected(ProfileListing.this);
      }
      @Override
      public void mouseEntered(MouseEvent e) {
        setBorder(new BevelBorder(BevelBorder.RAISED));
      }
      @Override
      public void mouseExited(MouseEvent e) {
        setBorder(new EmptyBorder(2, 2, 2, 2));
      }
      @Override
      public void mouseDragged(MouseEvent e) {
        mouseClicked(e);
      }
    });
  }

  public ProfileListing() {
    this("Profile name", "/path/to/renpy/project");
  }

  public String getProfileName() {
    return name;
  }

  public String getProfilePath() {
    return path;
  }

  public boolean isSelected() {
    return selected;
  }

  private void updateBackground() {
    if (selected) {
      setBackground(new Color(0xB598FB));
    } else {
      setBackground(null);
    }
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
    updateBackground();
  }

  public void setProfileSelectedListener(ProfileSelectedListener listener) {
    profileSelectedListener = listener;
  }

  public interface ProfileSelectedListener {
    void onSelected(ProfileListing source);
  }

}

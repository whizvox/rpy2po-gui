package me.whizvox.rpy2po;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Playground {

  private static final Logger LOGGER = LoggerFactory.getLogger(Playground.class);

  public static void main(String[] args) throws Exception {
    /*Path p1 = Paths.get("profiles\\Sisterhood\\en.pot");
    Path p2 = Paths.get("C:\\Users\\corne\\PycharmProjects\\sistertood_tl\\export\\ru.po");
    Path p3 = Paths.get("profiles\\Sisterhood\\ru.po");
    Catalog template = new PoParser().parseCatalog(p1.toFile());
    Catalog other = new PoParser().parseCatalog(p2.toFile());
    //other.updateFromTemplate(template);
    UpdateResolver resolver = new UpdateResolver(template, other, null);
    resolver.update();
    PoWriter writer = new PoWriter();
    writer.write(resolver.getResult(), p3.toFile());*/
  }

}
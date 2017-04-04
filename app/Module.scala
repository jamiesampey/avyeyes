import com.google.inject.AbstractModule
import com.google.inject.name.Names

class Module extends AbstractModule {
  def configure() = {

    bind(classOf[Hello])
      .annotatedWith(Names.named("en"))
      .to(classOf[EnglishHello])

    bind(classOf[Hello])
      .annotatedWith(Names.named("de"))
      .to(classOf[GermanHello])
  }
}
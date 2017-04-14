import com.avyeyes.data.{CachedDAL, CachedDALFactory, MemoryMapCachedDAL}
import com.google.inject.AbstractModule
import com.google.inject.assistedinject.FactoryModuleBuilder
import play.api.Logger

class Module extends AbstractModule {

  def configure(): Unit = {
    binder().install(new FactoryModuleBuilder()
      .implement(classOf[CachedDAL], classOf[MemoryMapCachedDAL])
      .build(classOf[CachedDALFactory]))

    bind(classOf[Logger]).toInstance(Logger("avyeyes"))
  }


}

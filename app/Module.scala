import com.avyeyes.data._
import com.avyeyes.service.AmazonS3Service
import com.google.inject.AbstractModule
import play.api.Logger

class Module extends AbstractModule {
  def configure() = {
    bind(classOf[Logger]).toInstance(Logger("avyeyes"))
    bind(classOf[CachedDAL]).to(classOf[MemoryMapCachedDAL])
    bind(classOf[AmazonS3Service]).toInstance(new AmazonS3Service)
  }
}
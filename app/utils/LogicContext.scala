package utils

import com.novus.salat.Context
import play.api.Play

object LogicContext {

  implicit val ctx = new Context {
    val name = "PlaySalatContext"
  }

  ctx.registerClassLoader(Play.classloader(Play.current))
}
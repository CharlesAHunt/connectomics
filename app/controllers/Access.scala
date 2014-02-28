package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import models.{UserDAO, User}
import com.mongodb.casbah.commons.MongoDBObject
import utils.{DatabaseService, EncryptionUtil}

trait Access extends Controller with DatabaseService {

  val loginForm: Form[User] = Form(
    mapping(
      "username" -> text,
      "token" -> text
    )(
      (username, token) =>
        User(username = username, token = token)
    )(
      (user: User) => Option(user.username, user.token)
    )
  )

  val registerForm: Form[User] = Form(
    mapping(
      "username" -> text,
      "token" -> text
    )(
      (username, token) =>
        User(username = username, token = token)
    )(
      (user: User) => Option(user.username, user.token)
    )
  )

  /**
   * Redirect to login if the user in not authorized.
   */
  private def onUnauthorized(request: RequestHeader) = Results.Redirect(routes.Application.index())

  // --

  /**
   * Action for authenticated users.
   */
//  def IsAuthenticated(f: => String => Request[AnyContent] => Result) = Security.Authenticated(username, onUnauthorized) {
//    user =>
//      Action(request => f(user)(request))
//  }

}

object Access extends Controller with Access {

  def register = Action { implicit request =>
    registerForm.bindFromRequest.fold(
      errors => {    Redirect("/index").flashing(
        "error" -> "There were errors in your registration form."
      )},
      user => {
        val a = MongoDBObject("username" -> user.username, "token" -> EncryptionUtil.encrypt(user.token))
        getCollection("users").insert(a)
      }
    )

    Redirect("/index").flashing(
      "success" -> "You have successfully registered. You may now log in."
    )
  }


  def login = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      errors => {    Redirect("/index").flashing(
        "error" -> "There were errors in your login form."
      )},
      user => {
        val found = UserDAO.findOne(MongoDBObject("username" -> user.username))
        if(user.token == EncryptionUtil.decrypt(found.get.token)) {
          Redirect("/index").withSession(
            session + ("logged_in_user" -> loginForm.bindFromRequest.data.get("username").get)
          ).flashing(
              "success" -> "You are now logged in."
            )
        }
      }
    )

    Redirect("/index").flashing(
        "danger" -> "Your username or password was incorrect"
      )

  }

  def logout = Action {
    Redirect("/index").withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }
}
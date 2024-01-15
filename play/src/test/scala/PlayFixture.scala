package pdi.jwt

import java.time.Clock
import scala.concurrent.Future

import akka.stream.Materializer
import akka.util.Timeout
import play.api.Configuration
import play.api.libs.json.*
import play.api.mvc.*
import play.api.mvc.Results.*
import play.api.test.*
import play.api.test.Helpers.*

case class User(id: Long, name: String)

trait PlayFixture extends Fixture {
  import pdi.jwt.JwtSession.*

  implicit var clock: Clock = validTimeClock

  def HEADER_NAME: String

  implicit val userFormat: OFormat[User] = Json.format[User]

  val user = User(1, "Paul")
  val userJson = Json.obj("id" -> 1, "name" -> "Paul")

  var defaultMaxAge = expiration - validTime

  // The expiration is not added in the same way, resulting in JSON properties not in the same order,
  // meaning a different Base64 encoding
  val playClaim64 =
    "eyJpc3MiOiJqb2UiLCJodHRwOi8vZXhhbXBsZS5jb20vaXNfcm9vdCI6dHJ1ZSwiZXhwIjoxMzAwODE5MzgwfQ"

  def loginAction(implicit conf: Configuration, Action: DefaultActionBuilder): EssentialAction =
    Action { implicit request =>
      val body = request.body.asJson
      val password = (body.get \ "password").as[String]

      password match {
        case "p4ssw0rd" => Ok.withJwtSession(Json.obj("user" -> userJson))
        case _          => BadRequest
      }
    }

  def logoutAction(implicit conf: Configuration, Action: DefaultActionBuilder): EssentialAction =
    Action {
      Ok.withoutJwtSession
    }

  def classicAction(implicit conf: Configuration, Action: DefaultActionBuilder): EssentialAction =
    Action { implicit request =>
      Ok.refreshJwtSession
    }

  def securedAction(implicit conf: Configuration, Action: DefaultActionBuilder): EssentialAction =
    Action { implicit request =>
      request.jwtSession.getAs[User]("user") match {
        case Some(_) => Ok.refreshJwtSession
        case _       => Unauthorized.withoutJwtSession
      }
    }

  def get(action: EssentialAction, header: Option[String] = None)(implicit
      conf: Configuration,
      mat: Materializer
  ) = {
    val request = header match {
      case Some(h) =>
        FakeRequest(GET, "/something").withHeaders((JwtSession.REQUEST_HEADER_NAME, h))
      case _ => FakeRequest(GET, "/something")
    }

    call(action, request)
  }

  def post(action: EssentialAction, body: JsObject)(implicit mat: Materializer) = {
    call(action, FakeRequest(POST, "/something").withJsonBody(body))
  }

  def jwtHeader(
      of: Future[Result]
  )(implicit timeout: Timeout, conf: Configuration): Option[String] =
    header(JwtSession.RESPONSE_HEADER_NAME, of)(timeout)
}

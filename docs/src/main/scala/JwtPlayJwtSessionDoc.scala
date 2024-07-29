package pdi.jwt.docs

import scala.annotation.nowarn

@nowarn
object JwtPlayJwtSessionDoc {
  // #example
  import java.time.Clock

  import pdi.jwt.JwtSession
  import play.api.Configuration

  implicit val clock: Clock = Clock.systemUTC

//In a real Play! App this should normally be injected in the constructor with @Inject()
  implicit val conf: Configuration = Configuration.reference

// Let's create a session, it will automatically assign a default header. No
// In your app, the default header would be generated from "application.conf" file
// but here, it will just use the default values (which are all empty)
  var session = JwtSession()

// We can add a (key, value)
  session = session + ("user", 1)

// Or several of them
  session = session ++ (("nbf", 1431520421), ("key", "value"), ("key2", 2), ("key3", 3))

// Also remove a key
  session = session - "key"

// Or several
  session = session -- ("key2", "key3")

// We can access a specific key
  session.get("user")

// Test if the session is empty or not
// (it is not here since we have several keys in the claimData)
  session.isEmpty()

// Serializing the session is the same as encoding it as a JSON Web Token
  val token = session.serialize

// You can create a JwtSession from a token of course
  JwtSession.deserialize(token)

// You could refresh the session to set its expiration in a few seconds from now
// but you need to set "session.maxAge" in your "application.conf" and since this
// is not a real Play application, we cannot do that, so here, the refresh will do nothing.
  session = session.refresh()
  // #example
  import pdi.jwt.JwtSession
// #implicits
// First, creating the implicits
  import play.api.libs.json._

  case class User(id: Long, name: String)
  implicit val formatUser: Format[User] = Json.format[User]

// Next, adding it to a new session
  val session2 = JwtSession() + ("user", User(42, "Paul"))

// Finally, accessing it
  session2.getAs[User]("user")
// #implicits
// #requestheader
  import pdi.jwt.JwtSession._
  import play.api.Configuration
  import play.api.test.FakeRequest

// Default JwtSession
  FakeRequest().jwtSession

// What about some headers?
// (the default header for a JSON Web Token is "Authorization" and it should be prefixed by "Bearer ")
  val request = FakeRequest().withHeaders(("Authorization", "Bearer " + session2.serialize))
  request.jwtSession

// It means you can directly read case classes from the session!
// And that's pretty cool
  request.jwtSession.getAs[User]("user")
// #requestheader
// #result
  import play.api.mvc._
  implicit val implRequest: FakeRequest[AnyContentAsEmpty.type] = request

// Let's begin by creating a Result
  var result: play.api.mvc.Result = play.api.mvc.Results.Ok

// We can already get a JwtSession from our implicit RequestHeader
  result.jwtSession

// Setting a new empty JwtSession
  result = result.withNewJwtSession

// Or from an existing JwtSession
  result = result.withJwtSession(session2)

// Or from a JsObject
  result = result.withJwtSession(Json.obj(("id", 1), ("key", "value")))

// Or from (key, value)
  result = result.withJwtSession(("id", 1), ("key", "value"))

// We can add stuff to the current session (only (String, String))
  result = result.addingToJwtSession(("key2", "value2"), ("key3", "value3"))

// Or directly classes or objects if you have the correct implicit Writes
  result = result.addingToJwtSession("user", User(1, "Paul"))

// Removing from session
  result = result.removingFromJwtSession("key2", "key3")

// Refresh the current session
  result = result.refreshJwtSession

// So, at the end, you can do
  result.jwtSession.getAs[User]("user")
// #result

}

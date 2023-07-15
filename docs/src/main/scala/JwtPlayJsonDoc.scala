package pdi.jwt.docs

import scala.annotation.nowarn

@nowarn
object JwtPlayJsonDoc {
  // #example
  import java.time.Clock

  import pdi.jwt._
  import play.api.libs.json.Json

  implicit val clock: Clock = Clock.systemUTC

  val claim = Json.obj(("user", 1), ("nbf", 1431520421))
  val key = "secretKey"
  val algo = JwtAlgorithm.HS256

  JwtJson.encode(claim)

  val token = JwtJson.encode(claim, key, algo)

  JwtJson.decodeJson(token, key, Seq(JwtAlgorithm.HS256))

  JwtJson.decode(token, key, Seq(JwtAlgorithm.HS256))
  // #example

  // #encode
  val header = Json.obj(("typ", "JWT"), ("alg", "HS256"))
  // From just the claim to all possible attributes
  JwtJson.encode(claim)
  JwtJson.encode(claim, key, algo)
  JwtJson.encode(header, claim, key)
  // #encode

  // #decode
  // You can decode to JsObject
  JwtJson.decodeJson(token, key, Seq(JwtAlgorithm.HS256))
  JwtJson.decodeJsonAll(token, key, Seq(JwtAlgorithm.HS256))
  // Or to case classes
  JwtJson.decode(token, key, Seq(JwtAlgorithm.HS256))
  JwtJson.decodeAll(token, key, Seq(JwtAlgorithm.HS256))
  // #decode

  implicit val c: Clock = Clock.systemUTC
  // #format
  import pdi.jwt.JwtJson._

  // Reads
  Json.fromJson[JwtHeader](header)
  Json.fromJson[JwtClaim](claim)

  // Writes
  Json.toJson(JwtHeader(JwtAlgorithm.HS256))
  Json.toJson(JwtClaim("""{"user":1}""").issuedNow.expiresIn(10))
  // Or
  JwtHeader(JwtAlgorithm.HS256).toJsValue()
  JwtClaim("""{"user":1}""").issuedNow.expiresIn(10).toJsValue()
  // #format
}

package ziofullstack.backend.endpoints

import cats.effect.Sync
import io.circe.{Decoder, Encoder}
import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import shapeless._
import zio._
import zio.interop.catz._

object CirceEncoders {
  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A], ev: A =:!= String, ev2: Sync[Task]): EntityDecoder[Task, A] = jsonOf[Task, A]
  implicit def circeJsonEncoder[A](implicit decoder: Encoder[A], ev: A =:!= String, ev2: Sync[Task]): EntityEncoder[Task, A] = jsonEncoderOf[Task, A]
}

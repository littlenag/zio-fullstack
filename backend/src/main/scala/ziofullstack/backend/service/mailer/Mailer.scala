package ziofullstack.backend.service.mailer

import zio._
import cats.Show
import cats.implicits.toShow
import courier.{Mailer => CMailer, _}

import javax.mail.internet.InternetAddress
import ziofullstack.backend.config.SmtpConfig
import ziofullstack.backend.util.{AnyEx, ZLogger}
import ziofullstack.shared.domain.User

trait Mailer {
  def send(envelope: Envelope): Task[Unit]

  def addressedEnvelope(
                         user: User,
                         from: InternetAddress = "Accounts <noreply@ziofullstack.markkegel.com>".addr
                       ): Envelope =
    Envelope
      .from(from)
      .to(user.email.addr)
}

object MailerLive {
  val layer: URLayer[Has[SmtpConfig] with Has[ZLogger], Has[Mailer]] =
    (MailerLive(_,_)).toLayer
}


case class MailerLive(smtpConfig: SmtpConfig, logger: ZLogger) extends Mailer {

  implicit lazy val showContent: Show[Content] = Show.show {
    case Text(body, _) => body
    case Multipart(parts, _) => parts.map(_.getContent.toString).mkString("\n")
    case Signed(body) => showContent.show(body)
  }

  private val courierMailer = {
    val builder = CMailer(smtpConfig.host, smtpConfig.port)

    val withUser = (builder: Session.Builder) =>
      if (smtpConfig.user.nonEmpty) {
        builder.as(smtpConfig.user, smtpConfig.password).auth(true)
      } else {
        builder
      }

    val withTls = (builder: Session.Builder) =>
      smtpConfig.startTls.fold(builder)(builder.startTls)

    builder |> withUser |> withTls |> (_.apply())
  }

  override def send(envelope: Envelope): Task[Unit] = {
    if (smtpConfig.mock) {
      logger.info(
        s"(mocked) Sending email:\n$envelope\nContents=${envelope.contents.show}"
      )
    } else {
      logger.info(s"(real) Sending email:\n$envelope") *>
        ZIO.fromFuture(implicit ec => courierMailer(envelope))
          .flatMap(_ => logger.info(s"(real) Sent email:\n$envelope"))
          .onTermination(cause => logger.error(s"(real) Unable to send email:\n$envelope", cause))
    }
  }
}

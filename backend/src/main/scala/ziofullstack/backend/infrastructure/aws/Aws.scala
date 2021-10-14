package ziofullstack.backend.infrastructure.aws

import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth._
import ziofullstack.backend.config.AwsConfig

/**
 *
 */
object Aws {
  def awsCredentialsProvider(config: AwsConfig): AWSCredentialsProvider = {
    config.`creds-provider` match {
      case "config" =>
        new AWSStaticCredentialsProvider(new BasicAWSCredentials(
          config.accessKey.getOrElse(throw new RuntimeException("Required setting 'accessKey' not provided.")),
          config.secretKey.getOrElse(throw new RuntimeException("Required setting 'secretKey' not provided."))
        ))

      case "profile-default" =>
        new ProfileCredentialsProvider()

      case "profile-instance" =>
        new InstanceProfileCredentialsProvider(false)

      case "system-properties" =>
        new SystemPropertiesCredentialsProvider()

      case "env" =>
        new EnvironmentVariableCredentialsProvider()

      case other =>
        throw new RuntimeException(s"Unrecognized AWS credentials provider: $other")
    }
  }
}

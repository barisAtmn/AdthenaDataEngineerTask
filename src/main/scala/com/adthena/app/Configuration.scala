package com.adthena.app

import com.adthena.{ConfigurationModule, Discounts, Prices}
import pureconfig.ConfigSource
import zio.{Task, ZIO, ZLayer}
import pureconfig.generic.auto._

/**
 *  Configuration Module
 **/
object Configuration {

  case class Config(prices: Prices, discounts: Discounts, logLevel:Option[String]=Some("ERROR"))

  trait Service {
    val load: Task[Config]
  }

  object ConfigurationService extends Service {
    override val load: Task[Config] = ZIO
      .fromEither(ConfigSource.default.load[Config])
      .mapError(failures =>
        new IllegalStateException(
          s"Error loading configuration: $failures"
        )
      )

    /**
     * Configuration layer
     **/
    val configLive: ZLayer[Any, Nothing, ConfigurationModule] =
      ZLayer.succeed(ConfigurationService)

    /**
     * accessors
     **/
    def getConfig:ZIO[ConfigurationModule, Throwable, Config] = ZIO.accessM[ConfigurationModule](_.get.load)

  }
}

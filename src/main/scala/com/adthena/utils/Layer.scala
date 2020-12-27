package com.adthena.utils

import com.adthena.app.Configuration.ConfigurationService.getConfig
import com.adthena.app.{BusinessLogic, Validation}
import com.adthena.{ConfigurationModule, Item, error, modules, output}
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.logging.{LogFormat, LogLevel, Logging, log}
import zio.{ExitCode, ZIO}

/**
 * Creating all needed layers for program
 **/
object Layer {

  def apply(program: ZIO[modules, error, output]):ZIO[Logging with Console with Clock with ConfigurationModule, Throwable, ExitCode] = for{
    config             <-  getConfig
    loglevel           = {config.logLevel.getOrElse("ERROR") match {
      case value if value.toUpperCase().equals("FATAL") => LogLevel.Fatal
      case value if value.toUpperCase().equals("WARN")  => LogLevel.Warn
      case value if value.toUpperCase().equals("DEBUG") => LogLevel.Debug
      case value if value.toUpperCase().equals("INFO")  => LogLevel.Info
      case value if value.toUpperCase().equals("TRACE") => LogLevel.Trace
      case value if value.toUpperCase().equals("OFF")   => LogLevel.Off
      case _                                            => LogLevel.Error
    }}
    prices             = config.prices.map(price => price.adjust(price.item.copy(name = price.item.name.toLowerCase), price.cost))
    _                  <- log.info(s"All prices : \n ${prices.map(price => (price.item.name,price.cost)).mkString(" ")}")
    discounts          = config.discounts.map(discount => discount.copy(item= Item(discount.item.name.toLowerCase,Some(discount.item.count.getOrElse(1))),discounted=Item(discount.discounted.name.toLowerCase,Some(discount.discounted.count.getOrElse(1)))))
    _                  <- log.info(s"All discounts : \n ${discounts.mkString(" ")}")
    businessLogicLayer <-  ZIO.succeed(BusinessLogic.BusinessLogicLive(prices, discounts))
    validationLayer    <-  ZIO.succeed(Validation.live(prices.map(_.item)))
    loggingLayer       <-  ZIO.succeed(Logging.console(
      logLevel = loglevel,
      format = LogFormat.ColoredLogFormat()
    ))
    helpLayer          <-  ZIO.succeed(Console.live ++ Clock.live ++ Blocking.live)
    exitCode           <-  program.provideLayer(businessLogicLayer ++ helpLayer ++ validationLayer ++ (loggingLayer >>> Logging.withRootLoggerName("Price-Basket-Logger"))).exitCode
  } yield exitCode

}

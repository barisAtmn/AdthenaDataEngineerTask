package com.adthena

import zio._
import zio.logging.{LogFormat, LogLevel, Logging, log}

/**
 * Program calculates shopping basket
 *
 * @author Baris Ataman
 * @version 1.0
 **/
object Program extends App {
  import app.Configuration
  import app.BusinessLogic._
  import app.Validation._
  import utils.Layer

  override def run(args: List[String]): URIO[zio.ZEnv, ExitCode] = {
    console.putStrLn("*** Shopping Basket ***\n - Please enter input as 'PriceBasket item1 item2 item3...' \n - Please type -e OR exit to close program!") *>
      Layer(program).provideCustomLayer(Configuration.ConfigurationService.configLive ++ {Logging.console(
        logLevel = LogLevel.Info,
        format = LogFormat.ColoredLogFormat()
      ) >>> Logging.withRootLoggerName("Price-Basket-Logger")}).exitCode
  }

  /**
   * It has 3 main method which are calculateSubtotal, calculateDiscount, calculateTotal
   * According to input from command line, program goes on.
   **/
  val program = (for {
    _                 <- console.putStrLn("\n" + List.fill(100)("-").mkString) *> console.putStrLn(" * Items are being waiting...") *> console.putStrLn("(Please type -h OR help to list helps!)")
    data              <- console.getStrLn
    _                 <- log.info(s"Input entered : $data")
    _                 <- if (data.toLowerCase ==  "-e" || data.toLowerCase == "exit") IO.fail(Exit) else if(data.toLowerCase ==  "-h" || data.toLowerCase == "help") IO.fail(Help)
                         else IO.succeed()
    input             <- validateInput(data)
    userData          <- if (input.isLeft) IO.fail(input.merge) else IO.succeed(input.merge)
    ref               <- Ref.make(Bill(userData.asInstanceOf[Items]))
    billWithSubtotal  <- calculateSubtotal(ref)
    _                 <- log.info(s"State 1 : ${billWithSubtotal.toString}")
    billWithDiscounts <- calculateDiscount(billWithSubtotal)
    _                 <- log.info(s"State 2 : ${billWithDiscounts.toString}")
    billWithTotal     <- calculateTotal(billWithDiscounts)
    _                 <- log.info(s"State 3 : ${billWithTotal.toString}")
    bill              <- billWithTotal.get
    _                 <- console.
      putStrLn("Items: " + bill.items.mkString(" ") + " \nSubtotal: "  +
        bill.getSubTotal + " \n" +
       {
         if(bill.comments.size > 1)
           bill.comments.dropRight(1).map(comment => comment._1 + ": " + bill.adjustDiscount(comment._2.toDouble)).mkString("\n") +"\nTotal Price: "
         else
           "(" + bill.comments.map(comment => comment._1 ).head + ")\nTotal Price: "
       }
        +
        bill.getTotal.toString
      )
  } yield 0)
    .catchAll(e =>  e match {
      case _:CommandInvalid => console.putStrLn(s"ERROR: ${e.asInstanceOf[CommandInvalid].errorMessage}").as(1)
      case _:ItemInvalid => console.putStrLn(s"ERROR: ${e.asInstanceOf[ItemInvalid].errorMessage}").as(1)
      case Help => console.putStrLn(s"HELP: \n- Enter input PriceBasket item1 item2 item3... \n- Please type -e OR exit to close program!").as(1)
      case _:Throwable => console.putStrLn(s"ERROR: ${e.asInstanceOf[Throwable].getMessage}").as(2)
      case Exit => console.putStrLn(s"EXIT!").as(2)
    })
    .repeat(Schedule.recurWhile(_ != 2))


}

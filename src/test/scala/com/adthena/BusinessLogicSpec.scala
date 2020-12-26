package com.adthena

import zio.{Has, Managed, Ref, ZIO, ZLayer, ZManaged}
import zio.test.Assertion._
import zio.test.{DefaultRunnableSpec, assertM, suite, testM}

import scala.io.Source

object BusinessLogicSpec extends DefaultRunnableSpec {

  import com.adthena.app.BusinessLogic._

  final case class TestData(
                             items: List[String]
                           )

  val testData: Managed[Nothing, List[TestData]] =
    ZManaged
      .make {
        ZIO.effectTotal(Source.fromResource("data.txt"))
      } { source => ZIO.effectTotal(source.close) }
      .mapM { source =>
        ZIO.effectTotal {
          source.getLines.toList.init
            .map(_.split(' '))
            .map {
              case (message) =>
                TestData(message.tail.toList)
            }
        }
      }

  val testBusinessLogicLive: ZLayer[Has[List[TestData]], Nothing, BusinessLogicModule] =
    ZLayer.fromService { testData =>
      val prices             =  List(Price(Item("Soup"), "65P"), Price(Item("Bread"), "30P"), Price(Item("MILK"), "£1.30"), Price(Item("Apples"), "£1.00"))
      val discounts          =  List(Discount(Item("Soup",Some(3)), Item("Bread",Some(2)), 0.5, "Buy 3 tins of soup and get two loaf of bread for half price"),
                                     Discount(Item("Soup",Some(2)), Item("Apples",Some(1)), 0.1, "Buy a loaf of bread and get a bottle of milk 10% discount"),
                                     Discount(Item("Milk",Some(1)), Item("Milk",Some(1)), 0.1, "Buy a loaf of bread and get a bottle of milk 10% discount"),
                                     Discount(Item("Apples",Some(1)), Item("Apples",Some(1)), 0.1, "Apples have a 10% discount off their normal price this week")
      ).sortBy(_.ratio).reverse
      BusinessLogic(prices, discounts)
    }


  val testEnvironment =
    testData.toLayer >>> testBusinessLogicLive

  def spec = suite("processEvent")(
    testM("calls putStrLn > getStrLn > putStrLn and returns unit") {
      val calculateSubtotalProgram = for {
        items             <- ZIO.effect(List(Item("ELMA",Some(4)),Item("ARMUT",Some(6)), Item("KIRAZ",Some(3))))
        ref               <- Ref.make(Bill(items))
        billWithSubtotal  <- calculateSubtotal(ref)
        result            <- billWithSubtotal.get
      } yield result
      assertM(calculateSubtotalProgram)(equalTo(Bill(List(Item("ELMA"),Item("ARMUT"), Item("KIRAZ")))))
    }
  ).provideCustomLayerShared(testEnvironment)
}
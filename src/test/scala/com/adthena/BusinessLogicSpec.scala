package com.adthena

import zio.{Has, Managed, Ref, ZIO, ZLayer, ZManaged}
import zio.test.Assertion._
import zio.test.{DefaultRunnableSpec, assertM, suite, testM}

import scala.io.Source

object BusinessLogicSpec extends DefaultRunnableSpec {

  import com.adthena.app.BusinessLogic._

  final case class TestData(
                             language: String
                           )

  val testData: Managed[Nothing, List[TestData]] =
    ZManaged
      .make {
        ZIO.effectTotal(Source.fromResource("data.txt"))
      } { source => ZIO.effectTotal(source.close) }
      .mapM { source =>
        ZIO.effectTotal {
          source.getLines.toList.init
            .map(_.split(','))
            .map {
              case (language) =>
                TestData(language.head)
            }
        }
      }

  val testBusinessLogicLive: ZLayer[Has[List[TestData]], Nothing, BusinessLogicModule] =
    ZLayer.fromService { testData =>
      val prices             =  List(Price(Item("ELMA"), "30P"), Price(Item("ARMUT"), "30P"), Price(Item("KIRAZ"), "E1"))
      val discounts          =  List(Discount(Item("ELMA",Some(3)), Item("ELMA",Some(3)), 0.5, "ELMA %50 indirim"),  Discount(Item("ARMUT",Some(2)), Item("KIRAZ",Some(1)), 0.1, "KIRAZ %10 indirim")).sortBy(_.ratio).reverse
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
package com.adthena.app

import com.adthena._
import zio.test.Assertion.equalTo
import zio.test._
import zio.{Ref, ZIO, ZLayer}

object BusinessLogicSpec extends DefaultRunnableSpec {

  import _root_.zio.test.environment._
  import com.adthena.app.BusinessLogic._
  import cats.implicits._

  val testBusinessLogicLive: ZLayer[Any, Nothing, BusinessLogicModule] =
    ZLayer.succeed{
      val prices             =  List(Price(Item("Soup"), "65P", costE = Some(0.65), costP = Some(65)),
                                     Price(Item("Bread"), "30P", costE = Some(0.3), costP = Some(30)),
                                     Price(Item("Milk"), "£1.30", costE = Some(1.3), costP = Some(130)),
                                     Price(Item("Apples"), "£1.00", costE = Some(1), costP = Some(100)),
                                     Price(Item("Peach"), "£1.50", costE = Some(1.5), costP = Some(150)),
                                     Price(Item("Watermelon"), "£1.10", costE = Some(1.1), costP = Some(110)),
                                     Price(Item("Melon"), "£1.0", costE = Some(1), costP = Some(100)),
                                     Price(Item("Fish"), "£3.50", costE = Some(3.5), costP = Some(350)))
      val discounts          =  List(Discount(Item("Soup",Some(3)), Item("Bread",Some(2)), 0.5, "Buy 3 tins of soup and get two loaf of bread for half price"),
                                     Discount(Item("Soup",Some(2)), Item("Apples",Some(1)), 0.2, "Buy 2 loaf of bread and get a apples 20% discount"),
                                     Discount(Item("Milk",Some(1)), Item("Milk",Some(1)), 0.2, "Milk has a 20% discount off their normal price this week"),
                                     Discount(Item("Apples",Some(1)), Item("Apples",Some(1)), 0.1, "Apples have a 10% discount off their normal price this week"),
                                     Discount(Item("Watermelon",Some(2)), Item("Milk",Some(1)), 0.2, "Buy 2 watermelon and get 20% discount for per bottle of milk"),
                                     Discount(Item("Watermelon",Some(3)), Item("Apples",Some(1)), 0.5, "Buy 3 watermelon and get 50% discount for per kg of apples")
      ).sortBy(discount => (discount.ratio * discount.discounted.count.getOrElse(1))).reverse
      BusinessLogic(prices, discounts)
    }


  val testEnvironment = testBusinessLogicLive

  def spec:Spec[TestEnvironment, TestFailure[Throwable], TestSuccess] = suite("PriceBasket Business Logic Unit Tests")(
    suite("If you buy 1 soup(per 65p), 1 bread(per 30p) ")(
      testM("subtotal should be 95p") {
        val calculateSubtotalProgram = for {
          items             <- ZIO.effect(List(Item("Soup",Some(1)),Item("Bread",Some(1))))
          ref               <- Ref.make(Bill(items))
          billWithSubtotal  <- calculateSubtotal(ref)
          billWithDiscounts <- calculateDiscount(billWithSubtotal)
          billWithTotal     <- calculateTotal(billWithDiscounts)
          result            <- billWithTotal.get
        } yield result.getSubTotal
        assertM(calculateSubtotalProgram)(equalTo( "95p"))
      },
      testM("total should be 95p") {
        val calculateSubtotalProgram = for {
          items             <- ZIO.effect(List(Item("Soup",Some(1)),Item("Bread",Some(1))))
          ref               <- Ref.make(Bill(items))
          billWithSubtotal  <- calculateSubtotal(ref)
          billWithDiscounts <- calculateDiscount(billWithSubtotal)
          billWithTotal     <- calculateTotal(billWithDiscounts)
          result            <- billWithTotal.get
        } yield result.getTotal
        assertM(calculateSubtotalProgram)(equalTo( "95p"))
      },
      testM("No discount") {
        val calculateSubtotalProgram = for {
          items             <- ZIO.effect(List(Item("Soup",Some(1)),Item("Bread",Some(1))))
          ref               <- Ref.make(Bill(items))
          billWithSubtotal  <- calculateSubtotal(ref)
          billWithDiscounts <- calculateDiscount(billWithSubtotal)
          billWithTotal     <- calculateTotal(billWithDiscounts)
          result            <- billWithTotal.get
        } yield result.discounts
        assertM(calculateSubtotalProgram)(equalTo(
          Map[String,Discounts]()
        ))
      }
    ),
    suite("If you buy 4 soup(per 65p), 6 bread(per 30p) and 3 milk(per £1.30)")(
      testM("subtotal should be £8.3") {
        val calculateSubtotalProgram = for {
          items             <- ZIO.effect(List(Item("Soup",Some(4)),Item("Bread",Some(6)), Item("Milk",Some(3))))
          ref               <- Ref.make(Bill(items))
          billWithSubtotal  <- calculateSubtotal(ref)
          billWithDiscounts <- calculateDiscount(billWithSubtotal)
          billWithTotal     <- calculateTotal(billWithDiscounts)
          result            <- billWithTotal.get
        } yield result.getSubTotal
        assertM(calculateSubtotalProgram)(equalTo( "£8.3"))
      },
      testM("total should be £6.62") {
        val calculateSubtotalProgram = for {
          items             <- ZIO.effect(List(Item("Soup",Some(4)),Item("Bread",Some(6)), Item("Milk",Some(3))))
          ref               <- Ref.make(Bill(items))
          billWithSubtotal  <- calculateSubtotal(ref)
          billWithDiscounts <- calculateDiscount(billWithSubtotal)
          billWithTotal     <- calculateTotal(billWithDiscounts)
          result            <- billWithTotal.get
        } yield result.getTotal
        assertM(calculateSubtotalProgram)(equalTo( "£6.62"))
      },
      testM("discounts should be applied for Apples,Milk,Bread") {
        val calculateSubtotalProgram = for {
          items             <- ZIO.effect(List(Item("Soup",Some(4)),Item("Bread",Some(6)), Item("Milk",Some(3))))
          ref               <- Ref.make(Bill(items))
          billWithSubtotal  <- calculateSubtotal(ref)
          billWithDiscounts <- calculateDiscount(billWithSubtotal)
          billWithTotal     <- calculateTotal(billWithDiscounts)
          result            <- billWithTotal.get
        } yield result.discounts
        assertM(calculateSubtotalProgram)(equalTo(
              Map("Apples" -> List(Discount(Item("Soup",Some(2)), Item("Apples",Some(1)), 0.2, "Buy 2 loaf of bread and get a apples 20% discount")))
              |+|
              Map("Milk" -> List(Discount(Item("Milk",Some(1)), Item("Milk",Some(1)), 0.2, "Milk has a 20% discount off their normal price this week")))
              |+|
              Map("Bread" -> List(Discount(Item("Soup",Some(3)),Item("Bread",Some(2)),0.5,"Buy 3 tins of soup and get two loaf of bread for half price")))
        ))
      }
    ),
    suite("If you buy 3 watermelon(per £1.10), 1 bread(per 30p) and 2 milk(per £1.30)")(
      testM("subtotal should be £6.2") {
        val calculateSubtotalProgram = for {
          items             <- ZIO.effect(List(Item("Watermelon",Some(3)),Item("Bread",Some(1)), Item("Milk",Some(2))))
          ref               <- Ref.make(Bill(items))
          billWithSubtotal  <- calculateSubtotal(ref)
          billWithDiscounts <- calculateDiscount(billWithSubtotal)
          billWithTotal     <- calculateTotal(billWithDiscounts)
          result            <- billWithTotal.get
        } yield result.getSubTotal
        assertM(calculateSubtotalProgram)(equalTo( "£6.2"))
      },
      testM("total should be £5.68") {
        val calculateSubtotalProgram = for {
          items             <- ZIO.effect(List(Item("Watermelon",Some(3)),Item("Bread",Some(1)), Item("Milk",Some(2))))
          ref               <- Ref.make(Bill(items))
          billWithSubtotal  <- calculateSubtotal(ref)
          billWithDiscounts <- calculateDiscount(billWithSubtotal)
          billWithTotal     <- calculateTotal(billWithDiscounts)
          result            <- billWithTotal.get
        } yield result.getTotal
        assertM(calculateSubtotalProgram)(equalTo( "£5.68"))
      },
      testM("discounts should be applied for Apples and Milk") {
        val calculateSubtotalProgram = for {
          items             <- ZIO.effect(List(Item("Watermelon",Some(3)),Item("Bread",Some(1)), Item("Milk",Some(2))))
          ref               <- Ref.make(Bill(items))
          billWithSubtotal  <- calculateSubtotal(ref)
          billWithDiscounts <- calculateDiscount(billWithSubtotal)
          billWithTotal     <- calculateTotal(billWithDiscounts)
          result            <- billWithTotal.get
        } yield result.discounts
        assertM(calculateSubtotalProgram)(equalTo(
          Map("Apples" -> List(
            Discount(Item("Watermelon",Some(3)),Item("Apples",Some(1)),0.5,"Buy 3 watermelon and get 50% discount for per kg of apples"))
          )
            |+|
            Map(
              "Milk" -> List(
                Discount(Item("Milk",Some(1)),Item("Milk",Some(1)),0.2,"Milk has a 20% discount off their normal price this week"),
                Discount(Item("Watermelon",Some(2)),Item("Milk",Some(1)),0.2,"Buy 2 watermelon and get 20% discount for per bottle of milk"),
                ))
        ))
      }
    )
  ).provideCustomLayerShared(testEnvironment)
}

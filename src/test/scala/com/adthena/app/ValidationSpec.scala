package com.adthena.app

import zio.test.Assertion.equalTo
import zio.{ZIO, ZLayer}
import zio.test.{DefaultRunnableSpec, Spec, TestFailure, TestSuccess, assertM, suite, testM}
import com.adthena._

object ValidationSpec extends DefaultRunnableSpec{

  import _root_.zio.test.environment._
  import com.adthena.app.Validation._


  val testValidationLive: ZLayer[Any, Nothing, ValidationModule] =
    ZLayer.succeed{
      val items = List(Item("bread"),Item("milk"),Item("soup"), Item("watermelon") )
      Validation(items)
    }

  val testEnvironment = testValidationLive

  def spec:Spec[TestEnvironment, TestFailure[Throwable], TestSuccess] = suite("Validation Logic Unit Tests")(
    suite("If you enter")(
      testM("PriceBasket bread milk, output should be bread,1 and milk,1") {
        val validate = for {
          items             <- ZIO.effect("PriceBasket bread milk")
          result            <- validateInput(items)
        } yield result
        assertM(validate)(equalTo(Right(List(Item("milk",Some(1)),Item("bread",Some(1))))))
      },
      testM("PriceBasket bread bread milk soup, output should be bread,2 - milk,1 - soup,1") {
        val validate = for {
          items             <- ZIO.effect("PriceBasket bread bread milk soup ")
          result            <- validateInput(items)
        } yield result
        assertM(validate)(equalTo(Right(List(Item("soup",Some(1)), Item("milk",Some(1)),Item("bread",Some(2))))))
      },
      testM("PriceBasket, output should be ItemInvalid") {
        val validate = for {
          items             <- ZIO.effect("PriceBasket")
          result            <- validateInput(items)
        } yield result
        assertM(validate)(equalTo(Left(ItemInvalid())))
      },
      testM("soup, output should be CommandInvalid") {
        val validate = for {
          items             <- ZIO.effect("soup")
          result            <- validateInput(items)
        } yield result
        assertM(validate)(equalTo(Left(CommandInvalid())))
      },
      testM("PriceBasket car, output should be ItemInvalid") {
        val validate = for {
          items             <- ZIO.effect("PriceBasket car")
          result            <- validateInput(items)
        } yield result
        assertM(validate)(equalTo(Left(ItemInvalid())))
      }
    )
  ).provideCustomLayerShared(testValidationLive)

}
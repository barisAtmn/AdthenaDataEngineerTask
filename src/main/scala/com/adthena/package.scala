package com

import com.adthena.app.{BusinessLogic, Configuration, Validation}
import zio.blocking.Blocking
import zio.clock.Clock
import zio.console.Console
import zio.logging.{Logging}
import zio.{Has, Ref}

package object adthena {

  val pound = "\u00a3"

  case class Exit()
  case class Help()

  /**
   * Error
   **/
  sealed trait InputErrorType{
    val errorMessage: String
  }
  case class CommandInvalid() extends InputErrorType {
    override val errorMessage: String = "Input has to start with PriceBasket!"
  }

  case class ItemInvalid() extends InputErrorType {
    override val errorMessage: String = "There is at least one item which is not in our good list or Basket is empty!"
  }

  type ErrorOr[A] = Either[InputErrorType, A]


  case class Price(item: Item=Item("",Some(1)), cost:String, costE: Option[Double]=Some(0), costP: Option[Double]=Some(0)) {
     def adjust(_item: Item, _cost: String): Price = _cost.toLowerCase match {
      case value if value.endsWith("p") =>  Price(_item, _cost, Some(_cost.dropRight(1).toDouble / 100),Some(_cost.dropRight(1).toDouble))
      case value if value.startsWith("£") =>  Price(_item, _cost, Some(_cost.drop(1).toDouble),Some((_cost.drop(1).toDouble * 100)))
      case _ => Price(_item, _cost, Some(1), Some(100))
    }

  }

  type Prices = List[Price]

  case class Input()

  case class Item(name:String, count:Option[Int]=Some(1))
  type Items  = List[Item]


  type Comment = (String,String)
  type Comments = List[Comment]

  case class Discount(item:Item=Item("",Some(1)), discounted:Item=Item("",Some(1)), ratio: Double = 1.0, message:String)
  type Discounts = List[Discount]

  case class Bill(items:Items, subtotal: Double=0, discounts: Discounts= List[Discount](), comments:Comments = List(("No offers available" , "")), totalPrice: Double=0){
    val getSubTotal = if (subtotal <100.0) subtotal.toInt.toString + "p" else pound + (subtotal/100).toString
    val getTotal = if (totalPrice <100.0) totalPrice.toInt.toString + "p" else pound + (totalPrice/100).toString
    def adjustDiscount(value: Double) = if (value <100.0) value.toInt.toString + "p" else pound + (value/100).toString
  }

  type CustomerBill = Ref[Bill]


  /**
   * Program Layers for DI.
   **/
  type BusinessLogicModule = Has[BusinessLogic.Service]
  type ConfigurationModule = Has[Configuration.Service]
  type ValidationModule = Has[Validation.Service]

  type modules = BusinessLogicModule with Console with ValidationModule  with Blocking with Clock with Logging
  type error   = Nothing
  type output  = Int



}

package com.adthena.app

import com.adthena._
import zio.{Task, ZIO, ZLayer}

object Validation {

  trait Service{
    def validate(input :String): Task[ErrorOr[Items]]
  }

  case class Validation(items: Items) extends Service{
    override def validate(input: String): Task[ErrorOr[Items]] = for {
      data         <- Task(input.trim.split(" ").filter(!_.isEmpty))
      head         = data.headOption
      basketItems  = if(data.size > 1) data.tail.toList else List()
    } yield validateInput(head,basketItems, items)
  }

  def validateCommand(name: Option[String]): ErrorOr[Boolean] = Either.cond(
    name.getOrElse("N/A").equalsIgnoreCase("PriceBasket"),
    true,
    CommandInvalid()
  )

  def validateItem(basketItems : List[String], items: Items): ErrorOr[Items] = Either.cond(
    basketItems.forall(items.map(_.name).contains) && !basketItems.isEmpty,
    {
      val groupBy          = basketItems.groupMapReduce(identity)(_ => 1)(_ + _)
      groupBy.map(data =>  Item(data._1, Some(data._2))).toList
    },
    ItemInvalid()
  )

  def validateInput(name: Option[String], basketItems : List[String], items: Items):ErrorOr[Items] = for {
    _          <- validateCommand(name)
    result     <- validateItem(basketItems.map(_.toLowerCase), items.map(item => item.copy(name=item.name.toLowerCase)))
  } yield result

  def live(items: Items):ZLayer[Any, Nothing, ValidationModule] = ZLayer.succeed(Validation(items))

  def validateInput(input: String):ZIO[ValidationModule, Throwable, ErrorOr[Items]] = ZIO.accessM[ValidationModule](_.get.validate(input))

}

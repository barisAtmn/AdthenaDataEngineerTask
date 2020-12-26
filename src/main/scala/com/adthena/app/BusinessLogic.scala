package com.adthena.app

import com.adthena.{BusinessLogicModule, CustomerBill, Discounts, Prices}
import zio.{Task, ZIO, ZLayer}

object BusinessLogic {

  trait Service {
    def calculateSubTotal(customerBill: CustomerBill) : Task[CustomerBill]
    def calculateDiscount(customerBill: CustomerBill): Task[CustomerBill]
    def calculateTotal(customerBill: CustomerBill): Task[CustomerBill]
  }


  case class BusinessLogic(prices : Prices, discounts : Discounts) extends Service {
    override def calculateSubTotal(customerBill: CustomerBill): Task[CustomerBill] = for {
       bill   <- customerBill.get
       items  = bill.items
        _     <- ZIO.foreach(items)(item => ZIO.foreach(prices)(price =>
        {
          if (price.item.name == item.name) {
            for {
              _   <- customerBill.update(tempBill => tempBill.copy(subtotal = tempBill.subtotal + (price.costP.get * item.count.getOrElse(1)), totalPrice = tempBill.subtotal + (price.costP.get * item.count.getOrElse(1))))
            } yield ()
          } else {
            ZIO.unit
          }
        })
        )
    } yield customerBill

    override def calculateDiscount(customerBill: CustomerBill): Task[CustomerBill] =
      for {
        bill       <- customerBill.get
        items      =  bill.items
        _     <- ZIO.foreach(discounts)(discount => ZIO.foreach(items)(item =>
        {
          if (discount.item.name == item.name && item.count.getOrElse(1) >= discount.item.count.getOrElse(1) ) {
            for {
              _   <- customerBill.update(tempBill => tempBill.copy(discounts = discount :: tempBill.discounts))
            } yield ()
          } else {
            ZIO.unit
          }
        })
        )
        updatedBill    <- customerBill.get
        billDiscounts  =  updatedBill.discounts.groupBy(_.item.name).map(grouped => grouped._2.head)
        _  <- customerBill.update(tempBill => tempBill.copy(discounts = billDiscounts.toList))
      } yield customerBill

    override def calculateTotal(customerBill: CustomerBill): Task[CustomerBill] =
      for {
        bill   <- customerBill.get
        items  = bill.items
        customerDiscounts = bill.discounts
        _     <- ZIO.foreach(customerDiscounts)(discount => ZIO.foreach(items)(item =>
        {
          if (discount.discounted.name == item.name && item.count.getOrElse(1)  >= discount.discounted.count.getOrElse(1)) {
            for {
              applyTime       <- ZIO.effect((item.count.getOrElse(1) / discount.discounted.count.getOrElse(1)).floor.toInt)
              discountAmount  = prices.filter(_.item.name == item.name).map(_.costP.get * applyTime * discount.ratio * discount.discounted.count.getOrElse(1)).head
              _               <- customerBill.update(tempBill => tempBill.copy(totalPrice = tempBill.totalPrice - discountAmount, comments = (discount.message, discountAmount.toString) :: tempBill.comments))
            } yield ()
          } else {
            ZIO.unit
          }
        })
        )
      } yield customerBill
  }


  /**
   * BusinessLogic Layer
   **/
  def BusinessLogicLive(prices : Prices, discounts : Discounts):ZLayer[Any, Nothing, BusinessLogicModule] = ZLayer.succeed(BusinessLogic(prices, discounts))

  /**
   * accessors
   **/
  def calculateSubtotal(customerBill: CustomerBill):ZIO[BusinessLogicModule, Throwable, CustomerBill] = ZIO.accessM[BusinessLogicModule](_.get.calculateSubTotal(customerBill))

  def calculateDiscount(customerBill: CustomerBill):ZIO[BusinessLogicModule, Throwable, CustomerBill] = ZIO.accessM[BusinessLogicModule](_.get.calculateDiscount(customerBill))

  def calculateTotal(customerBill: CustomerBill):ZIO[BusinessLogicModule, Throwable, CustomerBill] = ZIO.accessM[BusinessLogicModule](_.get.calculateTotal(customerBill))



}
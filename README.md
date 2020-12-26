# Adthena Data Engineer' Task
Write a program driven by unit tests that can price a basket of goods taking into account some special offers. The goods that can be purchased, together with their normal prices are:
 * Soup – 65p per tin
 * Bread – 80p per loaf
 * Milk – £1.30 per bottle
 * Apples – £1.00 per bag
Current special offers
 * Apples have a 10% discount off their normal price this week
 * Buy 2 tins of soup and get a loaf of bread for half price
 
The program should accept a list of items in the basket and output the subtotal, the special offer discounts and the final price. 

Input should be via the command line in the form `PriceBasket item1 item2 item3 ...`

## Notes

 * Configuration file includes inputs for prices, discounts and log level.
 * You can add new items to application.conf under resources directory.
 * Price item =>  `{ item: {name = ""}, cost = ""}` . Cost format : {[X]p} or {£[X]} 
 * Discount item =>  `item: {name = "" , count = 0 }, discounted: {name = "" , count = 0 }, ratio = 1, message = ""},`
    * `{name = "" , count = 0 }` => Item that you should buy. count: how many you should buy 
    * `{name = "" , count = 0 }` => Item which will be discounted. count: how many of them will be used for discount
 * log-level is OFF as default. 
```
prices:
   [
   { item: {name = "Soup"}, cost = "65p"},
   { item: {name = "Bread"}, cost = "80p"},
   { item: {name = "Milk" }, cost = "£1.30"},
   { item: {name = "Apples"}, cost = "£1.0"}
   ]
discounts:
  [
  { item: {name = "Apples" , count = 1 }, discounted: {name = "Apples" , count = 1 }, ratio = 0.1, message = "Apples 10% off!"},
  { item: {name = "Soup" , count = 2 }, discounted: {name = "Bread" , count = 1 }, ratio = 0.5, message = "Bread 50% off!"}
  ]
log-level: "OFF"
```

 * P.S : If there are discounts matched for same item, discount which is highest will be used.
package canteen.dsl

import groovy.xml.MarkupBuilder

class TransactionItem {
    String name
    int amount
    int costPerItem
}
class Transaction {
    String type
    List<TransactionItem> items
    Date timestamp
    int totalCost
}

class StockBuyer {
    Canteen canteen
    String ingredientName
    int ingredientAmount
    int ingredientCost
    
    def buy(name, amount = 1) {
        ingredientName = name
        ingredientAmount = amount
        return this
    }
    def at(cost) {
        ingredientCost = cost
        return this
    }
    def getEach() {
        canteen.addStock(ingredientName, ingredientAmount, ingredientCost)
        ingredientName = null
    }
    def getTotal() {
        ingredientCost = ingredientCost/ingredientAmount
        canteen.addStock(ingredientName, ingredientAmount, ingredientCost)
        ingredientName = null
    }
    
    def dump(name, amount = 0) {
        canteen.deleteStock(name,amount)
    }
    
    def getPrint() {
        doPrint(canteen)
    }
    private static doPrint(canteen) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        
        xml.stock {
            for(e in canteen.ingredientStock) {
                "${e.key}" "${e.value}"
            }
        }
        
        println writer
   }
    
}

class Order {
    String menuName
    int menuCount
}

class MenuOrderer {
    Canteen canteen
    List<Order> orders = []
    int chairUsed

    def methodMissing(String name, args) {
        int count = args ? args[0] : 0
        Order o = new Order(menuName: name, menuCount: count)
        orders << o
    }

    def of(count) {
        chairUsed = count
    }

    def getTakeaway() {
        chairUsed = 0
        canteen.orderStock(orders, chairUsed)
    }

    def getDinein() {
        canteen.orderStock(orders, chairUsed)
    }

}

class FoodIngredient {
    String name
    int amount
}

class Food {
    List<FoodIngredient> ingredients = []
    int cost
    
    def ingredient(name, amount){
        name = name.toLowerCase()
        ingredients << (new FoodIngredient(name:name,amount:amount))
    }
    
    def price(amount){
        cost = amount
    }
    
}

class Menu {
    Canteen canteen

    def add(name, closure){
        Food f = new Food()
        closure.delegate = f
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        closure.call()
        canteen.addFoodToMenu(name,f)
    }

    def delete(name) {
        canteen.deleteFoodInMenu(name)
    }

    def getPrint() {
        doPrint(canteen)
    }

    private static doPrint(canteen) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)
        
        xml.menu {
            for(e in canteen.foodMenu) {
                "Food" {
                    "Name" "${e.key}"
                    "Ingredients" {
                        for(ing in e.value.ingredients) {
                            "Ingredient" {
                                "Name" "${ing.name}"
                                "Amount" "${ing.amount}"
                            } 
                        }   
                    }
                    "Price" "${e.value.cost}"
                }
            }
        }
        
        println writer
   }
}

class Canteen {
    static ingredientStock = [:]
    static foodMenu = [:]
    static List<Transaction> transactionLog
    static int chairs
    
    /**
     * This method accepts a closure which is essentially the DSL. Delegate the 
     * closure methods to the DSL class so the calls can be processed
     */
    def static process(closure) {
        Canteen c = new Canteen()
        
        // If this is the first call, initialize the static variables
        if(chairs == 0) {
            chairs = 10
            ingredientStock."rice" = 0
            ingredientStock."chicken meat" = 0
            transactionLog = []
        }
        
        // any method called in closure will be delegated to the memoDsl class
        closure.delegate = c
        closure.call()
    }
    
    def stock(closure) {
        StockBuyer b = new StockBuyer(canteen: this)
        closure.delegate = b
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        
        def trans = new Transaction(
            type: "Buying Ingredients",
            items: [],
            timestamp: new Date(), 
            totalCost: 0,
        )
        transactionLog << trans
        
        closure.call()
        
        for(item in transactionLog.last().items) {
            transactionLog.last().totalCost += item.costPerItem*item.amount
        }
        if(transactionLog.last().totalCost == 0) {
            transactionLog.pop()
        }
    }

    def order(closure) {
        MenuOrderer m = new MenuOrderer(canteen: this)
        closure.delegate = m
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        
        def trans = new Transaction(
            type: "Ordered Menu",
            items: [],
            timestamp: new Date(),
            totalCost: 0,
        )
        transactionLog << trans
        
        closure.call()
        
        for(item in transactionLog.last().items) {
            transactionLog.last().totalCost += item.costPerItem*item.amount
        }
        if(transactionLog.last().totalCost == 0) {
            transactionLog.pop()
        }
    }
    
    def menu(closure) {
        Menu m = new Menu(canteen:this)
        closure.delegate = m
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        
        closure.call()
    }
    
    def addFoodToMenu(name, food){
        name = name.toLowerCase()
        if(foodMenu.get(name) == null) {
            foodMenu[name] = food
            println("DONE: " + name + " has been added")
        } else {
            println("ERROR: " + name + " is already in a menu")
        }
    }
    
    def deleteFoodInMenu(name) {
        name = name.toLowerCase()
        if(foodMenu.get(name) != null) {
            foodMenu.remove(name)
            println("DONE: " + name + " has been deleted")
        } else {
            println("ERROR: " + name + " does not exist in menu")
        }
    }
    
    def orderStock(orders, chairUsed) {
        if (this.chairs >= chairUsed) {
            this.chairs -= chairUsed
        } else {
            println 'Order cancelled: chairs not sufficient.'
            return
        }

        for(order in orders) {
            String menuName = order.menuName.toLowerCase()
            int menuCount = order.menuCount
            boolean isIgnore = false

            // Ignore menu if menu is not available(Implicit Cancellation by Canteen)
            if (foodMenu.get(menuName) == null) {
                this.chairs += chairUsed
                println 'Order ditolak karena stok bahan baku tidak ada'
                break
            }
            // Else
            // Iterate ingredients of the menu
            for (ingredient in foodMenu[menuName].ingredients) {
                // check if every ingredient stock is exist
                if (ingredientStock.get(ingredient.name) == null) {
                    println 'Order ditolak karena stok bahan baku tidak ada'
                    isIgnore = true
                    break
                }
                if (ingredientStock[ingredient.name] < ingredient.amount * menuCount) {
                    isIgnore = true
                    println 'Order ditolak karena stok bahan baku tidak cukup'
                    break
                }
            }
            if (isIgnore) {
                isIgnore = false
                this.chairs += chairUsed
                break
            } else {
                // println 'Else'
                // println ingredientStock
                // If every ingredient stock is exist
                for (ingredient in foodMenu[menuName].ingredients) {
                    // update stock
                    // println foodMenu[menuName].ingredients
                    // println menuName
                    // println menuCount
                    // println ingredient.name
                    ingredientStock[ingredient.name] -= ingredient.amount * menuCount
                }

                // define cost per item
                int menuCost = foodMenu[menuName].cost

                transactionLog.last().items << new TransactionItem(
                    name: menuName,
                    amount: menuCount,
                    costPerItem: menuCost
                )
            }
            
        }
    }

    def addStock(name, amount, price) {
        
        name = name.toLowerCase()
        if(ingredientStock.get(name) == null) {
            ingredientStock[name] = 0
        }
        ingredientStock[name] += amount
        
        transactionLog.last().items << new TransactionItem(
            name: name,
            amount: amount,
            costPerItem: price
        )
    }
    def deleteStock(name, amount) {
        name = name.toLowerCase()
        if(ingredientStock.get(name) != null) {
            if(ingredientStock.get(name) < amount) {
                ingredientStock[name] = 0
            } else {
                ingredientStock[name] -= amount
            }
        }
    }
    
    def getAudit() {
        doAudit(this)
    }
    private static doAudit(canteen) {
        def writer = new StringWriter()
        def xml = new MarkupBuilder(writer)

        xml.log {   
            for(trans in canteen.transactionLog) {
                "transaction" {

                    "type" trans.type
                    
                    for(i in trans.items) {
                        "item" {

                            "name" i.name
                            "amount" i.amount
                            "@cost" i.costPerItem

                        }
                    }
                    
                    "timestamp" trans.timestamp
                    "total cost" trans.totalCost
                
                }
            }
            
        }
        
        println writer
        canteen.transactionLog.clear()
   }

   def getPrintRemainder() {
       doPrintRemainder(this)
   }

   private static doPrintRemainder(canteen) {
        println '==== Stock remained ===='
        canteen.ingredientStock.each { k, v ->
            println "${k} - ${v}"
        }
   }
    
    def methodMissing(String methodName, args) {
        System.out.println(methodName)
    }
    
}

Canteen.process {
    stock {
        buy "rice", 100 at 1000 each
        buy "chicken meat", 10 at 10000 total
        buy "ketchup", 100 at 1000 each
        buy "air", 100 at 100 each
        buy "teh", 100 at 100 each
        dump "rice", 50
        print
    }
    
    menu {
        add "nasi goreng", {
            ingredient "rice", 10
            price 10000
        }
        
        add "es teh", {
            ingredient "air", 10
            ingredient "teh", 10
            price 1000
        }
        
        add "nasi bakar", {
            ingredient "rice", 10
            ingredient "soy sauce", 5
            price 1000
        }
        
        delete "nasi bakar"
    }

    order {
        of 2
        "nasi goreng" 6
        "es teh" 2
        dinein
    }

    order {
        of 2
        "nasi goreng" 2
        "es teh" 1
        dinein
    }

    printRemainder
    
    // audit
}
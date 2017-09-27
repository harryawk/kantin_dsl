package Groovy

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

class FoodIngredient {
    String name
    int amount
}
class Food {
    FoodIngredient[] ingredients
    int cost
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
    
    def methodMissing(String methodName, args) {
        System.out.println(methodName)
    }
    
}

Canteen.process {
    stock {
        buy "rice", 100 at 1000 each
        buy "chicken meat", 10 at 10000 total
        dump "rice", 50
        print
    }
    
    audit
}

/**
Canteen.process {
    stock {
        buy "rice", 100 at 1000 each
        buy "chicken meat", 10 at 10000 total
        
        dump "rice", 10
        print
    }
    
    menu {
        add "nasi goreng" {
            ingredient "rice", 10
            ingredient "soy sauce", 5
            price 1000
        }
        
        delete "nasi goreng"
    }
    
    order {
        "nasi goreng" 2
        "es teh" 1
        takeaway
    }
    
    order {
        for 2 person
        "nasi goreng" 2
        "es teh" 1
        dinein
    }
    
    leave 1 person

    audit
}
 * */
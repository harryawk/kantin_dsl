process {
    stock {
        buy "rice", 100 at 1000 each
        buy "chicken meat", 10 at 10000 total
        buy "ketchup", 100 at 1000 each
        dump "rice", 50
        print
    }
    
    menu {
        add "nasi goreng", {
            ingredient "rice", 10
            price 1000
        }
        
        // delete "nasi bakar"
    }

    order {
        of 2
        "nasi goreng" 6
        dinein
    }

    printRemainder
    
    // audit
}
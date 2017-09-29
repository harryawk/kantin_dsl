# DSL Kantin

By:
K-02
* 13514030 Aditio Pangestu
* 13514036 Harry Alvin Waidan Kefas
* 13514104 Fairuz Astra Pratama

## Grammar

Untuk memudahkan pemahaman terhadap dsl yang kami buat. Akan ditampilkan contoh penggunaanya.

```
Canteen.process {
    stock {
        buy "rice", 100 at 1000 each
        buy "chicken meat", 10 at 10000 total
        dump "rice", 50
        print
    }
    
    menu {
        add "nasi goreng", {
            ingredient "rice", 10
            ingredient "soy sauce", 5
            price 1000
        }
        
        delete "nasi goreng"
    }

    order {
        of 2
        "nasi goreng" 2
        "es teh" 3
        dinein
    }
    
    audit
}
```
Pada source code di atas dapat dilihat proses kantin yang datangani berupa manajemen stok, manjamen menu dan pemesanan.

### Manajemen Stok
tulis di sini

### Manajemen Menu
Menu merupakan daftar makanan atau minuman yang dijual oleh kantin. Tiap makanan dan minuman akan diberitahu bahan-bahan yang akan digunakan beserta harga dari makanan tersebut. Pada DSL yang dibuat, kita dapat menambah makanan ke menu dan menghapus makanan dari menu.

### Pemesanan
tulis di sini
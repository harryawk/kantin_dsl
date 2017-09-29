# DSL Kantin K-02
- 13514030 Aditio Pangestu
- 13514036 Harry Alvin Waidan Kefas
- 13514104 Fairuz Astra Pratama

Untuk memudahkan pemahaman terhadap dsl yang kami buat. Akan ditampilkan contoh penggunaanya.

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

Pada source code di atas dapat dilihat proses kantin yang datangani berupa manajemen stok, manajemen menu, pemesanan dan pencatatan transaksi.

### Manajemen Stok
Stok merupakan pasangan nama dan jumlah bahan baku yang disimpan di kantin, dimana nama merupakan *identifier* yang akan digunakan untuk mengaksesnya. Manajemen stok dapat dilaksanakan dengan menuliskan perintah di dalam scope "stock" seperti berikut

	    stock {
	        // Input command here
	    }

Ada tiga jenis operasi yang dapat dilakukan dalam scope "stock", diantaranya:

1. **Pembelian Stock**, untuk menambahkan jumlah stock yang disimpan. Ada dua cara pemanggilan:

		buy <<Ingredient_Name>>, <<Amount>> at <<@Price>> each
				or
		buy <<Ingredient_Name>>, <<Amount>> at <<Total_Price>> total

2. **Pengurangan Stock**, untuk mengurangi jumlah stock yang disimpan.

        dump <<Ingredient_Name>>, <<Amount>>

3. **Pengecekan jumlah Stock**, untuk mencetak nama dan jumlah bahan baku yang disimpan.

        print

Berikut adalah contoh penggunaan fitur manajemen stok,

    stock {
        buy "rice", 100 at 1000 each			// Menambah stok 100 "rice" dengan harga 1000 per unitnya
        buy "chicken meat", 10 at 10000 total	// Menambah stok 10 "chicken meat" dengan harga total 10000
        dump "rice", 50							// Mengurangi stok "rice" sebanyak 50
        print									// Mengeprint kondisi stok
    }
	
	// Hasil ekseskusi perintah diatas
	<stock>
	  <rice>50</rice>
	  <chicken meat>10</chicken meat>
	</stock>

### Manajemen Menu
Menu merupakan daftar makanan atau minuman yang dijual oleh kantin. Tiap makanan dan minuman akan diberitahu bahan-bahan yang akan digunakan beserta harga dari makanan tersebut. Pada tiap makanan dan minuman juga terdapat nama sebagai *identifier* yang digunakan untuk mengaksesnya. Manajemen menu dapat dilaksanakan dengan menuliskan perintah di dalam scope "menu" seperti berikut
    
    menu {
        // Input command here
    }

Ada tiga jenis operasi yang dapat dilakukan dalam scope "menu", diantaranya:

1. **Penambahan makanan pada menu**, untuk menambahkan makanan ke dalam daftar menu. Cara pemanggilannya:

        add <<Food_Name>>, {
            ingredient <<Ingredient_Name>>, <<Amount>>
            ingredient <<Ingredient_Name>>, <<Amount>>
            ...
            ingredient <<Ingredient_Name>>, <<Amount>>
            price <<Price>>
        }

2. **Penghapusan makanan dari menu**, untuk menghapus makanan dari daftar menu. Cara pemanggilannya:
        
        delete <<Food_Name>>

3. **Pengecekan menu**, untuk mencetak nama, bahan baku dan harga dari suatu makanan atau minuman.

        print

Berikut adalah contoh penggunaan fitur manajemen menu,

    menu {
        
        add "nasi goreng", {            //Menambahkan nasi goreng ke menu
            ingredient "rice", 10       //Bahan nasi goreng
            ingredient "soy sauce", 5   //Bahan nasi goreng
            price 1000                  //Harga nasi goreng
        }
        
        add "nasi bakar", {
            ingredient "rice", 10
            ingredient "soy sauce", 5
            price 1000
        }
        
        delete "nasi bakar"             // Mengahapus nasi goreng dari menu

        print                           //Mngeprint menu
    }


### Pemesanan
Pemesanan / *order* merupakan daftar makanan atau minuman yang dipesan / di-*order* oleh pelanggan. Suatu pemesanan / *order* memiliki jenis, diantaranya *takeaway* dan *dine-in*. Pemesanan / *order* jenis *dine-in* harus memberitahukan jumlah tempat makan yang hendak digunakan.
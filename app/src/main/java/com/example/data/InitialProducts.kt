package com.example.data

import org.json.JSONArray
import org.json.JSONObject

object InitialProducts {

    /**
     * Verbatim product catalog provided for Aadi Aarambh Private Limited.
     * Note: Settings also includes a JSON catalog importer to add or replace additional items.
     */
    val ITEMS: List<Product> = listOf(
        Product(slNo = 1, name = "A2 Desi Cow Ghee - 1L", hsnCode = "5223283649", stock = 0, price = 1780.0),
        Product(slNo = 2, name = "A2 Desi cow Ghee - 250 ML", hsnCode = "3709612689", stock = 19, price = 499.0),
        Product(slNo = 3, name = "A2 Desi Cow Ghee - 500 ML", hsnCode = "5512188001", stock = 16, price = 900.0),
        Product(slNo = 4, name = "A2 Gir Cow Bilona Ghee 1 L", hsnCode = "7789957456", stock = 5, price = 2450.0),
        Product(slNo = 5, name = "A2 Gir Cow Ghee 200 ML", hsnCode = "", stock = 17, price = 550.0),
        Product(slNo = 6, name = "A2 Gir Cow Ghee 500 ML", hsnCode = "1559172253", stock = 1, price = 1250.0),
        Product(slNo = 7, name = "A2 Gir Cow Ghee trial", hsnCode = "", stock = 0, price = 0.0),
        Product(slNo = 8, name = "acacia honey 250 gm", hsnCode = "8378236878", stock = 9, price = 380.0),
        Product(slNo = 9, name = "Advance Green Tea - 90 Tabs", hsnCode = "8606230217", stock = 2, price = 700.0),
        Product(slNo = 10, name = "Amla Candy Spicy", hsnCode = "9724347432", stock = 12, price = 100.0),
        Product(slNo = 11, name = "amla candy sweet", hsnCode = "", stock = 0, price = 100.0),
        Product(slNo = 12, name = "Anion Sanitary Pad", hsnCode = "", stock = 84, price = 50.0),
        Product(slNo = 13, name = "Apulse", hsnCode = "", stock = 0, price = 2000.0),
        Product(slNo = 14, name = "Ayuvita", hsnCode = "3695136849", stock = 0, price = 480.0),
        Product(slNo = 15, name = "Bajra / Pearl Millet 1 KG", hsnCode = "", stock = 9, price = 85.0),
        Product(slNo = 16, name = "Barley Dehusked / Jau 1 Kg", hsnCode = "3441916925", stock = 9, price = 115.0),
        Product(slNo = 17, name = "Barnyard Millet 1kg", hsnCode = "8154661167", stock = 0, price = 270.0),
        Product(slNo = 18, name = "Barnyard Millet 500 gm", hsnCode = "", stock = 2, price = 110.0),
        Product(slNo = 19, name = "Basmati rice", hsnCode = "", stock = 0, price = 160.0),
        Product(slNo = 20, name = "Basmati Rice (Long)", hsnCode = "", stock = 14, price = 180.0),
        Product(slNo = 21, name = "Black Pepper", hsnCode = "", stock = 0, price = 40.0),
        Product(slNo = 22, name = "Black Sesame Oil 1L", hsnCode = "", stock = 0, price = 230.0),
        Product(slNo = 23, name = "Black Sesame Oil 500 ML", hsnCode = "6723238703", stock = 5, price = 150.0),
        Product(slNo = 24, name = "Brown Rice 1Kg", hsnCode = "1003672358", stock = 5, price = 235.0),
        Product(slNo = 25, name = "Brown Rice 500 gm", hsnCode = "5231343805", stock = 19, price = 125.0),
        Product(slNo = 26, name = "Brown sugar 1 Kg", hsnCode = "8500677095", stock = 0, price = 110.0),
        Product(slNo = 27, name = "Browntop Millet 1 kg", hsnCode = "", stock = 0, price = 295.0),
        Product(slNo = 28, name = "Chana Dal 1kg", hsnCode = "8346926372", stock = 10, price = 165.0),
        Product(slNo = 29, name = "Chana dal 500 gm", hsnCode = "3175636739", stock = 20, price = 90.0),
        Product(slNo = 30, name = "chaula / lobiya 1Kg", hsnCode = "5811642398", stock = 9, price = 165.0),
        Product(slNo = 31, name = "chaula / Lobiya 500g", hsnCode = "", stock = 18, price = 90.0),
        Product(slNo = 32, name = "chia seeds", hsnCode = "2376749667", stock = 3, price = 125.0),
        Product(slNo = 33, name = "Chips (Millet) -150gm", hsnCode = "", stock = 15, price = 140.0),
        Product(slNo = 34, name = "Chips (Mix Veggies) - 150 gm", hsnCode = "8352588271", stock = 16, price = 140.0),
        Product(slNo = 35, name = "Coconut Oil 200 ML", hsnCode = "9111291520", stock = 0, price = 150.0),
        Product(slNo = 36, name = "Coconut Oil 500 ML", hsnCode = "5311544723", stock = 0, price = 350.0),
        Product(slNo = 37, name = "Cold pressed Groundnut Oil 1 L", hsnCode = "5498199674", stock = 4, price = 330.0),
        Product(slNo = 38, name = "Dcare", hsnCode = "6497675135", stock = 0, price = 700.0),
        Product(slNo = 39, name = "Desi Chana 1 Kg", hsnCode = "1511307700", stock = 16, price = 145.0),
        Product(slNo = 40, name = "Desi Chana 500gm", hsnCode = "", stock = 15, price = 80.0),
        Product(slNo = 41, name = "desi rai 100 gm", hsnCode = "", stock = 5, price = 30.0),
        Product(slNo = 42, name = "Desi Rai 200g", hsnCode = "4425968952", stock = 5, price = 60.0),
        Product(slNo = 43, name = "Dhaniya Powder 200gm", hsnCode = "2051442402", stock = 11, price = 95.0),
        Product(slNo = 44, name = "Dhaniya Sabut", hsnCode = "", stock = 0, price = 30.0),
        Product(slNo = 45, name = "Extra Virgin Olive Oil 100 ML", hsnCode = "4170166889", stock = 0, price = 150.0),
        Product(slNo = 46, name = "Extra Virgin Olive Oil 500 ML", hsnCode = "7919607282", stock = 5, price = 600.0),
        Product(slNo = 47, name = "Fennel / Saunf", hsnCode = "", stock = 0, price = 70.0),
        Product(slNo = 48, name = "Flax Seeds 200gm", hsnCode = "9263871596", stock = 7, price = 70.0),
        Product(slNo = 49, name = "Foxtail Millet / Kangni", hsnCode = "5382977496", stock = 4, price = 160.0),
        Product(slNo = 50, name = "Foxtail Millet 500 gm", hsnCode = "", stock = 4, price = 88.0),
        Product(slNo = 51, name = "Garam Masala", hsnCode = "", stock = 10, price = 110.0),
        Product(slNo = 52, name = "Groundnut Oil 5L", hsnCode = "1169740211", stock = 0, price = 1600.0),
        Product(slNo = 53, name = "Hair Revive Pro", hsnCode = "4426875411", stock = 0, price = 450.0),
        Product(slNo = 54, name = "Honey 250gm", hsnCode = "7341926322", stock = 4, price = 200.0),
        Product(slNo = 55, name = "Jaggery Block", hsnCode = "", stock = 0, price = 175.0),
        Product(slNo = 56, name = "Jaggery Cube 1 kg", hsnCode = "", stock = 0, price = 140.0),
        Product(slNo = 57, name = "Jaggery Cubes 500 gm", hsnCode = "", stock = 0, price = 70.0),
        Product(slNo = 58, name = "Jaggery Masala Tea Premix 475gm", hsnCode = "", stock = 0, price = 190.0),
        Product(slNo = 59, name = "Jaggery Powder 1kg", hsnCode = "9006663030", stock = 20, price = 150.0),
        Product(slNo = 60, name = "Jaggery tea premix 160 gm", hsnCode = "", stock = 30, price = 70.0),
        Product(slNo = 61, name = "Jeera 200gm", hsnCode = "1581271844", stock = 10, price = 130.0),
        Product(slNo = 64, name = "Khandsari 1Kg", hsnCode = "9487902534", stock = 17, price = 140.0),
        Product(slNo = 65, name = "khandsari 500gm", hsnCode = "", stock = 0, price = 80.0),
        Product(slNo = 66, name = "Kodo Millet 1 kg", hsnCode = "3660760099", stock = 0, price = 180.0),
        Product(slNo = 67, name = "kodo millet 500 gm", hsnCode = "", stock = 5, price = 99.0),
        Product(slNo = 68, name = "Lakadong Turmeric Powder 80gm", hsnCode = "4234915720", stock = 5, price = 150.0),
        Product(slNo = 69, name = "Little Millet / Kutki 1 Kg", hsnCode = "4116408006", stock = 0, price = 185.0),
        Product(slNo = 70, name = "Little millet / kutki 500 gm", hsnCode = "", stock = 4, price = 100.0),
        Product(slNo = 71, name = "long 50gm", hsnCode = "", stock = 0, price = 86.0),
        Product(slNo = 72, name = "Masoor Dal 1Kg", hsnCode = "7839761330", stock = 7, price = 175.0),
        Product(slNo = 73, name = "masoor dal 500 gm", hsnCode = "8440821415", stock = 10, price = 95.0),
        Product(slNo = 74, name = "moong dal Chhilka 1kg", hsnCode = "9753642243", stock = 15, price = 195.0),
        Product(slNo = 75, name = "moong dal chhilka 500gm", hsnCode = "6311536637", stock = 14, price = 105.0),
        Product(slNo = 76, name = "moong mogar 1kg", hsnCode = "8293540227", stock = 9, price = 205.0),
        Product(slNo = 77, name = "moong mogar 500gm", hsnCode = "", stock = 11, price = 110.0),
        Product(slNo = 78, name = "Multi Millet Pasta (Fueslli)", hsnCode = "5256858817", stock = 0, price = 60.0),
        Product(slNo = 79, name = "Multi Millet Pasta (Macaroni)", hsnCode = "", stock = 0, price = 60.0),
        Product(slNo = 80, name = "Mustard Oil 100 ML", hsnCode = "", stock = 8, price = 40.0),
        Product(slNo = 81, name = "Mustard Oil 1L", hsnCode = "6764587980", stock = 6, price = 310.0),
        Product(slNo = 82, name = "Mustard Oil 500 ML", hsnCode = "", stock = 0, price = 150.0),
        Product(slNo = 83, name = "Paan Jaggery", hsnCode = "", stock = 0, price = 100.0),
        Product(slNo = 84, name = "pasta jowar (Fusilli) - 200gm", hsnCode = "", stock = 16, price = 60.0),
        Product(slNo = 85, name = "pasta jowar (macaroni) - 200 gm", hsnCode = "", stock = 18, price = 60.0),
        Product(slNo = 86, name = "Pasta Ragi (fusilli) - 200gm", hsnCode = "3694779907", stock = 17, price = 60.0),
        Product(slNo = 87, name = "Pasta Ragi (Macaroni) 200 Gm", hsnCode = "8916609039", stock = 20, price = 60.0),
        Product(slNo = 88, name = "Pink Salt", hsnCode = "", stock = 42, price = 80.0),
        Product(slNo = 89, name = "Poha 1Kg", hsnCode = "8553583321", stock = 13, price = 110.0),
        Product(slNo = 90, name = "Poha 500gm", hsnCode = "", stock = 10, price = 60.0),
        Product(slNo = 91, name = "Pumpkin Seeds 200 gm", hsnCode = "", stock = 6, price = 190.0),
        Product(slNo = 92, name = "Quinoa 1kg", hsnCode = "5089035089", stock = 7, price = 195.0),
        Product(slNo = 93, name = "Ragi Millet 1 Kg", hsnCode = "9964218191", stock = 20, price = 115.0),
        Product(slNo = 94, name = "ragi millet 500gm", hsnCode = "", stock = 20, price = 62.0),
        Product(slNo = 97, name = "Red Chilly Powder ( bediya) - 200gm", hsnCode = "6299220523", stock = 9, price = 140.0),
        Product(slNo = 98, name = "Red Rajma 1 kg", hsnCode = "", stock = 5, price = 225.0),
        Product(slNo = 99, name = "Red Rajma 500 gm", hsnCode = "", stock = 16, price = 120.0),
        Product(slNo = 100, name = "Rolled Oats 1KG", hsnCode = "", stock = 0, price = 225.0),
        Product(slNo = 101, name = "Rolled Oats 500 gm", hsnCode = "", stock = 0, price = 120.0),
        Product(slNo = 102, name = "Sabja Seeds 200 gm", hsnCode = "5748603207", stock = 5, price = 115.0),
        Product(slNo = 103, name = "SBTC", hsnCode = "", stock = 0, price = 1199.0),
        Product(slNo = 105, name = "Selam Turmeric Powder 200 gm", hsnCode = "1244397772", stock = 9, price = 150.0),
        Product(slNo = 106, name = "Sesame Oil 1L", hsnCode = "", stock = 0, price = 380.0),
        Product(slNo = 107, name = "sesame Oil 500 ML", hsnCode = "9599749787", stock = 10, price = 190.0),
        Product(slNo = 108, name = "Shilajit Honey 20 gm", hsnCode = "9272043730", stock = 4, price = 150.0),
        Product(slNo = 109, name = "Shilajit Resin 20 gm", hsnCode = "5056104458", stock = 8, price = 700.0),
        Product(slNo = 110, name = "Slim Tea", hsnCode = "", stock = 0, price = 1800.0),
        Product(slNo = 111, name = "Sunflower Oil 1L", hsnCode = "", stock = 0, price = 320.0),
        Product(slNo = 112, name = "Sunflower Oil 500 ML", hsnCode = "", stock = 3, price = 170.0),
        Product(slNo = 113, name = "Til / white sesame", hsnCode = "", stock = 0, price = 150.0),
        Product(slNo = 114, name = "Toor / Arhar Daal 1Kg", hsnCode = "", stock = 8, price = 190.0),
        Product(slNo = 115, name = "toor dal hand processed 1kg", hsnCode = "", stock = 8, price = 175.0),
        Product(slNo = 116, name = "toor dal hand processed 500 gm", hsnCode = "", stock = 3, price = 97.0),
        Product(slNo = 117, name = "toor dal sortex 1kg", hsnCode = "7182001919", stock = 7, price = 245.0),
        Product(slNo = 118, name = "toor dal sortex 500 gm", hsnCode = "1414071706", stock = 5, price = 125.0),
        Product(slNo = 119, name = "urad dal white 1Kg", hsnCode = "", stock = 15, price = 220.0),
        Product(slNo = 120, name = "urad dal white 500 g", hsnCode = "", stock = 19, price = 120.0),
        Product(slNo = 121, name = "Whole Moong 1 Kg", hsnCode = "2032469125", stock = 14, price = 185.0),
        Product(slNo = 122, name = "whole moong 500 gm", hsnCode = "", stock = 16, price = 105.0),
        Product(slNo = 123, name = "wild forest Honey 500gm", hsnCode = "9928838421", stock = 3, price = 380.0),
        Product(slNo = 124, name = "Wood Pressed Groundnut Oil", hsnCode = "5141924449", stock = 8, price = 330.0)
    )

    fun parseProductsJson(rawJson: String): Result<List<Product>> {
        return runCatching {
            val trimmed = rawJson.trim()
            val array = JSONArray(trimmed)
            val parsed = mutableListOf<Product>()
            for (i in 0 until array.length()) {
                val obj: JSONObject = array.getJSONObject(i)
                val slNo = obj.optInt("slNo", i + 1)
                val name = obj.optString("name", "").trim()
                if (name.isEmpty()) continue
                val hsnCode = obj.optString("hsnCode", "").trim()
                val stock = obj.optInt("stock", 0)
                val price = obj.optDouble("price", 0.0).takeIf { !it.isNaN() } ?: 0.0
                parsed.add(
                    Product(
                        slNo = slNo,
                        name = name,
                        hsnCode = hsnCode,
                        stock = stock,
                        price = price
                    )
                )
            }
            require(parsed.isNotEmpty()) { "No valid products found in JSON array" }
            parsed
        }
    }
}

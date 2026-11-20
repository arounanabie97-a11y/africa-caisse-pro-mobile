
// ==========================================
// AFRICA CAISSE PRO
// TABLEAU DE BORD ET GESTION COMMERCIALE
// ==========================================

// MODELE PRODUIT

private data class ProductItem(
    val id: String,
    val name: String,
    val category: String,
    val purchasePrice: Long,
    val salePrice: Long,
    val quantity: Int
)

// STOCKAGE LOCAL DES PRODUITS

private fun productPreferences() =
    getSharedPreferences(
        "merchant_products_${auth.currentUser?.uid ?: "local"}",
        MODE_PRIVATE
    )

private fun getProducts(): MutableList<ProductItem> {
    val json = productPreferences()
        .getString("products", "[]") ?: "[]"

    val products = mutableListOf<ProductItem>()

    try {
        val array = org.json.JSONArray(json)

        for (i in 0 until array.length()) {
            val item = array.getJSONObject(i)

            products.add(
                ProductItem(
                    id = item.getString("id"),
                    name = item.getString("name"),
                    category = item.getString("category"),
                    purchasePrice = item.getLong("purchasePrice"),
                    salePrice = item.getLong("salePrice"),
                    quantity = item.getInt("quantity")
                )
            )
        }
    } catch (e: Exception) {
        Toast.makeText(
            this,
            "Erreur de lecture des produits",
            Toast.LENGTH_SHORT
        ).show()
    }

    return products
}

private fun saveProducts(products: List<ProductItem>) {
    val array = org.json.JSONArray()

    products.forEach { product ->
        val item = org.json.JSONObject()

        item.put("id", product.id)
        item.put("name", product.name)
        item.put("category", product.category)
        item.put("purchasePrice", product.purchasePrice)
        item.put("salePrice", product.salePrice)
        item.put("quantity", product.quantity)

        array.put(item)
    }

    productPreferences()
        .edit()
        .putString("products", array.toString())
        .apply()
}

// ==========================================
// TABLEAU DE BORD
// ==========================================

private fun showHome() {

    val layout = createLayout()

    addTitle(layout)

    addSubtitle(
        layout,
        "TABLEAU DE BORD COMMERCANT"
    )

    val user = auth.currentUser

    val welcome = TextView(this).apply {
        text = "Bienvenue dans votre espace commercial"
        textSize = 17f
        setTextColor(Color.rgb(15, 23, 42))
        gravity = Gravity.CENTER
        setPadding(0, dp(15), 0, dp(20))
    }

    layout.addView(welcome)

    addButton(layout, "MON ETABLISSEMENT") {
        showCommerceScreen()
    }

    addButton(layout, "PRODUITS ET STOCK") {
        showProductsScreen()
    }

    addButton(layout, "ENREGISTRER UNE VENTE") {
        showModule(
            "NOUVELLE VENTE",
            "Selection des produits, quantites, calcul du total et validation de la vente."
        )
    }

    addButton(layout, "HISTORIQUE DES VENTES") {
        showModule(
            "HISTORIQUE",
            "Consultez toutes vos transactions commerciales."
        )
    }

    addButton(layout, "DEPENSES ET BENEFICES") {
        showModule(
            "GESTION FINANCIERE",
            "Suivi des depenses, recettes et benefices."
        )
    }

    addButton(layout, "RAPPORTS") {
        showModule(
            "RAPPORTS",
            "Bilans journaliers, mensuels et annuels."
        )
    }

    val account = TextView(this).apply {
        text = "Compte : ${user?.phoneNumber ?: "Mode developpement"}"
        textSize = 12f
        setTextColor(Color.DKGRAY)
        gravity = Gravity.CENTER
        setPadding(0, dp(20), 0, dp(10))
    }

    layout.addView(account)

    addButton(layout, "SE DECONNECTER") {
        auth.signOut()
        showPhoneScreen()
    }

    setContentView(layout)
}

// ==========================================
// ETABLISSEMENT COMMERCIAL
// ==========================================

private fun showCommerceScreen() {

    val layout = createLayout()

    addTitle(layout)

    addSubtitle(
        layout,
        "ENREGISTREMENT DE VOTRE ETABLISSEMENT"
    )

    val prefs = getSharedPreferences(
        "merchant_profile_${auth.currentUser?.uid ?: "local"}",
        MODE_PRIVATE
    )

    fun createField(
        hintText: String,
        savedKey: String
    ): EditText {

        return EditText(this).apply {
            hint = hintText
            textSize = 15f
            setSingleLine(true)
            setText(prefs.getString(savedKey, ""))
        }
    }

    val name = createField("Nom du commerce", "name")
    val owner = createField("Nom du gerant", "owner")
    val phone = createField("Telephone commercial", "phone")
    val city = createField("Ville", "city")
    val type = createField("Activite commerciale", "type")

    layout.addView(name)
    layout.addView(owner)
    layout.addView(phone)
    layout.addView(city)
    layout.addView(type)

    addButton(layout, "ENREGISTRER MON COMMERCE") {

        if (name.text.toString().trim().isEmpty()) {

            Toast.makeText(
                this,
                "Veuillez saisir le nom du commerce",
                Toast.LENGTH_SHORT
            ).show()

        } else {

            prefs.edit()
                .putString("name", name.text.toString())
                .putString("owner", owner.text.toString())
                .putString("phone", phone.text.toString())
                .putString("city", city.text.toString())
                .putString("type", type.text.toString())
                .apply()

            Toast.makeText(
                this,
                "Informations enregistrees",
                Toast.LENGTH_LONG
            ).show()

            showHome()
        }
    }

    addButton(layout, "RETOUR AU TABLEAU DE BORD") {
        showHome()
    }

    setContentView(layout)
}

// ==========================================
// LISTE DES PRODUITS
// ==========================================

private fun showProductsScreen() {

    val layout = createLayout()

    addTitle(layout)

    addSubtitle(
        layout,
        "GESTION DES PRODUITS ET STOCKS"
    )

    val products = getProducts()

    val summary = TextView(this).apply {
        text = "NOMBRE DE PRODUITS : ${products.size}"
        textSize = 17f
        setTextColor(Color.rgb(15, 23, 42))
        gravity = Gravity.CENTER
        setPadding(0, dp(15), 0, dp(20))
    }

    layout.addView(summary)

    addButton(layout, "+ AJOUTER UN PRODUIT") {
        showProductDialog()
    }

    if (products.isEmpty()) {

        val empty = TextView(this).apply {
            text = "Aucun produit enregistre pour le moment."
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, dp(30), 0, dp(30))
            setTextColor(Color.DKGRAY)
        }

        layout.addView(empty)
    }

    products.forEach { product ->

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            setBackgroundColor(Color.rgb(240, 244, 250))
        }

        val info = TextView(this).apply {

            text = """
                ${product.name}
                
                Categorie : ${product.category}
                Prix d'achat : ${product.purchasePrice} FCFA
                Prix de vente : ${product.salePrice} FCFA
                Quantite disponible : ${product.quantity}
                Benefice unitaire : ${product.salePrice - product.purchasePrice} FCFA
            """.trimIndent()

            textSize = 15f
            setTextColor(Color.rgb(15, 23, 42))
            setPadding(0, 0, 0, dp(10))
        }

        card.addView(info)

        val edit = Button(this).apply {
            text = "MODIFIER"
            setOnClickListener {
                showProductDialog(product)
            }
        }

        val delete = Button(this).apply {
            text = "SUPPRIMER"

            setOnClickListener {

                android.app.AlertDialog.Builder(this@MainActivity)
                    .setTitle("SUPPRIMER LE PRODUIT")
                    .setMessage("Voulez-vous supprimer ${product.name} ?")
                    .setNegativeButton("ANNULER", null)
                    .setPositiveButton("SUPPRIMER") { _, _ ->

                        val updated = getProducts()
                            .filterNot { it.id == product.id }

                        saveProducts(updated)
                        showProductsScreen()
                    }
                    .show()
            }
        }

        card.addView(edit)
        card.addView(delete)

        layout.addView(card)
    }

    addButton(layout, "RETOUR AU TABLEAU DE BORD") {
        showHome()
    }

    setContentView(layout)
}

// ==========================================
// AJOUT ET MODIFICATION D'UN PRODUIT
// ==========================================

private fun showProductDialog(
    existing: ProductItem? = null
) {

    val form = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(20), dp(10), dp(20), dp(10))
    }

    fun createField(
        hintText: String,
        value: String = "",
        numeric: Boolean = false
    ): EditText {

        return EditText(this).apply {
            hint = hintText
            setSingleLine(true)
            setText(value)

            if (numeric) {
                inputType =
                    android.text.InputType.TYPE_CLASS_NUMBER
            }
        }
    }

    val name = createField(
        "Nom du produit",
        existing?.name ?: ""
    )

    val category = createField(
        "Categorie du produit",
        existing?.category ?: ""
    )

    val purchase = createField(
        "Prix d'achat en FCFA",
        existing?.purchasePrice?.toString() ?: "",
        true
    )

    val sale = createField(
        "Prix de vente en FCFA",
        existing?.salePrice?.toString() ?: "",
        true
    )

    val quantity = createField(
        "Quantite en stock",
        existing?.quantity?.toString() ?: "",
        true
    )

    form.addView(name)
    form.addView(category)
    form.addView(purchase)
    form.addView(sale)
    form.addView(quantity)

    val dialog = android.app.AlertDialog.Builder(this)
        .setTitle(
            if (existing == null)
                "NOUVEAU PRODUIT"
            else
                "MODIFIER LE PRODUIT"
        )
        .setView(form)
        .setNegativeButton("ANNULER", null)
        .setPositiveButton("ENREGISTRER", null)
        .create()

    dialog.setOnShowListener {

        dialog.getButton(
            android.app.AlertDialog.BUTTON_POSITIVE
        ).setOnClickListener {

            val productName = name.text.toString().trim()
            val productCategory = category.text.toString().trim()

            val purchaseValue =
                purchase.text.toString().toLongOrNull()

            val saleValue =
                sale.text.toString().toLongOrNull()

            val quantityValue =
                quantity.text.toString().toIntOrNull()

            if (
                productName.isEmpty() ||
                productCategory.isEmpty() ||
                purchaseValue == null ||
                saleValue == null ||
                quantityValue == null ||
                purchaseValue < 0 ||
                saleValue < 0 ||
                quantityValue < 0
            ) {

                Toast.makeText(
                    this,
                    "Veuillez remplir correctement tous les champs.",
                    Toast.LENGTH_LONG
                ).show()

                return@setOnClickListener
            }

            val products = getProducts()

            val newProduct = ProductItem(
                id = existing?.id
                    ?: java.util.UUID.randomUUID().toString(),
                name = productName,
                category = productCategory,
                purchasePrice = purchaseValue,
                salePrice = saleValue,
                quantity = quantityValue
            )

            val index = products.indexOfFirst {
                it.id == newProduct.id
            }

            if (index >= 0) {
                products[index] = newProduct
            } else {
                products.add(newProduct)
            }

            saveProducts(products)

            dialog.dismiss()

            Toast.makeText(
                this,
                "Produit enregistre avec succes.",
                Toast.LENGTH_SHORT
            ).show()

            showProductsScreen()
        }
    }

    dialog.show()
}

// ==========================================
// AUTRES MODULES
// ==========================================

private fun showModule(
    title: String,
    description: String
) {

    val layout = createLayout()

    addTitle(layout)

    addSubtitle(layout, title)

    val info = TextView(this).apply {
        text = description
        textSize = 17f
        setTextColor(Color.rgb(50, 60, 75))
        gravity = Gravity.CENTER
        setPadding(0, dp(30), 0, dp(30))
    }

    layout.addView(info)

    addButton(layout, "RETOUR AU TABLEAU DE BORD") {
        showHome()
    }

    setContentView(layout)
}

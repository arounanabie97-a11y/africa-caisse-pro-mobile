
package com.africa.caissepro

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.*
import android.text.InputType
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import java.util.concurrent.TimeUnit
import java.util.UUID

class MainActivity : Activity() {

    private lateinit var auth: FirebaseAuth
    private var verificationId: String? = null

    private val navy = Color.rgb(15, 35, 65)
    private val orange = Color.rgb(245, 135, 25)
    private val white = Color.WHITE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        window.statusBarColor = navy
        window.navigationBarColor = navy

        if (auth.currentUser != null) {
            showHome()
        } else {
            showPhoneScreen()
        }
    }

    // ==========================================
    // OUTILS D'INTERFACE
    // ==========================================

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun createLayout(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(20), dp(20), dp(20))
            setBackgroundColor(Color.rgb(248, 250, 252))
        }
    }

    private fun addTitle(layout: LinearLayout) {
        val title = TextView(this).apply {
            text = "AFRICA CAISSE PRO"
            textSize = 25f
            setTextColor(navy)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, dp(15), 0, dp(15))
        }
        layout.addView(title)
    }

    private fun addSubtitle(
        layout: LinearLayout,
        value: String
    ) {
        val subtitle = TextView(this).apply {
            text = value
            textSize = 16f
            setTextColor(orange)
            gravity = Gravity.CENTER
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, dp(10), 0, dp(20))
        }
        layout.addView(subtitle)
    }

    private fun addButton(
        layout: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            text = label
            textSize = 14f
            setTextColor(white)
            setBackgroundColor(navy)
            setPadding(dp(8), dp(10), dp(8), dp(10))
            setOnClickListener { action() }
        }

        val params = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(0, dp(6), 0, dp(6))
        layout.addView(button, params)
    }

    private fun createField(
        hintText: String,
        numeric: Boolean = false
    ): EditText {
        return EditText(this).apply {
            hint = hintText
            textSize = 16f
            setSingleLine(true)
            setPadding(dp(12), dp(10), dp(12), dp(10))

            if (numeric) {
                inputType = InputType.TYPE_CLASS_NUMBER
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    // ==========================================
    // CONNEXION FIREBASE PAR TELEPHONE
    // ==========================================

    private fun showPhoneScreen() {

        val layout = createLayout()

        addTitle(layout)

        addSubtitle(
            layout,
            "CONNEXION A VOTRE COMPTE"
        )

        val info = TextView(this).apply {
            text = "Connectez-vous avec votre numéro de téléphone."
            textSize = 16f
            gravity = Gravity.CENTER
            setTextColor(navy)
            setPadding(0, dp(15), 0, dp(20))
        }

        layout.addView(info)

        val phone = createField(
            "Numéro international : +226XXXXXXXX"
        )

        layout.addView(phone)

        addButton(layout, "RECEVOIR LE CODE SMS") {

            val number = phone.text.toString().trim()

            if (!number.startsWith("+") ||
                number.length < 9
            ) {
                showMessage(
                    "Saisissez un numéro international valide."
                )
            } else {
                sendVerificationCode(number)
            }
        }

        setContentView(layout)
    }

    private fun sendVerificationCode(number: String) {

        showMessage("Envoi du code de vérification...")

        val callbacks =
            object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(
                    credential: PhoneAuthCredential
                ) {
                    signInWithCredential(credential)
                }

                override fun onVerificationFailed(
                    exception: FirebaseException
                ) {
                    showMessage(
                        "Erreur Firebase : ${exception.localizedMessage}"
                    )
                }

                override fun onCodeSent(
                    id: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    verificationId = id
                    showMessage("Code SMS envoyé.")
                    showCodeScreen()
                }
            }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(number)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun showCodeScreen() {

        val layout = createLayout()

        addTitle(layout)

        addSubtitle(
            layout,
            "VERIFICATION DU CODE SMS"
        )

        val code = createField(
            "Saisissez le code reçu",
            true
        )

        layout.addView(code)

        addButton(layout, "VALIDER LE CODE") {

            val smsCode = code.text.toString().trim()
            val id = verificationId

            if (id == null || smsCode.length < 4) {
                showMessage("Code invalide.")
            } else {
                val credential =
                    PhoneAuthProvider.getCredential(
                        id,
                        smsCode
                    )

                signInWithCredential(credential)
            }
        }

        addButton(layout, "RETOUR") {
            showPhoneScreen()
        }

        setContentView(layout)
    }

    private fun signInWithCredential(
        credential: PhoneAuthCredential
    ) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {
                    showMessage("Connexion réussie.")
                    showHome()
                } else {
                    showMessage(
                        "Echec de connexion : ${task.exception?.localizedMessage}"
                    )
                }
            }
    }

    // ==========================================
    // MODELE PRODUIT
    // ==========================================

    private data class ProductItem(
        val id: String,
        val name: String,
        val category: String,
        val purchasePrice: Long,
        val salePrice: Long,
        val quantity: Int
    )

    // ==========================================
    // STOCKAGE LOCAL DES PRODUITS
    // ==========================================

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
            showMessage("Erreur de lecture des produits.")
        }

        return products
    }

    private fun saveProducts(
        products: List<ProductItem>
    ) {

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
            setTextColor(navy)
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
                "Sélection des produits, quantités, calcul du total et validation."
            )
        }

        addButton(layout, "HISTORIQUE DES VENTES") {
            showModule(
                "HISTORIQUE",
                "Consultez vos transactions commerciales."
            )
        }

        addButton(layout, "DEPENSES ET BENEFICES") {
            showModule(
                "GESTION FINANCIERE",
                "Suivi des dépenses, recettes et bénéfices."
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
            verificationId = null
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

        fun field(
            hintText: String,
            key: String
        ): EditText {
            return createField(hintText).apply {
                setText(prefs.getString(key, ""))
            }
        }

        val name = field("Nom du commerce", "name")
        val owner = field("Nom du gérant", "owner")
        val phone = field("Téléphone commercial", "phone")
        val city = field("Ville", "city")
        val type = field("Activité commerciale", "type")

        layout.addView(name)
        layout.addView(owner)
        layout.addView(phone)
        layout.addView(city)
        layout.addView(type)

        addButton(layout, "ENREGISTRER MON COMMERCE") {

            if (name.text.toString().trim().isEmpty()) {

                showMessage("Veuillez saisir le nom du commerce.")

            } else {

                prefs.edit()
                    .putString("name", name.text.toString())
                    .putString("owner", owner.text.toString())
                    .putString("phone", phone.text.toString())
                    .putString("city", city.text.toString())
                    .putString("type", type.text.toString())
                    .apply()

                showMessage("Informations enregistrées.")
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
            setTextColor(navy)
            gravity = Gravity.CENTER
            setPadding(0, dp(15), 0, dp(20))
        }

        layout.addView(summary)

        addButton(layout, "+ AJOUTER UN PRODUIT") {
            showProductDialog()
        }

        if (products.isEmpty()) {

            val empty = TextView(this).apply {
                text = "Aucun produit enregistré."
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
                    
                    Catégorie : ${product.category}
                    Prix d'achat : ${product.purchasePrice} FCFA
                    Prix de vente : ${product.salePrice} FCFA
                    Quantité disponible : ${product.quantity}
                    Bénéfice unitaire : ${product.salePrice - product.purchasePrice} FCFA
                """.trimIndent()

                textSize = 15f
                setTextColor(navy)
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

                    AlertDialog.Builder(this@MainActivity)
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

        fun field(
            hintText: String,
            value: String = "",
            numeric: Boolean = false
        ): EditText {

            return createField(hintText, numeric).apply {
                setText(value)
            }
        }

        val name = field(
            "Nom du produit",
            existing?.name ?: ""
        )

        val category = field(
            "Catégorie",
            existing?.category ?: ""
        )

        val purchase = field(
            "Prix d'achat en FCFA",
            existing?.purchasePrice?.toString() ?: "",
            true
        )

        val sale = field(
            "Prix de vente en FCFA",
            existing?.salePrice?.toString() ?: "",
            true
        )

        val quantity = field(
            "Quantité en stock",
            existing?.quantity?.toString() ?: "",
            true
        )

        form.addView(name)
        form.addView(category)
        form.addView(purchase)
        form.addView(sale)
        form.addView(quantity)

        val dialog = AlertDialog.Builder(this)
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
                AlertDialog.BUTTON_POSITIVE
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

                    showMessage(
                        "Veuillez remplir correctement tous les champs."
                    )

                    return@setOnClickListener
                }

                val products = getProducts()

                val newProduct = ProductItem(
                    id = existing?.id ?: UUID.randomUUID().toString(),
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

                showMessage("Produit enregistré avec succès.")

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
}



    

    

            
                    

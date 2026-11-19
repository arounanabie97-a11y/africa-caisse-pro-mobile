
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
        showModule(
            "PRODUITS ET STOCK",
            "Gestion des produits, categories, prix et quantites disponibles."
        )
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

private fun showCommerceScreen() {
    val layout = createLayout()

    addTitle(layout)

    addSubtitle(
        layout,
        "ENREGISTREMENT DE VOTRE ETABLISSEMENT"
    )

    val prefs = getSharedPreferences(
        "merchant_profile",
        MODE_PRIVATE
    )

    fun createField(hintText: String, savedKey: String): EditText {
        return EditText(this).apply {
            hint = hintText
            textSize = 15f
            setSingleLine(true)
            setText(prefs.getString(savedKey, ""))
        }
    }

    val name = createField(
        "Nom du commerce",
        "name"
    )

    val owner = createField(
        "Nom du gerant",
        "owner"
    )

    val phone = createField(
        "Telephone commercial",
        "phone"
    )

    val city = createField(
        "Ville",
        "city"
    )

    val type = createField(
        "Activite commerciale",
        "type"
    )

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
                "Informations enregistrees sur cet appareil",
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

private fun showModule(title: String, description: String) {
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

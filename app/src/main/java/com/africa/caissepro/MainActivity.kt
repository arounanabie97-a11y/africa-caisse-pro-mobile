
package com.africa.caissepro

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import android.graphics.Typeface

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(15, 23, 42)
        window.navigationBarColor = Color.rgb(15, 23, 42)

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setPadding(32, 40, 32, 40)
        layout.setBackgroundColor(Color.rgb(248, 250, 252))

        val title = TextView(this)
        title.text = "AFRICA CAISSE PRO"
        title.textSize = 27f
        title.setTypeface(null, Typeface.BOLD)
        title.setTextColor(Color.rgb(15, 23, 42))
        title.gravity = Gravity.CENTER

        val subtitle = TextView(this)
        subtitle.text = "Votre gestion commerciale simplifiée"
        subtitle.textSize = 15f
        subtitle.setTextColor(Color.DKGRAY)
        subtitle.gravity = Gravity.CENTER

        val phone = EditText(this)
        phone.hint = "Numéro de téléphone"
        phone.inputType = 3
        phone.textSize = 16f

        val password = EditText(this)
        password.hint = "Mot de passe"
        password.inputType = 129
        password.textSize = 16f

        val login = Button(this)
        login.text = "SE CONNECTER"
        login.setTextColor(Color.WHITE)
        login.setBackgroundColor(Color.rgb(15, 23, 42))

        val register = TextView(this)
        register.text = "Créer un compte commerçant"
        register.textSize = 16f
        register.setTextColor(Color.rgb(230, 126, 34))
        register.gravity = Gravity.CENTER

        layout.addView(title)
        layout.addView(subtitle)
        layout.addView(phone)
        layout.addView(password)
        layout.addView(login)
        layout.addView(register)

        login.setOnClickListener {
            Toast.makeText(
                this,
                "Connexion Firebase à configurer",
                Toast.LENGTH_SHORT
            ).show()
        }

        register.setOnClickListener {
            Toast.makeText(
                this,
                "Inscription à configurer",
                Toast.LENGTH_SHORT
            ).show()
        }

        setContentView(layout)
    }
}


package com.africa.caissepro

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import java.util.concurrent.TimeUnit

class MainActivity : Activity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var phoneInput: EditText
    private lateinit var codeInput: EditText
    private var verificationId: String? = null
    private var phoneNumber = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        window.statusBarColor = Color.rgb(15, 23, 42)
        window.navigationBarColor = Color.rgb(15, 23, 42)

        if (auth.currentUser != null) {
            showHome()
        } else {
            showPhoneScreen()
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun createLayout(): LinearLayout {
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(28), dp(40), dp(28), dp(40))
            setBackgroundColor(Color.rgb(248, 250, 252))
        }
    }

    private fun addTitle(layout: LinearLayout) {
        val title = TextView(this).apply {
            text = "AFRICA CAISSE PRO"
            textSize = 27f
            setTextColor(Color.rgb(15, 23, 42))
            setTypeface(null, android.graphics.Typeface.BOLD)
            gravity = Gravity.CENTER
        }
        layout.addView(title)
    }

    private fun addSubtitle(layout: LinearLayout, message: String) {
        val subtitle = TextView(this).apply {
            text = message
            textSize = 15f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
            setPadding(0, dp(12), 0, dp(28))
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
            textSize = 16f
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.rgb(15, 23, 42))
            setOnClickListener { action() }
        }

        layout.addView(
            button,
            LinearLayout.LayoutParams(
                -1, dp(55)
            ).apply {
                topMargin = dp(18)
            }
        )
    }

    private fun showPhoneScreen() {
        val layout = createLayout()
        addTitle(layout)
        addSubtitle(
            layout,
            "Votre gestion commerciale simplifiée"
        )

        phoneInput = EditText(this).apply {
            hint = "Numéro de téléphone (+226...)"
            textSize = 16f
            inputType = 3
            setSingleLine(true)
        }

        layout.addView(
            phoneInput,
            LinearLayout.LayoutParams(-1, dp(55))
        )

        addButton(layout, "RECEVOIR LE CODE SMS") {
            phoneNumber = phoneInput.text.toString().trim()

            if (!phoneNumber.startsWith("+") ||
                phoneNumber.length < 10) {
                Toast.makeText(
                    this,
                    "Entrez un numéro international valide",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                sendVerificationCode()
            }
        }

        setContentView(layout)
    }

    private fun sendVerificationCode() {

        Toast.makeText(
            this,
            "Envoi du code SMS...",
            Toast.LENGTH_SHORT
        ).show()

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    override fun onVerificationCompleted(
                        credential: PhoneAuthCredential
                    ) {
                        signIn(credential)
                    }

                    override fun onVerificationFailed(
                        e: Exception
                    ) {
                        Toast.makeText(
                            this@MainActivity,
                            "Erreur SMS : ${e.localizedMessage}",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    override fun onCodeSent(
                        id: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        verificationId = id
                        showCodeScreen()
                    }
                }
            )
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun showCodeScreen() {
        val layout = createLayout()
        addTitle(layout)

        addSubtitle(
            layout,
            "Un code de vérification a été envoyé au $phoneNumber"
        )

        codeInput = EditText(this).apply {
            hint = "Code reçu par SMS"
            textSize = 18f
            inputType = 2
            gravity = Gravity.CENTER
            setSingleLine(true)
        }

        layout.addView(
            codeInput,
            LinearLayout.LayoutParams(-1, dp(55))
        )

        addButton(layout, "VÉRIFIER LE CODE") {
            val code = codeInput.text.toString().trim()
            val id = verificationId

            if (id == null || code.length != 6) {
                Toast.makeText(
                    this,
                    "Entrez le code à 6 chiffres",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val credential =
                    PhoneAuthProvider.getCredential(id, code)
                signIn(credential)
            }
        }

        setContentView(layout)
    }

    private fun signIn(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    showHome()
                } else {
                    Toast.makeText(
                        this,
                        "Vérification échouée : ${task.exception?.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun showHome() {
        val layout = createLayout()
        addTitle(layout)

        addSubtitle(
            layout,
            "Connexion réussie ! Bienvenue dans votre espace commercial."
        )

        val user = auth.currentUser

        val info = TextView(this).apply {
            text = "Compte : ${user?.phoneNumber ?: ""}"
            textSize = 16f
            setTextColor(Color.DKGRAY)
            gravity = Gravity.CENTER
        }

        layout.addView(info)

        addButton(layout, "SE DÉCONNECTER") {
            auth.signOut()
            showPhoneScreen()
        }

        setContentView(layout)
    }
}

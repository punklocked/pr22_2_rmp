package com.example.recipefinder

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AuthFragment : Fragment(R.layout.fragment_auth) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val loginInput = view.findViewById<EditText>(R.id.loginInput)
        val passInput = view.findViewById<EditText>(R.id.passwordInput)
        val btn = view.findViewById<Button>(R.id.loginBtn)

        val prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val gson = Gson()


        val savedUserJson = prefs.getString("current_user", null)

        if (savedUserJson != null) {

            btn.text = "Войти"
        }
        else {
            btn.text = "Зарегистрироваться"
        }

        btn.setOnClickListener {
            val inputLogin = loginInput.text.toString()
            val inputPassword = passInput.text.toString()

            if (inputLogin.isEmpty() || inputPassword.isEmpty()) {
                Toast.makeText(context, "Введите данные", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (savedUserJson == null) {

                val newUser = mapOf(
                    "login" to inputLogin,
                    "password" to inputPassword
                )
                val json = gson.toJson(newUser)
                prefs.edit().putString("current_user", json).apply()

                Toast.makeText(context, "Данные сохранены", Toast.LENGTH_SHORT).show()

                parentFragmentManager.beginTransaction().replace(R.id.container, AuthFragment()).commit()
            }
            else {

                val type = object : TypeToken<Map<String, String>>() {}.type
                val savedUser: Map<String, String> = gson.fromJson(savedUserJson, type)
                val savedLogin = savedUser["login"]
                val savedPass = savedUser["password"]

                if (inputLogin == savedLogin && inputPassword == savedPass) {
                    Toast.makeText(context, "Вход выполнен", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.container, SearchFragment()).commit()
                }
                else {
                    Toast.makeText(context, "Неверные данные, повторите попытку", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
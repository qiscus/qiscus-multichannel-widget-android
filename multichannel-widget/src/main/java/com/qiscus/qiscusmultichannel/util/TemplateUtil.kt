package com.qiscus.qiscusmultichannel.util

/**
 * Created on : 28/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
class TemplateUtil {

    companion object {
        fun getDefaultTemplate(): List<String> {
            val templates: MutableList<String> = ArrayList()
            templates.add("Halo")
            templates.add("Apakah sesuai map ?")
            templates.add("Ok")
            templates.add("Saya sedang menuju kesana")
            return templates.toList()
        }

        fun getCustomerTemplate(): List<String> {
            val templates: MutableList<String> = ArrayList()
            templates.add("Halo")
            templates.add("Sudah sampai mana?")
            templates.add("Ok")
            templates.add("Dimana anda?")
            templates.add("Terima kasih")
            return templates.toList()
        }

        fun generateTemplate(template: String): String {
            return "{ \"template\" : $template }"
        }
    }
}
package com.thierry.iptvplayer.vpn

import android.content.Context
import android.content.Intent

/**
 * Pont vers l'appli Proton VPN installée sur la TV.
 *
 * LIMITE IMPORTANTE À CONNAÎTRE : Proton VPN ne publie pas d'API/Intent officielle
 * permettant à une appli tierce de sélectionner un pays précis et de forcer la
 * connexion au serveur le plus rapide de ce pays. Ce que ce pont fait concrètement :
 *
 *  1. Détecte le pays de la chaîne sélectionnée dans IPTV Player.
 *  2. Amène l'appli Proton VPN au premier plan (lancement standard).
 *  3. C'est ensuite le "Quick Connect" natif de Proton VPN qui choisit le serveur
 *     le plus rapide — mais UNIQUEMENT si le pays est déjà sélectionné par défaut
 *     dans Proton VPN (à faire manuellement une fois dans les réglages Proton, ou
 *     à chaque changement de pays pour l'instant).
 *
 * Si tu veux une automatisation complète (sélection de pays + connexion en un seul
 * geste depuis notre appli), les pistes possibles sont : l'API Proton VPN si elle
 * devient publique, ou un profil VPN configuré manuellement par pays sur le
 * routeur/box. On pourra creuser ça une fois le lecteur de base fonctionnel.
 */
class ProtonVpnBridge(private val context: Context) {

    companion object {
        const val PROTON_VPN_PACKAGE = "ch.protonvpn.android"
    }

    fun isProtonVpnInstalled(): Boolean =
        runCatching {
            context.packageManager.getPackageInfo(PROTON_VPN_PACKAGE, 0)
            true
        }.getOrDefault(false)

    /** Amène Proton VPN au premier plan. Retourne false si l'appli n'est pas trouvée. */
    fun launchProtonVpn(): Boolean {
        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(PROTON_VPN_PACKAGE) ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        return true
    }

    /**
     * Appelé quand l'utilisateur sélectionne une chaîne d'un nouveau pays.
     * Pour l'instant : ouvre juste Proton VPN. À affiner selon ce qu'on découvre
     * possible ou non avec l'appli Proton installée sur la TV.
     */
    fun onCountryChanged(countryCode: String) {
        launchProtonVpn()
    }
}

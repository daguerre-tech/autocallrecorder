package com.example.data.model

enum class CallType {
    INCOMING,
    OUTGOING,
    MISSED
}

enum class RecordingMode(val label: String, val description: String) {
    ONLY_UNKNOWN_NUMBERS(
        "Numéros hors répertoire uniquement",
        "Enregistre automatiquement les appels des numéros inconnus qui ne figurent pas dans vos contacts."
    ),
    ALL_CALLS(
        "Tous les appels",
        "Enregistre tous les appels téléphoniques sans exception."
    ),
    CUSTOM_LIST_ONLY(
        "Liste personnalisée",
        "Enregistre uniquement les numéros spécifiés dans votre liste dédiée."
    ),
    DISABLED(
        "Désactivé",
        "L'enregistrement automatique est temporairement mis en pause."
    )
}

enum class RuleType(val label: String) {
    ALWAYS_RECORD("Toujours enregistrer"),
    NEVER_RECORD("Ne jamais enregistrer (Exclure)")
}

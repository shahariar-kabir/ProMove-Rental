package com.example.promoverental.utils

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

object SupabaseManager {
    // TODO: Replace with your actual Supabase URL and API Key
    private const val SUPABASE_URL = "https://hwitzxxdcagcgalgeqdn.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_9j-ApqhyWL7QGWaXmf4x1w_vwt2e5M4"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_KEY
    ) {
        install(Postgrest)
        install(Auth)
        install(Storage)
    }
}

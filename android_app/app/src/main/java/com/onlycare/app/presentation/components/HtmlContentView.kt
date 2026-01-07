package com.onlycare.app.presentation.components

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/**
 * Minimal, safe HTML renderer for policy pages.
 * Used as a fallback when backend returns `html_content` instead of structured sections.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun HtmlContentView(
    html: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                // Policies are static text; keep JS off.
                settings.javaScriptEnabled = false
                settings.domStorageEnabled = false
                settings.loadsImagesAutomatically = true
                webViewClient = WebViewClient()
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                /* baseUrl = */ null,
                /* data = */ html,
                /* mimeType = */ "text/html",
                /* encoding = */ "utf-8",
                /* historyUrl = */ null
            )
        }
    )
}





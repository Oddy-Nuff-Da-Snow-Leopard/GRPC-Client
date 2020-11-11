package com.logicway.grpcclient;

import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import io.michaelrocks.paranoid.Obfuscate;

@Obfuscate
public class WebViewActivity extends AppCompatActivity {

    private static final String HEADING_TEXT_ID = "headingText";

    private static final String GOOGLE_ACCOUNTS_URL = "https://accounts.google.com";

    private static final String JAVA_SCRIPT_INTERFACE_NAME = "HTMLOUT";

    private WebView webView;

    private TextView textView;

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        textView = findViewById(R.id.textView);

        class MyJavaScriptInterface {
            @JavascriptInterface
            public void processHTML(String html) {
                Document doc = Jsoup.parse(html);
                Element header = doc.getElementById(HEADING_TEXT_ID);
                textView.setText(header.text());
            }
        }

        button = findViewById(R.id.button);
        button.setOnClickListener(v -> webView.loadUrl("javascript:window.HTMLOUT.processHTML" +
                "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');"));

        webView = findViewById(R.id.webview);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new MyJavaScriptInterface(), JAVA_SCRIPT_INTERFACE_NAME);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl(GOOGLE_ACCOUNTS_URL);
    }
}
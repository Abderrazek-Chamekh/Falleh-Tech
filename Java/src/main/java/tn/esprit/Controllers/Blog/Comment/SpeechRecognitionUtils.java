package tn.esprit.Controllers.Blog.Comment;

import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class SpeechRecognitionUtils {
    private WebView webView;
    private String lastResult = "";

    public SpeechRecognitionUtils() {
        webView = new WebView();
        webView.getEngine().loadContent("""
            <html>
            <body>
            <script>
                let recognition;
                function startListening() {
                    recognition = new (window.SpeechRecognition || window.webkitSpeechRecognition)();
                    recognition.lang = 'fr-FR';
                    recognition.interimResults = false;
                    recognition.start();
                    
                    recognition.onresult = function(event) {
                        const transcript = event.results[0][0].transcript;
                        window.java.setResult(transcript);
                    };
                    
                    recognition.onerror = function(event) {
                        window.java.setError(event.error);
                    };
                }
                
                function stopListening() {
                    if (recognition) recognition.stop();
                }
            </script>
            </body>
            </html>
            """);

        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webView.getEngine().executeScript("window");
                window.setMember("java", this);
            }
        });
    }

    public void startListening() {
        webView.getEngine().executeScript("startListening()");
    }

    public void stopListening() {
        webView.getEngine().executeScript("stopListening()");
    }

    public void setResult(String result) {
        this.lastResult = result;
    }

    public void setError(String error) {
        System.err.println("Speech recognition error: " + error);
    }

    public String getLastResult() {
        return lastResult;
    }
}
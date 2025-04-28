package tn.esprit.Controllers.Blog.Comment;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class TextToSpeechUtils {
    public void speak(String text) {
        try {
            File tempFile = File.createTempFile("speech", ".vbs");
            String vbs = "CreateObject(\"SAPI.SpVoice\").Speak(\"" +
                    text.replace("\"", "\"\"") + "\")";
            Files.write(tempFile.toPath(), vbs.getBytes());
            Desktop.getDesktop().open(tempFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
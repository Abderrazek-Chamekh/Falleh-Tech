package tn.esprit.utils;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ImageUtils {

    // ✅ Rendre ces constantes accessibles publiquement
    public static final String UPLOAD_DIR = "C:/xampp/htdocs/uploads/";
    public static final String IMAGE_BASE_URL = "http://localhost/uploads/";

    // 📁 Choisir et copier une image
    public static File ouvrirEtCopierImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            try {
                String newFileName = System.currentTimeMillis() + "_" + selectedFile.getName();
                File destination = new File(UPLOAD_DIR, newFileName);
                destination.getParentFile().mkdirs();
                java.nio.file.Files.copy(selectedFile.toPath(), destination.toPath());
                return destination;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    // 📷 Prendre une photo depuis la webcam
    public static File prendrePhotoDepuisWebcam() {
        try {
            Webcam webcam = Webcam.getDefault();
            if (webcam != null) {
                webcam.setViewSize(WebcamResolution.VGA.getSize());
                webcam.open();

                BufferedImage image = webcam.getImage();
                if (image != null) {
                    String filename = "photo_" + System.currentTimeMillis() + ".png";
                    File outputFile = new File(UPLOAD_DIR + filename);
                    outputFile.getParentFile().mkdirs();
                    ImageIO.write(image, "PNG", outputFile);
                    webcam.close();
                    return outputFile;
                }
                webcam.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 📦 Charger une image depuis nom de fichier
    public static Image chargerDepuisNom(String filename) {
        if (filename == null || filename.isBlank()) return null;
        return new Image(IMAGE_BASE_URL + filename);
    }
}

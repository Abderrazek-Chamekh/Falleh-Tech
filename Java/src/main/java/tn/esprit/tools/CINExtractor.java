package tn.esprit.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CINExtractor {

    public static String extractCIN(String text) {
        Matcher m = Pattern.compile("\\b\\d{6,8}\\b").matcher(text);
        return m.find() ? m.group() : "Non détecté";
    }

    public static String extractFirstName(String text) {
        Pattern p = Pattern.compile("الاسم\\s*[:\\-]?\\s*(\\p{IsArabic}+)");
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1).trim() : "Non détecté";
    }

    public static String extractLastName(String text) {
        Pattern p = Pattern.compile("اللقب\\s*[:\\-]?\\s*(\\p{IsArabic}+)");
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1).trim() : "Non détecté";
    }

    public static String extractFullName(String text) {
        // Look for full chain like "زياد بن الأسعد بن محمد" or parts of it
        Pattern p = Pattern.compile("(\\p{IsArabic}+\\s+بن\\s+\\p{IsArabic}+\\s+بن\\s+\\p{IsArabic}+)");
        Matcher m = p.matcher(text);
        return m.find() ? m.group(1).trim() : "Non détecté";
    }

    public static String extractBirthDate(String text) {
        // Match something like "31 أكتوبر 2001"
        Pattern p = Pattern.compile("(\\d{1,2})\\s+(جانفي|فيفري|مارس|أفريل|ماي|جوان|جويلية|أوت|سبتمبر|أكتوبر|نوفمبر|ديسمبر|اكتوبر|اكتوير)\\s+(\\d{4})");
        Matcher m = p.matcher(text);
        return m.find() ? m.group().trim() : "Non détectée";
    }

    public static String extractPlace(String text) {
        // Try to match something like "مكان الولادة: سبيطلة" or "الولاية: صفاقس"
        Pattern p = Pattern.compile("(مكان الولادة|الولاية|جهة)\\s*[:\\-]?\\s*(\\p{IsArabic}+)");
        Matcher m = p.matcher(text);
        return m.find() ? m.group(2).trim() : "Non détecté";
    }

    public static String formatExtracted(String text) {
        return "📄 Données extraites :\n"
                + "🆔 CIN : " + extractCIN(text) + "\n"
                + "👤 Nom : " + extractLastName(text) + "\n"
                + "🧑‍🦱 Prénom : " + extractFirstName(text) + "\n"
                + "🧬 Nom complet : " + extractFullName(text) + "\n"
                + "🎂 Date de naissance : " + extractBirthDate(text) + "\n"
                + "📍 Lieu : " + extractPlace(text);
    }
}

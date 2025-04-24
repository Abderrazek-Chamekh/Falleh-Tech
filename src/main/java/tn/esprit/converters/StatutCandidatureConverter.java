package tn.esprit.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tn.esprit.entities.StatutCandidature;

@Converter(autoApply = true)
public class StatutCandidatureConverter implements AttributeConverter<StatutCandidature, String> {

    @Override
    public String convertToDatabaseColumn(StatutCandidature statut) {
        return statut != null ? statut.toString() : null;
    }

    @Override
    public StatutCandidature convertToEntityAttribute(String dbValue) {
        return dbValue != null ? StatutCandidature.fromString(dbValue) : null;
    }
}

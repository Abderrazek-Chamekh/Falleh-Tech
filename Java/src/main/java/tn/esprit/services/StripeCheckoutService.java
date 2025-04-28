package tn.esprit.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import tn.esprit.entities.Produit;
import tn.esprit.tools.EnvLoader;

import java.math.BigDecimal;
import java.util.Map;

public class StripeCheckoutService {
    private static final String STRIPE_SECRET_KEY = EnvLoader.getEnv("SECRET_KEY");
    private static final String SUCCESS_URL = "http://localhost:8080/payment-success";
    private static final String CANCEL_URL = "http://localhost:8080/payment-cancel";

    static {
        Stripe.apiKey = STRIPE_SECRET_KEY;
    }

    public String createCheckoutSession(int commandeId, double total,
                                        Map<Produit, Integer> cartItems,
                                        String customerEmail) throws StripeException {

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(SUCCESS_URL + "?commande_id=" + commandeId)
                .setCancelUrl(CANCEL_URL + "?commande_id=" + commandeId)
                .setClientReferenceId(String.valueOf(commandeId))
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD);

        if (customerEmail != null && !customerEmail.isEmpty()) {
            paramsBuilder.setCustomerEmail(customerEmail);
        }

        // Add cart items
        for (Map.Entry<Produit, Integer> entry : cartItems.entrySet()) {
            Produit p = entry.getKey();
            paramsBuilder.addLineItem(
                    SessionCreateParams.LineItem.builder()
                            .setQuantity((long)entry.getValue())
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            .setCurrency("usd")
                                            .setUnitAmount(p.getPrix().multiply(BigDecimal.valueOf(100)).longValue())
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName(p.getNom())
                                                            .build())
                                            .build())
                            .build());
        }

        Session session = Session.create(paramsBuilder.build());
        return session.getUrl();
    }
}
package tn.esprit.services;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SmsService {

      // Your real Auth Token
    public static final String TWILIO_PHONE_NUMBER = "+17542850599";              // Your Twilio number

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public static void send(String to, String messageBody) {
        Message message = Message.creator(
                new PhoneNumber(to),                 // destination (recipient)
                new PhoneNumber(TWILIO_PHONE_NUMBER), // sender (your Twilio number)
                messageBody
        ).create();

        System.out.println("✅ SMS Sent: " + message.getSid());
    }
}

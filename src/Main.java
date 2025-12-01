import controllers.AppController;
import services.AuthService;
import services.TwilioService;
import ui.ResumeAnalyzingContainer;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ResumeAnalyzingContainer root = new ResumeAnalyzingContainer();
            AuthService auth = new AuthService();
            TwilioService twilio = new TwilioService();

            new AppController(root, auth, twilio);
            root.setVisible(true);
        });
    }
}

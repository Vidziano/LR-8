import javax.swing.*;
import java.awt.event.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

public class ConferenceClient extends JFrame {
    private JTextField nameField, familyNameField, placeOfWorkField, reportTitleField, emailField, hostField, portField;
    private ConferenceRegistration stub;

    public ConferenceClient() {
        setTitle("Conference Client");
        setSize(470, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel portLabel = new JLabel("Port:");
        portLabel.setBounds(10, 10, 100, 20);
        add(portLabel);

        portField = new JTextField("1099");
        portField.setBounds(120, 10, 80, 20);
        add(portField);

        JLabel hostLabel = new JLabel("Host:");
        hostLabel.setBounds(210, 10, 100, 20);
        add(hostLabel);

        hostField = new JTextField("localhost");
        hostField.setBounds(260, 10, 160, 20);
        add(hostField);

        JLabel nameLabel = new JLabel("Name:");
        nameLabel.setBounds(10, 40, 100, 20);
        add(nameLabel);

        nameField = new JTextField();
        nameField.setBounds(120, 40, 300, 20);
        add(nameField);

        JLabel familyNameLabel = new JLabel("Family Name:");
        familyNameLabel.setBounds(10, 70, 100, 20);
        add(familyNameLabel);

        familyNameField = new JTextField();
        familyNameField.setBounds(120, 70, 300, 20);
        add(familyNameField);

        JLabel placeOfWorkLabel = new JLabel("Place of Work:");
        placeOfWorkLabel.setBounds(10, 100, 100, 20);
        add(placeOfWorkLabel);

        placeOfWorkField = new JTextField();
        placeOfWorkField.setBounds(120, 100, 300, 20);
        add(placeOfWorkField);

        JLabel reportTitleLabel = new JLabel("Report Title:");
        reportTitleLabel.setBounds(10, 130, 100, 20);
        add(reportTitleLabel);

        reportTitleField = new JTextField();
        reportTitleField.setBounds(120, 130, 300, 20);
        add(reportTitleField);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(10, 160, 100, 20);
        add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(120, 160, 300, 20);
        add(emailField);

        JButton connectButton = new JButton("Connect");
        connectButton.setBounds(10, 200, 100, 30);
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                connectToServer();
            }
        });
        add(connectButton);

        JButton registerButton = new JButton("Register");
        registerButton.setBounds(120, 200, 100, 30);
        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                registerParticipant();
            }
        });
        add(registerButton);

        JButton clearButton = new JButton("Clear");
        clearButton.setBounds(230, 200, 100, 30);
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                clearFields();
            }
        });
        add(clearButton);

        JButton getInfoButton = new JButton("Get Info");
        getInfoButton.setBounds(340, 200, 100, 30);
        getInfoButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                getParticipants();
            }
        });
        add(getInfoButton);
    }

    private void connectToServer() {
        try {
            String host = hostField.getText();
            int port = Integer.parseInt(portField.getText());
            Registry registry = LocateRegistry.getRegistry(host, port);
            stub = (ConferenceRegistration) registry.lookup("ConferenceRegistration");
            JOptionPane.showMessageDialog(this, "Connected to server", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to connect: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void registerParticipant() {
        if (stub == null) {
            JOptionPane.showMessageDialog(this, "Not connected to server", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Перевірка, чи заповнені всі поля
        if (nameField.getText().isEmpty() || familyNameField.getText().isEmpty() ||
                placeOfWorkField.getText().isEmpty() || reportTitleField.getText().isEmpty() ||
                emailField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled out.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            Participant participant = new Participant(
                    nameField.getText(),
                    familyNameField.getText(),
                    placeOfWorkField.getText(),
                    reportTitleField.getText(),
                    emailField.getText()
            );
            int numberOfParticipants = stub.registerParticipant(participant);
            JOptionPane.showMessageDialog(this, "Register was successful. Total participants: " + numberOfParticipants, "Message", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Registration failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void clearFields() {
        nameField.setText("");
        familyNameField.setText("");
        placeOfWorkField.setText("");
        reportTitleField.setText("");
        emailField.setText("");
    }

    private void getParticipants() {
        if (stub == null) {
            JOptionPane.showMessageDialog(this, "Not connected to server", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            List<Participant> participants = stub.getParticipants();
            StringBuilder info = new StringBuilder("Registered Participants:\n");
            for (Participant p : participants) {
                info.append(p.getName()).append(" ").append(p.getFamilyName()).append("\n");
            }
            JOptionPane.showMessageDialog(this, info.toString(), "Participants", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to get participants: " + e.toString(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ConferenceClient().setVisible(true);
            }
        });
    }
}

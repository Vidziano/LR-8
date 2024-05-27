import javax.swing.*;
import java.awt.event.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.List;

public class ConferenceServer extends JFrame {
    private JTextField hostField, portField, participantsField;
    private JTextArea outputArea;
    private JButton startButton, stopButton, saveButton, loadButton, exitButton;
    private ConferenceRegistrationImpl server;
    private Registry registry;

    public ConferenceServer() {
        setTitle("Conference Server");
        setSize(600, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JLabel hostLabel = new JLabel("host:");
        hostLabel.setBounds(50, 10, 50, 20);
        add(hostLabel);

        hostField = new JTextField("localhost");
        hostField.setBounds(100, 10, 180, 20);
        add(hostField);

        JLabel portLabel = new JLabel("port:");
        portLabel.setBounds(250, 10, 50, 20);
        add(portLabel);

        portField = new JTextField("1099");
        portField.setBounds(290, 10, 80, 20);
        add(portField);

        JLabel participantsLabel = new JLabel("participants:");
        participantsLabel.setBounds(50, 40, 100, 20);
        add(participantsLabel);

        participantsField = new JTextField("0");
        participantsField.setEditable(false);
        participantsField.setBounds(130, 40, 350, 20);
        add(participantsField);

        outputArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBounds(10, 70, 520, 150);
        outputArea.setEditable(false);
        add(scrollPane);

        startButton = new JButton("Start");
        startButton.setBounds(50, 230, 70, 30);
        startButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startServer();
            }
        });
        add(startButton);

        stopButton = new JButton("Stop");
        stopButton.setBounds(130, 230, 70, 30);
        stopButton.setEnabled(false);
        stopButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                stopServer();
            }
        });
        add(stopButton);

        saveButton = new JButton("Save");
        saveButton.setBounds(210, 230, 70, 30);
        saveButton.setEnabled(false);
        saveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                saveParticipants();
            }
        });
        add(saveButton);

        loadButton = new JButton("Load");
        loadButton.setBounds(290, 230, 70, 30);
        loadButton.setEnabled(false);
        loadButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadParticipants();
            }
        });
        add(loadButton);

        exitButton = new JButton("Exit");
        exitButton.setBounds(370, 230, 70, 30);
        exitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        add(exitButton);
    }

    private void startServer() {
        try {
            server = new ConferenceRegistrationImpl() {
                @Override
                public synchronized int registerParticipant(Participant participant) throws RemoteException {
                    int result = super.registerParticipant(participant);
                    updateParticipantsArea();
                    return result;
                }

                @Override
                public synchronized void importFromXML(String xml) throws RemoteException {
                    super.importFromXML(xml);
                    updateParticipantsArea();
                }
            };
            int port = Integer.parseInt(portField.getText());
            registry = LocateRegistry.createRegistry(port);
            registry.rebind("ConferenceRegistration", server);
            outputArea.append("Server started\n");
            participantsField.setText(String.valueOf(server.getParticipants().size()));
            updateParticipantsArea();
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            saveButton.setEnabled(true);
            loadButton.setEnabled(true);

            // Додавання діалогового вікна
            JOptionPane.showMessageDialog(this, "The PMI Server is Worked", "Message", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception e) {
            outputArea.append("Failed to start server: " + e.toString() + "\n");
            e.printStackTrace();
        }
    }

    private void stopServer() {
        try {
            if (registry != null) {
                registry.unbind("ConferenceRegistration");
                UnicastRemoteObject.unexportObject(server, true);
                UnicastRemoteObject.unexportObject(registry, true);
                registry = null;
            }
            outputArea.append("Server stopped\n");
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            saveButton.setEnabled(false);
            loadButton.setEnabled(false);
        } catch (Exception e) {
            outputArea.append("Failed to stop server: " + e.toString() + "\n");
            e.printStackTrace();
        }
    }

    private void saveParticipants() {
        try {
            String xml = server.exportToXML();
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.nio.file.Files.write(fileChooser.getSelectedFile().toPath(), xml.getBytes());
                outputArea.append("Participants saved to XML\n");
            }
        } catch (Exception e) {
            outputArea.append("Failed to save participants: " + e.toString() + "\n");
            e.printStackTrace();
        }
    }

    private void loadParticipants() {
        try {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                String xml = new String(java.nio.file.Files.readAllBytes(fileChooser.getSelectedFile().toPath()));
                server.importFromXML(xml);
                participantsField.setText(String.valueOf(server.getParticipants().size()));
                updateParticipantsArea();
                outputArea.append("Participants loaded from XML\n");
            }
        } catch (Exception e) {
            outputArea.append("Failed to load participants: " + e.toString() + "\n");
            e.printStackTrace();
        }
    }

    private void updateParticipantsArea() {
        try {
            List<Participant> participants = server.getParticipants();
            participantsField.setText(String.valueOf(participants.size()));
            outputArea.setText("");
            int i = 1;
            for (Participant participant : participants) {
                outputArea.append(i + "). name: " + participant.getName() + ", familyName: " + participant.getFamilyName() +
                        ", placeOfWork: " + participant.getPlaceOfWork() + ", reportTitle: " + participant.getReportTitle() +
                        ", email: " + participant.getEmail() + "\n");
                i++;
            }
        } catch (RemoteException e) {
            outputArea.append("Failed to update participants: " + e.toString() + "\n");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ConferenceServer().setVisible(true);
            }
        });
    }
}

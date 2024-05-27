import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class ConferenceRegistrationImpl extends UnicastRemoteObject implements ConferenceRegistration {
    private List<Participant> participants;

    public ConferenceRegistrationImpl() throws RemoteException {
        participants = new ArrayList<>();
    }

    @Override
    public synchronized int registerParticipant(Participant participant) throws RemoteException {
        // Перевірка, чи вже зареєстрований учасник
        for (Participant p : participants) {
            if (p.getEmail().equals(participant.getEmail())) {
                throw new RemoteException("Participant already registered.");
            }
        }
        participants.add(participant);
        return participants.size();
    }

    @Override
    public synchronized List<Participant> getParticipants() throws RemoteException {
        return new ArrayList<>(participants);
    }

    @Override
    public String exportToXML() throws RemoteException {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.newDocument();

            Element rootElement = doc.createElement("RegisteredConferees");
            doc.appendChild(rootElement);

            for (Participant p : participants) {
                Element conferee = doc.createElement("Conferee");
                rootElement.appendChild(conferee);

                Element name = doc.createElement("name");
                name.appendChild(doc.createTextNode(p.getName()));
                conferee.appendChild(name);

                Element familyName = doc.createElement("familyName");
                familyName.appendChild(doc.createTextNode(p.getFamilyName()));
                conferee.appendChild(familyName);

                Element placeOfWork = doc.createElement("placeOfWork");
                placeOfWork.appendChild(doc.createTextNode(p.getPlaceOfWork()));
                conferee.appendChild(placeOfWork);

                Element reportTitle = doc.createElement("reportTitle");
                reportTitle.appendChild(doc.createTextNode(p.getReportTitle()));
                conferee.appendChild(reportTitle);

                Element email = doc.createElement("email");
                email.appendChild(doc.createTextNode(p.getEmail()));
                conferee.appendChild(email);
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StringWriter writer = new StringWriter();
            StreamResult result = new StreamResult(writer);
            transformer.transform(source, result);

            return writer.toString();
        } catch (Exception e) {
            throw new RemoteException("Error during XML export", e);
        }
    }

    @Override
    public void importFromXML(String xml) throws RemoteException {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new InputSource(new StringReader(xml)));

            NodeList nodeList = doc.getElementsByTagName("Conferee");
            participants.clear();

            for (int i = 0; i < nodeList.getLength(); i++) {
                Element element = (Element) nodeList.item(i);

                String name = element.getElementsByTagName("name").item(0).getTextContent();
                String familyName = element.getElementsByTagName("familyName").item(0).getTextContent();
                String placeOfWork = element.getElementsByTagName("placeOfWork").item(0).getTextContent();
                String reportTitle = element.getElementsByTagName("reportTitle").item(0).getTextContent();
                String email = element.getElementsByTagName("email").item(0).getTextContent();

                Participant participant = new Participant(name, familyName, placeOfWork, reportTitle, email);
                participants.add(participant);
            }
        } catch (Exception e) {
            throw new RemoteException("Error during XML import", e);
        }
    }
}

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ConferenceRegistration extends Remote {
    int registerParticipant(Participant participant) throws RemoteException;
    List<Participant> getParticipants() throws RemoteException;
    String exportToXML() throws RemoteException;
    void importFromXML(String xml) throws RemoteException;
}

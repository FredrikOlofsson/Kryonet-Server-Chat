import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import packets.Packet;
import packets.Packet1Connect;
import packets.Packet2ClientConnect;
import packets.Packet3ClientDisconnect;
import packets.Packet4Chat;

public class Main {

	private static HashMap<String, Connection> clients = new HashMap<String, Connection>();
	
	public static void main(String[] args) throws IOException {
		System.out.println("SERVER");
		final Server server = new Server();
		server.start();
		server.bind(54545, 54550);
		
		server.addListener(new Listener() {
			public void received (Connection connection, Object object) {
				if (object instanceof Packet){
					if (object instanceof Packet1Connect){
						Packet1Connect p1 = (Packet1Connect) object;						
						clients.put(p1.username, connection);
						Packet2ClientConnect p2 = new Packet2ClientConnect();
						p2.clientName = p1.username;
						server.sendToAllExceptTCP(connection.getID(), p2);
					} else if (object instanceof Packet3ClientDisconnect){
						Packet3ClientDisconnect p3 = (Packet3ClientDisconnect) object;
						clients.remove(p3.clientName);
						server.sendToAllExceptTCP(clients.get(p3.clientName).getID(), p3);
					} else if (object instanceof Packet4Chat){
						Packet4Chat p4 = (Packet4Chat) object;
						server.sendToAllTCP(p4);
					} else {
						System.out.println("Not a known object");
					}
				}
			}
			public void disconnected (Connection connection){
				Packet3ClientDisconnect p3 = new Packet3ClientDisconnect();
				Iterator it = clients.entrySet().iterator();
				while (it.hasNext()){
					Map.Entry pairs = (Map.Entry) it.next();
					if (pairs.getValue().equals(connection)){
						p3.clientName = (String) pairs.getKey();
						break;
					}
				}
				if (!p3.clientName.equalsIgnoreCase("")){
					server.sendToAllExceptTCP(connection.getID(), p3);
				}
			}
		});
		

		
		server.getKryo().register(Packet.class);
		server.getKryo().register(Packet1Connect.class);
		server.getKryo().register(Packet2ClientConnect.class);
		server.getKryo().register(Packet3ClientDisconnect.class);
		server.getKryo().register(Packet4Chat.class);

	}
}

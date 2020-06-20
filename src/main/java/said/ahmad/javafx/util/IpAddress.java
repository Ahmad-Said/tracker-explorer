package said.ahmad.javafx.util;

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.jetbrains.annotations.Nullable;

public class IpAddress {

	@Nullable
	public static String getLocalAddress() {
		try (final DatagramSocket socket = new DatagramSocket()) {
			socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
			return socket.getLocalAddress().getHostAddress();
		} catch (SocketException | UnknownHostException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<Integer> splitIpAddress(String ip) {
		ArrayList<Integer> ipsAsInt = new ArrayList<>();
		for (String ipPart : ip.split("[.]")) {
			ipsAsInt.add(Integer.parseInt(ipPart));
		}
		return ipsAsInt;
	}
}

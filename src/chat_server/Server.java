/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package chat_server;

/**
 *
 * @author Vlad
 */
import java.io.IOException;
import java.net.ServerSocket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


class V {
	public static int n = 1;
}

public class Server {
	/**
	 * @param args
	 *            the command line arguments
	 */
	public static ServerSocket server;
	public static Connection con;

	public static int getPort() {
		return 1234;
	}

	public static void main(String[] args) {

		try {
			server = new ServerSocket(getPort());
			System.out.println("Serverul a pornit.");
		} catch (IOException e) {
			System.out.println("Nu pot porni serverul.");
			return;
		}
		try {
			// Persoana P;
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException ex) {
			System.out.println(ex);
		}
		String url = "jdbc:mysql://localhost/clienti?"
	              + "user=root&password=pass";
		con = null;
		try {
			con = DriverManager.getConnection(url);
		} catch (SQLException ex) {
			System.out.println("Nu ma pot conecta la baza de date");
		}
		Statement stmt = null;
		try {
			stmt = con.createStatement();
		} catch (SQLException ex) {
			System.out.println("Nu pot creea statement-ul");
		}
		Client c[] = new Client[39];
		while (true) {
			try {
				// c[1].s=new Socket();
				c[V.n] = new Client();
				c[V.n].s = server.accept();
				System.out.println("Avem un client");
				new Input(c, V.n, stmt);
				V.n++;
			} catch (IOException e) {
				System.out.println("Nu pot porni serverul.");
				return;
			}

		}

	}

	@Override
	protected void finalize() {
		try {
			con.close();
		} catch (SQLException ex) {
			System.out.println("sdfsdf");
		}
		try {
			server.close();
			System.out.println("Socket has been closed");
		} catch (IOException e) {
			System.out.println("Could not close socket");
			System.exit(-1);
		}
	}

}

package chat_server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Scanner;

public class Output extends Thread {
	DataOutputStream stream;
	Scanner scanner;
	Client c1;

	public Output(Client c[], int i) throws IOException {
		c1 = c[i];
		stream = new DataOutputStream(c1.s.getOutputStream());
		scanner = new Scanner(System.in);
		start();
	}

	public void run() {
		String mesaj;

		while (c1.ok) {
			try {
				mesaj = scanner.nextLine();
				stream.writeUTF("[server]: " + mesaj);
			} catch (IOException e) {
				System.out.print("[eroare2]: " + e.getMessage());
			}
		}
	}
}
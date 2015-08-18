import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public class Server {

	private JFrame frame;
	private JTextField textField;
	static JTextArea textArea;
	JButton btnConnect;
	static boolean server_on = false;
	JLabel lblNewLabel;
	JLabel lblState;
	Thread start;
	private static Integer portNumber = 3058;
	private static ServerSocket server_socket;
	private static PrintWriter out;
	private static BufferedReader in;
	private static String message;
	
	/**
	 * Launch the application.
	 * @throws InterruptedException 
	 * @throws InvocationTargetException 
	 */
	public static void main(String[] args) throws InvocationTargetException, InterruptedException {
		EventQueue.invokeAndWait(new Runnable() {
			public void run() {
				try {
					Server window = new Server();
					window.frame.setVisible(true);
					System.out.println("Window created...");
					
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println("Exception caught...");
				}
				System.out.println("Run function finished...");
			}
			
		});
		System.out.println("Main function finished...");
	}

	/**
	 * Create the application.
	 */
	public Server() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 582, 403);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		textField = new JTextField();
		textField.setBounds(10, 333, 430, 20);
		frame.getContentPane().add(textField);
		textField.setColumns(10);
		
		JButton btnNewButton = new JButton("Command");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				out.println(textField.getText());
			}
		});
		btnNewButton.setBounds(450, 332, 89, 23);
		frame.getContentPane().add(btnNewButton);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setBounds(10, 11, 430, 304);
		frame.getContentPane().add(textArea);
		
		lblNewLabel = new JLabel("Status:");
		lblNewLabel.setBounds(475, 11, 64, 14);
		frame.getContentPane().add(lblNewLabel);
		
		btnConnect = new JButton("ODPAL");
		btnConnect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(!server_on) {
					start();
					btnConnect.setText("ROZWAL");
					lblState.setText("UP");
					server_on = true;
				} else {
					server_on = false;
					stop();
					btnConnect.setText("ODPAL");
					lblState.setText("DOWN");
					
				}
			}
		});
		btnConnect.setBounds(450, 75, 106, 23);
		frame.getContentPane().add(btnConnect);
		
		lblState = new JLabel("OFFLINE");
		lblState.setHorizontalAlignment(SwingConstants.CENTER);
		lblState.setFont(new Font("Tahoma", Font.PLAIN, 16));
		lblState.setBounds(450, 36, 106, 28);
		frame.getContentPane().add(lblState);
	}
	
	private static class MSGHost implements Runnable {
		
		@Override
		public void run() {
			System.out.println("Starting server...");
			try {
				server_socket = new ServerSocket(portNumber);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			System.out.println("Waiting for connection...");
			
			while(server_on) {
				Socket client_socket = null;
				try {
					client_socket = server_socket.accept();					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				new Thread(new MSGClient(client_socket)).start();
				System.out.println("Connection accepted!");
			}
		}
	}
	
	public static class MSGClient implements Runnable {

		public Socket client_socket;
		
		public MSGClient(Socket client_socket) {
			this.client_socket = client_socket;
		}
		
		@Override
		public void run() {
			while(server_on) {
				try {
					out = new PrintWriter(client_socket.getOutputStream(), true);
					in = new BufferedReader(new InputStreamReader(client_socket.getInputStream()));
					message = in.readLine();
					textArea.append(message);
					out.println(message);
					
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public synchronized void stop() {
		try {
			server_socket.close();
			start.interrupt();
			start.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public synchronized void start()  {
		start = new Thread(new MSGHost());
		start.start();
	}
}

package br.ufu.facom.network.poc.stream;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import br.ufu.facom.network.dlontology.FinSocket;
import br.ufu.facom.network.dlontology.msg.Message;

public class Client {
	Timer timer; // timer used to receive data
	byte[] buf; // buffer used to store data received

	BufferedReader bufferedReader;
	BufferedWriter bufferedWriter;

	static int MJPEG_TYPE = 26; // RTP payload type for MJPEG video
	
	private FinSocket finSocket;
	private String titleStream;
	
	/**
	 * GUI
	 */
	 JFrame f = new JFrame("Client");
	 JPanel mainPanel = new JPanel();
	 JLabel iconLabel = new JLabel();
	 ImageIcon icon;
	
	public Client(String titleServer, String titleStream) {
		timer = new Timer(20, new TimerListener());
		timer.setInitialDelay(0);
		timer.setCoalesce(true);

		// allocate enough memory for the buffer used to receive data from the server
		buf = new byte[15000];
		
		finSocket = FinSocket.open();
		finSocket.register(titleServer);
		finSocket.join(titleStream);
		
		createView();
		
		timer.start();
	}

	private void createView() {
		//Image display label
	    iconLabel.setIcon(null);
	    
	    //frame layout
	    mainPanel.setLayout(null);
	    mainPanel.add(iconLabel);
	    iconLabel.setBounds(0,0,380,280);

	    f.getContentPane().add(mainPanel, BorderLayout.CENTER);
	    f.setSize(new Dimension(390,320));
	    f.setVisible(true);
	    f.addWindowListener(new WindowAdapter() {
	        public void windowClosing(WindowEvent e) {
	            //stop the timer and exit
	            timer.stop();
	            System.exit(0);
	            endSocket();
	     }});
	}

	private void endSocket() {
		if(finSocket != null){
			finSocket.disjoin(titleStream);
			finSocket.unregister();
		}
	}
	
	class TimerListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {


			try {
				// receive the DP from the socket:
				Message message = finSocket.read();
				
				byte buf[] = message.getPayload();
				int size = buf.length;

				// create an RTPpacket object from the DP
				RTPPacket rtp_packet = new RTPPacket(buf, size);

				// get the payload bitstream from the RTPpacket object
				int payload_length = rtp_packet.getPayloadLength();
				byte[] payload = new byte[payload_length];
				rtp_packet.getPayload(payload);

				// get an Image object from the payload bitstream
				Toolkit toolkit = Toolkit.getDefaultToolkit();
				Image image = toolkit.createImage(payload, 0, payload_length);

				// display the image as an ImageIcon object
				icon = new ImageIcon(image);
				iconLabel.setIcon(icon);
			}catch (Exception ex) {
				System.out.println("Exception caught: " + ex);
			}
		}
	}
	
	public static void main(String[] args) {
		if(args.length == 2){
			new Client(args[0],args[1]);
		}else
			System.err.println("Wrong usage");
	}
}
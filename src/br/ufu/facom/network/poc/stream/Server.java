package br.ufu.facom.network.poc.stream;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import br.ufu.facom.network.dlontology.FinSocket;

public class Server {
	/**
	 * Constants
	 */
	private static final int MJPEG_TYPE = 26; // RTP payload type for MJPEG video
	private static final int FRAME_PERIOD = 100; // Frame period of the video to stream, in ms
	private static final int VIDEO_LENGTH = 500; // length of the video in frames
	

	/**
	 * Instance variables
	 */
	private int imageIndex = 0; // image index of the image currently transmitted
	private VideoStream videoStream; // VideoStream object used to access video frames

	private Timer timer; // timer used to send the images at the video frame rate
	private byte[] buf; // buffer used to store the images to send to the client

	private FinSocket finSocket;
	private String titleStream;

	/**
	 * Construtor
	 */
	public Server(String titleServer, String titleStream, String movie) {
		this.titleStream = titleStream;
		
		// init Timer
		timer = new Timer(FRAME_PERIOD, new TimerListener());
		timer.setInitialDelay(0);
		timer.setCoalesce(true);

		finSocket = FinSocket.open();
		finSocket.register(titleServer);
		finSocket.join(titleStream);
		
		buf = new byte[15000];

		try {
			videoStream = new VideoStream(movie);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		timer.start();
		
		while(timer.isRunning()){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void finalize(){
		if(finSocket != null){
			finSocket.disjoin(titleStream);
			finSocket.unregister();
		}
	}

	class TimerListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {

			// if the current image nb is less than the length of the video
			if (imageIndex < VIDEO_LENGTH) {
				// update current imagenb
				imageIndex++;

				try {
					// get next frame to send from the video, as well as its size
					int image_length = videoStream.getnextframe(buf);

					// builds an RTPpacket object containing the frame
					RTPPacket rtp_packet = new RTPPacket(MJPEG_TYPE, imageIndex, imageIndex * FRAME_PERIOD, buf, image_length);
					
					// writes the frame via DL-Ontology
					if(!finSocket.write(titleStream, rtp_packet.toBytes()))
						System.err.println("Error sending previous frame...");

				} catch (Exception ex) {
					System.err.println("Exception caught: " + e);
					ex.printStackTrace();
					System.exit(0);
				}
			} else {
				// if we have reached the end of the video file, stop the timer
				timer.stop();
			}
		}
	}
	
	public static void main(String[] args) {
		if(args.length == 3){
			new Server(args[0],args[1],args[2]);
		}else
			System.err.println("Wrong usage...");
	}
}
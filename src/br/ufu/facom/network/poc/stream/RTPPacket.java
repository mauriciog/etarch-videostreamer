package br.ufu.facom.network.poc.stream;

public class RTPPacket {
	// Header size
	static final int HEADER_SIZE = 12;
	// Bitstream of the header
	public byte[] header;

	// Payload size
	public int payload_size;
	// Bitstream of the payload
	public byte[] payload;

	// Fields that compose the RTP header
	private int Version;
	private int Padding;
	public int Extension;
	public int CC;
	public int Marker;
	public int PayloadType;
	public int SequenceNumber;
	public int TimeStamp;
	public int Ssrc;

	/**
	 * Constructor for RTP packet
	 * (Server perpective)
	 */
	public RTPPacket(int PType, int Framenb, int Time, byte[] data,	int data_length) {
		//Default fields
		Version = 2;
		Padding = 0;
		Extension = 0;
		CC = 0;
		Marker = 0;
		Ssrc = 15;

		//Fields passed as parameters
		SequenceNumber = Framenb;
		TimeStamp = Time;
		PayloadType = PType;

		/**
		 * Building the header bitstream
		 */
		header = new byte[HEADER_SIZE];
		
		// Set the Version
		header[0] = (byte) ((Version << 6 | Padding << 5 | Extension << 4 | CC));
		// Set the PayloadType
		header[1] = (byte) (Marker << 7 | PayloadType);
		// Set the SequenceNumber
		header[2] = (byte) (SequenceNumber >> 8);
		header[3] = (byte) (SequenceNumber & 0xFF);
		// Set the timestamp PROBLEM WITH THIS
		header[4] = (byte) (TimeStamp >> 24);
		header[5] = (byte) ((TimeStamp << 8) >> 24);
		header[6] = (byte) ((TimeStamp << 16) >> 24);
		header[7] = (byte) ((TimeStamp << 24) >> 24);
		// Set the Ssrc
		header[8] = (byte) (Ssrc >> 24);
		header[9] = (byte) ((Ssrc << 8) >> 24);
		header[10] = (byte) ((Ssrc << 16) >> 24);
		header[11] = (byte) ((Ssrc << 24) >> 24);

		/**
		 * Building the payload bitstream
		 */
		payload_size = data_length;
		payload = new byte[data_length];

		for (int i = 0; i < data_length; i++) {
			payload[i] = data[i];
		}
	}

	/**
	 * Constructor for RTP packet
	 * (Client perpective)
	 */
	public RTPPacket(byte[] packet, int packet_size) {
		//Default fields
		Version = 2;
		Padding = 0;
		Extension = 0;
		CC = 0;
		Marker = 0;
		Ssrc = 0;

		//Minimal check : the packet contains at least the header
		if (packet_size >= HEADER_SIZE) {
			
			//The first HEADER_SIZE bits composes the Header
			header = new byte[HEADER_SIZE];
			for (int i = 0; i < HEADER_SIZE; i++)
				header[i] = packet[i];

			//The remainder is the payload
			payload_size = packet_size - HEADER_SIZE;
			payload = new byte[payload_size];
			for (int i = HEADER_SIZE; i < packet_size; i++)
				payload[i - HEADER_SIZE] = packet[i];

			//Interpretation of the others fields in the header
			PayloadType = header[1] & 127;
			SequenceNumber = toUnsignedInt(header[3]) + 256 * toUnsignedInt(header[2]);
			TimeStamp = toUnsignedInt(header[7]) + 256 * toUnsignedInt(header[6]) + 65536 * toUnsignedInt(header[5]) + 16777216 * toUnsignedInt(header[4]);
		}
	}

	/**
	 * Return the content of the packet in bytes
	 */
	public int getPayload(byte[] data) {

		for (int i = 0; i < payload_size; i++)
			data[i] = payload[i];

		return (payload_size);
	}
	
	/**
	 * Return the entire packet in bytes
	 */
	public byte[] toBytes() {
		byte[] packet = new byte[(payload_size + HEADER_SIZE)];
		for (int i = 0; i < HEADER_SIZE; i++)
			packet[i] = header[i];
		for (int i = 0; i < payload_size; i++)
			packet[i + HEADER_SIZE] = payload[i];

		return packet;
	}
	
	/**
	 * Length access methods
	 */
	public int getPayloadLength() {
		return (payload_size);
	}
	public int getLength() {
		return (payload_size + HEADER_SIZE);
	}

	/**
	 * Header access methods
	 */
	public int getTimestamp() {
		return (TimeStamp);
	}
	public int getSequenceNumber() {
		return (SequenceNumber);
	}

	public int getPayloadType() {
		return (PayloadType);
	}


	static int toUnsignedInt(int nb) {
		if (nb >= 0)
			return (nb);
		else
			return (256 + nb);
	}

}
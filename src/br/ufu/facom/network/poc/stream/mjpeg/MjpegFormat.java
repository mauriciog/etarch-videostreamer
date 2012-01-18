package br.ufu.facom.network.poc.stream.mjpeg;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import java.util.Properties;


public class MjpegFormat extends JpegFormat {
	/**
	 * The MJPEG framing header should always contain a content type line as:
	 * Content-Type: image/jpeg
	 *
	 */
	public static final String CONTENT_TYPE = "Content-Type";

	/**
	 * Optional MJPEG frame header key used to indicate bytes of jpeg file data.
	 * This header is optional and depends on the API call used with the camera.
	 */
	public static final String CONTENT_LENGTH = "Content-Length";

	/**
	 * Optional MJPEG frame header key used to indicate milliseconds between frames.
	 * This header is optional and depends on the API call used with the camera.
	 */
	public static final String DELTA_TIME = "Delta-time";

	/**
	 * Typical max length of header data.
	 */
	public static int HEADER_MAX_LENGTH = 100;

	/**
	 * Expected length of an mjpeg frame
	 */
	public static int FRAME_MAX_LENGTH = JpegFormat.JPEG_MAX_LENGTH +
		HEADER_MAX_LENGTH;

	/**
	 * Parse the content length string for a MJPEG frame from the given bytes.
	 * The string is parsed into an int and returned.
	 *
	 * @return the Content-Length, or -1 if not found
	 */
	public static int parseContentLength(byte[] headerBytes)
		throws IOException, NumberFormatException {
		return parseContentLength(new ByteArrayInputStream(headerBytes));
	}

	private static int parseContentLength(ByteArrayInputStream headerIn)
		throws IOException, NumberFormatException {
		Properties props = new Properties();
		props.load(headerIn);

		return Integer.parseInt(props.getProperty(MjpegFormat.CONTENT_LENGTH));
	}
}
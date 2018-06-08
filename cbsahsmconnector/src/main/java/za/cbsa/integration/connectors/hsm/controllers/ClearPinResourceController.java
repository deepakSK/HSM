package za.cbsa.integration.connectors.hsm.controllers;

import com.google.gson.Gson;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.codec.DecoderException;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.net.Socket;
import org.apache.commons.codec.binary.Hex;
import za.cbsa.integration.connectors.hsm.adapters.PinblockEncryptor;
import za.cbsa.integration.connectors.hsm.beans.PinBlockBean;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Path("/encryptionservice")
public class ClearPinResourceController {

	private static Logger customLog = Logger.getLogger(ClearPinResourceController.class.getName());

	private static final Log wso2Log = LogFactory.getLog(ClearPinResourceController.class);

	private static final Logger LOGGER = Logger.getLogger(ClearPinResourceController.class.getName());
	private static byte[] eracom_header = new byte[6];
	private static byte[] len_prefix = new byte[2];
	private static int length_of_response = 0;
	private static String resp_str;

	public ClearPinResourceController() {
		customLog.info("ClearPinResource created here");
		wso2Log.info("ClearPinResource created here");
	}

	/**
	 *
	 * @param pin
	 * @param cardNumber
	 * @return
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/encrypt/{pin}/")
	public String getEncryptedPin(@PathParam("pin") String pin) {
		LOGGER.info("in ::getEncryptedPin");
		/// customLog.info(" pin " + pin);
		/// wso2Log.info(" pin " + pin);
		Gson gson = new Gson();
		String key = "8F1F2ACD516ED9D0A88FE39BEC75F1FE";
		/// customLog.info("Key :" + key);
		/// wso2Log.info("Key :" + key);
		// String IP_addr = "10.135.1.122"; //AWS
		// int port = 1500; //AWS
		// String IP_addr = "10.135.1.122";
		// int port = 1500;

		// String IP_addr = "10.210.88.170"; prod/preprod
		// int port = 1559;
		String IP_addr = "10.135.1.122"; // AWS
		int port = 1501;

		customLog.info("Ipadress :" + IP_addr + " Port :" + port);
		wso2Log.info("Ipadress :" + IP_addr + " Port :" + port);
		try {
			String softPin = PinblockEncryptor.getInstance(key).encryptPinBlock(pin);

			String hardBlock = getPinBlock(IP_addr, port, softPin);
			/// customLog.info(" hardBlock :"+ hardBlock);
			/// wso2Log.info(" hardBlock :"+ hardBlock);
			PinBlockBean pinBlockObject = new PinBlockBean(pin);
			pinBlockObject.setProcessedBlock(hardBlock.substring(26, hardBlock.length()));
			/// customLog.info(" hardBlock :"+
			/// pinBlockObject.getProcessedBlock());
			/// wso2Log.info(" hardBlock :"+
			/// pinBlockObject.getProcessedBlock());
			return gson.toJson(pinBlockObject);
		} catch (IOException | InterruptedException | DecoderException | InvalidKeyException | IllegalBlockSizeException
				| NoSuchAlgorithmException | NoSuchPaddingException | BadPaddingException | NumberFormatException ex) {
			LOGGER.error(ex);
			return gson.toJson(ex);
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	public void putJson(String content) {
	}

	private String getPinBlock(String ip, int port, String softPin)
			throws IOException, DecoderException, InterruptedException {

		try (Socket socket = new Socket(ip, port);
				DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
				DataInputStream dataInputStream = new DataInputStream(
						new BufferedInputStream(socket.getInputStream()));) {
			socket.setTcpNoDelay(true);
			String cmd = softPin;
			out.write(Hex.decodeHex(cmd.toCharArray()));
			out.flush();
			dataInputStream.read(eracom_header, 0, 6);
			len_prefix[0] = eracom_header[4];
			len_prefix[1] = eracom_header[5];
			length_of_response = byteToInt(len_prefix);
			byte[] read_buf = new byte[length_of_response];
			dataInputStream.read(read_buf, 0, length_of_response);
			resp_str = byteArrayToHexString(read_buf);
			return resp_str;

		}
	}

	private String hexadecimal(String input, String charsetName) throws UnsupportedEncodingException {
		if (input == null) {
			throw new NullPointerException();
		}
		return asHex(input.getBytes(charsetName));
	}

	private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

	private String asHex(byte[] buf) {
		char[] chars = new char[2 * buf.length];
		for (int i = 0; i < buf.length; ++i) {
			chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
			chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
		}
		return new String(chars);
	}

	public static String byteArrayToHexString(byte in[]) {
		byte ch = 0x00;
		int i = 0;
		if (in == null || in.length <= 0) {
			return null;
		}
		String pseudo[] = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A", "B", "C", "D", "E", "F" };
		StringBuffer out_str_buf = new StringBuffer(in.length * 2);
		while (i < in.length) {
			ch = (byte) (in[i] & 0xF0); // Strip off high nibble
			ch = (byte) (ch >>> 4); // shift the bits down
			ch = (byte) (ch & 0x0F); // must do this is high order bit is on!
			out_str_buf.append(pseudo[(int) ch]); // convert the nibble to a
													// String Character
			ch = (byte) (in[i] & 0x0F); // Strip off low nibble
			out_str_buf.append(pseudo[(int) ch]); // convert the nibble to a
													// String Character
			i++;
		}
		String rslt = new String(out_str_buf);
		return rslt;
	}

	private int byteToInt(byte[] b) {
		int val = 0;
		for (int i = b.length - 1, j = 0; i >= 0; i--, j++) {
			val += (b[i] & 0xff) << (8 * j);
		}
		return val;
	}

}

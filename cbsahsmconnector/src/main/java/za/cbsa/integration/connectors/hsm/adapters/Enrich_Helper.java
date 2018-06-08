package za.cbsa.integration.connectors.hsm.adapters;

import java.io.PrintStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Enrich_Helper {
	public Enrich_Helper() {
	}

	public byte[] encryptPinBlock(String pin) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		String header = "010100000028";
		System.out.println("Header :" + header);

		String id = "ee0800";
		System.out.println("ID :" + id);

		String fm = "00";
		System.out.println("FM :" + fm);

		String dpk = "1111a6c08f5e86b486e8e13d081eb6f2b04f";
		System.out.println("DPK :" + dpk);

		String cm = "00";
		System.out.println("cm :" + cm);

		String icv = "0000000000000000";
		System.out.println("icv :" + icv);

		String pb = "0814" + pin + "ffffffffff";

		System.out.println("pb :" + pb);

		String d = header + id + fm + dpk + cm + icv + pb;

		System.out.println("d :" + d);

		byte[] bytes = hexStringToByteArray(d);
		return bytes;
	}

	public static byte[] hexStringToByteArray(String s) {
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[(i / 2)] = ((byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16)));
		}
		return data;
	}

	private static String getHexString(byte[] input) {
		StringBuilder strBuilder = new StringBuilder();
		byte[] arrayOfByte = input;
		int j = input.length;
		for (int i = 0; i < j; i++) {
			byte hexByte = arrayOfByte[i];
			strBuilder.append(String.format("%02X", new Object[] { Byte.valueOf(hexByte) }));
		}
		return strBuilder.toString();
	}
}
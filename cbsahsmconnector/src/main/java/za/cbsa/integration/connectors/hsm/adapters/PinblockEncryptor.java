package za.cbsa.integration.connectors.hsm.adapters;

import java.io.PrintStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.bind.DatatypeConverter;

public class PinblockEncryptor {
	public static final short KEY_SIZE = 56;
	public static final short KEY_SIZE_DOUBLE = 168;
	private byte[] keyBytes;
	private String encAlgorithmName;
	private String encAlgModeName;
	private static final String PIN_PAD = "FFFFFFFFFFFFFF";
	private static final String ZERO_PAD = "0000000000000000";

	public static PinblockEncryptor getInstance(String key) throws InvalidKeyException, IllegalBlockSizeException {
		return new PinblockEncryptor(key);
	}

	private PinblockEncryptor(String key) throws InvalidKeyException, IllegalBlockSizeException {
		switch (key.length()) {
		case 16:
			keyBytes = getEncryptionKey(key, 56);
			encAlgorithmName = "DES";
			encAlgModeName = "DES/ECB/NoPadding";
			break;
		case 32:
			keyBytes = getEncryptionKey(key, 168);
			encAlgorithmName = "DESede";
			encAlgModeName = "DESede/ECB/NoPadding";
			break;
		default:
			throw new InvalidKeyException("Wrong key size");
		}
	}

	public String encryptPinBlock(String pin) throws NoSuchAlgorithmException, NoSuchPaddingException,
			InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		byte[] pinBlock = getPinBlock(pin);

		return getHexString(pinBlock);
	}

	private static byte[] getPinBlock(String cardNumber, String pin) throws IllegalBlockSizeException {
		int[] paddedPin = padPin(pin);
		int[] paddedCard = padCard(cardNumber);
		byte[] pinBlock = new byte[8];
		for (int cnt = 0; cnt < 8; cnt++) {
			pinBlock[cnt] = ((byte) (paddedPin[cnt] ^ paddedCard[cnt]));
		}
		return pinBlock;
	}

	private static byte[] getPinBlock(String pin) throws IllegalBlockSizeException {
		String header = "010100000028";

		String id = "ee0800";

		String fm = "00";

		String dpk = "1111a6c08f5e86b486e8e13d081eb6f2b04f";

		String cm = "00";

		String icv = "0000000000000000";

		String pb = "0814" + pin + "ffffffffff";

		String d = header + id + fm + dpk + cm + icv + pb;

		System.out.println("d :" + d);

		byte[] pinBlock = DatatypeConverter.parseHexBinary(d);

		return pinBlock;
	}

	private static int[] padPin(String pin) throws IllegalBlockSizeException {
		String pinBlockString = "0" + pin.length() + pin + "FFFFFFFFFFFFFF";
		pinBlockString = pinBlockString.substring(0, 16);
		return getHexIntArray(pinBlockString);
	}

	private static int[] padCard(String cardNumber) throws IllegalBlockSizeException {
		cardNumber = "0000000000000000" + cardNumber;
		int cardNumberLength = cardNumber.length();
		int beginIndex = cardNumberLength - 13;
		String acctNumber = "0000" + cardNumber.substring(beginIndex, cardNumberLength - 1);
		return getHexIntArray(acctNumber);
	}

	private static byte[] getEncryptionKey(String keyString, int keySize)
			throws IllegalBlockSizeException, InvalidKeyException {
		int keyLength = keyString.length();
		switch (keySize) {
		case 56:
			if (keyLength != 16) {
				throw new InvalidKeyException(
						"Hex Key length should be 16 for a 56 Bit Encryption, found [" + keyLength + "]");
			}
			break;
		case 112:
			if (keyLength != 32) {
				throw new InvalidKeyException(
						"Hex Key length should be 32 for a 112 Bit Encryption, found[" + keyLength + "]");
			}
			break;
		case 168:
			if ((keyLength != 32) && (keyLength != 48)) {
				throw new InvalidKeyException(
						"Hex Key length should be 32 or 48 for a 168 Bit Encryption, found[" + keyLength + "]");
			}
			if (keyLength == 32) {
				keyString = keyString + keyString.substring(0, 16);
			}
			break;
		default:
			throw new InvalidKeyException("Invalid Key Size, expected one of [56, 112, 168], found[" + keySize + "]");
		}
		byte[] keyBytes = getHexByteArray(keyString);
		return keyBytes;
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

	private static int[] getHexIntArray(String input) throws IllegalBlockSizeException {
		if (input.length() % 2 != 0) {
			throw new IllegalBlockSizeException("Invalid Hex String, Hex representation length is not a multiple of 2");
		}
		int[] resultHex = new int[input.length() / 2];
		for (int iCnt1 = 0; iCnt1 < input.length(); iCnt1++) {
			String byteString = input.substring(iCnt1++, iCnt1 + 1);
			int hexOut = Integer.parseInt(byteString, 16);
			resultHex[(iCnt1 / 2)] = (hexOut & 0xFF);
		}
		return resultHex;
	}

	private static byte[] getHexByteArray(String input) throws IllegalBlockSizeException {
		int[] resultHex = getHexIntArray(input);
		byte[] returnBytes = new byte[resultHex.length];
		for (int cnt = 0; cnt < resultHex.length; cnt++) {
			returnBytes[cnt] = ((byte) resultHex[cnt]);
		}
		return returnBytes;
	}
}
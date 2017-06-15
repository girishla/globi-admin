package com.globi.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

import org.apache.commons.codec.binary.Base64;

/**
 * Handy utilities for working with UUID's 
 * 
 * @author Girish Lakshmanan 
 *
 */
public class UUIDUtils
{
	/**
	 * Generate a secure random uuid and return it as base 64 encoded url safe string.
	 * 
	 * @return base64 encoded url safe string
	 */
	public static String urlSafeRandomUUID()
	{
		return toBase64Url(UUID.randomUUID());
	}

	/**
	 * Decode a base64 encoded UUID to java UUID. It is assumed that the byte array is stored in big endian fashion that is most significant
	 * bits first. Strings in in traditional base64 and url safe base64.
	 * 
	 * see http://www.ietf.org/rfc/rfc3548.txt for details of the url safe base64 encoding
	 * 
	 * @param base64String
	 *            a base64 encoded string
	 * @return A java UUID
	 */
	public static UUID fromBase64(String base64String)
	{
		return fromBytes(Base64.decodeBase64(base64String));
	}

	/**
	 * Encode a UUUID as a base64 url safe string.
	 * 
	 * @param uuid
	 *            the uuid to encode.
	 * @return base64 encoded string that is safe to use in a url
	 */
	public static String toBase64Url(UUID uuid)
	{
		return Base64.encodeBase64URLSafeString(toBytes(uuid));
	}

	/**
	 * Convert a UUID to bytes
	 * 
	 * @param uuid
	 * @return byte array from the uuid
	 */
	public static byte[] toBytes(UUID uuid)
	{
		byte[] bytes = new byte[16];
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		bb.order(ByteOrder.BIG_ENDIAN);
		bb.putLong(uuid.getMostSignificantBits());
		bb.putLong(uuid.getLeastSignificantBits());
		return bb.array();
	}

	/**
	 * Convert bytes to UUD, bytes must be in big endian which means significant bits first.
	 * 
	 * @param bytes
	 *            array in big endian format.
	 * @return UUID
	 */
	public static UUID fromBytes(byte[] bytes)
	{
		ByteBuffer bb = ByteBuffer.wrap(bytes);
		return new UUID(bb.getLong(), bb.getLong());
	}

	/**
	 * 
	 * @param uuid
	 *            String represenation of a uuid
	 * @return true if the uuid is valid, false otherwise
	 */
	public static boolean isValid(String uuid)
	{
		if (uuid == null)
			return false;
		try
		{
			// we have to convert to object and back to string because the built in fromString does not have
			// good validation logic.
			UUID fromStringUUID = UUID.fromString(uuid);
			String toStringUUID = fromStringUUID.toString();
			return toStringUUID.equals(uuid);
		} catch (IllegalArgumentException e)
		{
			return false;
		}
	}
}

package org.jopenray.rdp;

public class RDPError {

	public static final int exDiscReasonNoInfo = 0x0000;
	public static final int exDiscReasonAPIInitiatedDisconnect = 0x0001;
	public static final int exDiscReasonAPIInitiatedLogoff = 0x0002;
	public static final int exDiscReasonServerIdleTimeout = 0x0003;
	public static final int exDiscReasonServerLogonTimeout = 0x0004;
	public static final int exDiscReasonReplacedByOtherConnection = 0x0005;
	public static final int exDiscReasonOutOfMemory = 0x0006;
	public static final int exDiscReasonServerDeniedConnection = 0x0007;
	public static final int exDiscReasonServerDeniedConnectionFips = 0x0008;
	public static final int exDiscReasonLicenseInternal = 0x0100;
	public static final int exDiscReasonLicenseNoLicenseServer = 0x0101;
	public static final int exDiscReasonLicenseNoLicense = 0x0102;
	public static final int exDiscReasonLicenseErrClientMsg = 0x0103;
	public static final int exDiscReasonLicenseHwidDoesntMatchLicense = 0x0104;
	public static final int exDiscReasonLicenseErrClientLicense = 0x0105;
	public static final int exDiscReasonLicenseCantFinishProtocol = 0x0106;
	public static final int exDiscReasonLicenseClientEndedProtocol = 0x0107;
	public static final int exDiscReasonLicenseErrClientEncryption = 0x0108;
	public static final int exDiscReasonLicenseCantUpgradeLicense = 0x0109;
	public static final int exDiscReasonLicenseNoRemoteConnections = 0x010a;

	/**
	 * Translate a disconnect code into a text message
	 * 
	 * @param errorCode
	 *            disconnect code received from server
	 * @return the text message
	 */
	public static String getMessageFromDisconnectError(int errorCode) {
		String text;

		switch (errorCode) {
		case exDiscReasonNoInfo:
			text = "No information available";
			break;

		case exDiscReasonAPIInitiatedDisconnect:
			text = "Server initiated disconnect";
			break;

		case exDiscReasonAPIInitiatedLogoff:
			text = "Server initiated logoff";
			break;

		case exDiscReasonServerIdleTimeout:
			text = "Server idle timeout reached";
			break;

		case exDiscReasonServerLogonTimeout:
			text = "Server logon timeout reached";
			break;

		case exDiscReasonReplacedByOtherConnection:
			text = "Another user connected to the session";
			break;

		case exDiscReasonOutOfMemory:
			text = "The server is out of memory";
			break;

		case exDiscReasonServerDeniedConnection:
			text = "The server denied the connection";
			break;

		case exDiscReasonServerDeniedConnectionFips:
			text = "The server denied the connection for security reason";
			break;

		case exDiscReasonLicenseInternal:
			text = "Internal licensing error";
			break;

		case exDiscReasonLicenseNoLicenseServer:
			text = "No license server available";
			break;

		case exDiscReasonLicenseNoLicense:
			text = "No valid license available";
			break;

		case exDiscReasonLicenseErrClientMsg:
			text = "Invalid licensing message";
			break;

		case exDiscReasonLicenseHwidDoesntMatchLicense:
			text = "Hardware id doesn't match software license";
			break;

		case exDiscReasonLicenseErrClientLicense:
			text = "Client license error";
			break;

		case exDiscReasonLicenseCantFinishProtocol:
			text = "Network error during licensing protocol";
			break;

		case exDiscReasonLicenseClientEndedProtocol:
			text = "Licensing protocol was not completed";
			break;

		case exDiscReasonLicenseErrClientEncryption:
			text = "Incorrect client license enryption";
			break;

		case exDiscReasonLicenseCantUpgradeLicense:
			text = "Can't upgrade license";
			break;

		case exDiscReasonLicenseNoRemoteConnections:
			text = "The server is not licensed to accept remote connections";
			break;

		default:
			if (errorCode > 0x1000 && errorCode < 0x7fff) {
				text = "Internal protocol error";
			} else {
				text = "Unknown reason";
			}
		}
		return text;
	}

}

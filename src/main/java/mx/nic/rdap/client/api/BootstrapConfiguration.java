package mx.nic.rdap.client.api;

import java.time.DateTimeException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import mx.nic.rdap.client.exception.ConfigurationException;

public class BootstrapConfiguration {

	private boolean isFileMode;

	private String dnsUrl;

	private String ipv4Url;

	private String ipv6Url;

	private String asnUrl;

	private long timerPeriod;

	private static final String dnsKey = "bootstrap.dns_url";
	private static final String ipv4Key = "bootstrap.ipv4_url";
	private static final String ipv6Key = "bootstrap.ipv6_url";
	private static final String asnKey = "bootstrap.asn_url";
	private static final String isFileModeKey = "bootstrap.is_file_mode";
	private static final String timerPeriodKey = "bootstrap.timer_period";
	private static final String minimumTimeToUpdateKey = "bootstrap.minimum_time_to_update_bootstrap";

	protected BootstrapConfiguration(Properties properties) throws ConfigurationException {
		isFileMode = parseBoolean(properties.getProperty(isFileModeKey));
		dnsUrl = properties.getProperty(dnsKey);
		ipv4Url = properties.getProperty(ipv4Key);
		ipv6Url = properties.getProperty(ipv6Key);
		asnUrl = properties.getProperty(asnKey);

		timerPeriod = getPeriod(TimeUnit.MILLISECONDS, properties.getProperty(timerPeriodKey));
		String minimumTimeToUpdateString = properties.getProperty(minimumTimeToUpdateKey);
		long minimumTimeToUpdate = getPeriod(TimeUnit.MILLISECONDS, minimumTimeToUpdateString);

		if (timerPeriod > 0 && timerPeriod < minimumTimeToUpdate) {
			throw new ConfigurationException(timerPeriodKey + ":" + properties.getProperty(timerPeriodKey)
					+ " must be greater than " + minimumTimeToUpdateString);
		}
	}

	private static long getPeriod(TimeUnit destUnit, String period) {
		if (period == null) {
			throw new DateTimeException("Invalid time unit format: 'null period'");
		}
		period = period.trim();
		if (period.isEmpty()) {
			throw new DateTimeException("Invalid time unit format: 'empty period'");
		}
		int periodSize = 1;
		if (period.endsWith("ms")) {
			periodSize = 2;
		}
		String timeUnit = period.substring(period.length() - periodSize).toLowerCase();
		long duration = Long.parseLong(period.substring(0, period.length() - periodSize).trim());

		long result;
		switch (timeUnit) {
		case "ms":
			result = destUnit.convert(duration, TimeUnit.MILLISECONDS);
			break;
		case "s":
			result = destUnit.convert(duration, TimeUnit.SECONDS);
			break;
		case "m":
			result = destUnit.convert(duration, TimeUnit.MINUTES);
			break;
		case "h":
			result = destUnit.convert(duration, TimeUnit.HOURS);
			break;
		case "d":
			result = destUnit.convert(duration, TimeUnit.DAYS);
			break;
		default:
			throw new DateTimeException("Invalid time unit format: '" + timeUnit + "'");
		}

		return result;
	}

	public boolean parseBoolean(String booleanString) throws ConfigurationException {
		if (booleanString == null) {
			throw new ConfigurationException("missing value : " + isFileModeKey);
		}
		booleanString = booleanString.trim().toLowerCase();
		boolean result;
		switch (booleanString) {
		case "false":
			result = false;
			break;
		case "true":
			result = true;
			break;
		default:
			throw new ConfigurationException("Invalid value " + isFileModeKey + "=" + booleanString);
		}

		return result;
	}

	/**
	 * @return true if the bootstrap is in file mode, <code>false</code> if the
	 *         Factory should bring the bootstraps from the root.
	 */
	public boolean isFileMode() {
		return isFileMode;
	}

	public String getAsnUrl() {
		return asnUrl;
	}

	public String getDnsUrl() {
		return dnsUrl;
	}

	public String getIpv4Url() {
		return ipv4Url;
	}

	public String getIpv6Url() {
		return ipv6Url;
	}

	public long getTimerPeriod() {
		return timerPeriod;
	}
}

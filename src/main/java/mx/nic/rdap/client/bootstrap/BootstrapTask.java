package mx.nic.rdap.client.bootstrap;

import java.io.IOException;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BootstrapTask extends TimerTask {

	private static Logger logger = Logger.getLogger(BootstrapTask.class.getName());

	@Override
	public void run() {
		logger.log(Level.INFO, "Starting Bootstrap objects update.");
		try {
			BootstrapFactory.updateBootstrap();
			logger.log(Level.INFO, "Bootstrap objects updated.");
		} catch (IOException | BootstrapException e) {
			logger.log(Level.SEVERE,
					"Error while updating the bootstrap, the previous version of bootstrap files will be used", e);
		}

	}

}

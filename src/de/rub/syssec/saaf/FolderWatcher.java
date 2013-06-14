/**
 * 
 */
package de.rub.syssec.saaf;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.log4j.Logger;

import de.rub.syssec.saaf.analysis.Analysis;
import de.rub.syssec.saaf.application.Application;
import de.rub.syssec.saaf.model.analysis.AnalysisException;

public class FolderWatcher {

	private static final Logger LOGGER = Logger.getLogger(FolderWatcher.class);

	private String path;
	private long interval;
	private ExecutorService fileConsumer;

	public FolderWatcher(String path, long interval) {
		super();
		this.path = path;
		this.interval = interval;
		this.fileConsumer = Executors.newFixedThreadPool(10);
	}

	public void startWatching() throws Exception {
		// The monitor will perform polling on the folder every 5 seconds
		final long pollingInterval = interval;

		File folder = new File(path);

		if (!folder.exists()) {
			// Test to see if monitored folder exists
			throw new RuntimeException("Directory not found: " + path);
		}

		FileAlterationObserver observer = new FileAlterationObserver(folder);
		FileAlterationMonitor monitor = new FileAlterationMonitor(
				pollingInterval);
		FileAlterationListener listener = new FileAlterationListenerAdaptor() {
			// Is triggered when a file is created in the monitored folder
			@Override
			public void onFileCreate(File file) {
				try {
					// "file" is the reference to the newly created file
					LOGGER.info("File created: " + file.getCanonicalPath());
					try {
						if(Application.isAPKFile(file))
						{
							fileConsumer.submit(new AnalysisTask(file));
						}
					} catch (AnalysisException e) {
						LOGGER.error(e);
					}
				} catch (IOException e) {
					LOGGER.error(e);
				}
			}

//			// Is triggered when a file is deleted from the monitored folder
//			@Override
//			public void onFileDelete(File file) {
//				try {
//					// "file" is the reference to the removed file
//					LOGGER.info("File removed: " + file.getCanonicalPath());
//					// "file" does not exists anymore in the location
//					workItems.remove(file);
//				} catch (IOException e) {
//					LOGGER.error(e);
//				}
//			}
		};
		
		
		
		observer.addListener(listener);
		monitor.addObserver(observer);
		LOGGER.info("Monitoring " + path + " for new apks");
		monitor.start();
	}
}

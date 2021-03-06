package cz.jfx.CodeAnalyser.TaskManager.Runners;

import cz.jfx.CodeAnalyser.Control.LoaderController;
import cz.jfx.CodeAnalyser.TaskManager.TaskManager;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Felix
 */
public class Loader extends Runner {

    private static final Logger logger = Logger.getLogger(Loader.class.getName());
    private LoaderController controller;

    public Loader(TaskManager tm, LoaderController controller) {
        super(tm);
        this.controller = controller;
    }

    public Loader(String name, TaskManager tm, LoaderController controller) {
        super(name, tm);
        this.controller = controller;
    }

    public void run() {
        running = true;
        while (running) {
            logger.log(Level.FINEST, "{0} - starts", Thread.currentThread().getName());
            setStatus(Runner.RUNNING);

            // Checking, if storage is empty..
            if (controller.isFolderStorageEmpty()) {
                // Then waiting..
                synchronized (this) {
                    try {
                        setStatus(Runner.WAITING);
                        controller.checkLoadingProcess();
                        logger.log(Level.FINEST, "{0} - waiting", Thread.currentThread().getName());
                        wait();
                        continue;
                    } catch (InterruptedException ex) {
                        logger.log(Level.FINEST, "{0} - was intterupted", Thread.currentThread().getName());
                        setRunning(false);
                        setStatus(Runner.OFF);
                        break;
                    }
                }
            }

            // Search and process files + folders
            search(controller.nextFolder());

            // Yielding..
            logger.log(Level.FINEST, "{0} - yield", Thread.currentThread().getName());
            yield();
        }
    }

    private void search(File folder) {
        if (folder == null || !folder.isDirectory()) {
            return;
        }

        File[] objects;
        if (filter != null) {
            objects = folder.listFiles(filter);
        } else {
            objects = folder.listFiles();
        }

        if (objects == null || objects.length == 0) {
            return;
        }

        for (File f : objects) {
            if (f.canRead()) {
                if (f.isFile()) {
                    controller.addFile(f);
                    logger.log(Level.FINER, "{0} - file ({1})", new Object[]{Thread.currentThread().getName(), f.getAbsolutePath()});
                } else if (f.isDirectory()) {
                    controller.addFolder(f);
                    logger.log(Level.FINER, "{0} - folder ({1})", new Object[]{Thread.currentThread().getName(), f.getAbsolutePath()});
                }
            }
        }
    }
}

package de.feuerwehr.kremmen.dispatcher.ocr;

import de.feuerwehr.kremmen.dispatcher.config.Config;
import java.io.File;
import java.util.logging.Logger;

public class OCRScanner {

    private static final Logger LOG = Logger.getLogger(OCRScanner.class.getSimpleName());

    private static Client restClient;

    /**
     * @param args
     */
    public static File pdfToTxt(File fileToScan) throws Exception {
        if (Boolean.valueOf(Config.get(Config.KEY_RETURN_TEST_TXT))) {
            LOG.warning("Running in test mode. Returning default fax");
            return new File(System.getProperty("java.io.tmpdir"), "fax.txt");
        }
        LOG.fine("Process documents using ABBYY Cloud OCR SDK.\n");

        restClient = new Client();
        // replace with 'https://cloud.ocrsdk.com' to enable secure connection
        restClient.serverUrl = "http://cloud.ocrsdk.com";
        restClient.applicationId = Config.get(Config.KEY_ABBYY_OCR_APPLICATION_ID);
        restClient.password = Config.get(Config.KEY_ABBYY_OCR_API_KEY);

        return performRecognition(fileToScan);
    }

    /**
     * Parse command line and recognize one or more documents.
     */
    private static File performRecognition(File fileToScan)
            throws Exception {
        String language = "German";
        String outputPath = new File(System.getProperty("java.io.tmpdir"), fileToScan.getName() + ".txt").getAbsolutePath();

        ProcessingSettings.OutputFormat outputFormat = ProcessingSettings.OutputFormat.txt;

        ProcessingSettings settings = new ProcessingSettings();
        settings.setLanguage(language);
        settings.setOutputFormat(outputFormat);

        Task task = null;
        LOG.info("Uploading file..");
        task = restClient.processImage(fileToScan.getAbsolutePath(), settings);
        waitAndDownloadResult(task, outputPath);
        File resultFile = new File(outputPath);
        if (resultFile.exists()) {
            return resultFile;
        }
        throw new OCRException("Textfile was not created");

    }

    /**
     * Wait until task processing finishes
     */
    private static Task waitForCompletion(Task task) throws Exception {
        // Note: it's recommended that your application waits
        // at least 2 seconds before making the first getTaskStatus request
        // and also between such requests for the same task.
        // Making requests more often will not improve your application performance.
        // Note: if your application queues several files and waits for them
        // it's recommended that you use listFinishedTasks instead (which is described
        // at http://ocrsdk.com/documentation/apireference/listFinishedTasks/).
        while (task.isTaskActive()) {

            Thread.sleep(5000);
            System.out.println("Waiting..");
            task = restClient.getTaskStatus(task.Id);
        }
        return task;
    }

    /**
     * Wait until task processing finishes and download result.
     */
    private static void waitAndDownloadResult(Task task, String outputPath)
            throws Exception {
        task = waitForCompletion(task);

        if (task.Status == Task.TaskStatus.Completed) {
            System.out.println("Downloading..");
            restClient.downloadResult(task, outputPath);
            System.out.println("Ready");
        } else if (task.Status == Task.TaskStatus.NotEnoughCredits) {
            System.out.println("Not enough credits to process document. "
                    + "Please add more pages to your application's account.");
        } else {
            System.out.println("Task failed");
        }

    }

    /**
     * Extract output format from extension of output file.
     */
    private static ProcessingSettings.OutputFormat outputFormatByFileExt(
            String filePath) {
        int extIndex = filePath.lastIndexOf('.');
        if (extIndex < 0) {
            System.out
                    .println("No file extension specified. Plain text will be used as output format.");
            return ProcessingSettings.OutputFormat.txt;
        }
        String ext = filePath.substring(extIndex).toLowerCase();
        if (ext.equals(".txt")) {
            return ProcessingSettings.OutputFormat.txt;
        } else if (ext.equals(".xml")) {
            return ProcessingSettings.OutputFormat.xml;
        } else if (ext.equals(".pdf")) {
            return ProcessingSettings.OutputFormat.pdfSearchable;
        } else if (ext.equals(".docx")) {
            return ProcessingSettings.OutputFormat.docx;
        } else if (ext.equals(".rtf")) {
            return ProcessingSettings.OutputFormat.rtf;
        } else {
            System.out
                    .println("Unknown output extension. Plain text will be used.");
            return ProcessingSettings.OutputFormat.txt;
        }
    }

}

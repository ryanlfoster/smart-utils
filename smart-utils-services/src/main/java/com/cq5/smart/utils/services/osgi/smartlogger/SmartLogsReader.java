package com.cq5.smart.utils.services.osgi.smartlogger;

import com.cq5.smart.utils.services.osgi.common.FileWriterDecorator;
import com.cq5.smart.utils.services.osgi.common.LogFilter;
import com.cq5.smart.utils.services.osgi.common.OutputStrategy;
import com.cq5.smart.utils.services.osgi.common.impl.EmptyLogFilter;
import com.cq5.smart.utils.services.osgi.common.impl.LogFilterImpl;
import com.cq5.smart.utils.services.osgi.common.impl.SimpleOutputStrategy;
import com.cq5.smart.utils.services.osgi.common.impl.ZipOutputStrategy;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.settings.SlingSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: Andrii_Manuiev
 * Date: 20.05.13
 * Time: 14:53
 * This service is output a log file into browser or zip file.
 */
@SlingServlet(metatype = false, paths = SmartLogsReader.MAPPING_PATH)
public class SmartLogsReader extends SlingAllMethodsServlet {

    public static final java.lang.String MAPPING_PATH = "/services/smart-utils/logger";

    private static final Logger LOGGER = LoggerFactory.getLogger(SmartLogsReader.class);

    private static final String PAGE_ENCODING = "UTF-8";

    private static final String LOG_FILE_PARAM = "file";
    private static final String LOG_FILTER_PARAM = "filter";
    private static final String LOG_ACTION_PARAM = "action";
    private static final String ZIP = "zip";

    @Reference
    private SlingSettingsService slingSettings;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        response.setCharacterEncoding(PAGE_ENCODING);

        String requestedFile = request.getParameter(LOG_FILE_PARAM);
        String requestedFilter = request.getParameter(LOG_FILTER_PARAM);

        String requestedAction = request.getParameter(LOG_ACTION_PARAM);

        File logFile = new File(slingSettings.getAbsolutePathWithinSlingHome("logs").concat(File.separator).concat(requestedFile));
        LogFilter filter = StringUtils.isNotBlank(requestedFilter) ? new LogFilterImpl(requestedFilter) : new EmptyLogFilter();

        if (logFile.exists()) {
            OutputStrategy writeStrategy = getOutputWriter(requestedAction.equals(ZIP));
            writeStrategy.execute(response, logFile, filter);
        } else {
            LOGGER.error("Wrong file name, no log found: {}", logFile.getAbsolutePath());
        }
    }

    private OutputStrategy getOutputWriter(boolean isZip) {
        return isZip ? new ZipOutputStrategy() : new SimpleOutputStrategy();
    }

}

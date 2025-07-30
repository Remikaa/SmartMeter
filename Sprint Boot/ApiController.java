package com.Meter.IskraemcoInterns;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.math.BigDecimal;
import java.util.Calendar;



/**
 * REST controller that exposes Iskra DLMS smart-meter functionality over HTTP.
 *
 * Supported operations:
 * <ul>
 *   <li>Read active energy (kWh)</li>
 *   <li>Read meter RTC</li>
 *   <li>Open / close the internal relay</li>
 *   <li>Query relay state</li>
 *   <li>Gracefully disconnect / reconnect the serial port</li>
 * </ul>
 *
 * The controller implements {@link SmartLifecycle} so the serial port is
 * automatically closed when the Spring application shuts down.
 *
 * All endpoints are mapped under {@code /api/meter/**}.
 */
@RestController
@RequestMapping("/api/meter")
@Service
public class ApiController implements SmartLifecycle {

    /** Lazily-initialised DLMS reader; created on first use. */
    private volatile IskraDLMSReader reader;
    /** Flag used by SmartLifecycle to indicate running state. */// refactored class
    private volatile boolean running;
    /** Absolute path to JSON configuration file. */
    private static final Path CFG_FILE = Paths.get("D:/IntelliJ/Projects/IskraemcoInterns/src/main/resources/meter-config.json");
    /** Jackson mapper for deserialising {@code meter-config.json}. */
    private final ObjectMapper mapper = new ObjectMapper();


    /* ---------- helpers ---------- */

    /**
     * Loads meter configuration from {@link #CFG_FILE}.
     *
     * @return populated {@link MeterConfig} object
     * @throws Exception if the file is missing or malformed
     */
    private MeterConfig loadConfig() throws Exception {
        return mapper.readValue(CFG_FILE.toFile(), MeterConfig.class);
    }

    /**
     * Thread-safe factory for the {@link IskraDLMSReader} singleton.
     * <p>
     * If the reader has not yet been created (or was previously closed),
     * a new instance is built from the configuration file and the serial
     * connection is established.
     *
     * @return ready-to-use DLMS reader
     * @throws Exception on any I/O or DLMS error
     */
    private synchronized IskraDLMSReader getReader() throws Exception {
        if (reader == null) {
            MeterConfig cfg = loadConfig();
            reader = new IskraDLMSReader(
                    cfg.server.port,
                    cfg.server.baudRate,
                    cfg.server.dataBits,
                    cfg.server.stopBits,
                    cfg.server.parity,
                    cfg.client.clientAddress,
                    cfg.client.serverAddress,
                    cfg.client.authentication,
                    cfg.client.password,
                    cfg.client.interfaceType);
            reader.meterConnection();
        }
        return reader;
    }

    /* SmartLifecycle so the serial port is closed gracefully */

    /** Marks the controller as started; invoked by Spring. */
    @Override public void start() { running = true; }
    @Override public void stop() { running = false; reader.close(); }
    @Override public boolean isRunning() { return running; }

    /* ---------- endpoints ---------- */

    /**
     * GET /api/meter/energy
     *
     * @return cumulative active energy in kWh as plain text
     * @throws Exception on communication or parsing error
     */
    @GetMapping("/energy")
    public String readEnergy() throws Exception {
        return getReader().registerReader();
    }

    /**
     * GET /api/meter/clock
     *
     * @return meter RTC as plain text (format depends on meter firmware)
     * @throws Exception on communication or parsing error
     */
    @GetMapping("/clock")
    public String readClock() throws Exception {
        return getReader().clockReader();
    }

    /**
     * GET /api/meter/disconnect
     *
     * Opens the internal relay (cuts power to the load).
     *
     * @return confirmation message
     * @throws Exception on communication or relay failure
     */
    @GetMapping("/disconnect")
    public String disconnectRelay() throws Exception {
        return getReader().relayDisconnector();
    }

    /**
     * GET /api/meter/connect
     *
     * Closes the internal relay (restores power to the load).
     *
     * @return confirmation message
     * @throws Exception on communication or relay failure
     */
    @GetMapping("/connect")
    public String connectRelay() throws Exception {
        return getReader().relayConnector();
    }

    /**
     * GET /api/meter/status
     *
     * @return current relay state, typically {@code OPEN} or {@code CLOSED}
     * @throws Exception on communication error
     */
    @GetMapping("/status")
    public String relayStatus() throws Exception {
        return getReader().relayStateReader();
    }

    /**
     * disconnects the media (serial port and connection and so)
     * @return a string that the disconnection is successful
     * @throws Exception the meter doesn't respond, IO error, and such
     */
    @GetMapping("/mediaDisconnect")
    public String closeConnection() throws Exception {
        getReader().close();
        if (reader != null) {
           reader = null;
        }
        return "successfully closed the connection 🥹";
    }

    /**
     * connects the media again (serial port and connection with a handshake and so)
     * @return the connection is successful
     * @throws Exception the meter doesn't respond, IO error, and such
     */
    @GetMapping("/mediaConnect")
    public String openConnection() throws Exception {
        getReader().relayStateReader();
        return "successfully opened the connection 😎";
    }


    /**
     * Invoked by Spring on JVM shutdown.
     * Ensures the serial port is released even if {@link #stop()} was not called.
     */
    @PreDestroy
    public void close() {
        if (reader != null) {
            reader.close();

        }
    }

}

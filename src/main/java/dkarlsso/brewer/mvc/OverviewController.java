package dkarlsso.brewer.mvc;

import com.pi4j.io.gpio.GpioFactory;
import dkarlsso.brewer.model.SettingsData;
import dkarlsso.brewer.rpi.EffectRegulator;
import dkarlsso.commons.raspberry.OSHelper;
import dkarlsso.commons.raspberry.enums.GPIOPins;
import dkarlsso.commons.raspberry.relay.OptoRelay;
import dkarlsso.commons.raspberry.relay.StubRelay;
import dkarlsso.commons.raspberry.sensor.temperature.DS18B20;
import dkarlsso.commons.raspberry.sensor.temperature.TemperatureSensorStub;
import dkarlsso.commons.repository.settings.SettingsFilesystemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class OverviewController {

    private final static Logger log = LoggerFactory.getLogger(OverviewController.class);

    private final EffectRegulator effectRegulator;

    private final Thread thread;

    private final SettingsFilesystemRepository<SettingsData> fileDataRepository =
            new SettingsFilesystemRepository<>(SettingsData.class, OSHelper.isRaspberryPi()
            ? "/var/opt/brewer/brewer-settings.json"
                    : "C:\\Users\\Dag Karlsson\\brewer-settings.json", SettingsData::new);

    @Autowired
    public OverviewController() {
        if (OSHelper.isRaspberryPi()) {
            GpioFactory.getInstance();
            effectRegulator = new EffectRegulator(
                    new OptoRelay(GPIOPins.GPIO14_TXDO, true),
                    new DS18B20("28-01144b7e8aaa"), // Column sensor
                    new DS18B20("28-01145512f4aa"),
                    fileDataRepository.read()); // Tank sensor
        }
        else {
            effectRegulator = new EffectRegulator(new StubRelay(),
                    new TemperatureSensorStub(70.9),
                    new TemperatureSensorStub(80.9),
                    fileDataRepository.read());
        }
        thread = new Thread(effectRegulator);
        thread.start();
    }

    @GetMapping(value = "/")
    public String overview(final ModelMap model) {
        model.addAttribute("heaterData", effectRegulator.getHeaterData());
        model.addAttribute("settingsData", effectRegulator.getSettingsData());
        return "overview";
    }

    @PostMapping(value = "/")
    public String overviewPost(final ModelMap model,
                               @ModelAttribute(value = "settingsData") final SettingsData settingsData) {
        this.fileDataRepository.save(settingsData);
        this.effectRegulator.setSettingsData(settingsData);
        model.addAttribute("heaterData", effectRegulator.getHeaterData());
        return "overview";
    }
}

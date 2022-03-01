package dkarlsso.brewer.rpi;

import dkarlsso.brewer.model.HeaterData;
import dkarlsso.brewer.model.SettingsData;
import dkarlsso.commons.raspberry.relay.interfaces.RelayInterface;
import dkarlsso.commons.raspberry.sensor.temperature.TemperatureSensor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

@Slf4j
public class EffectRegulator implements Runnable {



    private final RelayInterface relayInterface;

    private final HeaterData heaterData = new HeaterData();

    private final TemperatureSensor columnTemperatureSensor;

    private final TemperatureSensor tankTemperatureSensor;

    private SettingsData settingsData;

    private int iterationsCounter = 0;

    public EffectRegulator(RelayInterface relayInterface,
                           TemperatureSensor columnTemperatureSensor,
                           TemperatureSensor tankTemperatureSensor,
                           SettingsData settingsData) {
        this.relayInterface = relayInterface;
        this.columnTemperatureSensor = columnTemperatureSensor;
        this.tankTemperatureSensor = tankTemperatureSensor;
        this.settingsData = settingsData;
    }

    void heatForIteration() {
        try {
            final double columnTemp = columnTemperatureSensor.readTemp();
            final double tankTemp = tankTemperatureSensor.readTemp();
            final SettingsData settingsData;
            synchronized (this) {
                heaterData.setColumnTemperature(columnTemp);
                heaterData.setTankTemperature(tankTemp);
                settingsData = SettingsData.clone(this.settingsData);
            }
            final double columnTemperatureLimit = settingsData.getColumnTemperatureLimit();
            final double tankTemperatureLimit = settingsData.getTankTemperatureLimit();
            final Duration sleepTime = settingsData.getSleepTime();

            if (tankTemp < 5 || columnTemp < 5 || tankTemp > 100 || columnTemp > 100) {
                log.error("Temperatures are weird, shutting off. Tank {}, column {}", tankTemp, columnTemp);
                relayInterface.setLow();
                Thread.sleep(2000);
            }
            else if (tankTemp < settingsData.getMagicLimit() && columnTemp < settingsData.getMagicLimit()) {
                relayInterface.setHigh();
                log.info("Full effect, sleeping for {} ms. Column temp {} and tank temp {}", sleepTime.toMillis(), columnTemp, tankTemp);
                Thread.sleep(sleepTime.toMillis());
            }
            else if (columnTemp > columnTemperatureLimit || tankTemp > tankTemperatureLimit) {
                relayInterface.setLow();
                log.info("Stopped heating, reached column temp {} and tank temp {}", columnTemp, tankTemp);
                Thread.sleep(2000);
            }
            else {
                final double percentOfMaximalEffect = ((double)settingsData.getWantedEffectWatts())
                        / ((double)settingsData.getTotalEffectWatts());

                final long timeToBeOn = (long)(((double)sleepTime.toMillis())
                        * percentOfMaximalEffect);
                final long timeToBeOff = sleepTime.toMillis() - timeToBeOn;

                if (iterationsCounter > 5) {
                    log.info("Column temp is {} and tank temperature is {}. Effect at {}%", columnTemp, tankTemp, percentOfMaximalEffect * ((double)100));
                    iterationsCounter = 0;
                }

                log.debug("Turning on relay for {} ms", timeToBeOn);
                relayInterface.setHigh();
                Thread.sleep(timeToBeOn);

                log.debug("Turning off relay for {} ms", timeToBeOff);
                relayInterface.setLow();
                Thread.sleep(timeToBeOff);
                iterationsCounter++;
            }
        }
        catch (final Throwable e) {
            log.error("Could not read temperature", e);
            relayInterface.setLow();
        }
    }

    @Override
    public void run() {
        while (true) {
            heatForIteration();
        }
    }

    public void setSettingsData(final SettingsData settingsData) {
        synchronized (this) {
            this.settingsData = SettingsData.clone(settingsData);
        }
    }

    public SettingsData getSettingsData() {
        return SettingsData.clone(settingsData);
    }

    public HeaterData getHeaterData() {
        synchronized (this) {
            return HeaterData.clone(heaterData);
        }
    }
}

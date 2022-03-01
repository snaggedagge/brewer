package dkarlsso.brewer.rpi;

import dkarlsso.brewer.model.SettingsData;
import dkarlsso.commons.raspberry.exception.NoConnectionException;
import dkarlsso.commons.raspberry.relay.interfaces.RelayInterface;
import dkarlsso.commons.raspberry.sensor.temperature.TemperatureSensor;
import dkarlsso.commons.raspberry.sensor.temperature.TemperatureSensorStub;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

@Slf4j
public class EffectRegulatorTest {

    private EffectRegulator effectRegulator;

    private RelayInterface relay;

    private TemperatureSensorStub columnSensor;

    private TemperatureSensorStub tankSensor;

    @BeforeEach
    public void setup() {
        columnSensor = new TemperatureSensorStub(20);
        tankSensor = new TemperatureSensorStub(20);
        relay = Mockito.mock(RelayInterface.class);
        effectRegulator = new EffectRegulator(relay, columnSensor, tankSensor, new SettingsData());
        final SettingsData settingsData = effectRegulator.getSettingsData();
        settingsData.setSleepTimeSeconds(1);
        effectRegulator.setSettingsData(settingsData);
    }

    @Test
    public void effectRegulator_givenRoomHeat_expectHeating() {
        effectRegulator.heatForIteration();
        verify(relay, times(1)).setHigh();
        verify(relay, times(0)).setLow();
    }

    @Test
    public void effectRegulator_givenMagicTemperatureWindow_expectHeatingSlowly() {
        columnSensor.setTemperature(20); // Spirits rise to column after tank is 78.3
        tankSensor.setTemperature(75);
        effectRegulator.heatForIteration();
        verify(relay, times(1)).setHigh();
        verify(relay, times(1)).setLow();
    }

    @Test
    public void effectRegulator_givenWarmColumnTemperature_expectNoHeating() {
        columnSensor.setTemperature(effectRegulator.getSettingsData().getColumnTemperatureLimit() + 1);
        tankSensor.setTemperature(75);
        effectRegulator.heatForIteration();
        verify(relay, times(0)).setHigh();
        verify(relay, times(1)).setLow();
    }

    @Test
    public void effectRegulator_givenWarmTankTemperature_expectNoHeating() {
        columnSensor.setTemperature(70);
        tankSensor.setTemperature(effectRegulator.getSettingsData().getTankTemperatureLimit() + 1);
        effectRegulator.heatForIteration();
        verify(relay, times(0)).setHigh();
        verify(relay, times(1)).setLow();
    }

    @Test
    public void effectRegulator_givenBadTankSensor_expectHeatingSlowly() {
        columnSensor.setTemperature(70);
        tankSensor.setTemperature(20);
        effectRegulator.heatForIteration();
        verify(relay, times(1)).setHigh();
        verify(relay, times(1)).setLow();
    }

    @Test
    public void effectRegulator_whenTankSensorShowsLowValue_expectNoHeating() {
        columnSensor.setTemperature(70);
        tankSensor.setTemperature(0);
        effectRegulator.heatForIteration();
        verify(relay, times(0)).setHigh();
        verify(relay, times(1)).setLow();
    }

    @Test
    public void effectRegulator_whenTankSensorShowsHighValue_expectNoHeating() {
        columnSensor.setTemperature(70);
        tankSensor.setTemperature(120);
        effectRegulator.heatForIteration();
        verify(relay, times(0)).setHigh();
        verify(relay, times(1)).setLow();
    }

    @Test
    public void effectRegulator_whenColumnSensorShowsLowValue_expectNoHeating() {
        columnSensor.setTemperature(0);
        tankSensor.setTemperature(70);
        effectRegulator.heatForIteration();
        verify(relay, times(0)).setHigh();
        verify(relay, times(1)).setLow();
    }

    @Test
    public void effectRegulator_whenColumnSensorShowsHighValue_expectNoHeating() {
        columnSensor.setTemperature(120);
        tankSensor.setTemperature(70);
        effectRegulator.heatForIteration();
        verify(relay, times(0)).setHigh();
        verify(relay, times(1)).setLow();
    }

    @Test
    public void effectRegulator_whenColumnSensorThrowsRandomException_expectNoHeating()
            throws NoConnectionException {
        TemperatureSensor badSensor = mock(TemperatureSensor.class);
        when(badSensor.readTemp()).thenThrow(new RuntimeException());
        effectRegulator = new EffectRegulator(relay, badSensor, tankSensor, new SettingsData());
        effectRegulator.heatForIteration();
        verify(relay, times(0)).setHigh();
        verify(relay, times(1)).setLow();
    }

    @Test
    public void effectRegulator_whenTankSensorThrowsRandomException_expectNoHeating()
            throws NoConnectionException {
        TemperatureSensor badSensor = mock(TemperatureSensor.class);
        when(badSensor.readTemp()).thenThrow(new RuntimeException());
        effectRegulator = new EffectRegulator(relay, columnSensor, badSensor, new SettingsData());
        effectRegulator.heatForIteration();
        verify(relay, times(0)).setHigh();
        verify(relay, times(1)).setLow();
    }
}

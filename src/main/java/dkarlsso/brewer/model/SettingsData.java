package dkarlsso.brewer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettingsData {
    private double columnTemperatureLimit = 79.7;

    private double tankTemperatureLimit = 97;

    private int wantedEffectWatts = 650;

    // Slow down heating closer to the edge
    // Practical note, first started coming through the column at about 89 degrees in tank
    // Started coming through evenly at about 89.5 - 89.6
    // Flow is really nice and slow at about 600W.
    // At 78.4 in column, spirits are coming through
    // With isolation off sensor and cooking the body, i ran 79.7 and 800 W. Kind of strange

    // Optimal range seems to be about 500 - 600 W

    // Seemed to require higher after the first two deciliters - 7650 - 700W

    // Scrap everything. Best result seems to come from an even effect. Drop is slow and nice at about 600W.
    private int magicLimit = 88;

    // Effect of the heating element
    private int totalEffectWatts = 1000;

    // If wanted effect is 1/5 of max effect and sleep time is 10 sec, then relay will be on 2 seconds and off 8
    // Before restarting
    // Note: Quicker seemed to burn relays quickly
    private int sleepTimeSeconds = 7;

    @JsonIgnore
    public Duration getSleepTime() {
        return Duration.ofSeconds(sleepTimeSeconds);
    }

    public static SettingsData clone(final SettingsData settingsData) {
        return SettingsData.builder()
                .magicLimit(settingsData.getMagicLimit())
                .sleepTimeSeconds(settingsData.getSleepTimeSeconds())
                .columnTemperatureLimit(settingsData.getColumnTemperatureLimit())
                .tankTemperatureLimit(settingsData.getTankTemperatureLimit())
                .totalEffectWatts(settingsData.getTotalEffectWatts())
                .wantedEffectWatts(settingsData.getWantedEffectWatts())
                .build();
    }
}

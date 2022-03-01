package dkarlsso.brewer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HeaterData {

    private double columnTemperature = 100;

    private double tankTemperature = 100;

    public static HeaterData clone(final HeaterData heaterData) {
        return HeaterData.builder()
                .columnTemperature(heaterData.getColumnTemperature())
                .tankTemperature(heaterData.getTankTemperature())
                .build();
    }
}

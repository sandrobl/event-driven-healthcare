package com.eventdriven.healthcare.insulincalculator.service;

import com.eventdriven.healthcare.insulincalculator.model.InsulinCalculationRequest;
import org.springframework.stereotype.Service;

@Service
public class InsulinCalculatorService {
    public static float calculateBolusInsulinDose(InsulinCalculationRequest icr) {
        // Calculate carbohydrate coverage dose
        float carbohydrateCoverageDose =
                icr.getNextMealCarbohydrates() / icr.getInsulinToCarbohydrateRatio();

        // Calculate correction dose
        float correctionDose = 0.0f;
        if (icr.getBloodGlucose()  > icr.getTargetBloodGlucoseLevel()) {
            correctionDose =
                    (icr.getBloodGlucose() - icr.getTargetBloodGlucoseLevel()) / icr.getInsulinSensitivityFactor();
        }

        // Calculate total bolus dose
        float totalBolusDose = carbohydrateCoverageDose + correctionDose;

        // Return the total bolus dose in ml and not units
        return totalBolusDose / 100;
    }
}

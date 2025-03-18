package com.eventdriven.healthcare.insulincalculator.service;

import com.eventdriven.healthcare.insulincalculator.dto.InsulinCalculationCommand;
//import com.eventdriven.healthcare.insulincalculator.model.InsulinCalculationRequest;
import org.springframework.stereotype.Service;

@Service
public class InsulinCalculatorService {
    public static float calculateBolusInsulinDose(InsulinCalculationCommand icr) {

        // Check if the patient's blood glucose level is high
        // https://www.healthline.com/health/how-much-insulin-to-take-chart
        if(icr.getBloodGlucose() < 6.5){
            return 0.0f;
        }

        // Calculate carbohydrate coverage dose
        float carbohydrateCoverageDose =
                icr.getNextMealCarbohydrates() / icr.getInsulinToCarbohydrateRatio();

        // Calculate correction dose
        float correctionDose = 0.0f;
        if (icr.getBloodGlucose()  > icr.getTargetBloodGlucoseLevel()) {
            correctionDose =
                    (icr.getBloodGlucose() - icr.getTargetBloodGlucoseLevel()) / icr.getPatientInsulinSensitivityFactor();
        }

        // Calculate total bolus dose
        float totalBolusDose = carbohydrateCoverageDose + correctionDose;

        // Return the total bolus dose in ml and not units
        return totalBolusDose / 100;
    }
}

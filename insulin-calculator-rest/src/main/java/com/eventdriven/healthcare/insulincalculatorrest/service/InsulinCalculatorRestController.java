package com.eventdriven.healthcare.insulincalculatorrest.service;

import com.eventdriven.healthcare.insulincalculatorrest.model.InsulinCalculatedResponse;
import com.eventdriven.healthcare.insulincalculatorrest.model.InsulinCalculationRequest;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
public class InsulinCalculatorRestController {

    public static boolean slow = false;

    @RequestMapping(path = "/calculateInsulin", method = POST)
    public InsulinCalculatedResponse calculateInsulin(@RequestBody InsulinCalculationRequest request) throws Exception {
        InsulinCalculatedResponse response = new InsulinCalculatedResponse();

        long waitTimeMillis = 0;
        if (slow) {
            waitTimeMillis = Math.round( Math.random() * 60 * 1000 ); // up to 60 seconds
        }

        if (Math.random() > 0.8d) {
           response.setErrorCode("insulin calculation failed");
        }

        response.setInsulinDoses(InsulinCalculatorService.calculateBolusInsulinDose(request));
        response.setInsulinRequired(response.getInsulinDoses() != 0);


        System.out.println("Insulin calculation will take " + waitTimeMillis/1000 + " seconds");
        System.out.println("Insulin doses: " + response.getInsulinDoses());
        Thread.sleep(waitTimeMillis);

        return response;
    }
}

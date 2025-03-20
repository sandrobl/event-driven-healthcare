package com.eventdriven.healthcare.insulincalculatorrest;

import com.eventdriven.healthcare.insulincalculatorrest.service.InsulinCalculatorRestController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class InsulinCalculatorRestApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsulinCalculatorRestApplication.class, args);

        try {
            System.out.println("Service is operating normal");

            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.print("[S]low, [N]ormal: ");
                String mode = scanner.next().toUpperCase();
                if ("S".equals(mode)) {
                    InsulinCalculatorRestController.slow = true;
                    System.out.println("Service is now slow");
                }
                else if ("N".equals(mode)) {
                    InsulinCalculatorRestController.slow = false;
                    System.out.println("Service is back to normal");
                }
            }
        }
        catch (Exception ex) {
            // silently ignore
        }
    }

}

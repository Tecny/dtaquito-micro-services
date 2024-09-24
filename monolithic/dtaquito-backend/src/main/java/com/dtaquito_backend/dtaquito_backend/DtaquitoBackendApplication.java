package com.dtaquito_backend.dtaquito_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling

public class DtaquitoBackendApplication {

	public static void main(String[] args) {

		SpringApplication.run(DtaquitoBackendApplication.class, args);

//		// Open Swagger UI in the default browser
//		String url = "https://dtaquito-backend.azurewebsites.net/swagger-ui/index.html";
//		Runtime rt = Runtime.getRuntime();
//
//		try {
//			rt.exec("cmd /c start " + url);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
}

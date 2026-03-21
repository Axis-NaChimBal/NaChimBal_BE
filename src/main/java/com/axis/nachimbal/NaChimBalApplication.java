package com.axis.nachimbal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

// 테스트를 위해 DB 꺼둠 -> 작업 진행하면서 삭제 필요
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class NaChimBalApplication {

	public static void main(String[] args) {
		SpringApplication.run(NaChimBalApplication.class, args);
	}

}

package com.spring.springbootapplication;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.spring.springbootapplication.repository.UserInfoMapper;
import com.spring.springbootapplication.service.FileStorageService;

@SpringBootTest
class SpringbootApplicationTests {

	@MockBean
	private FileStorageService fileStorageService;

	@MockBean
    private UserInfoMapper userInfoMapper;
	
	@Test
	void contextLoads() {
	}

}

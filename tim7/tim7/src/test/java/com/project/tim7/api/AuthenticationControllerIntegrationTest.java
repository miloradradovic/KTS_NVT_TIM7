package com.project.tim7.api;

import com.project.tim7.constants.RegisteredConstants;
import com.project.tim7.dto.UserDTO;
import com.project.tim7.dto.UserLoginDTO;
import com.project.tim7.dto.UserTokenStateDTO;

import com.project.tim7.model.Administrator;
import com.project.tim7.model.Registered;
import com.project.tim7.service.RegisteredService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import static com.project.tim7.constants.AdministratorConstants.DB_EMAIL;
import static com.project.tim7.constants.AdministratorConstants.DB_PASSWORD;
import static com.project.tim7.constants.AdministratorConstants.DB_USERNAME;
import static com.project.tim7.constants.RegisteredConstants.*;
import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource("classpath:test.properties")
public class AuthenticationControllerIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private RegisteredService registeredService;


    @Test
    public void testCreateAuthenticationToken() {
        ResponseEntity<UserTokenStateDTO> responseEntity = restTemplate.postForEntity("/auth/log-in",
                new UserLoginDTO(DB_USERNAME,DB_PASSWORD_RAW), UserTokenStateDTO.class);

        UserTokenStateDTO userToken =  responseEntity.getBody();

        assertNotNull(userToken);
    }

    @Test
    public void testCreateAuthenticationTokenInvalidUsername() {

        ResponseEntity<?> responseEntity =
                restTemplate.postForEntity("/auth/log-in",
                    new UserLoginDTO(DB_USERNAME_NONEXIST,DB_PASSWORD_RAW), UserTokenStateDTO.class);


        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void testCreateAuthenticationTokenInvalidPassword() {

        ResponseEntity<?> responseEntity =
            restTemplate.postForEntity("/auth/log-in",
                    new UserLoginDTO(DB_USERNAME,DB_PASSWORD), UserTokenStateDTO.class);


        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void testSignUp(){

        HttpEntity<Object> httpEntity = new HttpEntity<>(new UserDTO(NEW_USERNAME, NEW_EMAIL, NEW_PASSWORD));

        ResponseEntity<UserDTO> responseEntity =
                restTemplate.exchange("/auth/sign-up", HttpMethod.POST, httpEntity,
                        UserDTO.class);

        UserDTO reg = responseEntity.getBody();

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertNotNull(reg);

        //returning db to original state
        boolean isDeleted = registeredService.delete(reg.getId());
        assertTrue(isDeleted);

    }

    @Test
    public void testSignUpInvalidEmail(){

        HttpEntity<Object> httpEntity = new HttpEntity<>(new UserDTO(NEW_USERNAME, BAD_EMAIL, NEW_PASSWORD));

        ResponseEntity<String> responseEntity =
                restTemplate.exchange("/auth/sign-up", HttpMethod.POST, httpEntity,
                        String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
    }

    @Test
    public void testSignUpEmailExist(){

        HttpEntity<Object> httpEntity = new HttpEntity<>(new UserDTO(NEW_USERNAME, DB_EMAIL, NEW_PASSWORD));

        ResponseEntity<String> responseEntity =
                restTemplate.exchange("/auth/sign-up", HttpMethod.POST, httpEntity,
                        String.class);

        String message = responseEntity.getBody();
        String expectedMessage = "Username or email already exists.";

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(expectedMessage, message);

    }

    @Test
    public void testSignUpUsernameExist(){
        HttpEntity<Object> httpEntity = new HttpEntity<>(new UserDTO(DB_USERNAME, NEW_EMAIL, NEW_PASSWORD));

        ResponseEntity<String> responseEntity =
                restTemplate.exchange("/auth/sign-up", HttpMethod.POST, httpEntity,
                        String.class);

        String message = responseEntity.getBody();
        String expectedMessage = "Username or email already exists.";

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(expectedMessage, message);

    }

    @Test
    public void testSignUpInvalidPassword(){
        HttpEntity<Object> httpEntity = new HttpEntity<>(new UserDTO(NEW_USERNAME, NEW_EMAIL, BAD_PASSWORD));

        ResponseEntity<String> responseEntity =
                restTemplate.exchange("/auth/sign-up", HttpMethod.POST, httpEntity,
                        String.class);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());

    }

    @Test
    public void activate(){
        HttpEntity<Object> httpEntity = new HttpEntity<>(REG_ID);

        ResponseEntity<String> responseEntity =
                restTemplate.postForEntity("/auth/activate/" + REG_ID, HttpMethod.POST,
                        String.class);


        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        //returning db to original state
        Registered reg_restore = new Registered(REG_ID, DB_EMAIL, DB_USERNAME, DB_PASSWORD);
        reg_restore.setVerified(false);
        registeredService.update(reg_restore);

    }

    @Test
    public void activateInvalidId(){
        ResponseEntity<String> responseEntity =
                restTemplate.postForEntity("/auth/activate/" + REG_ID_NONEXIST, HttpMethod.POST,
                        String.class);

        String message = responseEntity.getBody();
        String expectedMessage = "Activation failed.";

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(expectedMessage, message);
    }
}

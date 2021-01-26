package com.project.tim7.api;

import com.project.tim7.dto.UserDTO;
import com.project.tim7.dto.UserLoginDTO;
import com.project.tim7.dto.UserTokenStateDTO;
import com.project.tim7.helper.RegisteredMapper;
import com.project.tim7.model.Administrator;
import com.project.tim7.model.Authority;
import com.project.tim7.model.Person;
import com.project.tim7.model.Registered;
import com.project.tim7.security.TokenUtils;
import com.project.tim7.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.List;

//123qweASD
@RestController
@RequestMapping(value = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
public class AuthenticationController {

    @Autowired
    private TokenUtils tokenUtils;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private RegisteredService regService;

    @Autowired
    private AdministratorService adminService;

    @Autowired
    AuthorityService authorityService;
    
    @Autowired
	private PasswordEncoder passwordEncoder;

    private RegisteredMapper regMapper;

    public AuthenticationController() {
        regMapper = new RegisteredMapper();
    }

    // Prvi endpoint koji pogadja korisnik kada se loguje.
    // Tada zna samo svoje korisnicko ime i lozinku i to prosledjuje na backend.
    @PostMapping("/log-in")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody UserLoginDTO authenticationRequest,
                                                       HttpServletResponse response) {

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(),
                        authenticationRequest.getPassword()));

        // Ubaci korisnika u trenutni security kontekst
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Kreiraj token za tog korisnika
        Person person = (Person) authentication.getPrincipal();
        String jwt = tokenUtils.generateToken(person.getUsername(), person.getId(), person.getAuthorities().get(0).getAuthority()); // prijavljujemo se na sistem sa email adresom

        // Vrati token kao odgovor na uspesnu autentifikaciju
        return ResponseEntity.ok(new UserTokenStateDTO(jwt));
    }

    // Endpoint za registraciju novog korisnika
    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@Valid @RequestBody UserDTO userRequest) throws Exception {

        Registered existReg = this.regService.findByUsernameOrEmail(userRequest.getUsername(), userRequest.getEmail());
        Administrator existAdmin = this.adminService.findByUsernameOrEmail(userRequest.getUsername(), userRequest.getEmail());
        if (existReg != null || existAdmin != null) {
        	return new ResponseEntity<>("Username or email already exists.", HttpStatus.BAD_REQUEST);
        }
        existReg = regMapper.toEntity(userRequest);
        existReg.setPassword(passwordEncoder.encode(userRequest.getPassword()));

        long role = 2;
        List<Authority> auth = authorityService.findById(role);
        existReg.setAuthorities(auth);
        existReg.setVerified(false);

        Registered newReg = regService.registerUser(existReg);

        if(newReg == null){
            return new ResponseEntity<>("Username or email already exists.", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(regMapper.toDto(newReg), HttpStatus.CREATED);
    }
    
    // Endpoint za aktivaciju naloga
    @PostMapping("/activate/{id}")
    public ResponseEntity<?> activate(@PathVariable Integer id) {
    	Registered regUser = regService.activateAccount(id);

    	if(regUser == null)
        	return new ResponseEntity<>("Activation failed.", HttpStatus.BAD_REQUEST);

    	return new ResponseEntity<>(HttpStatus.OK);
    }

    // U slucaju isteka vazenja JWT tokena, endpoint koji se poziva da se token osvezi
    @PostMapping(value = "/refresh")
    public ResponseEntity<UserTokenStateDTO> refreshAuthenticationToken(HttpServletRequest request) {

        String token = tokenUtils.getToken(request);
        String username = this.tokenUtils.getUsernameFromToken(token);
        Person person = (Person) this.userDetailsService.loadUserByUsername(username);

        if (this.tokenUtils.canTokenBeRefreshed(token, person.getLastPasswordResetDate())) {
            String refreshedToken = tokenUtils.refreshToken(token);
            int expiresIn = tokenUtils.getExpiredIn();

            return ResponseEntity.ok(new UserTokenStateDTO(refreshedToken));
        } else {
            UserTokenStateDTO userTokenState = new UserTokenStateDTO();
            return ResponseEntity.badRequest().body(userTokenState);
        }
    }

    @PreAuthorize("hasRole('ROLE_ADMINISTRATOR') || hasRole('ROLE_REGISTERED')")
    public void updatedLoggedIn(String username, String password){

        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

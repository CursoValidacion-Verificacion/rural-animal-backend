package com.project.demo.rest.auth;

import com.project.demo.logic.entity.auth.AuthenticationService;
import com.project.demo.logic.entity.auth.JwtService;
import com.project.demo.logic.entity.auth.TokenStorage;
import com.project.demo.logic.entity.direction.TblDirection;
import com.project.demo.logic.entity.direction.TblDirectionRepository;
import com.project.demo.logic.entity.http.GlobalResponseHandler;
import com.project.demo.logic.entity.role.RoleEnum;
import com.project.demo.logic.entity.role.TblRole;
import com.project.demo.logic.entity.role.TblRoleRepository;
import com.project.demo.logic.entity.user.LoginResponse;
import com.project.demo.logic.utils.EmailService;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import com.project.demo.logic.utils.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RequestMapping("/auth")
@RestController
public class AuthRestController {


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    @Autowired
    private TblRoleRepository roleRepository;

    @Autowired
    private TblDirectionRepository tblDirectionRepository;

    private final TokenStorage tokenStorage;

    private final AuthenticationService authenticationService;
    private final JwtService jwtService;

    public AuthRestController(JwtService jwtService, AuthenticationService authenticationService,
                              TokenStorage tokenStorage) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.tokenStorage = tokenStorage;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody TblUser user) {
        Optional<TblUser> optionalUser = userRepository.findByEmail(user.getEmail());

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("Usuario no encontrado"));
        }

        TblUser foundUser = optionalUser.get();

        // Comprobar si la cuenta está bloqueada
        if (foundUser.getLockTime() != null) {
            long lockDuration = 5 * 60 * 1000; // 5 minutos en milisegundos
            long unlockTime = foundUser.getLockTime() + lockDuration;

            if (System.currentTimeMillis() < unlockTime) {
                long remainingTime = unlockTime - System.currentTimeMillis();
                long minutes = (remainingTime / 1000) / 60;
                long seconds = (remainingTime / 1000) % 60;
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new LoginResponse("Demasiados intentos fallidos. Inténtalo nuevamente en " + minutes + " minutos y " + seconds + " segundos."));
            } else {
                // Reiniciar el estado de bloqueo si ha pasado el tiempo de bloqueo
                foundUser.setFailedAttempts(0);
                foundUser.setLockTime(null);
                userRepository.save(foundUser);
            }
        }

        try {
            TblUser authenticatedUser = authenticationService.authenticate(user);

            // Reiniciar intentos fallidos
            foundUser.setFailedAttempts(0);
            foundUser.setLockTime(null);
            userRepository.save(foundUser);

            String jwtToken = jwtService.generateToken(authenticatedUser);
            LoginResponse loginResponse = new LoginResponse(jwtToken, authenticatedUser, jwtService.getExpirationTime());

            return ResponseEntity.ok(loginResponse);
        } catch (Exception e) {
            foundUser.setFailedAttempts(foundUser.getFailedAttempts() == null ? 1 : foundUser.getFailedAttempts() + 1);

            if (foundUser.getFailedAttempts() >= 5) {
                foundUser.setLockTime(System.currentTimeMillis());
            }

            userRepository.save(foundUser);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new LoginResponse("Credenciales incorrectas"));
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticateEmail(@RequestBody TblUser user) {
        System.out.println("here");
        try {
            Optional<TblUser> foundUser = userRepository.findByEmail(user.getEmail());

            if (foundUser.isPresent()) {
                String userEmail = foundUser.get().getEmail();

                // Configuración del correo electrónico
                String sender = "ruralanimalcr@gmail.com";
                String subject = "Confirmación de correo electrónico";
                String baseUrl = "https://localhost:8080/auth/emailConfirm/";
                String urlConfirmation = baseUrl +  "?id=" + foundUser.get().getId();
                String body = String.format("Estimado cliente,<br/><br/>Por favor haz click en el siguiente enlace para confirmar tu correo electrónico:<br/><br/><a href=\"%s\"><button style=\"padding: 10px; background-color: #77C040; color: #fff; border: none; border-radius: 5px; cursor: pointer;\">Confirmar Correo</button></a><br/><br/>Gracias!", urlConfirmation);

                emailService.sendEmail(sender, userEmail, subject, body);

                return ResponseEntity.ok().body("Correo enviado a " + userEmail);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Correo electrónico inválido.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al enviar el correo: " + e.getMessage());
        }
    }

    @GetMapping("/emailConfirm/{id}")
    public ResponseEntity<?> EmailConfirm(@PathVariable long id) {
        System.out.println("here 2");
        try {
            Optional<TblUser> foundUser = userRepository.findById(id);

            if (foundUser.isPresent()) {
                TblUser user = foundUser.get();
                user.setState("Active");
                userRepository.save(user);

                return ResponseEntity.ok().body("Correo electrónico confirmado correctamente.");
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ID de usuario inválido.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody TblUser user, HttpServletRequest request) {

        // Validar email
        if (!isValidEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("El correo ingresado no tiene un formato válido.");
        }

        // Ver si email existe
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Este correo ya existe");
        }

        // Validar conteseña
        if (!isValidPassword(user.getPassword())) {
            return ResponseEntity.badRequest().body("La contraseña debe tener al menos 8 caracteres, incluir una letra mayúscula, " +
                    "una letra minúscula, un número y un carácter especial.");
        }

        // Validar edad
        if (!isAdult(user.getBirthDate())) {
            return ResponseEntity.badRequest().body("Debes ser mayor de 18 años para registrarte.");
        }

        // Formato para ID y teléfono
        user.setIdentification(formatIdentification(user.getIdentification()));
        user.setPhoneNumber(formatPhoneNumber(user.getPhoneNumber()));

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Validar rol
        Optional<TblRole> optionalRole = roleRepository.findByTitle(user.getRole().getTitle());
        if (optionalRole.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Role not found");
        }
        user.setRole(optionalRole.get());

        if (user.getDirection() != null) {
            TblDirection tblDirection = new TblDirection();
            tblDirection.setDistrict(user.getDirection().getDistrict());
            tblDirection.setCanton(user.getDirection().getCanton());
            tblDirection.setProvince(user.getDirection().getProvince());
            tblDirection.setOtherDetails(user.getDirection().getOtherDetails());
            user.setDirection(tblDirectionRepository.save(tblDirection));
        }

        // Save user
        TblUser savedUser = userRepository.save(user);
        return new GlobalResponseHandler().handleResponse("User registered successfully", savedUser, HttpStatus.OK, request);
    }

    public boolean isValidEmail(String email) {
        String emailRegex = "^(?!\\.|-)[a-zA-Z0-9._-]+@[a-zA-Z0-9]+(\\.[a-zA-Z]{2,6})+$";
        return email != null && email.matches(emailRegex);
    }

    public boolean isValidPassword(String password) {
        String passwordRegex = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$";
        return password != null && password.matches(passwordRegex);
    }

    public boolean isAdult(LocalDate birthDate) {
        if (birthDate == null) {
            return false;
        }

        int age = Period.between(birthDate, LocalDate.now()).getYears();
        return age >= 18;
    }

    public String formatIdentification(String identification) {
        if (identification == null || identification.length() != 9) {
            return identification;
        }
        StringBuilder formattedId = new StringBuilder();
        formattedId.append(identification.charAt(0)).append("-");

        if (identification.length() >= 5) {
            formattedId.append(identification.substring(1, Math.min(5, identification.length()))).append("-");
        }

        if (identification.length() > 5) {
            formattedId.append(identification.substring(5));
        }

        return formattedId.toString();
    }

    public String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() != 8) {
            return phoneNumber;
        }
        return phoneNumber.substring(0, 4) + "-" + phoneNumber.substring(4);
    }

    @PostMapping("/complete-registration")
    public ResponseEntity<?> completeRegistration(
            @RequestParam String sessionId,
            @RequestBody Map<String, String> roleRequest) {

        String token = tokenStorage.getToken(sessionId);
        if (token == null) {
            return ResponseEntity.badRequest().body("Invalid session");
        }

        try {
            String email = jwtService.extractUsername(token);
            TblUser user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getRole() == null || user.getRole().getTitle() != RoleEnum.PENDING) {
                return ResponseEntity.badRequest().body("Invalid user state");
            }

            String roleTitle = roleRequest.get("role");
            TblRole newRole = roleRepository.findByTitle(RoleEnum.valueOf(roleTitle))
                    .orElseThrow(() -> new RuntimeException("Invalid role"));

            user.setRole(newRole);
            userRepository.save(user);

            String newToken = jwtService.generateToken(user);
            tokenStorage.removeToken(sessionId);

            return ResponseEntity.ok(new LoginResponse(newToken, user, jwtService.getExpirationTime()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error completing registration");
        }
    }

    @GetMapping("/oauth/token/{sessionId}")
    public ResponseEntity<?> getToken(@PathVariable String sessionId) {
        String token = tokenStorage.getToken(sessionId);
        if (token == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            String email = jwtService.extractUsername(token);
            TblUser user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getRole().getTitle() == RoleEnum.PENDING) {
                Map<String, Object> response = new HashMap<>();
                response.put("needsRoleSelection", true);
                response.put("sessionId", sessionId);
                return ResponseEntity.ok(response);
            }

            LoginResponse loginResponse = new LoginResponse(token, user, jwtService.getExpirationTime());
            tokenStorage.removeToken(sessionId);
            return ResponseEntity.ok(loginResponse);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Authentication error");
        }
    }
}

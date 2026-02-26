package com.project.demo.logic.entity.bill;

import com.project.demo.logic.entity.publication.TblPublication;
import com.project.demo.logic.entity.transaction.TblTransaction;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BillService")
class BillServiceTest {

    @Mock
    private UserRepository userRepository;

    private BillService billService;

    @BeforeEach
    void setUp() {
        billService = new BillService();
        ReflectionTestUtils.setField(billService, "userRepository", userRepository);
    }

    @Test
    @DisplayName("generateHtmlContent wraps body in html structure")
    void generateHtmlContent_wrapsBody() {
        String body = "<p>Test</p>";
        String result = BillService.generateHtmlContent(body);
        assertTrue(result.contains("<html>"));
        assertTrue(result.contains("</html>"));
        assertTrue(result.contains(body));
    }

    @Test
    @DisplayName("generateHtmlContent includes style block")
    void generateHtmlContent_includesStyle() {
        String result = BillService.generateHtmlContent("x");
        assertTrue(result.contains("font-size"));
    }

    @Test
    @DisplayName("generateBillBody throws when user not found")
    void generateBillBody_userNotFound_throws() {
        TblTransaction transaction = new TblTransaction();
        TblUser user = new TblUser();
        user.setId(1L);
        transaction.setUser(user);
        when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> billService.generateBillBody(transaction));
    }

    @Test
    @DisplayName("generateBillBody includes user name and date when user found")
    void generateBillBody_userFound_includesNameAndDate() {
        TblUser user = new TblUser();
        user.setId(1L);
        user.setName("Juan");
        user.setLastName1("Pérez");
        TblTransaction transaction = new TblTransaction();
        transaction.setUser(user);
        transaction.setCreationDate(LocalDateTime.of(2025, 2, 25, 12, 0));
        transaction.setSubTotal(BigDecimal.valueOf(100));
        transaction.setTax(BigDecimal.valueOf(13));
        transaction.setTotal(BigDecimal.valueOf(113));
        transaction.setPublications(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        String body = billService.generateBillBody(transaction);
        assertTrue(body.contains("Juan"));
        assertTrue(body.contains("Pérez"));
        assertTrue(body.contains("25/02/2025"));
        assertTrue(body.contains("100"));
        assertTrue(body.contains("13"));
        assertTrue(body.contains("113"));
    }

    @Test
    @DisplayName("generateBillBody includes publications table when present")
    void generateBillBody_withPublications_includesTable() {
        TblUser user = new TblUser();
        user.setId(1L);
        user.setName("Ana");
        user.setLastName1("López");
        TblPublication pub = new TblPublication();
        pub.setTitle("Vaca Holstein");
        pub.setSpecie("bovino");
        pub.setPrice(500000L);
        List<TblPublication> pubs = List.of(pub);
        TblTransaction transaction = new TblTransaction();
        transaction.setUser(user);
        transaction.setCreationDate(LocalDateTime.now());
        transaction.setSubTotal(BigDecimal.valueOf(500000));
        transaction.setTax(BigDecimal.valueOf(65000));
        transaction.setTotal(BigDecimal.valueOf(565000));
        transaction.setPublications(pubs);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        String body = billService.generateBillBody(transaction);
        assertTrue(body.contains("Vaca Holstein"));
        assertTrue(body.contains("bovino"));
        assertTrue(body.contains("500000"));
    }

    @Test
    @DisplayName("generateBillBody handles null creationDate")
    void generateBillBody_nullCreationDate_stillBuildsBody() {
        TblUser user = new TblUser();
        user.setId(1L);
        user.setName("Carlos");
        user.setLastName1("García");
        TblTransaction transaction = new TblTransaction();
        transaction.setUser(user);
        transaction.setCreationDate(null);
        transaction.setSubTotal(BigDecimal.ONE);
        transaction.setTax(BigDecimal.ZERO);
        transaction.setTotal(BigDecimal.ONE);
        transaction.setPublications(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        String body = billService.generateBillBody(transaction);
        assertNotNull(body);
        assertTrue(body.contains("Carlos"));
    }
}

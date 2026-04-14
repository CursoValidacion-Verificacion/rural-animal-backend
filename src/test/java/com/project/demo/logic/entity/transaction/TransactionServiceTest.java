package com.project.demo.logic.entity.transaction;

import com.project.demo.logic.entity.paypal.PayPalRequest;
import com.project.demo.logic.entity.publication.PublicationService;
import com.project.demo.logic.entity.publication.TblPublication;
import com.project.demo.logic.entity.user.TblUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService")
@Tag("unit")
class TransactionServiceTest {

    @Mock
    private TblTransactionRepository transactionRepository;

    @Mock
    private PublicationService publicationService;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository, publicationService);
    }

    @Test
    @DisplayName("createPendingTransaction sets subtotal as sum of publication prices")
    void createPendingTransaction_subtotalIsSumOfPrices() {
        TblUser user = new TblUser();
        user.setId(1L);
        TblPublication p1 = new TblPublication();
        p1.setId(1L);
        p1.setPrice(100000L);
        TblPublication p2 = new TblPublication();
        p2.setId(2L);
        p2.setPrice(50000L);
        List<TblPublication> pubs = List.of(p1, p2);
        when(publicationService.getPublicationsByIds(any())).thenReturn(pubs);
        TblTransaction saved = new TblTransaction();
        saved.setSubTotal(BigDecimal.valueOf(150000).setScale(2, java.math.RoundingMode.HALF_UP));
        when(transactionRepository.saveAndFlush(any(TblTransaction.class))).thenAnswer(i -> {
            TblTransaction t = i.getArgument(0);
            t.setId(1L);
            return t;
        });

        PayPalRequest request = new PayPalRequest();
        request.setPublications(List.of(BigInteger.ONE, BigInteger.TWO));
        TblTransaction result = transactionService.createPendingTransaction(user, request);

        assertEquals(new BigDecimal("150000.00"), result.getSubTotal());
    }

    @Test
    @DisplayName("createPendingTransaction applies 13% tax")
    void createPendingTransaction_taxIsThirteenPercent() {
        TblUser user = new TblUser();
        TblPublication p = new TblPublication();
        p.setId(1L);
        p.setPrice(100000L);
        when(publicationService.getPublicationsByIds(any())).thenReturn(List.of(p));
        when(transactionRepository.saveAndFlush(any(TblTransaction.class))).thenAnswer(i -> {
            TblTransaction t = i.getArgument(0);
            t.setId(1L);
            return t;
        });
        PayPalRequest request = new PayPalRequest();
        request.setPublications(List.of(BigInteger.ONE));

        TblTransaction result = transactionService.createPendingTransaction(user, request);

        assertEquals(new BigDecimal("13000.00"), result.getTax());
    }

    @Test
    @DisplayName("createPendingTransaction sets total as subtotal plus tax")
    void createPendingTransaction_totalIsSubtotalPlusTax() {
        TblUser user = new TblUser();
        TblPublication p = new TblPublication();
        p.setId(1L);
        p.setPrice(100000L);
        when(publicationService.getPublicationsByIds(any())).thenReturn(List.of(p));
        when(transactionRepository.saveAndFlush(any(TblTransaction.class))).thenAnswer(i -> {
            TblTransaction t = i.getArgument(0);
            t.setId(1L);
            return t;
        });
        PayPalRequest request = new PayPalRequest();
        request.setPublications(List.of(BigInteger.ONE));

        TblTransaction result = transactionService.createPendingTransaction(user, request);

        assertEquals(new BigDecimal("113000.00"), result.getTotal());
    }

    @Test
    @DisplayName("createPendingTransaction sets status PENDING")
    void createPendingTransaction_statusIsPending() {
        TblUser user = new TblUser();
        TblPublication p = new TblPublication();
        p.setId(1L);
        p.setPrice(100L);
        when(publicationService.getPublicationsByIds(any())).thenReturn(List.of(p));
        when(transactionRepository.saveAndFlush(any(TblTransaction.class))).thenAnswer(i -> {
            TblTransaction t = i.getArgument(0);
            t.setId(1L);
            return t;
        });
        PayPalRequest request = new PayPalRequest();
        request.setPublications(List.of(BigInteger.ONE));

        TblTransaction result = transactionService.createPendingTransaction(user, request);

        assertEquals("PENDING", result.getStatus());
    }

    @Test
    @DisplayName("createPendingTransaction propagates exception when getPublicationsByIds fails")
    void createPendingTransaction_publicationServiceThrows_propagates() {
        TblUser user = new TblUser();
        when(publicationService.getPublicationsByIds(any())).thenThrow(new IllegalArgumentException("bad"));

        PayPalRequest request = new PayPalRequest();
        request.setPublications(List.of(BigInteger.ONE));

        assertThrows(IllegalArgumentException.class,
                () -> transactionService.createPendingTransaction(user, request));
    }

    @Test
    @DisplayName("updateTransactionStatus throws when transaction not found")
    void updateTransactionStatus_notFound_throws() {
        when(transactionRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        assertThrows(jakarta.persistence.EntityNotFoundException.class,
                () -> transactionService.updateTransactionStatus(999L, "APPROVED"));
    }

    @Test
    @DisplayName("updateTransactionStatus calls updatePublicationsState when status is APPROVED")
    void updateTransactionStatus_approved_updatesPublications() {
        TblTransaction transaction = new TblTransaction();
        transaction.setId(1L);
        transaction.setStatus("PENDING");
        TblPublication pub = new TblPublication();
        pub.setId(10L);
        transaction.setPublications(List.of(pub));
        when(transactionRepository.findById(1L)).thenReturn(java.util.Optional.of(transaction));
        when(transactionRepository.save(any(TblTransaction.class))).thenAnswer(i -> i.getArgument(0));

        transactionService.updateTransactionStatus(1L, "APPROVED");

        verify(publicationService).updatePublicationsState(List.of(10L), "Vendido");
    }

    @Test
    @DisplayName("updateTransactionStatus does not call updatePublicationsState when status is not APPROVED")
    void updateTransactionStatus_pending_doesNotUpdatePublications() {
        TblTransaction transaction = new TblTransaction();
        transaction.setId(1L);
        when(transactionRepository.findById(1L)).thenReturn(java.util.Optional.of(transaction));
        when(transactionRepository.save(any(TblTransaction.class))).thenAnswer(i -> i.getArgument(0));

        transactionService.updateTransactionStatus(1L, "PENDING");

        verify(publicationService, never()).updatePublicationsState(any(), any());
    }
}

package com.project.demo.logic.entity.bid;

import com.project.demo.logic.entity.publication.TblPublication;
import com.project.demo.logic.entity.publication.TblPublicationRepository;
import com.project.demo.logic.entity.user.TblUser;
import com.project.demo.logic.entity.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BidService")
class BidServiceTest {

    @Mock
    private TblBidRepository tblBidRepository;

    @Mock
    private TblPublicationRepository publicationRepository;

    @Mock
    private UserRepository userRepository;

    private BidService bidService;

    @BeforeEach
    void setUp() {
        bidService = new BidService(tblBidRepository, publicationRepository, userRepository);
    }

    @Test
    @DisplayName("findHighestBid returns bid with max amount")
    void findHighestBid_returnsMax() {
        TblBid low = new TblBid();
        low.setBidAmmount(100L);
        TblBid high = new TblBid();
        high.setBidAmmount(500L);
        TblBid mid = new TblBid();
        mid.setBidAmmount(250L);
        TblBid result = bidService.findHighestBid(List.of(low, mid, high));
        assertEquals(500L, result.getBidAmmount());
    }

    @Test
    @DisplayName("findHighestBid with single bid returns it")
    void findHighestBid_singleElement_returnsIt() {
        TblBid only = new TblBid();
        only.setBidAmmount(99L);
        assertEquals(only, bidService.findHighestBid(List.of(only)));
    }

    @Test
    @DisplayName("findHighestBid with null list throws")
    void findHighestBid_nullList_throws() {
        assertThrows(IllegalArgumentException.class, () -> bidService.findHighestBid(null));
    }

    @Test
    @DisplayName("findHighestBid with empty list throws")
    void findHighestBid_emptyList_throws() {
        assertThrows(IllegalArgumentException.class, () -> bidService.findHighestBid(List.of()));
    }

    @Test
    @DisplayName("placeBid throws when publication not found")
    void placeBid_publicationNotFound_throws() throws Exception {
        when(publicationRepository.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> bidService.placeBid(1L, 1L));
    }

    @Test
    @DisplayName("placeBid throws when user not found")
    void placeBid_userNotFound_throws() throws Exception {
        TblPublication pub = new TblPublication();
        pub.setType("Subasta");
        pub.setStartDate(LocalDateTime.now().minusDays(1));
        pub.setEndDate(LocalDateTime.now().plusDays(1));
        pub.setState("Activa");
        pub.setMinimumIncrease(1000);
        pub.setBids(new ArrayList<>());
        TblUser owner = new TblUser();
        owner.setId(2L);
        pub.setUser(owner);
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(pub));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(Exception.class, () -> bidService.placeBid(1L, 1L));
    }

    @Test
    @DisplayName("placeBid throws when publication is not Subasta")
    void placeBid_notSubasta_throws() throws Exception {
        TblPublication pub = new TblPublication();
        pub.setType("Venta");
        TblUser user = new TblUser();
        user.setId(1L);
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(pub));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Exception ex = assertThrows(Exception.class, () -> bidService.placeBid(1L, 1L));
        assertTrue(ex.getMessage().contains("subasta"));
    }

    @Test
    @DisplayName("placeBid throws when minimumIncrease is null")
    void placeBid_nullMinimumIncrease_throws() throws Exception {
        TblPublication pub = new TblPublication();
        pub.setType("Subasta");
        pub.setStartDate(LocalDateTime.now().minusDays(1));
        pub.setEndDate(LocalDateTime.now().plusDays(1));
        pub.setState("Activa");
        pub.setMinimumIncrease(null);
        pub.setPrice(10000L);
        pub.setBids(new ArrayList<>());
        TblUser owner = new TblUser();
        owner.setId(2L);
        pub.setUser(owner);
        TblUser bidder = new TblUser();
        bidder.setId(1L);
        when(publicationRepository.findById(1L)).thenReturn(Optional.of(pub));
        when(userRepository.findById(1L)).thenReturn(Optional.of(bidder));

        Exception ex = assertThrows(Exception.class, () -> bidService.placeBid(1L, 1L));
        assertTrue(ex.getMessage().toLowerCase().contains("incremento"));
    }
}

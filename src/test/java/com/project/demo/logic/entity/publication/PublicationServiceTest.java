package com.project.demo.logic.entity.publication;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PublicationService")
class PublicationServiceTest {

    @Mock
    private TblPublicationRepository publicationRepository;

    private PublicationService publicationService;

    @BeforeEach
    void setUp() {
        publicationService = new PublicationService(publicationRepository);
    }

    @Test
    @DisplayName("getPublicationsByIds throws when list is null")
    void getPublicationsByIds_null_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> publicationService.getPublicationsByIds(null));
    }

    @Test
    @DisplayName("getPublicationsByIds throws when list is empty")
    void getPublicationsByIds_empty_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> publicationService.getPublicationsByIds(List.of()));
    }

    @Test
    @DisplayName("getPublicationsByIds throws when some ids not found")
    void getPublicationsByIds_sizeMismatch_throws() {
        when(publicationRepository.findAllById(List.of(1L))).thenReturn(List.of());
        assertThrows(EntityNotFoundException.class,
                () -> publicationService.getPublicationsByIds(List.of(BigInteger.ONE)));
    }

    @Test
    @DisplayName("getPublicationsByIds returns publications when all found")
    void getPublicationsByIds_allFound_returnsList() {
        TblPublication p = new TblPublication();
        p.setId(1L);
        when(publicationRepository.findAllById(List.of(1L))).thenReturn(List.of(p));

        List<TblPublication> result = publicationService.getPublicationsByIds(List.of(BigInteger.ONE));

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    @DisplayName("updatePublicationsState throws when some publications not found")
    void updatePublicationsState_sizeMismatch_throws() {
        when(publicationRepository.findAllById(anyList())).thenReturn(List.of());
        assertThrows(EntityNotFoundException.class,
                () -> publicationService.updatePublicationsState(List.of(1L), "Vendido"));
    }

    @Test
    @DisplayName("updatePublicationsState saves with new state when all found")
    void updatePublicationsState_allFound_savesWithNewState() {
        TblPublication p = new TblPublication();
        p.setId(1L);
        when(publicationRepository.findAllById(List.of(1L))).thenReturn(List.of(p));
        when(publicationRepository.saveAll(anyList())).thenAnswer(i -> i.getArgument(0));

        publicationService.updatePublicationsState(List.of(1L), "Vendido");

        verify(publicationRepository).saveAll(anyList());
    }
}

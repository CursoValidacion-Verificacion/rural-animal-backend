package com.project.demo.logic.entity.chat;

import com.project.demo.logic.entity.publication.TblPublication;
import com.project.demo.logic.entity.publication.TblPublicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnimalInfoCommand")
@Tag("unit")
class AnimalInfoCommandTest {

    @Mock
    private TblPublicationRepository publicationRepository;

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private ChatContextManager contextManager;

    private AnimalInfoCommand animalInfoCommand;

    @BeforeEach
    void setUp() {
        animalInfoCommand = new AnimalInfoCommand(publicationRepository, geminiClient, contextManager);
        lenient().when(contextManager.getOrCreateContext(anyLong())).thenReturn(new ChatContextManager.ConversationContext());
    }

    @Test
    @DisplayName("execute returns initial response when message contains información")
    void execute_messageWithInformacion_returnsInitialResponse() {
        String result = animalInfoCommand.execute("Quiero información", 1L);
        assertTrue(result.contains("Qué tipo de animal") || result.contains("especie"));
    }

    @Test
    @DisplayName("execute returns initial response when message contains informacion without accent")
    void execute_messageWithInformacionNoAccent_returnsInitialResponse() {
        String result = animalInfoCommand.execute("informacion de animales", 1L);
        assertTrue(result.contains("Qué tipo") || result.contains("especie"));
    }

    @Test
    @DisplayName("execute returns no match message when species found but repo empty")
    void execute_speciesInMessage_repoEmpty_returnsNoMatchMessage() {
        when(geminiClient.processMessage(anyString(), anyLong())).thenThrow(new RuntimeException("mock"));
        when(publicationRepository.findBySpeciesAndBreed(anyString(), any(), any())).thenReturn(new PageImpl<>(Collections.emptyList()));

        String result = animalInfoCommand.execute("busco gallinas", 1L);

        assertTrue(result.contains("no encontré"));
    }

    @Test
    @DisplayName("execute returns publication details when repo has results")
    void execute_speciesInMessage_repoHasResults_returnsPublicationDetails() {
        when(geminiClient.processMessage(anyString(), anyLong())).thenThrow(new RuntimeException("mock"));
        TblPublication pub = new TblPublication();
        pub.setTitle("Gallina Leghorn");
        pub.setSpecie("gallina");
        pub.setRace("leghorn");
        pub.setGender("Hembra");
        pub.setWeight(2L);
        pub.setType("Venta");
        pub.setPrice(15000L);
        when(publicationRepository.findBySpeciesAndBreed(eq("gallina"), any(), any())).thenReturn(new PageImpl<>(List.of(pub)));

        String result = animalInfoCommand.execute("gallinas leghorn", 1L);

        assertTrue(result.contains("Gallina Leghorn"));
        assertTrue(result.contains("gallina"));
    }
}

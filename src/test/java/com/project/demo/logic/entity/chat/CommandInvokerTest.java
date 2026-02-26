package com.project.demo.logic.entity.chat;

import com.project.demo.logic.entity.publication.TblPublicationRepository;
import com.project.demo.logic.entity.veterinaryAppointment.VeterinaryAppointmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CommandInvoker")
class CommandInvokerTest {

    @Mock
    private TblPublicationRepository publicationRepository;

    @Mock
    private VeterinaryAppointmentService veterinaryAppointmentService;

    @Mock
    private GeminiClient geminiClient;

    @Mock
    private ChatContextManager contextManager;

    private CommandInvoker commandInvoker;

    @BeforeEach
    void setUp() {
        AnimalInfoCommand animalInfoCommand = new AnimalInfoCommand(publicationRepository, geminiClient, contextManager);
        VetAppointmentCommand vetCommand = new VetAppointmentCommand(veterinaryAppointmentService, geminiClient, contextManager);
        AuctionQueryCommand auctionCommand = new AuctionQueryCommand(publicationRepository, contextManager, geminiClient);
        commandInvoker = new CommandInvoker(vetCommand, auctionCommand, animalInfoCommand, geminiClient, contextManager);
        when(contextManager.getOrCreateContext(anyLong())).thenReturn(new ChatContextManager.ConversationContext());
    }

    @Test
    @DisplayName("executeCommand with UNKNOWN delegates to GeminiClient")
    void executeCommand_unknownIntent_callsGemini() {
        when(geminiClient.processMessage(anyString(), anyLong())).thenReturn("generic reply");

        String result = commandInvoker.executeCommand(Intent.UNKNOWN, "hello", 1L);

        assertEquals("generic reply", result);
        verify(geminiClient).processMessage("hello", 1L);
    }

    @Test
    @DisplayName("executeCommand with ANIMAL_INFO runs command and returns response")
    void executeCommand_animalInfo_runsCommand() {
        String result = commandInvoker.executeCommand(Intent.ANIMAL_INFO, "información", 1L);

        assertTrue(result.contains("Qué tipo") || result.contains("especie"));
    }

    @Test
    @DisplayName("executeCommand with explicit change message updates context")
    void executeCommand_explicitChange_updatesContext() {
        ChatContextManager.ConversationContext ctx = new ChatContextManager.ConversationContext();
        ctx.updateIntent(Intent.VET_APPOINTMENT);
        when(contextManager.getOrCreateContext(1L)).thenReturn(ctx);

        commandInvoker.executeCommand(Intent.ANIMAL_INFO, "quiero cambiar a ver animales", 1L);

        verify(contextManager).updateContext(eq(1L), any(ChatContextManager.ConversationContext.class));
    }
}

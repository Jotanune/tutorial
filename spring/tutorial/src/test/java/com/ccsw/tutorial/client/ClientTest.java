package com.ccsw.tutorial.client;

import com.ccsw.tutorial.client.model.Client;
import com.ccsw.tutorial.client.model.ClientDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientServiceImpl clientService;

    @Test
    public void findAllShouldReturnAllClients() {

        List<Client> list = new ArrayList<>();
        list.add(mock(Client.class));

        when(clientRepository.findAll()).thenReturn(list);

        List<Client> clients = clientService.findAll();

        assertNotNull(clients);
        assertEquals(1, clients.size());
    }

    public static final String CLIENT_NAME = "Cliente Test";

    @Test
    public void saveNotExistsClientIdShouldInsert() throws Exception {

        ClientDto clientDto = new ClientDto();
        clientDto.setName(CLIENT_NAME);

        when(clientRepository.findByName(CLIENT_NAME)).thenReturn(null);

        ArgumentCaptor<Client> client = ArgumentCaptor.forClass(Client.class);

        clientService.save(null, clientDto);

        verify(clientRepository).save(client.capture());

        assertEquals(CLIENT_NAME, client.getValue().getName());
    }

    public static final Long EXISTS_CLIENT_ID = 1L;

    @Test
    public void saveExistsClientIdShouldUpdate() throws Exception {

        ClientDto clientDto = new ClientDto();
        clientDto.setName(CLIENT_NAME);

        Client client = mock(Client.class);
        when(clientRepository.findById(EXISTS_CLIENT_ID)).thenReturn(Optional.of(client));
        when(clientRepository.findByName(CLIENT_NAME)).thenReturn(null);

        clientService.save(EXISTS_CLIENT_ID, clientDto);

        verify(clientRepository).save(client);
    }

    @Test
    public void saveWithDuplicateNameShouldThrowException() {

        ClientDto clientDto = new ClientDto();
        clientDto.setName(CLIENT_NAME);

        Client existingClient = new Client();
        existingClient.setId(2L);
        existingClient.setName(CLIENT_NAME);

        when(clientRepository.findByName(CLIENT_NAME)).thenReturn(existingClient);

        Exception exception = assertThrows(Exception.class, () -> {
            clientService.save(null, clientDto);
        });

        assertTrue(exception.getMessage().contains("Ya existe un cliente con el nombre"));
        verify(clientRepository, never()).save(any());
    }

    @Test
    public void saveWithSameNameAndSameIdShouldUpdate() throws Exception {

        ClientDto clientDto = new ClientDto();
        clientDto.setName(CLIENT_NAME);

        Client existingClient = mock(Client.class);
        when(existingClient.getId()).thenReturn(EXISTS_CLIENT_ID);
        when(clientRepository.findById(EXISTS_CLIENT_ID)).thenReturn(Optional.of(existingClient));
        when(clientRepository.findByName(CLIENT_NAME)).thenReturn(existingClient);

        clientService.save(EXISTS_CLIENT_ID, clientDto);

        verify(clientRepository).save(existingClient);
    }

    @Test
    public void deleteExistsClientIdShouldDelete() throws Exception {

        Client client = mock(Client.class);
        when(clientRepository.findById(EXISTS_CLIENT_ID)).thenReturn(Optional.of(client));

        clientService.delete(EXISTS_CLIENT_ID);

        verify(clientRepository).deleteById(EXISTS_CLIENT_ID);
    }

    public static final Long NOT_EXISTS_CLIENT_ID = 0L;

    @Test
    public void deleteNotExistsClientIdShouldThrowException() {

        when(clientRepository.findById(NOT_EXISTS_CLIENT_ID)).thenReturn(Optional.empty());

        Exception exception = assertThrows(Exception.class, () -> {
            clientService.delete(NOT_EXISTS_CLIENT_ID);
        });

        assertTrue(exception.getMessage().contains("Not exists"));
    }

    @Test
    public void getExistsClientIdShouldReturnClient() {

        Client client = mock(Client.class);
        when(client.getId()).thenReturn(EXISTS_CLIENT_ID);
        when(clientRepository.findById(EXISTS_CLIENT_ID)).thenReturn(Optional.of(client));

        Client clientResponse = clientService.get(EXISTS_CLIENT_ID);

        assertNotNull(clientResponse);
        assertEquals(EXISTS_CLIENT_ID, client.getId());
    }

    @Test
    public void getNotExistsClientIdShouldReturnNull() {

        when(clientRepository.findById(NOT_EXISTS_CLIENT_ID)).thenReturn(Optional.empty());

        Client client = clientService.get(NOT_EXISTS_CLIENT_ID);

        assertNull(client);
    }

}

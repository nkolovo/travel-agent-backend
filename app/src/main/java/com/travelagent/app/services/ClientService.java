package com.travelagent.app.services;

import com.travelagent.app.models.Client;
import com.travelagent.app.repositories.ClientRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client getClientById(Long id) {
        Optional<Client> client = clientRepository.findById(id);
        if (client.isPresent())
            return client.get();
        else
            return null;
    }

    public Client getClientByName(String name) {
        Optional<Client> client = clientRepository.findByName(name);
        if (client.isPresent())
            return client.get();
        else
            return null;
    }

    public Client saveClient(Client client) {
        return clientRepository.save(client);
    }

    public void deleteClient(Long id) {
        clientRepository.deleteById(id);
    }
}

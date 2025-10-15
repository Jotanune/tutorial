package com.ccsw.tutorial.client;

import com.ccsw.tutorial.client.model.Client;
import org.springframework.data.repository.CrudRepository;

/**
 * @author ccsw
 *
 */
public interface ClientRepository extends CrudRepository<Client, Long> {

    /**
     * Método para buscar un cliente por nombre
     *
     * @param name nombre del cliente
     * @return {@link Client}
     */
    Client findByName(String name);

}


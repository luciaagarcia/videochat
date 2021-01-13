package edu.uclm.esi.videochat.springdao;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import edu.uclm.esi.videochat.model.Message;

public interface MessageRepository extends CrudRepository <Message, String> {
    @Query(value = "SELECT * FROM message WHERE (recipient=:recipient AND sender=:sender) OR (recipient=:sender AND sender=:recipient) ORDER BY date", nativeQuery = true)
    public ArrayList<Message> recuperarMensajes(@Param("recipient") String recipient, @Param("sender") String sender);

}

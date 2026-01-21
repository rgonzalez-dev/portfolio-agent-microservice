package rgonzalez.agent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rgonzalez.agent.entity.Message;

import java.util.List;

/**
 * Repository for Message entity persistence operations.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    /**
     * Find messages by conversation ID.
     */
    List<Message> findByConversationIdOrderByCreatedAtAsc(Long conversationId);

    /**
     * Count messages in a conversation.
     */
    long countByConversationId(Long conversationId);
}

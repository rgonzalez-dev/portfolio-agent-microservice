package rgonzalez.agent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rgonzalez.agent.entity.Conversation;
import rgonzalez.agent.entity.ConversationStatus;

import java.util.List;

/**
 * Repository for Conversation entity persistence operations.
 */
@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    /**
     * Find conversations by user ID.
     */
    List<Conversation> findByUserId(String userId);

    /**
     * Find conversations by status.
     */
    List<Conversation> findByStatus(ConversationStatus status);

}

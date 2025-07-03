package az.kb.mstransaction.repository;

import az.kb.mstransaction.entity.TransactionStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HistoryRepository extends JpaRepository<TransactionStatusHistory, Integer> {
}

package az.kb.mstransaction.repository;

import az.kb.mstransaction.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndCustomerId(Long id, Long customerId);

    Optional<List<Transaction>> findByCustomerId(Long customerId);
}

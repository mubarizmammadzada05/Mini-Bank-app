package az.kb.mscore.model;

import az.kb.mscore.enums.TransactionStatus;
import az.kb.mscore.enums.TransactionType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TransactionResponse {
     Long id;

     Long customerId;

     TransactionType type;

    TransactionStatus status;

    BigDecimal amount;

    Long relatedTransactionId;
}

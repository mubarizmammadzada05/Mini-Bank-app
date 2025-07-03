package az.kb.mstransaction.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Builder
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateBalanceRequest {
    BigDecimal amount;
}

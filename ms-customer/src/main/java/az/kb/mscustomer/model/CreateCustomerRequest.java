package az.kb.mscustomer.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCustomerRequest {
     String name;
     String surname;
     @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd.MM.yyyy")
     LocalDate birthDate;
     String phoneNumber;
}
